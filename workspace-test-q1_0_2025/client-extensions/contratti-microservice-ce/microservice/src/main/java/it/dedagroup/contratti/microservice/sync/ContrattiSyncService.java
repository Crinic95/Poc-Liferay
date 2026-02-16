package it.dedagroup.contratti.microservice.sync;

import it.dedagroup.contratti.microservice.liferay.LiferayContrattoClient;
import it.dedagroup.contratti.microservice.oracle.OracleContrattiRepository;
import it.dedagroup.contratti.microservice.oracle.dto.OracleContrattoRow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ContrattiSyncService {

    private final OracleContrattiRepository oracleRepo;
    private final LiferayContrattoClient liferayClient;
    private final int pageSize;

    public ContrattiSyncService(OracleContrattiRepository oracleRepo,
                                LiferayContrattoClient liferayClient,
                                @Value("${liferay.contratti.pageSize}") int pageSize) {
        this.oracleRepo = oracleRepo;
        this.liferayClient = liferayClient;
        this.pageSize = pageSize;
    }

    public int syncTestByCodiceFiscale(String cf, int limit) {
        List<OracleContrattoRow> rows = oracleRepo.fetchByCodiceFiscaleWithBollettaCounts(cf, limit);

        int processed = 0;
        for (OracleContrattoRow row : rows) {
            String externalId = _buildExternalId(row.annoContratto(), row.numeroContratto());

            Map<String, Object> payload = new HashMap<>();
            payload.put("externalReferenceCode", externalId);
            payload.put("externalId", externalId);
            payload.put("annoContratto", row.annoContratto());
            payload.put("numeroContratto", row.numeroContratto());

            if (row.codiceCliente() != null) payload.put("codiceCliente", row.codiceCliente());
            if (row.ragioneSociale() != null) payload.put("ragioneSociale", row.ragioneSociale());
            if (row.statoContratto() != null) payload.put("statoContratto", row.statoContratto());
            if (row.nomeUtente() != null) payload.put("nomeUtente", row.nomeUtente());
            if (row.caArera() != null) payload.put("caArera", row.caArera());
            if (row.iban() != null) payload.put("iban", row.iban());
            if (row.cif() != null) payload.put("cif", row.cif());
            if (row.ulm() != null) payload.put("ulm", row.ulm());
            if (row.bollettaOnline() != null) payload.put("bollettaOnline", row.bollettaOnline());
            if (row.tipologiaContratto() != null) payload.put("tipologiaContratto", row.tipologiaContratto());
            if (row.domiciliazioneBancaria() != null) payload.put("domiciliazioneBancaria", row.domiciliazioneBancaria());
            if (row.dtAttivazioneContratto() != null) payload.put("dtAttivazione", row.dtAttivazioneContratto().toString());
            if (row.dtCessazioneContratto() != null) payload.put("dtCessazione", row.dtCessazioneContratto().toString());
            if (row.ubicazione() != null) payload.put("ubicazione", row.ubicazione());
            if (row.recapito() != null) payload.put("recapito", row.recapito());
            if (row.ragioneSocialeAgg() != null) payload.put("ragioneSocialeAgg", row.ragioneSocialeAgg());
            if (row.residenza() != null) payload.put("residenza", row.residenza());
            if (row.telefono() != null) payload.put("telefono", row.telefono());
            if (row.cellulare() != null) payload.put("cellulare", row.cellulare());
            if (row.cellulare2() != null) payload.put("cellulare2", row.cellulare2());
            if (row.tipoUtente() != null) payload.put("tipoUtente", row.tipoUtente());
            if (row.partitaIva() != null) payload.put("partitaIva", row.partitaIva());
            if (row.codiceFiscale() != null) payload.put("codiceFiscale", row.codiceFiscale());
            if (row.cognomeUtente() != null) payload.put("cognomeUtente", row.cognomeUtente());

            if (row.bolletteTotali() != null) payload.put("bolletteTotali", row.bolletteTotali());
            if (row.bolletteDaPagare() != null) payload.put("bolletteDaPagare", row.bolletteDaPagare());

            liferayClient.upsertByERC(payload, externalId);
            processed++;
        }

        return processed;
    }

    private String _buildExternalId(String anno, long numero) {
        return anno + "C" + numero;
    }
}