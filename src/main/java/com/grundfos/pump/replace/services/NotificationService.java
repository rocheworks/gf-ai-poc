package com.grundfos.pump.replace.services;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

public class NotificationService {
    public void sendNotification(String deviceToken) {
        Notification notification = Notification.builder()
                .setTitle("Pump Replacement")
                .setBody("You have recently replaced your pump")
                .build();

        Message message = Message.builder()
                .setToken(deviceToken)
                .setNotification(notification)
                .putData("message", "You have recently replaced your pump")
                .build();
        try{
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Successfully send message: "+response);
        }catch (Exception e){
            System.err.println("Error sending FCM message: "+e.getMessage());
        }
    }
}
