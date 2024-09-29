package com.grundfos.pump.replace.component;

//import com.google.auth.oauth2.GoogleCredentials;
//import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;

@Component
public class FirebaseInitializer {
    @PostConstruct
    public void initialize() throws IOException {
        //FileInputStream serviceAccount = new FileInputStream("./src/main/resources/grundfos-goapp-firebase-adminsdk-fm9tk-0ab02995e1.json");
       // FirebaseOptions options = FirebaseOptions.builder()
                //.setCredentials(GoogleCredentials.fromStream(serviceAccount))
                //.build();
        //if (FirebaseApp.getApps().isEmpty()) {
            //FirebaseApp.initializeApp(options);
        //}
    }
}
