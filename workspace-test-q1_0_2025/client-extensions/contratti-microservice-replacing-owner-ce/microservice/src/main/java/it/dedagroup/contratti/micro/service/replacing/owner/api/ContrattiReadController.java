package it.dedagroup.contratti.micro.service.replacing.owner.api;

import it.dedagroup.contratti.micro.service.replacing.owner.oracle.OracleContrattiRepository;
import it.dedagroup.contratti.micro.service.replacing.owner.oracle.dto.OracleContrattoRow;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ContrattiReadController {

    private final OracleContrattiRepository oracleRepo;

    public ContrattiReadController(OracleContrattiRepository oracleRepo) {
        this.oracleRepo = oracleRepo;
    }

    @GetMapping("/contratti")
    public List<OracleContrattoRow> getContrattiByCodiceFiscale(
            @RequestParam String codiceFiscale,
            @RequestParam(defaultValue = "10") int limit
    ) {
        var contratti = oracleRepo.fetchByCodiceFiscale(codiceFiscale, limit);

        return oracleRepo.fetchByCodiceFiscale(codiceFiscale, limit);
    }
}