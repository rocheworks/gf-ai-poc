package com.grundfos.pump.replace.controller;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final Map<String, String> deviceTokens = new HashMap<>();

    @PostMapping("/register-token")
    public String registerToken(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        if (token != null) {
            deviceTokens.put("user", token); // In practice, use a real identifier for users
            return "Token registered successfully";
        } else {
            return "Token registration failed";
        }
    }

    @PostMapping("/handle-response")
    public String handleUserResponse(@RequestBody Map<String, String> payload) {
        if (payload != null) {
            payload.forEach((k, v)-> System.out.println("key: "+k+" ,v: "+v));
            return payload.get("response");
        }
        else
            return "response payload is empty";

    }

    @PostMapping("/send")
    public String sendNotification(@RequestParam String messageTitle, @RequestParam String messageBody) {
        String token = deviceTokens.get("user");
        if (token == null) {
            return "No device token found!";
        }

        // Send notification using Firebase Admin SDK
        try {
            FirebaseMessaging.getInstance().send(
                    Message.builder()
                            .setToken(token)
                            .setNotification(Notification.builder()
                                    .setTitle(messageTitle)
                                    .setBody(messageBody)
                                    .build())
                            .build()
            );
            return "Notification sent successfully!";
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
            return "Failed to send notification!";
        }
    }
}