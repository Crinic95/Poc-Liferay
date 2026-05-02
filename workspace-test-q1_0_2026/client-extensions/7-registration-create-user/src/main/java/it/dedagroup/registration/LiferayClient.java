package it.dedagroup.registration;

import it.dedagroup.registration.dto.UserAccountRequest;
import it.dedagroup.registration.dto.UserAccountResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class LiferayClient {

    private final RestClient restClient;

    @Value("${liferay.base-url}")
    private String liferayBaseUrl;

    @Value("${liferay.registration-request-path}")
    private String registrationRequestPath;

    public LiferayClient(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    public void updateRegistrationRequest(String accessToken, Long objectEntryId, Map<String, Object> body) {
        String uri = liferayBaseUrl + registrationRequestPath + "/" + objectEntryId;

        restClient.patch()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    public UserAccountResponse createUser(String accessToken, UserAccountRequest request) {
        String uri = liferayBaseUrl + "/o/headless-admin-user/v1.0/user-accounts";

        return restClient.post()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .body(request)
                .retrieve()
                .body(UserAccountResponse.class);
    }
}