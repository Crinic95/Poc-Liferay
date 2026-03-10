package it.dedagroup.contratti.microservice.oracle.dto;

import java.time.LocalDate;

public record OracleLetturaRow(
        String matricola,
        LocalDate dataLettura,
        Long lettura,
        Long consumo,
        String causaleMovimento
) {}