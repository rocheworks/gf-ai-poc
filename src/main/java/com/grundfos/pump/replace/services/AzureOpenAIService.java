package com.grundfos.pump.replace.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AzureOpenAIService {
    private final WebClient webClient;
    private final ObjectMapper objectMapper;  // Jackson ObjectMapper to handle JSON parsing
    private static final Logger logger = LoggerFactory.getLogger(AzureOpenAIService.class);

    public AzureOpenAIService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://gf-ais-openai-d.openai.azure.com/openai/deployments/gpt-4o/chat/completions?api-version=2024-08-01-preview")  // Replace with your correct deployment ID
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("api-key", "ab003a30cec64c739f1c9b6922e636c9")  // Replace with your API key
                .build();
        this.objectMapper = new ObjectMapper();  // Initialize Jackson ObjectMapper
    }

    // This method now returns a List<Map<String, String>> instead of a String
    public List<Map<String, String>> generateRequirements(String stakeholderRequirement) throws Exception {
        Map<String, Object> requestBody = Map.of(
                "messages", List.of(
                        Map.of("role", "system", "content", "You are an assistant that helps generate system requirements."),
                        Map.of("role", "user", "content", "Generate system and system-element requirements for the following stakeholder requirement: " + stakeholderRequirement)
                ),
                "max_tokens", 1000,
                "temperature", 0.7
        );

        // Make the API request and retrieve the response as a String
        String responseBody = this.webClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // Log the response from Azure OpenAI for debugging purposes
        logger.info("OpenAI Response: {}", responseBody);

        // Parse the response to find the "choices" array and extract the generated content
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode choices = root.get("choices");

        if (choices != null && choices.isArray()) {
            String generatedText = choices.get(0).get("message").get("content").asText();
            logger.info("Generated Text: {}", generatedText);  // Log the generated text for debugging

            // Parse the generated text into structured requirements and test cases
            List<Map<String, String>> requirements = parseGeneratedText(generatedText);
            return requirements;
        } else {
            throw new Exception("Unexpected response format from OpenAI.");
        }
    }

    // Helper method to parse the generated text into structured requirements and test cases
    private List<Map<String, String>> parseGeneratedText(String generatedText) {
        List<Map<String, String>> requirements = new ArrayList<>();
        // Split the generated text by line breaks or periods (consider sentences as separate requirements)
        String[] lines = generatedText.split("\\r?\\n|\\. ");
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;  // Skip empty lines

            Map<String, String> reqMap = new HashMap<>();

            // Assign the requirement type based on the content (you can customize this logic)
            if (line.toLowerCase().contains("timer") || line.toLowerCase().contains("operation")) {
                reqMap.put("type", "Functional");
            } else if (line.toLowerCase().contains("backup") || line.toLowerCase().contains("power")) {
                reqMap.put("type", "Non-functional");
            } else {
                reqMap.put("type", "UI");
            }

            // For now, the title can be the first few words of the requirement (this can be improved)
            String title = extractTitleFromDescription(line);
            reqMap.put("description", line);
            reqMap.put("testCase", "Derived from generated text");  // Placeholder

            // Add this requirement to the list
            requirements.add(reqMap);
        }

        return requirements;
    }

    // Helper method to extract title from the first part of the description
    private String extractTitleFromDescription(String description) {
        // Split description by newline or punctuation and return the first sentence as the title
        String[] parts = description.split("\\r?\\n|\\.");
        return parts.length > 0 ? parts[0].trim() : description;
    }
}

