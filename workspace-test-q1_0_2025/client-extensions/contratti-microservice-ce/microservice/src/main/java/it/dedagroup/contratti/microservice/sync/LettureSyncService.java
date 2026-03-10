package it.dedagroup.contratti.microservice.sync;

import it.dedagroup.contratti.microservice.liferay.LiferayLetturaClient;
import it.dedagroup.contratti.microservice.oracle.OracleLettureRepository;
import it.dedagroup.contratti.microservice.oracle.dto.OracleLetturaRow;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class LettureSyncService {

    private final OracleLettureRepository lettureRepo;
    private final LiferayLetturaClient letturaClient;

    private static final String REL_CONTRATTO_FIELD = "r_letture_c_contrattoId";
    private static final String REL_ACCOUNT_FIELD = "r_relatedAccountEntry_accountEntryId";

    public LettureSyncService(OracleLettureRepository lettureRepo,
                              LiferayLetturaClient letturaClient) {
        this.lettureRepo = lettureRepo;
        this.letturaClient = letturaClient;
    }

    public int syncForContratto(
            String anno,
            long numero,
            Long contrattoId,
            Long accountId,
            int limit) {

        var rows = lettureRepo.fetchByContratto(anno, numero, limit);

        int processed = 0;

        for (var row : rows) {

            String erc = buildErc(anno, numero, row);

            Map<String, Object> payload = new HashMap<>();
            payload.put("externalReferenceCode", erc);

            if (row.matricola() != null)
                payload.put("matricola", row.matricola());

            if (row.dataLettura() != null)
                payload.put("dataLettura", row.dataLettura().toString());

            if (row.lettura() != null)
                payload.put("lettura", row.lettura());

            if (row.consumo() != null)
                payload.put("consumo", row.consumo());

            if (row.causaleMovimento() != null)
                payload.put("causaleMovimento", row.causaleMovimento());

            payload.put(REL_CONTRATTO_FIELD, contrattoId);
            payload.put(REL_ACCOUNT_FIELD, accountId);

            letturaClient.upsertByERC(payload, erc);

            processed++;
        }

        return processed;
    }

    private String buildErc(String anno, long numero, OracleLetturaRow row) {
        String data = row.dataLettura() == null
                ? "nodate"
                : row.dataLettura().toString().replace("-", "");

        return anno + "C" + numero + "-"
                + row.matricola() + "-"
                + data;
    }
}