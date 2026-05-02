package it.dedagroup.contratti.micro.service.replacing.owner.sync;

import it.dedagroup.contratti.micro.service.replacing.owner.liferay.LiferayContrattoClient;
import it.dedagroup.contratti.micro.service.replacing.owner.liferay.LiferayObjectPermissionClient;
import it.dedagroup.contratti.micro.service.replacing.owner.liferay.LiferayRoleClient;
import it.dedagroup.contratti.micro.service.replacing.owner.liferay.LiferayUserClient;
import it.dedagroup.contratti.micro.service.replacing.owner.liferay.dto.LiferayUserMatch;
import it.dedagroup.contratti.micro.service.replacing.owner.oracle.OracleContrattiRepository;
import it.dedagroup.contratti.micro.service.replacing.owner.oracle.dto.OracleContrattoRow;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ContrattiSyncService {

    private final OracleContrattiRepository oracleRepo;
    private final LiferayContrattoClient liferayContrattoClient;
    private final LiferayUserClient liferayUserClient;
    private final LiferayRoleClient liferayRoleClient;
    private final LiferayObjectPermissionClient liferayObjectPermissionClient;
    private final LettureSyncService lettureSyncService;

    public ContrattiSyncService(
            OracleContrattiRepository oracleRepo,
            LiferayContrattoClient liferayContrattoClient,
            LiferayUserClient liferayUserClient,
            LiferayRoleClient liferayRoleClient,
            LiferayObjectPermissionClient liferayObjectPermissionClient,
            LettureSyncService lettureSyncService
    ) {
        this.oracleRepo = oracleRepo;
        this.liferayContrattoClient = liferayContrattoClient;
        this.liferayUserClient = liferayUserClient;
        this.liferayRoleClient = liferayRoleClient;
        this.liferayObjectPermissionClient = liferayObjectPermissionClient;
        this.lettureSyncService = lettureSyncService;
    }

    public int syncTestByCodiceFiscale(String cf, int limit) {
        List<OracleContrattoRow> rows = oracleRepo.fetchByCodiceFiscaleWithBollettaCounts(cf, limit);

        int processed = 0;

        for (OracleContrattoRow row : rows) {
            String taxCode = normalize(row.codiceFiscale());

            if (taxCode == null) {
                System.out.println("SKIP contratto senza codice fiscale. anno=" + row.annoContratto()
                        + ", numero=" + row.numeroContratto());
                continue;
            }

            LiferayUserClient.UserLookupResult lookup = liferayUserClient.findStrictUniqueUserByTaxCode(taxCode);

            if (lookup.matches() == 0) {
                System.out.println("SKIP nessun utente con taxCode=" + taxCode);
                continue;
            }

            if (lookup.matches() > 1) {
                System.out.println("SKIP taxCode non univoco: " + taxCode + " matches=" + lookup.matches());
                continue;
            }

            LiferayUserMatch user = lookup.user();

            if (user == null) {
                System.out.println("SKIP impossibile risolvere utente univoco per taxCode=" + taxCode);
                continue;
            }

            LiferayRoleClient.RoleBinding roleBinding = liferayRoleClient.ensureRegularRoleAssigned(user);

            String externalId = buildExternalId(row.annoContratto(), row.numeroContratto());
            Map<String, Object> payload = buildContrattoPayload(row, externalId);

            liferayContrattoClient.upsertByERC(payload, externalId);

            Long contrattoId = liferayContrattoClient.findIdByERC(externalId);

            if (contrattoId == null) {
                throw new IllegalStateException(
                        "Contratto creato/aggiornato ma id non trovato per ERC=" + externalId);
            }

            System.out.println("Permessi correnti per contrattoId=" + contrattoId + ": "
                    + liferayObjectPermissionClient.getPermissions(contrattoId));

            liferayObjectPermissionClient.grantViewToRole(contrattoId, roleBinding.roleName());

            System.out.println("VIEW assegnato a roleName=" + roleBinding.roleName()
                    + " userId=" + user.userId()
                    + " contrattoId=" + contrattoId);

            lettureSyncService.syncForFornitura(
                    row.annoContratto(),
                    row.numeroContratto(),
                    contrattoId,
                    roleBinding.roleName(),
                    30
            );

            processed++;
        }

        return processed;
    }

    private Map<String, Object> buildContrattoPayload(OracleContrattoRow row, String externalId) {
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

        return payload;
    }

    private String buildExternalId(String anno, long numero) {
        return "FORNITURA_" + anno + "C" + numero;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim().toUpperCase();
        return normalized.isBlank() ? null : normalized;
    }
}