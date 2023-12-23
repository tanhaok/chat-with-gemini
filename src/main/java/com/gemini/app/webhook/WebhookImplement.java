package com.gemini.app.webhook;

import com.google.common.hash.Hashing;
import java.net.URLEncoder;
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
    @Value("${telegram.user-id}")
    private String userId;
    @Value("${telegram.second-user-id}")
    private String secondUserId;
    private static final String KEY = "text";

    public WebhookImplement() {
        this.restClient = RestClient.create();
    }

    public String webhookHandler(String data, String secretKey) {
        if (!isSecure(secretKey)) {
            LOGGER.error("SECRET KEY INVALID");
            return "SECRET KEY INVALID";
        }
        String text = data.replaceAll("\"", "");
        String reqBody = "{\"contents\":[{\"parts\":[{\"text\":\"" + text + "\"}]}]}";
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
        } catch (IllegalArgumentException e) {
            result = "Fuck you";
            LOGGER.error("Can not get data");
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
            T fromUser = (T) msgObj.get("from");
            String id = fromUser.get("id").toString();
            if (userId.equals(id) || secondUserId.equals(id)) {
                String text = msgObj.get("text").toString();
                String msg = URLEncoder.encode(this.webhookHandler(text, secretKey),
                    StandardCharsets.UTF_8);
                T chatObj = (T) msgObj.get("chat");
                String chatId = chatObj.get("id").toString();
                String telegramUri =
                    telegramUrl + telegramKey + "/sendMessage" + "?chat_id=" + chatId + "&text="
                        + msg;
                String resp = restClient.get().uri(telegramUri).retrieve().body(String.class);
                LOGGER.info(resp);
            } else {
                LOGGER.error("USER ID NOT VALID");
            }


        } catch (JSONException | NullPointerException e) {
            LOGGER.error(e.toString());
        }

    }

}
