package it.dedagroup.registration.dto;

public record UserAccountResponse(Long id,
                                  String emailAddress,
                                  String givenName,
                                  String familyName,
                                  String alternateName) {
}
