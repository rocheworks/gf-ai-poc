package com.grundfos.pump.replace.services;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;

@Service
public class FirebaseService {
    @PostConstruct
    public void initialize() {
        try {
            System.out.println("In FirebaseService Initialize");
            FileInputStream serviceAccount = new FileInputStream("./src/main/resources/gf-goapp-push-notices-firebase-adminsdk-rn8qe-99e4a641f9.json");
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            System.out.println("Before initialize:: Firebase has been initialized successfully");
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase has been initialized successfully");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}