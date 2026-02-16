package it.dedagroup.contratti.microservice.liferay;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

@Component
public class LiferayBollettaClient {

    private final WebClient liferay;
    private final String bollettePath;

    public LiferayBollettaClient(
            WebClient liferayWebClient,
            @Value("${liferay.bollette.path}") String bollettePath
    ) {
        this.liferay = liferayWebClient;
        this.bollettePath = bollettePath;
    }

    public void upsertByERC(Map<String, Object> payload, String erc) {
        try {
            liferay.post()
                    .uri(bollettePath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return;
        } catch (WebClientResponseException e) {
            // 409 conflict (ERC già esiste) oppure 400 in certi casi
            if (e.getStatusCode().value() != 409 && e.getStatusCode().value() != 400) {
                logError("POST", e, payload);
                throw e;
            }
        }

        Long id = findIdByERC(erc);
        if (id == null) {
            throw new IllegalStateException("Impossibile fare upsert: non trovo Bolletta per ERC=" + erc);
        }

        try {
            liferay.patch()
                    .uri(bollettePath + "/" + id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException e) {
            logError("PATCH", e, payload);
            throw e;
        }
    }

    private Long findIdByERC(String erc) {
        Map resp = liferay.get()
                .uri(bollettePath + "/by-external-reference-code/" + erc)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (resp == null || resp.get("id") == null) return null;
        return Long.parseLong(String.valueOf(resp.get("id")));
    }

    private void logError(String phase, WebClientResponseException e, Map<String, Object> payload) {
        System.out.println("=== LIFERAY ERROR Bolletta (" + phase + ") ===");
        System.out.println("Status: " + e.getStatusCode());
        System.out.println("Body  : " + e.getResponseBodyAsString());
        System.out.println("Payload: " + payload);
        System.out.println("============================================");
    }
}