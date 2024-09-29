package com.grundfos.pump.replace.services;

import com.microsoft.applicationinsights.TelemetryClient;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CustomTelemetryService {
    private final TelemetryClient telemetryClient;

    public CustomTelemetryService(TelemetryClient telemetryClient) {
        this.telemetryClient = telemetryClient;
    }

    public void trackCustomEvent(String eventName, Map<String, String> properties) {
        telemetryClient.trackEvent(eventName,properties, null);
    }
}
