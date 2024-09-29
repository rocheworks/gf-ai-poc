package com.grundfos.pump.replace.configuration;

import com.microsoft.applicationinsights.TelemetryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class TelemetryConfig {
    @Bean
    public TelemetryClient telemetryClient() {
        return new TelemetryClient();
    }
}
