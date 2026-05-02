package it.dedagroup.registration.dto;

public record UserAccountRequest(String emailAddress,
                                 String givenName,
                                 String familyName,
                                 String alternateName) {
}
