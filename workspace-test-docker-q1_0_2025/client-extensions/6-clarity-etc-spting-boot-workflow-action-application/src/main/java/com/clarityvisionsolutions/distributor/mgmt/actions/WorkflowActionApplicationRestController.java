package com.clarityvisionsolutions.distributor.mgmt.actions;

import com.liferay.client.extension.util.spring.boot.BaseRestController;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

@RequestMapping("/workflow/action/application")
@RestController
public class WorkflowActionApplicationRestController extends BaseRestController {

    private static final String AUTO_APPROVE_STATE_KEY = "withdrawn";

    @Value("${liferay.base.url}")
    private String liferayBaseUrl;

    @PostMapping
    public ResponseEntity<String> post(@AuthenticationPrincipal Jwt jwt, @RequestBody String json) {
        System.out.println("WORKFLOW ACTION");
        System.out.println("payload=" + json);

        JSONObject payload = new JSONObject(json);

        String transitionURL = payload.getString("transitionURL");
        String absoluteTransitionURL =
                transitionURL.startsWith("http")
                        ? transitionURL
                        : liferayBaseUrl + transitionURL;

        String transitionName = _getTransitionName(payload);

        WebClient webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + jwt.getTokenValue())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        String out = webClient.post()
                .uri(absoluteTransitionURL)
                .bodyValue("{\"transitionName\": \"" + transitionName + "\"}")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        System.out.println("Workflow transition [" + transitionName + "] result: " + out);

        return ResponseEntity.ok(json);
    }

    private String _getTransitionName(JSONObject payload) {
        JSONObject entryDTO = payload.getJSONObject("entryDTO");

        Object appStateObj = entryDTO.opt("applicationState");
        String key = "";

        if (appStateObj instanceof JSONObject jo) {
            key = jo.optString("key", "");
        }
        else if (appStateObj instanceof String s) {
            key = s;
        }
        return AUTO_APPROVE_STATE_KEY.equalsIgnoreCase(key) ? "auto-approve" : "review";
    }
}