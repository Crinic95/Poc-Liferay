package it.dedagroup.contratti.microservice.sync;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sync")
public class SyncController {

    private final ContrattiSyncService contrattiSyncService;
    private final BolletteSyncService bolletteSyncService;
    private final int defaultPages;

    public SyncController(
            ContrattiSyncService contrattiSyncService,
            BolletteSyncService bolletteSyncService,
            @Value("${sync.defaultPages:1}") int defaultPages
    ) {
        this.contrattiSyncService = contrattiSyncService;
        this.bolletteSyncService = bolletteSyncService;
        this.defaultPages = defaultPages;
    }

    @PostMapping("/contratti/test")
    public String syncContrattiTest(@RequestParam String codiceFiscale,
                                    @RequestParam(defaultValue = "10") int limit) {
        int processed = contrattiSyncService.syncTestByCodiceFiscale(codiceFiscale, limit);
        return "OK contratti processed=" + processed + " cf=" + codiceFiscale;
    }

    @PostMapping("/bollette/test")
    public String syncBolletteTest(@RequestParam String codiceFiscale,
                                   @RequestParam(defaultValue = "200") int limit) {
        int processed = bolletteSyncService.syncByCodiceFiscale(codiceFiscale, limit);
        return "OK bollette processed=" + processed + " cf=" + codiceFiscale;
    }
}