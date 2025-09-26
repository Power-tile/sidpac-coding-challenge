package edu.mit.sidpac.flightsearch.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class TestJpaAuditingConfig {
    // This enables JPA auditing for tests
}
