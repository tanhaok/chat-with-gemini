package com.gemini.app.webhook;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/hook")
public class WebhookController {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebhookController.class);
    private final WebhookImplement webhookImplement;

    public WebhookController(WebhookImplement webhookImplement) {
        this.webhookImplement = webhookImplement;
    }

    @PostMapping("/personal")
    public ResponseEntity<String> webhook(@RequestParam String data, @RequestParam String secretKey) {
        LOGGER.info("Receive quest with request param: " + data);
        String resp = this.webhookImplement.webhookHandler(data, secretKey);

        return ResponseEntity.ok(resp);
    }

    @GetMapping()
    public ResponseEntity<String> webHook(@RequestBody JSONObject jsonObject){
        LOGGER.info(jsonObject.toString());
        return null;
    }
}
