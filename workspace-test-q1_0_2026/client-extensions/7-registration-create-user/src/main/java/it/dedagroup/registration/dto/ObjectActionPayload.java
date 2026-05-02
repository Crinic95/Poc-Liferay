package it.dedagroup.registration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ObjectActionPayload(
        Long classPK,
        ObjectEntry objectEntry
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ObjectEntry(
            Long objectEntryId,
            Values values
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Values(
            String email,
            String givenName,
            String familyName,
            String statusRegistration,
            Boolean privacyAccepted
    ) {}
}