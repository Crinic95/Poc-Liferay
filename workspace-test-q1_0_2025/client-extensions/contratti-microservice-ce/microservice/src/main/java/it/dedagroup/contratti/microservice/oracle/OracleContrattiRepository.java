package it.dedagroup.contratti.microservice.oracle;

import it.dedagroup.contratti.microservice.oracle.dto.OracleContrattoRow;
import it.dedagroup.contratti.microservice.util.OracleDateUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class OracleContrattiRepository {

    private final JdbcTemplate jdbc;

    public OracleContrattiRepository(JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbcTemplate;
    }

    public List<OracleContrattoRow> fetchByCodiceFiscale(String codiceFiscale, int limit) {
        String sql = """
            SELECT *
            FROM (
                SELECT
                    c.ANNO_CONTRATTO,
                    c.NUMERO_CONTRATTO,
                    c.CODICE_CLIENTE,
                    c.RAGIONE_SOCIALE,
                    c.STATO_CONTRATTO,
                    c.NOME_UTENTE,
                    c.CA_ARERA,
                    c.IBAN,
                    c.CIF,
                    c.ULM,
                    c.BOLLETTAONLINE,
                    c.TIPOLOGIA_CONTRATTO,
                    c.DOMICILIAZIONE_BANCARIA,
                    c.DT_ATTIVAZIONE_CONTRATTO,
                    c.DT_CESSAZIONE_CONTRATTO,
                    c.UBICAZIONE,
                    c.RECAPITO,
                    c.RAGIONE_SOCIALE_AGG,
                    c.RESIDENZA,
                    c.TELEFONO,
                    c.CELLULARE,
                    c.CELLULARE2,
                    c.TIPO_UTENTE,
                    c.PARTITA_IVA,
                    c.CODICE_FISCALE,
                    c.COGNOME_UTENTE
                FROM V_SO_CONTRATTI c
                WHERE c.CODICE_FISCALE = ?
                ORDER BY c.ANNO_CONTRATTO DESC, c.NUMERO_CONTRATTO DESC
            )
            WHERE ROWNUM <= ?
        """;

        return jdbc.query(sql, this::mapRowBase, codiceFiscale, limit);
    }

    public List<OracleContrattoRow> fetchByCodiceFiscaleWithBollettaCounts(String codiceFiscale, int limit) {
        String sql = """
            SELECT *
            FROM (
                SELECT
                    c.ANNO_CONTRATTO,
                    c.NUMERO_CONTRATTO,
                    c.CODICE_CLIENTE,
                    c.RAGIONE_SOCIALE,
                    c.STATO_CONTRATTO,
                    c.NOME_UTENTE,
                    c.CA_ARERA,
                    c.IBAN,
                    c.CIF,
                    c.ULM,
                    c.BOLLETTAONLINE,
                    c.TIPOLOGIA_CONTRATTO,
                    c.DOMICILIAZIONE_BANCARIA,
                    c.DT_ATTIVAZIONE_CONTRATTO,
                    c.DT_CESSAZIONE_CONTRATTO,
                    c.UBICAZIONE,
                    c.RECAPITO,
                    c.RAGIONE_SOCIALE_AGG,
                    c.RESIDENZA,
                    c.TELEFONO,
                    c.CELLULARE,
                    c.CELLULARE2,
                    c.TIPO_UTENTE,
                    c.PARTITA_IVA,
                    c.CODICE_FISCALE,
                    c.COGNOME_UTENTE,
                    COUNT(b.NUMERO_CONTRATTO) AS BOLLETTE_TOTALI,
                    SUM(CASE WHEN NVL(b.SALDATA,'N') <> 'S' THEN 1 ELSE 0 END) AS BOLLETTE_DA_PAGARE
                FROM V_SO_CONTRATTI c
                LEFT JOIN V_SO_BOLLETTE b
                       ON b.ANNO_CONTRATTO = c.ANNO_CONTRATTO
                      AND b.NUMERO_CONTRATTO = c.NUMERO_CONTRATTO
                WHERE c.CODICE_FISCALE = ?
                GROUP BY
                    c.ANNO_CONTRATTO,
                    c.NUMERO_CONTRATTO,
                    c.CODICE_CLIENTE,
                    c.RAGIONE_SOCIALE,
                    c.STATO_CONTRATTO,
                    c.NOME_UTENTE,
                    c.CA_ARERA,
                    c.IBAN,
                    c.CIF,
                    c.ULM,
                    c.BOLLETTAONLINE,
                    c.TIPOLOGIA_CONTRATTO,
                    c.DOMICILIAZIONE_BANCARIA,
                    c.DT_ATTIVAZIONE_CONTRATTO,
                    c.DT_CESSAZIONE_CONTRATTO,
                    c.UBICAZIONE,
                    c.RECAPITO,
                    c.RAGIONE_SOCIALE_AGG,
                    c.RESIDENZA,
                    c.TELEFONO,
                    c.CELLULARE,
                    c.CELLULARE2,
                    c.TIPO_UTENTE,
                    c.PARTITA_IVA,
                    c.CODICE_FISCALE,
                    c.COGNOME_UTENTE
                ORDER BY c.ANNO_CONTRATTO DESC, c.NUMERO_CONTRATTO DESC
            )
            WHERE ROWNUM <= ?
        """;

        return jdbc.query(sql, this::mapRowWithCounts, codiceFiscale, limit);
    }

    private OracleContrattoRow mapRowBase(ResultSet rs, int rowNum) throws SQLException {
        return new OracleContrattoRow(
                rs.getString("ANNO_CONTRATTO"),
                rs.getLong("NUMERO_CONTRATTO"),
                rs.getObject("CODICE_CLIENTE", Long.class),
                rs.getString("RAGIONE_SOCIALE"),
                rs.getString("STATO_CONTRATTO"),
                rs.getString("NOME_UTENTE"),
                rs.getObject("CA_ARERA", Long.class),
                rs.getString("IBAN"),
                rs.getString("CIF"),
                rs.getObject("ULM", Long.class),
                rs.getString("BOLLETTAONLINE"),
                rs.getString("TIPOLOGIA_CONTRATTO"),
                rs.getString("DOMICILIAZIONE_BANCARIA"),
                OracleDateUtil.yyyymmddToLocalDate(rs.getObject("DT_ATTIVAZIONE_CONTRATTO", Long.class)),
                OracleDateUtil.yyyymmddToLocalDate(rs.getObject("DT_CESSAZIONE_CONTRATTO", Long.class)),
                rs.getString("UBICAZIONE"),
                rs.getString("RECAPITO"),
                rs.getString("RAGIONE_SOCIALE_AGG"),
                rs.getString("RESIDENZA"),
                rs.getString("TELEFONO"),
                rs.getString("CELLULARE"),
                rs.getString("CELLULARE2"),
                rs.getString("TIPO_UTENTE"),
                rs.getString("PARTITA_IVA"),
                rs.getString("CODICE_FISCALE"),
                rs.getString("COGNOME_UTENTE"),
                null, // bolletteTotali
                null  // bolletteDaPagare
        );
    }

    private OracleContrattoRow mapRowWithCounts(ResultSet rs, int rowNum) throws SQLException {
        Long tot = rs.getObject("BOLLETTE_TOTALI", Long.class);
        Long daPagare = rs.getObject("BOLLETTE_DA_PAGARE", Long.class);

        return new OracleContrattoRow(
                rs.getString("ANNO_CONTRATTO"),
                rs.getLong("NUMERO_CONTRATTO"),
                rs.getObject("CODICE_CLIENTE", Long.class),
                rs.getString("RAGIONE_SOCIALE"),
                rs.getString("STATO_CONTRATTO"),
                rs.getString("NOME_UTENTE"),
                rs.getObject("CA_ARERA", Long.class),
                rs.getString("IBAN"),
                rs.getString("CIF"),
                rs.getObject("ULM", Long.class),
                rs.getString("BOLLETTAONLINE"),
                rs.getString("TIPOLOGIA_CONTRATTO"),
                rs.getString("DOMICILIAZIONE_BANCARIA"),
                OracleDateUtil.yyyymmddToLocalDate(rs.getObject("DT_ATTIVAZIONE_CONTRATTO", Long.class)),
                OracleDateUtil.yyyymmddToLocalDate(rs.getObject("DT_CESSAZIONE_CONTRATTO", Long.class)),
                rs.getString("UBICAZIONE"),
                rs.getString("RECAPITO"),
                rs.getString("RAGIONE_SOCIALE_AGG"),
                rs.getString("RESIDENZA"),
                rs.getString("TELEFONO"),
                rs.getString("CELLULARE"),
                rs.getString("CELLULARE2"),
                rs.getString("TIPO_UTENTE"),
                rs.getString("PARTITA_IVA"),
                rs.getString("CODICE_FISCALE"),
                rs.getString("COGNOME_UTENTE"),
                tot == null ? 0L : tot,
                daPagare == null ? 0L : daPagare
        );
    }
}