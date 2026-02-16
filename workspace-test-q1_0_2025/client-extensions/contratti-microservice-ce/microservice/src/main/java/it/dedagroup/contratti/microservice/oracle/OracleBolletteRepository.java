package it.dedagroup.contratti.microservice.oracle;

import it.dedagroup.contratti.microservice.oracle.dto.OracleBollettaRow;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class OracleBolletteRepository {

    private final JdbcTemplate jdbc;

    public OracleBolletteRepository(JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbcTemplate;
    }

    public long countTotali(String annoContratto, long numeroContratto) {
        String sql = """
            SELECT COUNT(1)
            FROM V_SO_BOLLETTE
            WHERE ANNO_CONTRATTO = ?
              AND NUMERO_CONTRATTO = ?
        """;
        Long v = jdbc.queryForObject(sql, Long.class, annoContratto, numeroContratto);
        return v == null ? 0L : v;
    }

    public long countDaPagare(String annoContratto, long numeroContratto) {
        String sql = """
            SELECT COUNT(1)
            FROM V_SO_BOLLETTE
            WHERE ANNO_CONTRATTO = ?
              AND NUMERO_CONTRATTO = ?
              AND NVL(SALDATA, 'N') <> 'S'
        """;
        Long v = jdbc.queryForObject(sql, Long.class, annoContratto, numeroContratto);
        return v == null ? 0L : v;
    }

    public Map<ContractKey, Counts> countsByCodiceFiscale(String codiceFiscale, int limitContratti) {
        String sql = """
            SELECT
                b.ANNO_CONTRATTO,
                b.NUMERO_CONTRATTO,
                COUNT(1) AS TOT,
                SUM(CASE WHEN NVL(b.SALDATA,'N') <> 'S' THEN 1 ELSE 0 END) AS DA_PAGARE
            FROM V_SO_BOLLETTE b
            JOIN (
                SELECT ANNO_CONTRATTO, NUMERO_CONTRATTO
                FROM (
                    SELECT ANNO_CONTRATTO, NUMERO_CONTRATTO
                    FROM V_SO_CONTRATTI
                    WHERE CODICE_FISCALE = ?
                    ORDER BY ANNO_CONTRATTO DESC, NUMERO_CONTRATTO DESC
                )
                WHERE ROWNUM <= ?
            ) c
              ON c.ANNO_CONTRATTO = b.ANNO_CONTRATTO
             AND c.NUMERO_CONTRATTO = b.NUMERO_CONTRATTO
            GROUP BY b.ANNO_CONTRATTO, b.NUMERO_CONTRATTO
        """;

        var rows = jdbc.query(sql, (rs, rn) -> new CountsRow(
                rs.getString("ANNO_CONTRATTO"),
                rs.getLong("NUMERO_CONTRATTO"),
                rs.getLong("TOT"),
                rs.getLong("DA_PAGARE")
        ), codiceFiscale, limitContratti);

        return rows.stream().collect(Collectors.toMap(
                r -> new ContractKey(r.annoContratto(), r.numeroContratto()),
                r -> new Counts(r.tot(), r.daPagare())
        ));
    }

    public List<OracleBollettaRow> fetchByCodiceFiscale(String codiceFiscale, int limit) {
        String sql = """
            SELECT *
            FROM (
                SELECT
                    b.BOLLETTA,
                    b.DATA_EMISSIONE,
                    b.TIPO_FATTURA,
                    b.CODICE_UTENTE,
                    b.ULM,
                    b.ANNO_CONTRATTO,
                    b.NUMERO_CONTRATTO,
                    b.CIF,
                    b.INIZIO_PERIODO,
                    b.FINE_PERIODO,
                    b.SOLLECITO_BONARIO,
                    b.DIFFIDA,
                    b.SCADENZA,
                    b.PAGAMENTO,
                    b.IMPORTO,
                    b.DAPAGARE,
                    b.EMAIL_INVIO_BOLLETTA,
                    b.ID_MANDATO,
                    b.IBAN,
                    b.SALDATA,
                    b.DT_TIPO,
                    b.IUV
                FROM V_SO_BOLLETTE b
                JOIN V_SO_CONTRATTI c
                  ON c.ANNO_CONTRATTO = b.ANNO_CONTRATTO
                 AND c.NUMERO_CONTRATTO = b.NUMERO_CONTRATTO
                WHERE c.CODICE_FISCALE = ?
                ORDER BY b.DATA_EMISSIONE DESC NULLS LAST
            )
            WHERE ROWNUM <= ?
        """;

        return jdbc.query(sql, (rs, rn) -> new OracleBollettaRow(
                rs.getString("BOLLETTA"),
                rs.getDate("DATA_EMISSIONE") == null ? null : rs.getDate("DATA_EMISSIONE").toLocalDate(),
                rs.getString("TIPO_FATTURA"),
                rs.getObject("CODICE_UTENTE", Long.class),
                rs.getObject("ULM", Long.class),
                rs.getString("ANNO_CONTRATTO"),
                rs.getLong("NUMERO_CONTRATTO"),
                rs.getString("CIF"),
                rs.getDate("INIZIO_PERIODO") == null ? null : rs.getDate("INIZIO_PERIODO").toLocalDate(),
                rs.getDate("FINE_PERIODO") == null ? null : rs.getDate("FINE_PERIODO").toLocalDate(),
                rs.getDate("SOLLECITO_BONARIO") == null ? null : rs.getDate("SOLLECITO_BONARIO").toLocalDate(),
                rs.getDate("DIFFIDA") == null ? null : rs.getDate("DIFFIDA").toLocalDate(),
                rs.getDate("SCADENZA") == null ? null : rs.getDate("SCADENZA").toLocalDate(),
                rs.getDate("PAGAMENTO") == null ? null : rs.getDate("PAGAMENTO").toLocalDate(),
                rs.getBigDecimal("IMPORTO"),
                rs.getBigDecimal("DAPAGARE"),
                rs.getString("EMAIL_INVIO_BOLLETTA"),
                rs.getString("ID_MANDATO"),
                rs.getString("IBAN"),
                rs.getString("SALDATA"),
                rs.getString("DT_TIPO"),
                rs.getString("IUV")
        ), codiceFiscale, limit);
    }

    public record ContractKey(String annoContratto, long numeroContratto) {}
    public record Counts(long totali, long daPagare) {}
    private record CountsRow(String annoContratto, long numeroContratto, long tot, long daPagare) {}
}