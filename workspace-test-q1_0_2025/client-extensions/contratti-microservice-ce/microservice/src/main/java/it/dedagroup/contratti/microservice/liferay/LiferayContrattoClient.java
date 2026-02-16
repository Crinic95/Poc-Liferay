package it.dedagroup.contratti.microservice.liferay;

import it.dedagroup.contratti.microservice.liferay.dto.LiferayListResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Component
public class LiferayContrattoClient {

    private final WebClient liferay;
    private final String objectPath;

    public LiferayContrattoClient(WebClient liferayWebClient,
                                  @Value("${liferay.contratti.path}") String objectPath) {
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
            if (e.getStatusCode().value() != 409 && e.getStatusCode().value() != 400) {
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
        try {
            Map resp = liferay.get()
                    .uri(objectPath + "/by-external-reference-code/" + erc)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (resp == null || resp.get("id") == null) return null;
            return Long.parseLong(String.valueOf(resp.get("id")));
        } catch (WebClientResponseException e) {
            System.out.println("=== LIFERAY ERROR (findByERC) ===");
            System.out.println("Status: " + e.getStatusCode());
            System.out.println("Body  : " + e.getResponseBodyAsString());
            System.out.println("================================");
            throw e;
        }
    }

    private void logUpsertError(String phase, WebClientResponseException e, Map<String, Object> payload) {
        System.out.println("=== LIFERAY ERROR (" + phase + ") ===");
        System.out.println("Status: " + e.getStatusCode());
        System.out.println("Body  : " + e.getResponseBodyAsString());
        System.out.println("Payload: " + payload);
        System.out.println("===============================");
    }
}