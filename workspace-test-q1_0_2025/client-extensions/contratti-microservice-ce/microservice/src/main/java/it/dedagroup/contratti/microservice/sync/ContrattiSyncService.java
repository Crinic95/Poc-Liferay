package it.dedagroup.contratti.microservice.sync;

import it.dedagroup.contratti.microservice.liferay.LiferayAccountClient;
import it.dedagroup.contratti.microservice.liferay.LiferayContrattoClient;
import it.dedagroup.contratti.microservice.liferay.LiferayUserClient;
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
    private final LiferayUserClient userClient;
    private final LiferayAccountClient accountClient;
    private final LettureSyncService lettureSyncService;
    private final int pageSize;

    // ATTENZIONE: questo deve combaciare col nome field relationship del tuo Object "Contratto"
    private static final String REL_ACCOUNT_FIELD = "r_relatedAccount_accountEntryId";

    public ContrattiSyncService(
            OracleContrattiRepository oracleRepo,
            LiferayContrattoClient liferayClient,
            LiferayUserClient userClient,
            LiferayAccountClient accountClient,
            LettureSyncService lettureSyncService,
            @Value("${liferay.contratti.pageSize}") int pageSize
    ) {
        this.oracleRepo = oracleRepo;
        this.liferayClient = liferayClient;
        this.userClient = userClient;
        this.accountClient = accountClient;
        this.lettureSyncService = lettureSyncService;
        this.pageSize = pageSize;
    }

    public int syncTestByCodiceFiscale(String cf, int limit) {
        List<OracleContrattoRow> rows = oracleRepo.fetchByCodiceFiscaleWithBollettaCounts(cf, limit);

        Long accountId = accountClient.findOrCreateAccount(cf);
        if (accountId == null) {
            throw new IllegalStateException("AccountId null per cf=" + cf);
        }

        // assegna TUTTI gli utenti che hanno taxCode=cf all'account (via email, come stai facendo tu)
        List<String> emails = userClient.findUserEmailsByTaxCode(cf);
        for (String email : emails) {
            if (email == null || email.isBlank()) continue;
            accountClient.assignUserToAccountIfMissing(accountId, email);
        }

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

            // relazione account sul Contratto
            payload.put(REL_ACCOUNT_FIELD, accountId);

            liferayClient.upsertByERC(payload, externalId);

            // sync letture per questo contratto
            Long contrattoId = liferayClient.findIdByERC(externalId);
            if (contrattoId == null) {
                // se vuoi essere più “strict”, fai throw; così invece non blocchi tutta la sync
                System.out.println("SKIP letture: contrattoId null per ERC=" + externalId);
                processed++;
                continue;
            }

            lettureSyncService.syncForContratto(
                    row.annoContratto(),
                    row.numeroContratto(),
                    contrattoId,
                    accountId,
                    30
            );

            processed++;
        }

        return processed;
    }

    private String _buildExternalId(String anno, long numero) {
        return anno + "C" + numero;
    }
}