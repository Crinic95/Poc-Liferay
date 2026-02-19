package it.dedagroup.microservice.proxy;

import com.liferay.client.extension.util.spring.boot3.BaseRestController;
import it.dedagroup.microservice.proxy.liferay.LiferayUserAccountClient;
import it.dedagroup.microservice.proxy.liferay.TaxCodeExtractor;
import it.dedagroup.microservice.proxy.oracle.OracleSuppliesRepository;
import it.dedagroup.microservice.proxy.oracle.dto.OracleSupplyProxyRow;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequestMapping("/object/entry/manager/supplies")
@RestController
public class SuppliesObjectEntryManagerRestController extends BaseRestController {

    private static final Log _log = LogFactory.getLog(SuppliesObjectEntryManagerRestController.class);

    private final OracleSuppliesRepository suppliesRepository;
    private final LiferayUserAccountClient liferayUserAccountClient;
    private final TaxCodeExtractor taxCodeExtractor;

    public SuppliesObjectEntryManagerRestController(OracleSuppliesRepository contrattiRepository,
                                                    LiferayUserAccountClient liferayUserAccountClient,
                                                    TaxCodeExtractor taxCodeExtractor) {
        this.suppliesRepository = contrattiRepository;
        this.liferayUserAccountClient = liferayUserAccountClient;
        this.taxCodeExtractor = taxCodeExtractor;
    }

    @GetMapping("/{objectDefinitionExternalReferenceCode}")
    public String list(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam Map<String, String> params
    ) {
        _log.info("List method - params=" + params);

        int page = _parseIntOrDefault(params.get("page"), 1);
        int pageSize = _parseIntOrDefault(params.get("pageSize"), 20);
        String search = _blankToNull(params.get("search"));
        String filter = _blankToNull(params.get("filter"));

        String supplyStatus = _blankToNull(params.get("supplyStatus"));
        String supplyAddress = _blankToNull(params.get("supplyAddress"));

        String authorization = jwt.getTokenValue();

        String taxCode = null;

        try {
            JSONObject myUserAccount = liferayUserAccountClient.getMyUserAccount(authorization);
            taxCode = taxCodeExtractor.extractTaxCode(myUserAccount);
        }
        catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.FORBIDDEN || e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                _log.warn("Cannot call my-user-account with current token (" + e.getRawStatusCode() +
                        "). Falling back to codiceFiscale extracted from filter.");
            }
            else {
                _log.warn("Error calling my-user-account. Falling back to filter extraction.", e);
            }
        }
        catch (Exception e) {
            _log.warn("Unexpected error resolving tax code from my-user-account. Falling back to filter extraction.", e);
        }

        if (taxCode == null || taxCode.isBlank()) {
            taxCode = extractCodiceFiscale(filter);
        }

        if (taxCode == null || taxCode.isBlank()) {
            JSONObject empty = new JSONObject()
                    .put("items", new JSONArray())
                    .put("page", page)
                    .put("pageSize", pageSize)
                    .put("totalCount", 0)
                    .put("lastPage", true);

            return empty.toString();
        }

        SupplyFilterCriteria criteria = SupplyFilterParser.parse(filter);
        String statoEq = (supplyStatus != null) ? supplyStatus : criteria.statoContrattoEq();
        String ubicazioneEq = (supplyAddress != null) ? supplyAddress : criteria.ubicazioneEq();

        List<OracleSupplyProxyRow> rows = suppliesRepository.fetchByCodiceFiscale(
                taxCode,
                statoEq,
                ubicazioneEq,
                search,
                page,
                pageSize
        );

        long totalCount = suppliesRepository.countByCodiceFiscale(
                taxCode,
                statoEq,
                ubicazioneEq,
                search
        );

        JSONArray items = new JSONArray();
        for (OracleSupplyProxyRow r : rows) {
            items.put(toObjectEntryJson(r));
        }

        boolean lastPage = (page * (long) pageSize) >= totalCount;

        JSONObject out = new JSONObject()
                .put("items", items)
                .put("page", page)
                .put("pageSize", pageSize)
                .put("totalCount", totalCount)
                .put("lastPage", lastPage);

        return out.toString();
    }

    @GetMapping("/{objectDefinitionExternalReferenceCode}/{externalReferenceCode}")
    public ResponseEntity<String> getByErc(
            @PathVariable String objectDefinitionExternalReferenceCode,
            @PathVariable String externalReferenceCode,
            @RequestParam Map<String, String> parameters,
            @AuthenticationPrincipal Jwt jwt
    ) {
        _log.info("GetByErc method");
        OracleSupplyProxyRow row = suppliesRepository.findByErc(externalReferenceCode);
        if (row == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return ResponseEntity.ok(toObjectEntryJson(row).toString());
    }

    @PostMapping("/{objectDefinitionExternalReferenceCode}")
    public ResponseEntity<String> postReadOnly() {
        return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @PutMapping("/{objectDefinitionExternalReferenceCode}/{externalReferenceCode}")
    public ResponseEntity<String> putReadOnly() {
        return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @DeleteMapping("/{objectDefinitionExternalReferenceCode}/{externalReferenceCode}")
    public ResponseEntity<String> deleteReadOnly() {
        return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
    }

    private JSONObject toObjectEntryJson(OracleSupplyProxyRow r) {
        String externalId = r.annoContratto() + "C" + r.numeroContratto();

        return new JSONObject()
                .put("externalReferenceCode", externalId)
                .put("externalId", externalId)
                .put("codiceFiscale", r.codiceFiscale())
                .put("cif", r.cif())
                .put("nomeUtente", r.nomeUtente())
                .put("cognomeUtente", r.cognomeUtente())
                .put("ragioneSocialeAgg", r.ragioneSocialeAgg())
                .put("codiceCliente", r.codiceCliente())
                .put("telefono", r.telefono())
                .put("cellulare", r.cellulare())
                .put("cellulare2", r.cellulare2())
                .put("residenza", r.residenza())
                .put("recapito", r.recapito())
                .put("partitaIva", r.partitaIva())
                .put("tipoUtente", r.tipoUtente())
                .put("tipologiaContratto", r.tipologiaContratto())
                .put("domiciliazioneBancaria", r.domiciliazioneBancaria())
                .put("bollettaOnline", r.bollettaOnline())
                .put("caArera", r.caArera())
                .put("ulm", r.ulm())
                .put("annoContratto", r.annoContratto())
                .put("numeroContratto", r.numeroContratto())
                .put("ragioneSociale", r.ragioneSociale())
                .put("statoContratto", r.statoContratto())
                .put("ubicazione", r.ubicazione())
                .put("iban", r.iban())
                .put("dtAttivazione", r.dtAttivazione() == null ? JSONObject.NULL : r.dtAttivazione().toString())
                .put("dtCessazione", r.dtCessazione() == null ? JSONObject.NULL : r.dtCessazione().toString());
    }

    private static int _parseIntOrDefault(String v, int def) {
        try {
            if (v == null) return def;
            return Integer.parseInt(v);
        } catch (Exception e) {
            return def;
        }
    }

    private static String _blankToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static final Pattern CF_FILTER =
            Pattern.compile("codiceFiscale\\s+eq\\s+'([^']+)'", Pattern.CASE_INSENSITIVE);

    @Nullable
    private static String extractCodiceFiscale(String filter) {
        if (filter == null) return null;
        Matcher m = CF_FILTER.matcher(filter);
        if (m.find()) return m.group(1);
        return null;
    }
}