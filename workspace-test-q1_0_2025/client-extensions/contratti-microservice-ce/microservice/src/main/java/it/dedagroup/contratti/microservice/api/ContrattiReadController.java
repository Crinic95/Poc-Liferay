package it.dedagroup.contratti.microservice.api;

import it.dedagroup.contratti.microservice.oracle.OracleBolletteRepository;
import it.dedagroup.contratti.microservice.oracle.OracleContrattiRepository;
import it.dedagroup.contratti.microservice.oracle.dto.OracleContrattoRow;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ContrattiReadController {

    private final OracleContrattiRepository oracleRepo;
    private final OracleBolletteRepository bolletteRepo;

    public ContrattiReadController(OracleContrattiRepository oracleRepo,
                                   OracleBolletteRepository bolletteRepo) {
        this.oracleRepo = oracleRepo;
        this.bolletteRepo = bolletteRepo;
    }

    @GetMapping("/contratti")
    public List<OracleContrattoRow> getContrattiByCodiceFiscale(
            @RequestParam String codiceFiscale,
            @RequestParam(defaultValue = "10") int limit
    ) {
        var contratti = oracleRepo.fetchByCodiceFiscale(codiceFiscale, limit);
        var countsMap = bolletteRepo.countsByCodiceFiscale(codiceFiscale, limit);

        return contratti.stream().map(c -> {
            var key = new OracleBolletteRepository.ContractKey(c.annoContratto(), c.numeroContratto());
            var counts = countsMap.getOrDefault(key, new OracleBolletteRepository.Counts(0, 0));

            return new OracleContrattoRow(
                    c.annoContratto(),
                    c.numeroContratto(),
                    c.codiceCliente(),
                    c.ragioneSociale(),
                    c.statoContratto(),
                    c.nomeUtente(),
                    c.caArera(),
                    c.iban(),
                    c.cif(),
                    c.ulm(),
                    c.bollettaOnline(),
                    c.tipologiaContratto(),
                    c.domiciliazioneBancaria(),
                    c.dtAttivazioneContratto(),
                    c.dtCessazioneContratto(),
                    c.ubicazione(),
                    c.recapito(),
                    c.ragioneSocialeAgg(),
                    c.residenza(),
                    c.telefono(),
                    c.cellulare(),
                    c.cellulare2(),
                    c.tipoUtente(),
                    c.partitaIva(),
                    c.codiceFiscale(),
                    c.cognomeUtente(),
                    counts.totali(),
                    counts.daPagare()
            );
        }).toList();
    }
}