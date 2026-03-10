package it.dedagroup.registration;

import it.dedagroup.registration.dto.TokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Component
public class ClientCredentialsAccessTokenProvider implements AccessTokenProvider {

    private final RestClient restClient;

    @Value("${liferay.base-url}")
    private String liferayBaseUrl;

    @Value("${registration-create-user-oahs.oauth2.headless.server.client.id:}")
    private String clientId;

    @Value("${registration-create-user-oahs.oauth2.headless.server.client.secret:}")
    private String clientSecret;

    @Value("${registration-create-user-oahs.oauth2.token.uri:}")
    private String tokenUri;

    private volatile String cachedAccessToken;
    private volatile Instant expiresAt;

    public ClientCredentialsAccessTokenProvider(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    @Override
    public synchronized String getAccessToken() {
        if (_isValid()) {
            return cachedAccessToken;
        }

        _validateConfig();

        TokenResponse tokenResponse = _fetchToken();

        cachedAccessToken = tokenResponse.accessToken();

        long expiresIn = tokenResponse.expiresIn() != null ? tokenResponse.expiresIn() : 300L;
        expiresAt = Instant.now().plusSeconds(Math.max(30, expiresIn - 30));

        return cachedAccessToken;
    }

    private boolean _isValid() {
        return cachedAccessToken != null &&
                expiresAt != null &&
                Instant.now().isBefore(expiresAt);
    }

    private void _validateConfig() {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalStateException(
                    "Missing property: registration-create-user-oahs.oauth2.headless.server.client.id");
        }

        if (clientSecret == null || clientSecret.isBlank()) {
            throw new IllegalStateException(
                    "Missing property: registration-create-user-oahs.oauth2.headless.server.client.secret");
        }

        if (tokenUri == null || tokenUri.isBlank()) {
            throw new IllegalStateException(
                    "Missing property: registration-create-user-oahs.oauth2.token.uri");
        }
    }

    private TokenResponse _fetchToken() {
        String basicAuth = Base64.getEncoder().encodeToString(
                (clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8)
        );

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        return restClient.post()
                .uri(liferayBaseUrl + tokenUri)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + basicAuth)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(TokenResponse.class);
    }
}