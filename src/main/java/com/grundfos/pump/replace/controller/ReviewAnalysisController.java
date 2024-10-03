package com.grundfos.pump.replace.controller;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.core.credential.AzureKeyCredential;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
public class ReviewAnalysisController {

    private final TextAnalyticsClient client;

    public ReviewAnalysisController() {
        // Initialize Azure Text Analytics Client
        this.client = new TextAnalyticsClientBuilder()
                .credential(new AzureKeyCredential("904dfa3a6fcd41f0b8ced5973039961d"))
                .endpoint("https://gf-ais-go-review-d.cognitiveservices.azure.com/")
                .buildClient();
    }

    @PostMapping("/sentimentAnalysis")
    public String performSentimentAnalysis(@RequestBody Map<String, String> payload) {
        String reviewText = payload.get("reviewText");
        if (reviewText == null || reviewText.isEmpty()) {
            return "No review text provided";
        }

        // Perform sentiment analysis
        DocumentSentiment documentSentiment = client.analyzeSentiment(reviewText);
        return documentSentiment.getSentiment().toString(); // Returns Positive, Negative, or Neutral
    }
}
