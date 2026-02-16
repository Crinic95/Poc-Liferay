package it.dedagroup.contratti.microservice.sync;

import it.dedagroup.contratti.microservice.liferay.LiferayBollettaClient;
import it.dedagroup.contratti.microservice.liferay.LiferayContrattoClient;
import it.dedagroup.contratti.microservice.oracle.OracleBolletteRepository;
import it.dedagroup.contratti.microservice.oracle.dto.OracleBollettaRow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class BolletteSyncService {

    private final OracleBolletteRepository bolletteRepo;
    private final LiferayBollettaClient bollettaClient;
    private final LiferayContrattoClient contrattoClient;
    private final String relContrattoField;

    public BolletteSyncService(
            OracleBolletteRepository bolletteRepo,
            LiferayBollettaClient bollettaClient,
            LiferayContrattoClient contrattoClient,
            @Value("${liferay.bollette.relContrattoField}") String relContrattoField
    ) {
        this.bolletteRepo = bolletteRepo;
        this.bollettaClient = bollettaClient;
        this.contrattoClient = contrattoClient;
        this.relContrattoField = relContrattoField;
    }

    public int syncByCodiceFiscale(String cf, int limit) {
        var rows = bolletteRepo.fetchByCodiceFiscale(cf, limit);

        Map<String, Long> contrattoIdCache = new HashMap<>();

        int processed = 0;
        for (OracleBollettaRow row : rows) {

            String contrattoErc = row.annoContratto() + "C" + row.numeroContratto();
            Long contrattoId = contrattoIdCache.computeIfAbsent(contrattoErc, contrattoClient::findIdByERC);

            if (contrattoId == null) {
                System.out.println("SKIP bolletta: contratto non trovato in Liferay ERC=" + contrattoErc);
                continue;
            }

            String bollettaErc = buildBollettaErc(row);

            Map<String, Object> payload = new HashMap<>();
            payload.put("externalReferenceCode", bollettaErc);

            if (row.bolletta() != null) payload.put("bolletta", row.bolletta());
            payload.put("annoContratto", row.annoContratto());
            payload.put("numeroContratto", row.numeroContratto());

            if (row.saldata() != null) payload.put("saldata", row.saldata());
            if (row.iuv() != null) payload.put("iuv", row.iuv());

            if (row.dataEmissione() != null) payload.put("dataEmissione", row.dataEmissione().toString());
            if (row.scadenza() != null) payload.put("scadenza", row.scadenza().toString());
            if (row.pagamento() != null) payload.put("pagamento", row.pagamento().toString());

            if (row.importo() != null) payload.put("importo", row.importo());
            if (row.daPagare() != null) payload.put("daPagare", row.daPagare());

            payload.put(relContrattoField, contrattoId);

            bollettaClient.upsertByERC(payload, bollettaErc);
            processed++;
        }

        return processed;
    }

    private String buildBollettaErc(OracleBollettaRow row) {
        if (row.iuv() != null && !row.iuv().isBlank()) return row.iuv();

        String bol = (row.bolletta() == null || row.bolletta().isBlank()) ? "NA" : row.bolletta();
        return row.annoContratto() + "C" + row.numeroContratto() + "-" + bol;
    }
}