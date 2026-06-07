package com.wonjun.journeylog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("com.wonjun.journeylog.config")
public class JourneyLogBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(JourneyLogBackendApplication.class, args);
	}

}
