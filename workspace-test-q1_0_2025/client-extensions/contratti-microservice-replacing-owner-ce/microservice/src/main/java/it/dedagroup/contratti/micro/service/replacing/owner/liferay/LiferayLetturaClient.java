package it.dedagroup.contratti.micro.service.replacing.owner.liferay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

@Component
public class LiferayLetturaClient {

    private final WebClient liferay;
    private final String objectPath;

    public LiferayLetturaClient(
            @Qualifier("liferayWebClient") WebClient liferayWebClient,
            @Value("${liferay.letture.path}") String objectPath
    ) {
        this.liferay = liferayWebClient;
        this.objectPath = objectPath;
    }

    public void upsertByERC(Map<String, Object> payload, String erc) {
        try {
            System.out.println("PUT lettura by ERC=" + erc);

            liferay.put()
                    .uri(objectPath + "/by-external-reference-code/" + erc)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

        } catch (WebClientResponseException e) {
            logUpsertError("PUT_BY_ERC", e, payload);
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    public Long findIdByERC(String erc) {
        try {
            Map<String, Object> resp = liferay.get()
                    .uri(objectPath + "/by-external-reference-code/" + erc)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (resp == null || resp.get("id") == null) {
                return null;
            }

            return Long.parseLong(String.valueOf(resp.get("id")));
        } catch (WebClientResponseException.NotFound e) {
            return null;
        }
    }

    private void logUpsertError(String phase, WebClientResponseException e, Map<String, Object> payload) {
        System.out.println("=== LIFERAY ERROR Lettura (" + phase + ") ===");
        System.out.println("Status: " + e.getStatusCode());
        System.out.println("Body  : " + e.getResponseBodyAsString());
        System.out.println("Payload: " + payload);
        System.out.println("===================================");
    }
}