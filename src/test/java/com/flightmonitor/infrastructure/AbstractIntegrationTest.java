package com.flightmonitor.infrastructure;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for integration tests.
 *
 * Tests in this category require the Docker Compose stack to be running:
 *
 *   docker compose up -d
 *
 * The "test" profile (application-test.yml) points to the services exposed
 * on localhost by Docker Compose (MySQL on 3306, Redis on 6379).
 * Database tests use @Transactional so every test rolls back automatically.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {}
