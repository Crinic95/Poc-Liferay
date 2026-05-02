package it.dedagroup.contratti.micro.service.replacing.owner.liferay;

import it.dedagroup.contratti.micro.service.replacing.owner.liferay.dto.LiferayUserMatch;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;

@Component
public class LiferayRoleClient {

    private final WebClient liferay;

    public LiferayRoleClient(@Qualifier("liferayWebClient") WebClient liferayWebClient) {
        this.liferay = liferayWebClient;
    }


    public String buildRoleNameForUser(long userId) {
        return "FORNITURE_USER_" + userId;
    }

    public String buildRoleTitleForUser(LiferayUserMatch user) {
        String label = firstNonBlank(
                user.givenName(),
                user.familyName(),
                user.alternateName(),
                user.emailAddress(),
                String.valueOf(user.userId())
        );

        return "Forniture " + label;
    }

    @SuppressWarnings("unchecked")
    public Long findRoleIdByName(String roleName) {
        int page = 1;

        while (true) {
            Map<String, Object> resp = liferay.get()
                    .uri("/o/headless-admin-user/v1.0/roles?page={page}&pageSize={pageSize}", page, 20)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (resp == null) {
                return null;
            }

            List<Map<String, Object>> items = (List<Map<String, Object>>) resp.get("items");
            if (items == null || items.isEmpty()) {
                return null;
            }

            for (Map<String, Object> item : items) {
                String existingName = String.valueOf(item.get("name"));
                String roleType = String.valueOf(item.get("roleType"));

                if (roleName.equals(existingName) && "regular".equalsIgnoreCase(roleType)) {
                    return Long.parseLong(String.valueOf(item.get("id")));
                }
            }

            Number lastPage = (Number) resp.get("lastPage");
            Number responsePage = (Number) resp.get("page");

            if (lastPage == null || responsePage == null || responsePage.intValue() >= lastPage.intValue()) {
                return null;
            }

            page++;
        }
    }

    public Long ensureRegularRoleExists(String roleName) {
        Long existingRoleId = findRoleIdByName(roleName);

        if (existingRoleId != null) {
            return existingRoleId;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("name", roleName);
        body.put("description", "Ruolo dedicato alle forniture per " + roleName);
        body.put("roleType", "regular");

        try {
            System.out.println("Creating regular role: " + roleName);

            Map<?, ?> created = liferay.post()
                    .uri("/o/headless-admin-user/v1.0/roles")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (created != null && created.get("id") != null) {
                return Long.parseLong(String.valueOf(created.get("id")));
            }
        } catch (WebClientResponseException e) {
            System.out.println("Role create error status=" + e.getStatusCode().value()
                    + " body=" + e.getResponseBodyAsString());

            if (e.getStatusCode().value() != 409) {
                throw e;
            }
        }

        Long roleId = findRoleIdByName(roleName);
        if (roleId == null) {
            throw new IllegalStateException("Ruolo non trovato dopo create: " + roleName);
        }

        return roleId;
    }

    public void ensureUserAssignedToRegularRole(long roleId, long userId) {
        try {
            liferay.post()
                    .uri("/o/headless-admin-user/v1.0/roles/{roleId}/association/user-account/{userId}", roleId, userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException e) {
            int status = e.getStatusCode().value();

            if (status == 409 || status == 204 || status == 200) {
                return;
            }

            System.out.println("Role association error status=" + status
                    + " body=" + e.getResponseBodyAsString());
            throw e;
        }
    }

    public RoleBinding ensureRegularRoleAssigned(LiferayUserMatch user) {
        String roleName = buildRoleNameForUser(user.userId());
        String roleTitle = buildRoleTitleForUser(user);

        Long roleId = ensureRegularRoleExists(roleName);
        ensureUserAssignedToRegularRole(roleId, user.userId());

        return new RoleBinding(roleId, roleName);
    }

    public record RoleBinding(long roleId, String roleName) {
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}