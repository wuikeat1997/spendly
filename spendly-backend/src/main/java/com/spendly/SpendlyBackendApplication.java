package com.spendly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class SpendlyBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpendlyBackendApplication.class, args);
	}

}
