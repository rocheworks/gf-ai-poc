package com.grundfos.pump.replace.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
public class VisionController {
    private final WebClient webClient;
    private static final String AZURE_VISION_ENDPOINT = "https://<YOUR_REGION>.api.cognitive.microsoft.com/vision/v3.2/analyze?visualFeatures=Description";
    private static final String AZURE_VISION_SUBSCRIPTION_KEY = "<YOUR_SUBSCRIPTION_KEY>";

    @Autowired
    public VisionController(WebClient webClient) {
        this.webClient = webClient;
    }

    @PostMapping("/analyze-figma-image")
    public Mono<ResponseEntity<String>> analyzeFigmaImage(@RequestPart("file") MultipartFile file) {
        return Mono.just(file)
                .flatMap(imageFile -> {
                    try {
                        return webClient.post()
                                .uri(AZURE_VISION_ENDPOINT)
                                .header("Ocp-Apim-Subscription-Key", AZURE_VISION_SUBSCRIPTION_KEY)
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .bodyValue(imageFile.getBytes())
                                .retrieve()
                                .bodyToMono(String.class);
                    } catch (Exception e) {
                        return Mono.error(new RuntimeException("Error processing image", e));
                    }
                })
                .map(responseBody -> ResponseEntity.ok(responseBody))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error processing image: " + e.getMessage())));
    }
}
