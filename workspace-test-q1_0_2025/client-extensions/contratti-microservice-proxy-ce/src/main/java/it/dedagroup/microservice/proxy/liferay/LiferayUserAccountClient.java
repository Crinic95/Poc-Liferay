package it.dedagroup.microservice.proxy.liferay;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class LiferayUserAccountClient {

    private final WebClient webClient;

    public LiferayUserAccountClient(
            WebClient.Builder builder,
            @Value("${liferay.base-url}") String liferayBaseUrl
    ) {
        this.webClient = builder.baseUrl(liferayBaseUrl).build();
    }

    public JSONObject getMyUserAccount(String bearerToken) {
        String body = webClient.get()
                .uri("/o/headless-admin-user/v1.0/my-user-account")
                .header("Authorization", "Bearer " + bearerToken)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return new JSONObject(body);
    }
}