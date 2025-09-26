package edu.mit.sidpac.flightsearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
// @EnableJpaAuditing // Moved to JPAAuditingConfig.java
public class FlightSearchEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlightSearchEngineApplication.class, args);
    }

}
