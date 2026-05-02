package it.dedagroup.contratti.micro.service.replacing.owner.oracle;

import it.dedagroup.contratti.micro.service.replacing.owner.oracle.dto.OracleLetturaRow;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OracleLettureRepository {

    private final JdbcTemplate jdbc;

    public OracleLettureRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<OracleLetturaRow> fetchByContratto(String annoContratto, long numeroContratto, int limit) {
        String sql = """
            SELECT *
            FROM (
                SELECT
                    t.MATRICOLA,
                    t.CAUSALE_MOVIMENTO,
                    t.DATA_LETTURA,
                    t.LETTURA,
                    t.CONSUMO
                FROM V_SO_LETTURE t
                WHERE t.ANNO_CONTRATTO = ?
                  AND t.NUMERO_CONTRATTO = ?
                ORDER BY t.DATA_LETTURA DESC NULLS LAST
            )
            WHERE ROWNUM <= ?
        """;

        return jdbc.query(sql, (rs, rn) -> new OracleLetturaRow(
                rs.getString("MATRICOLA"),
                rs.getDate("DATA_LETTURA") == null ? null : rs.getDate("DATA_LETTURA").toLocalDate(),
                rs.getObject("LETTURA", Long.class),
                rs.getObject("CONSUMO", Long.class),
                rs.getString("CAUSALE_MOVIMENTO")
        ), annoContratto, numeroContratto, limit);
    }
}