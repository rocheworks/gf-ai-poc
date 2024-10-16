package com.grundfos.pump.replace.controller;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.SentenceSentiment;
import com.azure.core.credential.AzureKeyCredential;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
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
    public Map<String, Object> performSentimentAnalysis(@RequestBody Map<String, String> payload) {
        String reviewText = payload.get("reviewText");
        Map<String, Object> response = new HashMap<>();

        if (reviewText == null || reviewText.isEmpty()) {
            response.put("error", "No review text provided");
            return response;
        }

        // Perform sentiment analysis
        DocumentSentiment documentSentiment = client.analyzeSentiment(reviewText);

        // Prepare response data
        response.put("overallSentiment", documentSentiment.getSentiment().toString());
        response.put("confidenceScores", documentSentiment.getConfidenceScores());

        // Sentence-level analysis
        Map<String, Object> sentenceResults = new HashMap<>();
        for (SentenceSentiment sentenceSentiment : documentSentiment.getSentences()) {
            Map<String, Object> sentenceData = new HashMap<>();
            sentenceData.put("sentiment", sentenceSentiment.getSentiment().toString());
            sentenceData.put("confidenceScores", sentenceSentiment.getConfidenceScores());
            sentenceResults.put(sentenceSentiment.getText(), sentenceData);
        }
        response.put("sentenceSentiments", sentenceResults);
response.forEach((k,v)-> System.out.println("sentence sentiments: Key: "+k+", value: "+v.toString()));
        return response; // Return response as JSON
    }
}