package com.grundfos.pump.replace.controller;

import com.microsoft.applicationinsights.TelemetryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/telemetry")
public class TelemetryController {

    private final TelemetryClient telemetryClient;

    public TelemetryController(TelemetryClient telemetryClient) {
        this.telemetryClient = telemetryClient;
    }

    @PostMapping("/track-event")
    public ResponseEntity<String> trackEvent(@RequestBody Map<String, Object> payload) {
        String eventName = (String) payload.get("eventName");
        Map<String, String> properties = (Map<String, String>) payload.get("properties");
        telemetryClient.trackEvent(eventName, properties, null);
        return ResponseEntity.ok("Telemetry event tracked successfully");
    }
}