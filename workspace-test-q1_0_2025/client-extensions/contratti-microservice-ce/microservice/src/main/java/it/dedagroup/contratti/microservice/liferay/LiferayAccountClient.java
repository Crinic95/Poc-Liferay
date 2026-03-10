package it.dedagroup.contratti.microservice.liferay;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

@Component
public class LiferayAccountClient {

    private final WebClient liferay;

    public LiferayAccountClient(WebClient liferayWebClient) {
        this.liferay = liferayWebClient;
    }

    @SuppressWarnings("unchecked")
    public Long findOrCreateAccount(String taxCode) {
        Map resp = liferay.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/o/headless-admin-user/v1.0/accounts")
                        .queryParam("search", taxCode)
                        .queryParam("pageSize", 50)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        List<Map<String, Object>> items = resp == null ? null : (List<Map<String, Object>>) resp.get("items");

        if (items != null && !items.isEmpty()) {
            return Long.parseLong(String.valueOf(items.get(0).get("id")));
        }

        Map<String, Object> payload = Map.of(
                "name", taxCode,
                "type", "business"
        );

        Map created = liferay.post()
                .uri("/o/headless-admin-user/v1.0/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (created == null || created.get("id") == null) {
            throw new IllegalStateException("Creazione Account fallita per taxCode=" + taxCode);
        }

        return Long.parseLong(String.valueOf(created.get("id")));
    }

    public void assignUserToAccountIfMissing(Long accountId, String emailAddress) {
        if (emailAddress == null || emailAddress.isBlank()) return;

        try {
            liferay.post()
                    .uri("/o/headless-admin-user/v1.0/accounts/" + accountId
                            + "/user-accounts/by-email-address/" + java.net.URLEncoder.encode(emailAddress, java.nio.charset.StandardCharsets.UTF_8))
                    .retrieve()
                    .toBodilessEntity()
                    .block();

        } catch (org.springframework.web.reactive.function.client.WebClientResponseException e) {
            if (e.getStatusCode().value() == 409) return;

            System.out.println("=== LIFERAY ERROR assignUserToAccountIfMissing ===");
            System.out.println("Status: " + e.getStatusCode());
            System.out.println("Body  : " + e.getResponseBodyAsString());
            System.out.println("Payload: {accountId=" + accountId + ", emailAddress=" + emailAddress + "}");
            System.out.println("===============================================");
            throw e;
        }
    }
}