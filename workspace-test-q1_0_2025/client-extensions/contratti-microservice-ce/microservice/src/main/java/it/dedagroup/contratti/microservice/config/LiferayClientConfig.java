package it.dedagroup.contratti.microservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class LiferayClientConfig {

    @Bean
    public WebClient liferayWebClient(
            @Value("${liferay.baseUrl}") String baseUrl,
            @Value("${liferay.username}") String username,
            @Value("${liferay.password}") String password
    ) {
        String basic = Base64.getEncoder()
                .encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + basic)
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .build();
    }
}