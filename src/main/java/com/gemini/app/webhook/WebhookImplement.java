package com.gemini.app.webhook;

import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class WebhookImplement {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebhookController.class);
    private final RestClient restClient;
    @Value("${gg.gemini.url}")
    private String baseUrl;
    @Value("${gg.gemini.api-key}")
    private String apiKey;
    @Value("${telegram.api-key}")
    private String telegramKey;
    @Value("${telegram.url}")
    private String telegramUrl;
    private static final String KEY = "text";

    public WebhookImplement() {
        this.restClient = RestClient.create();
    }

    public String webhookHandler(String data, String secretKey) {
        if (!isSecure(secretKey)) {
            LOGGER.error("SECRET KEY INVALID");
            return "SECRET KEY INVALID";
        }
        String text = data.replace("\"", "");
        String reqBody = "{\"contents\":[{\"parts\":[{\"text\":\"" + data + "\"}]}]}";
        LOGGER.info("Send request with request body: " + reqBody + " to google api");
        String uri = baseUrl + "?key=" + apiKey;
        String resp = restClient.post().uri(uri).contentType(MediaType.APPLICATION_JSON)
            .body(reqBody).retrieve().body(String.class);
        String result;
        try {
            JSONObject jsonObject = new JSONObject(resp);
            result = jsonObject.getJSONArray("candidates").getJSONObject(0).getJSONObject("content")
                .getJSONArray("parts").getJSONObject(0).get("text").toString();

        } catch (JSONException e) {
            result = "Cannot get data";
            LOGGER.error(e.toString());
        }

        return result;
    }

    private boolean isSecure(String key) {
        String sha256hex = Hashing.sha256().hashString(apiKey, StandardCharsets.UTF_8).toString();
        return sha256hex.equals(key);
    }

    protected <T extends LinkedHashMap> void sendMessageTelegram(String secretKey, T object) {
        try {
            T msgObj = (T) object.get("message");
            String text = msgObj.get("text").toString();
            String msg = this.webhookHandler(text, secretKey);
            T chatObj = (T) msgObj.get("chat");
            String chatId = chatObj.get("id").toString();
            String telegramUri =
                telegramUrl + telegramKey + "/sendMessage" + "?chat_id=" + chatId + "&text=" + msg;
            String resp = restClient.get().uri(telegramUri).retrieve().body(String.class);
            LOGGER.info(resp);

        } catch (JSONException e) {
            LOGGER.error(e.toString());
        }

    }

}
