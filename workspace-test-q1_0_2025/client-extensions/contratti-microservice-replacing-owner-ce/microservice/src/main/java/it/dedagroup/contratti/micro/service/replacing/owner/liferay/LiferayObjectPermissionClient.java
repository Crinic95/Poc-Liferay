package it.dedagroup.contratti.micro.service.replacing.owner.liferay;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class LiferayObjectPermissionClient {

    private final WebClient liferay;
    private final String objectPath;

    public LiferayObjectPermissionClient(
            WebClient liferayWebClient,
            @Value("${liferay.contratti.path}") String objectPath
    ) {
        this.liferay = liferayWebClient;
        this.objectPath = objectPath;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getPermissions(long objectEntryId) {
        Map<String, Object> resp = liferay.get()
                .uri(objectPath + "/" + objectEntryId + "/permissions")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (resp == null || !(resp.get("items") instanceof List<?> list)) {
            return List.of();
        }

        return (List<Map<String, Object>>) (List<?>) list;
    }

    @SuppressWarnings("unchecked")
    public void grantViewToRole(long objectEntryId, String roleName) {
        List<Map<String, Object>> currentItems = getPermissions(objectEntryId);

        Map<String, List<String>> merged = new LinkedHashMap<>();

        for (Map<String, Object> item : currentItems) {
            String existingRoleName = String.valueOf(item.get("roleName"));
            Object actionIdsObj = item.get("actionIds");

            List<String> actionIds = new ArrayList<>();

            if (actionIdsObj instanceof List<?> actionList) {
                for (Object action : actionList) {
                    if (action != null) {
                        String value = String.valueOf(action).trim().toUpperCase();
                        if (!value.isBlank() && !actionIds.contains(value)) {
                            actionIds.add(value);
                        }
                    }
                }
            }

            merged.put(existingRoleName, actionIds);
        }

        merged.computeIfAbsent(roleName, k -> new ArrayList<>());

        if (!merged.get(roleName).contains("VIEW")) {
            merged.get(roleName).add("VIEW");
        }

        List<Map<String, Object>> body = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : merged.entrySet()) {
            Map<String, Object> permission = new LinkedHashMap<>();
            permission.put("roleName", entry.getKey());
            permission.put("actionIds", entry.getValue());
            body.add(permission);
        }

        liferay.put()
                .uri(objectPath + "/" + objectEntryId + "/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public void grantViewToRoleForPath(String path, long objectEntryId, String roleName) {
        List<Map<String, Object>> currentItems = getPermissionsForPath(path, objectEntryId);

        Map<String, List<String>> merged = new LinkedHashMap<>();

        for (Map<String, Object> item : currentItems) {
            String existingRoleName = String.valueOf(item.get("roleName"));
            Object actionIdsObj = item.get("actionIds");

            List<String> actionIds = new ArrayList<>();

            if (actionIdsObj instanceof List<?> actionList) {
                for (Object action : actionList) {
                    if (action != null) {
                        String value = String.valueOf(action).trim().toUpperCase();
                        if (!value.isBlank() && !actionIds.contains(value)) {
                            actionIds.add(value);
                        }
                    }
                }
            }

            merged.put(existingRoleName, actionIds);
        }

        merged.computeIfAbsent(roleName, k -> new ArrayList<>());

        if (!merged.get(roleName).contains("VIEW")) {
            merged.get(roleName).add("VIEW");
        }

        List<Map<String, Object>> body = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : merged.entrySet()) {
            Map<String, Object> permission = new LinkedHashMap<>();
            permission.put("roleName", entry.getKey());
            permission.put("actionIds", entry.getValue());
            body.add(permission);
        }

        liferay.put()
                .uri(path + "/" + objectEntryId + "/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getPermissionsForPath(String path, long objectEntryId) {
        Map<String, Object> resp = liferay.get()
                .uri(path + "/" + objectEntryId + "/permissions")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (resp == null || !(resp.get("items") instanceof List<?> list)) {
            return List.of();
        }

        return (List<Map<String, Object>>) (List<?>) list;
    }
}