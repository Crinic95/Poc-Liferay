package it.dedagroup.contratti.microservice.liferay;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class LiferayUserClient {

    private final WebClient liferay;

    private static final String TAXCODE_CUSTOM_FIELD = "taxCode";

    public LiferayUserClient(WebClient liferayWebClient) {
        this.liferay = liferayWebClient;
    }

    @SuppressWarnings("unchecked")
    public List<Long> findUserIdsByTaxCode(String taxCode) {
        if (taxCode == null || taxCode.isBlank()) return Collections.emptyList();

        Map resp = liferay.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/o/headless-admin-user/v1.0/user-accounts")
                        .queryParam("search", taxCode)
                        .queryParam("pageSize", 200)
                        .queryParam("nestedFields", "customFields")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (resp == null) return Collections.emptyList();

        List<Map<String, Object>> items = (List<Map<String, Object>>) resp.get("items");
        if (items == null || items.isEmpty()) return Collections.emptyList();

        List<Long> result = new ArrayList<>();

        for (Map<String, Object> u : items) {
            Object customFieldsObj = u.get("customFields");
            if (!(customFieldsObj instanceof List<?> customFields)) continue;

            String cfValue = extractTaxCode(customFields);
            if (cfValue != null && taxCode.equalsIgnoreCase(cfValue.trim())) {
                Object id = u.get("id");
                if (id != null) {
                    result.add(Long.parseLong(String.valueOf(id)));
                }
            }
        }

        return result;
    }

    public Long findUserIdByTaxCode(String taxCode) {
        List<Long> ids = findUserIdsByTaxCode(taxCode);
        return ids.isEmpty() ? null : ids.get(0);
    }

    private String extractTaxCode(List<?> customFields) {
        for (Object o : customFields) {
            if (!(o instanceof Map<?, ?> cf)) continue;

            Object name = cf.get("name");
            if (name == null || !TAXCODE_CUSTOM_FIELD.equals(String.valueOf(name))) continue;

            Object customValueObj = cf.get("customValue");
            if (!(customValueObj instanceof Map<?, ?> customValue)) return null;

            Object data = customValue.get("data");
            return (data == null) ? null : String.valueOf(data);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<String> findUserEmailsByTaxCode(String taxCode) {
        if (taxCode == null || taxCode.isBlank()) return List.of();

        Map resp = liferay.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/o/headless-admin-user/v1.0/user-accounts")
                        .queryParam("search", taxCode)
                        .queryParam("pageSize", 200)
                        .queryParam("nestedFields", "customFields")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (resp == null) return List.of();

        List<Map<String, Object>> items = (List<Map<String, Object>>) resp.get("items");
        if (items == null || items.isEmpty()) return List.of();

        List<String> emails = new java.util.ArrayList<>();

        for (Map<String, Object> u : items) {
            Object customFieldsObj = u.get("customFields");
            if (!(customFieldsObj instanceof List<?> customFields)) continue;

            String cfValue = extractTaxCode(customFields);
            if (cfValue != null && taxCode.equalsIgnoreCase(cfValue.trim())) {
                Object email = u.get("emailAddress");
                if (email != null && !String.valueOf(email).isBlank()) {
                    emails.add(String.valueOf(email));
                }
            }
        }

        return emails;
    }
}