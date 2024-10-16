package com.grundfos.pump.replace.controller;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatMessage;
//import com.azure.ai.openai.models.ChatRequestMessage;
import com.azure.ai.openai.models.ChatRole;
import com.azure.core.credential.AzureKeyCredential;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final Map<String, String> deviceTokens = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    // Azure OpenAI Client Configuration
    private final OpenAIClient openAIClient;
    private final String deploymentName = "gpt-4o"; // Replace with your actual deployment name
    private final String endpoint = "https://gf-ais-openai-d.openai.azure.com/"; // Your Azure OpenAI resource endpoint
    private final String openAIKey = "ab003a30cec64c739f1c9b6922e636c9"; // Replace with your actual OpenAI key

    public NotificationController() {
        this.openAIClient = new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(openAIKey))
                .endpoint(endpoint)
                .buildClient();
    }

    @PostMapping("/register-token")
    public ResponseEntity<String> registerToken(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        if (token != null && !token.isEmpty()) {
            deviceTokens.put("user", token); // In practice, use a real identifier for users
            logger.info("Token registered successfully");
            return ResponseEntity.ok("Token registered successfully");
        } else {
            logger.warn("Token registration failed: Token is null or empty");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token registration failed");
        }
    }

    @PostMapping("/handle-response")
    public ResponseEntity<String> handleUserResponse(@RequestBody Map<String, String> payload) {
        if (payload != null && payload.containsKey("response")) {
            String userFeedback = payload.get("response");
            logger.info("Received user feedback: {}", userFeedback);

            // Analyze sentiment and generate automated response
            String automatedResponse = analyzeSentimentAndGenerateResponse(userFeedback);

            // Send automated response via FCM
            try {
                String token = deviceTokens.get("user");
                if (token == null) {
                    logger.error("No device token found for user");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No device token found!");
                }

                // Create and send the push notification
                Message message = Message.builder()
                        .setToken(token)
                        .setNotification(Notification.builder()
                                .setTitle("Thank You for Your Feedback")
                                .setBody(automatedResponse)
                                .build())
                        .build();

                FirebaseMessaging.getInstance().send(message);
                logger.info("Automated response sent successfully");
                return ResponseEntity.ok("Automated response sent successfully!");
            } catch (Exception e) {
                logger.error("Failed to send automated response: ", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send response!");
            }
        } else {
            logger.warn("Response payload is empty or missing 'response' key");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Response payload is empty or missing 'response' key");
        }
    }

    @PostMapping("/send-feedback")
    public ResponseEntity<String> sendNotification(@RequestBody Map<String, String> payload) {
        String messageTitle = payload.get("messageTitle");
        String messageBody = payload.get("messageBody");
        String token = deviceTokens.get("user");
        logger.info("token:: {}", token);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No device token found!");
        }

        // Send notification using Firebase Admin SDK
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(messageTitle)
                            .setBody(messageBody)
                            .build())
                    .build();

            FirebaseMessaging.getInstance().send(message);
            logger.info("Notification sent successfully!");
            return ResponseEntity.ok("Notification sent successfully!");
        } catch (Exception e) {
            logger.error("Failed to send notification!", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send notification!");
        }
    }

    /**
     * Analyzes the sentiment of the user feedback and generates an automated response.
     *
     * @param userFeedback The feedback provided by the user.
     * @return An automated response based on the sentiment.
     */
    private String analyzeSentimentAndGenerateResponse(String userFeedback) {
        // Create a prompt for sentiment analysis
        String prompt = "Analyze the sentiment of the following feedback and generate an appropriate response.\n\nFeedback: \"" + userFeedback + "\"\n\nSentiment: ";

        // Build the ChatCompletionsOptions object
        ChatMessage chatMessage = new ChatMessage(ChatRole.USER);
        //chatMessage.setRole(ChatRole.USER);
        chatMessage.setContent(prompt);

        ChatCompletionsOptions options = new ChatCompletionsOptions(Collections.singletonList(chatMessage))
                .setMaxTokens(100); // Adjust max tokens as needed

        // Get the completion (response) from OpenAI
        String openAIResponse;
        try {
            openAIResponse = openAIClient.getChatCompletions(deploymentName, options)
                    .getChoices()
                    .get(0)
                    .getMessage()
                    .getContent()
                    .trim();
            logger.info("OpenAI Response: {}", openAIResponse);
        } catch (Exception e) {
            logger.error("Failed to get response from OpenAI", e);
            return "Thank you for your feedback!";
        }

        // Parse the sentiment from OpenAI's response
        String sentiment;
        String generatedResponse;

        if (openAIResponse.toLowerCase().contains("positive")) {
            sentiment = "positive";
            generatedResponse = "Thank you for your positive feedback! We're glad you had a great experience.";
        } else if (openAIResponse.toLowerCase().contains("negative")) {
            sentiment = "negative";
            generatedResponse = "We are sorry to hear about your unpleasant experience. We are committed to improving and will address the issue promptly.";
        } else {
            sentiment = "neutral";
            generatedResponse = "Thank you for your feedback! We appreciate your input.";
        }

        logger.info("Sentiment: {}", sentiment);
        logger.info("Generated Response: {}", generatedResponse);

        // Optionally, store the feedback and sentiment in the database here

        return generatedResponse;
    }
    @PostMapping("/send-pump-replacement-complete")
    public ResponseEntity<String> sendPumpReplacementCompleteNotification() {
        String token = deviceTokens.get("user");
        if (token == null) {
            logger.error("No device token found for user");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No device token found!");
        }

        // Send a notification that pump replacement is complete
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle("Pump Replacement Completed")
                            .setBody("The pump replacement was successfully completed. We value your feedback and would love to hear how we can serve you better. Thank you!")
                            .build())
                    .build();

            FirebaseMessaging.getInstance().send(message);
            logger.info("Notification for pump replacement completion sent successfully");
            return ResponseEntity.ok("Notification sent successfully!");
        } catch (Exception e) {
            logger.error("Failed to send notification!", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send notification!");
        }
    }
    @PostMapping("/send-push-notice")
    public ResponseEntity<String> sendPushNotice(@RequestBody Map<String, String> payload) {
        String deviceToken = payload.get("deviceToken");
        String eventName = payload.get("eventName");
        Message message = null;
        System.out.println("deviceToken:  "+deviceToken+" & eventName:: "+eventName);
        if (deviceToken == null || deviceToken.isEmpty()) {
            logger.error("Device token is missing or empty");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Device token is missing or empty");
        }

        // Send notification to the specified device token
        try {
            if (eventName.startsWith("Go Replace Button Clicked")){
                 message = Message.builder()
                    .setToken(deviceToken)
                    .setNotification(Notification.builder()
                            .setTitle("Notification: Go Replace")
                            .setBody("Tell us about your experience with the Go Replace feature in the Go App. Your input is important to us!")
                            .build())
                    .build();
            } else if (eventName.startsWith("Pump Replacement Done")) {
                message = Message.builder()
                    .setToken(deviceToken)
                    .setNotification(Notification.builder()
                         .setTitle("Notification: Pump Replace")
                         .setBody("Please share your thoughts on using the pump replacement feature in the Go App. Your feedback helps us improve your experience!")
                         .build())
                    .build();
            } else if (eventName.startsWith("Done Button Clicked")) {
                message = Message.builder()
                   .setToken(deviceToken)
                   .setNotification(Notification.builder()
                   .setTitle("Notification: Guided Installation")
                   .setBody("We'd love to hear about your experience with the guided installation feature for pump replacement in the Go App. Your feedback is invaluable to us!")
                   .build())
                .build();
            }
            FirebaseMessaging.getInstance().send(message);
            logger.info("Push notification sent successfully to device token: {}", deviceToken);
            return ResponseEntity.ok("Push notification sent successfully!");
        } catch (Exception e) {
            logger.error("Failed to send push notification: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send push notification");
        }
    }
}
