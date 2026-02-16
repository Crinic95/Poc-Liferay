package it.dedagroup.microservice.proxy.oracle;

import it.dedagroup.microservice.proxy.oracle.dto.OracleSupplyProxyRow;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class OracleSuppliesRepository {

    private static final int MAX_PAGE_SIZE = 100;

    private final JdbcTemplate jdbc;

    public OracleSuppliesRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<OracleSupplyProxyRow> fetchByCodiceFiscale(String codiceFiscale, String statoContrattoEq, String ubicazioneEq,
                                                           String search, int page, int pageSize) {
        int safePage = Math.max(1, page);
        int safePageSize = Math.min(Math.max(1, pageSize), MAX_PAGE_SIZE);

        int startRnExclusive = (safePage - 1) * safePageSize;
        int endRnInclusive = startRnExclusive + safePageSize;

        StringBuilder sql = new StringBuilder("""
            SELECT *
            FROM (
              SELECT
                c.ANNO_CONTRATTO,
                c.NUMERO_CONTRATTO,
                c.CIF,
                c.CODICE_CLIENTE,
                c.NOME_UTENTE,
                c.COGNOME_UTENTE,
                c.RAGIONE_SOCIALE,
                c.RAGIONE_SOCIALE_AGG,
                c.STATO_CONTRATTO,
                c.TIPOLOGIA_CONTRATTO,
                c.TIPO_UTENTE,
                c.UBICAZIONE,
                c.RECAPITO,
                c.RESIDENZA,
                c.TELEFONO,
                c.CELLULARE,
                c.CELLULARE2,
                c.PARTITA_IVA,
                c.IBAN,
                c.BOLLETTAONLINE,
                c.CA_ARERA,
                c.DOMICILIAZIONE_BANCARIA,
                c.ULM,
                c.CODICE_FISCALE,
                c.DT_ATTIVAZIONE_CONTRATTO,
                c.DT_CESSAZIONE_CONTRATTO,
                ROW_NUMBER() OVER (
                    ORDER BY c.ANNO_CONTRATTO DESC, c.NUMERO_CONTRATTO DESC
                ) AS RN
              FROM V_SO_CONTRATTI c
              WHERE c.CODICE_FISCALE = ?
        """);

        List<Object> params = new ArrayList<>();
        params.add(codiceFiscale);

        if (statoContrattoEq != null && !statoContrattoEq.isBlank()) {
            sql.append(" AND c.STATO_CONTRATTO = ?\n");
            params.add(statoContrattoEq);
        }

        if (ubicazioneEq != null && !ubicazioneEq.isBlank()) {
            sql.append(" AND c.UBICAZIONE = ?\n");
            params.add(ubicazioneEq);
        }

        if (search != null && !search.isBlank()) {
            sql.append("""
                AND (
                     UPPER(c.STATO_CONTRATTO) LIKE '%' || UPPER(?) || '%'
                  OR UPPER(c.UBICAZIONE)      LIKE '%' || UPPER(?) || '%'
                  OR UPPER(c.RAGIONE_SOCIALE) LIKE '%' || UPPER(?) || '%'
                )
                """);
            params.add(search);
            params.add(search);
            params.add(search);
        }

        sql.append("""
            )
            WHERE RN > ? AND RN <= ?
        """);

        params.add(startRnExclusive);
        params.add(endRnInclusive);

        return jdbc.query(sql.toString(), this::mapRow, params.toArray());
    }

    public long countByCodiceFiscale(String codiceFiscale, String statoContrattoEq,
            String ubicazioneEq, String search) {
        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(1)
            FROM V_SO_CONTRATTI c
            WHERE c.CODICE_FISCALE = ?
        """);

        List<Object> params = new ArrayList<>();
        params.add(codiceFiscale);

        if (statoContrattoEq != null && !statoContrattoEq.isBlank()) {
            sql.append(" AND c.STATO_CONTRATTO = ?\n");
            params.add(statoContrattoEq);
        }

        if (ubicazioneEq != null && !ubicazioneEq.isBlank()) {
            sql.append(" AND c.UBICAZIONE = ?\n");
            params.add(ubicazioneEq);
        }

        if (search != null && !search.isBlank()) {
            sql.append("""
                AND (
                     UPPER(c.STATO_CONTRATTO) LIKE '%' || UPPER(?) || '%'
                  OR UPPER(c.UBICAZIONE)      LIKE '%' || UPPER(?) || '%'
                  OR UPPER(c.RAGIONE_SOCIALE) LIKE '%' || UPPER(?) || '%'
                )
                """);
            params.add(search);
            params.add(search);
            params.add(search);
        }

        Long v = jdbc.queryForObject(sql.toString(), Long.class, params.toArray());
        return v == null ? 0L : v;
    }

    public OracleSupplyProxyRow findByErc(String erc) {
        String anno = _parseAnno(erc);
        long numero = _parseNumero(erc);

        String sql = """
            SELECT *
            FROM V_SO_CONTRATTI c
            WHERE c.ANNO_CONTRATTO = ?
              AND c.NUMERO_CONTRATTO = ?
        """;

        List<OracleSupplyProxyRow> list = jdbc.query(sql, this::mapRowNoRn, anno, numero);
        return list.isEmpty() ? null : list.get(0);
    }
    private OracleSupplyProxyRow mapRow(ResultSet rs, int rowNum) throws SQLException {
        return mapCommon(rs);
    }

    private OracleSupplyProxyRow mapRowNoRn(ResultSet rs, int rowNum) throws SQLException {
        return mapCommon(rs);
    }

    private OracleSupplyProxyRow mapCommon(ResultSet rs) throws SQLException {
        return new OracleSupplyProxyRow(
                rs.getString("ANNO_CONTRATTO"),
                rs.getLong("NUMERO_CONTRATTO"),
                rs.getString("CIF"),
                rs.getObject("CODICE_CLIENTE", Long.class),
                rs.getString("NOME_UTENTE"),
                rs.getString("COGNOME_UTENTE"),
                rs.getString("RAGIONE_SOCIALE"),
                rs.getString("RAGIONE_SOCIALE_AGG"),
                rs.getString("STATO_CONTRATTO"),
                rs.getString("TIPOLOGIA_CONTRATTO"),
                rs.getString("TIPO_UTENTE"),
                rs.getString("UBICAZIONE"),
                rs.getString("RECAPITO"),
                rs.getString("RESIDENZA"),
                rs.getString("TELEFONO"),
                rs.getString("CELLULARE"),
                rs.getString("CELLULARE2"),
                rs.getString("PARTITA_IVA"),
                rs.getString("IBAN"),
                rs.getString("BOLLETTAONLINE"),
                rs.getObject("CA_ARERA", Long.class),
                rs.getString("DOMICILIAZIONE_BANCARIA"),
                rs.getObject("ULM", Long.class),
                rs.getString("CODICE_FISCALE"),
                toLocalDate(rs.getObject("DT_ATTIVAZIONE_CONTRATTO")),
                toLocalDate(rs.getObject("DT_CESSAZIONE_CONTRATTO"))
        );
    }

    private static LocalDate toLocalDate(Object o) {
        if (o == null) return null;
        if (o instanceof java.sql.Date d) return d.toLocalDate();
        if (o instanceof Number n) {
            String s = String.valueOf(n.longValue());
            if (s.length() != 8) return null;
            return LocalDate.of(
                    Integer.parseInt(s.substring(0, 4)),
                    Integer.parseInt(s.substring(4, 6)),
                    Integer.parseInt(s.substring(6, 8))
            );
        }
        return null;
    }

    private static String _parseAnno(String erc) {
        int i = erc.indexOf('C');
        if (i <= 0) throw new IllegalArgumentException("ERC contratto non valido: " + erc);
        return erc.substring(0, i);
    }

    private static long _parseNumero(String erc) {
        int i = erc.indexOf('C');
        if (i <= 0 || i == erc.length() - 1) throw new IllegalArgumentException("ERC contratto non valido: " + erc);
        return Long.parseLong(erc.substring(i + 1));
    }
}