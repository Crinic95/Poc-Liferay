package it.dedagroup.contratti.micro.service.replacing.owner.liferay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

@Component
public class LiferayContrattoClient {

    private final WebClient liferay;
    private final String objectPath;

    public LiferayContrattoClient(
            @Qualifier("liferayWebClient") WebClient liferayWebClient,
            @Value("${liferay.contratti.path}") String objectPath
    ) {
        this.liferay = liferayWebClient;
        this.objectPath = objectPath;
    }

    public void upsertByERC(Map<String, Object> payload, String erc) {
        System.out.println("upsertByERC START erc=" + erc);
        System.out.println("objectPath=" + objectPath);

        Long existingId = findIdByERC(erc);

        System.out.println("upsertByERC existingId=" + existingId + " for erc=" + erc);

        if (existingId != null) {
            try {
                System.out.println("PATCH existing contratto id=" + existingId + " erc=" + erc);

                liferay.patch()
                        .uri(objectPath + "/" + existingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(payload)
                        .retrieve()
                        .toBodilessEntity()
                        .block();

                return;
            } catch (WebClientResponseException e) {
                logUpsertError("PATCH", e, payload);
                throw e;
            }
        }

        try {
            System.out.println("PUT contratto by ERC=" + erc);

            liferay.post()
                    .uri(objectPath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

        } catch (WebClientResponseException e) {
            logUpsertError("POST", e, payload);
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    public Long findIdByERC(String erc) {
        try {
            System.out.println("findIdByERC GET " + objectPath + "/by-external-reference-code/" + erc);

            Map<String, Object> resp = liferay.get()
                    .uri(objectPath + "/by-external-reference-code/" + erc)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            System.out.println("findIdByERC response=" + resp);

            if (resp == null || resp.get("id") == null) {
                return null;
            }

            return Long.parseLong(String.valueOf(resp.get("id")));
        } catch (WebClientResponseException.NotFound e) {
            System.out.println("findIdByERC NOT FOUND erc=" + erc);
            return null;
        } catch (WebClientResponseException e) {
            System.out.println("findIdByERC ERROR status=" + e.getStatusCode().value()
                    + " body=" + e.getResponseBodyAsString());
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