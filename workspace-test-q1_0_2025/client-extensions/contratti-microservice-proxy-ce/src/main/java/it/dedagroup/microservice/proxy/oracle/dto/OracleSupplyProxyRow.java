package it.dedagroup.microservice.proxy.oracle.dto;

import java.time.LocalDate;

public record OracleSupplyProxyRow(
        String annoContratto,
        long numeroContratto,
        String cif,
        Long codiceCliente,
        String nomeUtente,
        String cognomeUtente,
        String ragioneSociale,
        String ragioneSocialeAgg,
        String statoContratto,
        String tipologiaContratto,
        String tipoUtente,
        String ubicazione,
        String recapito,
        String residenza,
        String telefono,
        String cellulare,
        String cellulare2,
        String partitaIva,
        String iban,
        String bollettaOnline,
        Long caArera,
        String domiciliazioneBancaria,
        Long ulm,
        String codiceFiscale,
        LocalDate dtAttivazione,
        LocalDate dtCessazione
) {}