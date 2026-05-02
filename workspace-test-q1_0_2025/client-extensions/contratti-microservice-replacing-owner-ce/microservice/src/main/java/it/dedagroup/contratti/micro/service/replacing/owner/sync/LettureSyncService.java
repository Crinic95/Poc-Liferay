package it.dedagroup.contratti.micro.service.replacing.owner.sync;

import it.dedagroup.contratti.micro.service.replacing.owner.liferay.LiferayLetturaClient;
import it.dedagroup.contratti.micro.service.replacing.owner.liferay.LiferayObjectPermissionClient;
import it.dedagroup.contratti.micro.service.replacing.owner.oracle.OracleLettureRepository;
import it.dedagroup.contratti.micro.service.replacing.owner.oracle.dto.OracleLetturaRow;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LettureSyncService {

    private final OracleLettureRepository lettureRepo;
    private final LiferayLetturaClient letturaClient;
    private final LiferayObjectPermissionClient permissionClient;

    private static final String REL_FORNITURA_FIELD = "r_contractToLectures_c_fornitureId";

    public LettureSyncService(
            OracleLettureRepository lettureRepo,
            LiferayLetturaClient letturaClient,
            LiferayObjectPermissionClient permissionClient
    ) {
        this.lettureRepo = lettureRepo;
        this.letturaClient = letturaClient;
        this.permissionClient = permissionClient;
    }

    public int syncForFornitura(
            String anno,
            long numero,
            Long fornituraId,
            String roleName,
            int limit
    ) {
        var rows = lettureRepo.fetchByContratto(anno, numero, limit);

        int processed = 0;

        for (var row : rows) {
            String erc = buildErc(anno, numero, row);

            Map<String, Object> payload = new HashMap<>();
            payload.put("externalReferenceCode", erc);

            if (row.matricola() != null) {
                payload.put("matricola", row.matricola());
            }

            if (row.dataLettura() != null) {
                payload.put("dataLettura", row.dataLettura().toString());
            }

            if (row.lettura() != null) {
                payload.put("lettura", row.lettura());
            }

            if (row.consumo() != null) {
                payload.put("consumo", row.consumo());
            }

            if (row.causaleMovimento() != null) {
                payload.put("causaleMovimento", row.causaleMovimento());
            }

            payload.put(REL_FORNITURA_FIELD, fornituraId);

            letturaClient.upsertByERC(payload, erc);

            Long letturaId = letturaClient.findIdByERC(erc);

            if (letturaId != null && roleName != null && !roleName.isBlank()) {
                permissionClient.grantViewToRoleForPath("/o/c/letturas", letturaId, roleName);
            }

            processed++;
        }

        return processed;
    }

    private String buildErc(String anno, long numero, OracleLetturaRow row) {
        String data = row.dataLettura() == null
                ? "NODATE"
                : row.dataLettura().toString().replace("-", "");

        String matricola = row.matricola() == null ? "NOMATRICOLA" : row.matricola();

        return "LETTURA_" + anno + "_" + numero + "_" + matricola + "_" + data;
    }
}