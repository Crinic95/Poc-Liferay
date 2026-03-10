package it.dedagroup.contratti.microservice.liferay;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Locale;
import java.util.Map;

@Component
public class LiferayContrattoClient {

    private final WebClient liferay;
    private final String objectPath;

    public LiferayContrattoClient(
            WebClient liferayWebClient,
            @Value("${liferay.contratti.path}") String objectPath
    ) {
        this.liferay = liferayWebClient;
        this.objectPath = objectPath;
    }

    public void upsertByERC(Map<String, Object> payload, String erc) {
        try {
            liferay.post()
                    .uri(objectPath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return;

        } catch (WebClientResponseException e) {
            if (!isDuplicateERC(e)) {
                logUpsertError("POST", e, payload);
                throw e;
            }
        }

        Long id = findIdByERC(erc);
        if (id == null) {
            throw new IllegalStateException("Impossibile fare upsert: non trovo record per ERC=" + erc);
        }

        try {
            liferay.patch()
                    .uri(objectPath + "/" + id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

        } catch (WebClientResponseException e) {
            logUpsertError("PATCH", e, payload);
            throw e;
        }
    }

    public Long findIdByERC(String erc) {
        Map resp = liferay.get()
                .uri(objectPath + "/by-external-reference-code/" + erc)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (resp == null || resp.get("id") == null) return null;
        return Long.parseLong(String.valueOf(resp.get("id")));
    }

    private boolean isDuplicateERC(WebClientResponseException e) {
        int status = e.getStatusCode().value();
        if (status == 409) return true;

        if (status == 400) {
            String body = e.getResponseBodyAsString();
            if (body == null) return false;

            String b = body.toLowerCase(Locale.ROOT);
            return b.contains("external reference code is already in use");
        }

        return false;
    }

    private void logUpsertError(String phase, WebClientResponseException e, Map<String, Object> payload) {
        System.out.println("=== LIFERAY ERROR (" + phase + ") ===");
        System.out.println("Status: " + e.getStatusCode());
        System.out.println("Body  : " + e.getResponseBodyAsString());
        System.out.println("Payload: " + payload);
        System.out.println("===============================");
    }
}