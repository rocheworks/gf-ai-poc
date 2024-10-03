package com.grundfos.pump.replace.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@Service
public class PushNotificationService {

    @Autowired
    private ResourceLoader resourceLoader;

    FileInputStream SERVICE_ACCOUNT_FILE_PATH = new FileInputStream("./src/main/resources/gf-goapp-push-notices-firebase-adminsdk-rn8qe-b1081a1909.json");

    private static final String FCM_URL = "https://fcm.googleapis.com/v1/projects/gf-goapp-push-notices/messages:send";

    public PushNotificationService() throws FileNotFoundException {
    }

    // Generate OAuth 2.0 token
    public String getAccessToken() throws IOException {
        GoogleCredentials credentials = GoogleCredentials
                .fromStream(new FileInputStream("./src/main/resources/gf-goapp-push-notices-firebase-adminsdk-rn8qe-b1081a1909.json"))
                .createScoped("https://www.googleapis.com/auth/cloud-platform");

        credentials.refreshIfExpired();
        return credentials.getAccessToken().getTokenValue();
    }

    // Send push notification
    public void pushNotices() {
        try {
            // Get OAuth token
            String accessToken = getAccessToken();
            System.out.println("accessToken:: "+accessToken);

            // Replace <your_device_registration_token> with actual device token
            String requestBody = "{"
                    + "\"message\": {"
                    + "\"token\": \"your_device_registration_token\","
                    + "\"notification\": {"
                    + "\"title\": \"Grundfos GO App Alert\","
                    + "\"body\": \"Your pump installation was successful! Please rate the experience.\""
                    + "},"
                    + "\"data\": {"
                    + "\"additionalData\": \"Some additional data if needed\""
                    + "}"
                    + "}"
                    + "}";

            // Construct the HTTP request using WebClient
            WebClient client = WebClient.builder()
                    .baseUrl(FCM_URL)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .build();

            // Send the request
            Mono<String> response = client.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class);

            // Print the response (for debugging purposes)
            response.subscribe(System.out::println);

        } catch (IOException e) {
            e.printStackTrace();
            // Handle the error (e.g., log or show an error message in the UI)
        }
    }
}