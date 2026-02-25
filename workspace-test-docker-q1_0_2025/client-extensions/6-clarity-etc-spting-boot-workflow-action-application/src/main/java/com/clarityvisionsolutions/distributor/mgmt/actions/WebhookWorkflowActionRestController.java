package com.clarityvisionsolutions.distributor.mgmt.actions;

import com.liferay.client.extension.util.spring.boot.BaseRestController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

@RequestMapping("/workflow/action/webhook")
@RestController
public class WebhookWorkflowActionRestController extends BaseRestController {

    private static final Log _log = LogFactory.getLog(WebhookWorkflowActionRestController.class);

    @Value("${clarity.webhook.url}")
    private String webhookUrl;

    @PostMapping
    public ResponseEntity<String> post(@AuthenticationPrincipal Jwt jwt, @RequestBody String json) {
        log(jwt, _log, json);

        JSONObject payload = new JSONObject(json);

        JSONObject out = new JSONObject();
        out.put("source", "workflowAction");
        out.put("taskName", payload.optString("taskName"));
        out.put("entryType", payload.optString("entryType"));
        out.put("entryClassPK", payload.optString("entryClassPK"));
        out.put("companyId", payload.opt("companyId"));
        out.put("userId", payload.opt("userId"));
        out.put("context", payload);

        WebClient.create()
                .post()
                .uri(webhookUrl)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(out.toString())
                .retrieve()
                .bodyToMono(String.class)
                .onErrorReturn("webhook-error")
                .doOnNext(resp -> _log.info("Webhook sent, response=" + resp))
                .block();

        return ResponseEntity.ok(json);
    }
}