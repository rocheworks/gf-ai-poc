package com.grundfos.pump.replace;

import com.microsoft.applicationinsights.attach.ApplicationInsights;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ReplaceApplication {
	public static void main(String[] args) {
		ApplicationInsights.attach();
		SpringApplication.run(ReplaceApplication.class, args);
	}

}
