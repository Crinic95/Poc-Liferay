package it.dedagroup.contratti.micro.service.replacing.owner.sync;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sync")
public class SyncController {

    private final ContrattiSyncService contrattiSyncService;

    public SyncController(ContrattiSyncService contrattiSyncService) {
        this.contrattiSyncService = contrattiSyncService;
    }

    @PostMapping("/contratti/test")
    public String syncContrattiTest(
            @RequestParam String codiceFiscale,
            @RequestParam(defaultValue = "10") int limit
    ) {
        int processed = contrattiSyncService.syncTestByCodiceFiscale(codiceFiscale, limit);
        return "OK contratti processed=" + processed + " cf=" + codiceFiscale;
    }
}