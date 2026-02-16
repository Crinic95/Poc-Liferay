package it.dedagroup.contratti.microservice.oracle.dto;

import java.time.LocalDate;

public record OracleContrattoRow(String annoContratto,
                                 long numeroContratto,
                                 Long codiceCliente,
                                 String ragioneSociale,
                                 String statoContratto,
                                 String nomeUtente,
                                 Long caArera,
                                 String iban,
                                 String cif,
                                 Long ulm,
                                 String bollettaOnline,
                                 String tipologiaContratto,
                                 String domiciliazioneBancaria,
                                 LocalDate dtAttivazioneContratto,
                                 LocalDate dtCessazioneContratto,
                                 String ubicazione,
                                 String recapito,
                                 String ragioneSocialeAgg,
                                 String residenza,
                                 String telefono,
                                 String cellulare,
                                 String cellulare2,
                                 String tipoUtente,
                                 String partitaIva,
                                 String codiceFiscale,
                                 String cognomeUtente,
                                 Long bolletteTotali,
                                 Long bolletteDaPagare) {
}