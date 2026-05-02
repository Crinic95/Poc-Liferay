package it.dedagroup.contratti.micro.service.replacing.owner.liferay;

import it.dedagroup.contratti.micro.service.replacing.owner.liferay.dto.LiferayUserMatch;
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
    public List<Map<String, Object>> findUsersByTaxCodeRaw(String taxCode) {
        if (taxCode == null || taxCode.isBlank()) {
            return Collections.emptyList();
        }

        Map<String, Object> resp = liferay.get()
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

        if (resp == null) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> items = (List<Map<String, Object>>) resp.get("items");
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (Map<String, Object> user : items) {
            Object customFieldsObj = user.get("customFields");
            if (!(customFieldsObj instanceof List<?> customFields)) {
                continue;
            }

            String cfValue = extractTaxCode(customFields);

            if (cfValue != null && taxCode.equalsIgnoreCase(cfValue.trim())) {
                result.add(user);
            }
        }

        return result;
    }

    public UserLookupResult findStrictUniqueUserByTaxCode(String taxCode) {
        List<Map<String, Object>> users = findUsersByTaxCodeRaw(taxCode);

        if (users.isEmpty()) {
            return new UserLookupResult(null, 0);
        }

        if (users.size() > 1) {
            return new UserLookupResult(null, users.size());
        }

        Map<String, Object> user = users.get(0);

        LiferayUserMatch match = new LiferayUserMatch(
                Long.parseLong(String.valueOf(user.get("id"))),
                stringOrNull(user.get("emailAddress")),
                stringOrNull(user.get("alternateName")),
                stringOrNull(user.get("givenName")),
                stringOrNull(user.get("familyName"))
        );

        return new UserLookupResult(match, 1);
    }

    public record UserLookupResult(LiferayUserMatch user, int matches) {
    }

    private String extractTaxCode(List<?> customFields) {
        for (Object o : customFields) {
            if (!(o instanceof Map<?, ?> cf)) {
                continue;
            }

            Object name = cf.get("name");
            if (name == null || !TAXCODE_CUSTOM_FIELD.equals(String.valueOf(name))) {
                continue;
            }

            Object customValueObj = cf.get("customValue");
            if (!(customValueObj instanceof Map<?, ?> customValue)) {
                return null;
            }

            Object data = customValue.get("data");
            return data == null ? null : String.valueOf(data);
        }

        return null;
    }

    private String stringOrNull(Object o) {
        if (o == null) {
            return null;
        }

        String s = String.valueOf(o);
        return s.isBlank() ? null : s;
    }
}