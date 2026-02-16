package it.dedagroup.contratti.microservice.oracle.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OracleBollettaRow(
        String bolletta,
        LocalDate dataEmissione,
        String tipoFattura,
        Long codiceUtente,
        Long ulm,
        String annoContratto,
        long numeroContratto,
        String cif,
        LocalDate inizioPeriodo,
        LocalDate finePeriodo,
        LocalDate sollecitoBonario,
        LocalDate diffida,
        LocalDate scadenza,
        LocalDate pagamento,
        BigDecimal importo,
        BigDecimal daPagare,
        String emailInvioBolletta,
        String idMandato,
        String iban,
        String saldata,
        String dtTipo,
        String iuv
) {}