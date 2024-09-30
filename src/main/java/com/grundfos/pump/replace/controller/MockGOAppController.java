package com.grundfos.pump.replace.controller;

import com.grundfos.pump.replace.services.ConvertUTCService;
import com.grundfos.pump.replace.services.NotificationService;
import com.microsoft.applicationinsights.TelemetryClient;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import com.grundfos.pump.replace.model.MetadataOutput;
import com.grundfos.pump.replace.model.MetadataInput;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class MockGOAppController {

    private final Logger logger = LoggerFactory.getLogger(MockGOAppController.class);
    private final ResourceLoader resourceLoader;

    // API Key for Azure Search
    private static final String AZURE_API_KEY = "kacTC4UfWmeKI9vzKFSCHSx1MqcHUG5VG6ZbwkdQmxAzSeCj8hTS";
    private static final String AZURE_SEARCH_URL = "https://gf-ais-pumpsearch-d.search.windows.net/indexes/pump-replacement-index/docs?api-version=2021-04-30-Preview&search=";

    @Autowired
    public MockGOAppController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    private TelemetryClient telemetryClient;
    private String userName;

    @Autowired
    public void setTelemetryClient (TelemetryClient telemetryClient) {
        this.telemetryClient = telemetryClient;
    }

    @Autowired
    @Qualifier("convertUTCServiceImpl")
    private ConvertUTCService convertUTCService;

    @PostMapping("/login")
    public Map<String, String> loginUser(@RequestBody Map<String, String> loginData, HttpServletRequest request) {
        NotificationService notificationService = new NotificationService();
        userName = loginData.get("userName");
        logger.info("username in login: "+userName);
        String password = loginData.get("password");
        if (userName != null && password != null) {
            request.getSession().setAttribute("userName", userName);
            Map<String, String> response = new HashMap<>();
            response.put("userName", userName);
            telemetryClient.trackEvent("mockGOvisits1", response, null);
            telemetryClient.flush();
            return response;
        } else {
            throw new RuntimeException("Invalid login details");
        }
    }

    @GetMapping("/mockGO")
    public ResponseEntity<Resource> mockGoApp() {
        logger.info("the url is /mockGO");
        try {
            Resource resource = resourceLoader.getResource("classpath:/static/mock-go-login.html");
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "text/html").body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/trackTestEvent")
    public ResponseEntity<String> trackTestEvent() {
        telemetryClient.trackEvent("TestEventFromAzureWebApp-revised");
        telemetryClient.flush();
        return ResponseEntity.ok("Test Event Tracked Successfully");
    }

    @PostMapping(path = "metadata", consumes = "application/json", produces = "application/json")
    public ResponseEntity<MetadataOutput> logMetadata(@RequestBody MetadataInput metadatInput) {
        logger.info("post the metatdata object");
        logger.info("user-name is {}", metadatInput.getUserName());
        trackReplace();
        String utcInstallDate = convertUTCService.convertTimeZone(metadatInput.getInstallDate(), metadatInput.getTimezone());
        metadatInput.setInstallDate(utcInstallDate);
        MetadataOutput metadataOutput = new MetadataOutput(metadatInput.toString());
        return new ResponseEntity<>(metadataOutput, HttpStatus.OK);
    }

    @PostMapping(path = "utc", consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> getUTC(@RequestBody MetadataInput metadatInput) {
        logger.info("convert install date to UTC");
        trackReplace();
        String utcInstallDate = convertUTCService.convertTimeZone(metadatInput.getInstallDate(), metadatInput.getTimezone());
        return new ResponseEntity<>(utcInstallDate, HttpStatus.OK);
    }

    public void trackReplace() {
        telemetryClient.trackEvent("mockGOvisits");
    }

    // ================= Search Proxy for Azure Cognitive Search ================= //
    @GetMapping("/search")
    @CrossOrigin(origins = "*")
    public ResponseEntity<String> searchPump(@RequestParam("q") String query) {
        // Construct the search URL with the query term
        String searchUrl = AZURE_SEARCH_URL + query + "&$select=pump_name,feature_description,replacement_pump_name&$count=true";
        System.out.println("searchURL:: "+searchUrl);
        // Create headers and include the Azure Search API key
        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", AZURE_API_KEY);

        // Create an HttpEntity object with the headers
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Use RestTemplate to forward the request to Azure Cognitive Search
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<String> response = restTemplate.exchange(searchUrl, HttpMethod.GET, entity, String.class);

            // Log the response to verify it
            System.out.println("Azure Search Response: " + response.getBody());

            // Return the response received from Azure Cognitive Search
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            // Log the error and return a 500 Internal Server Error response
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching search results");
        }
    }
}