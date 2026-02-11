plugins {
    java
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
    jacoco
}

group = "io.github.vinipx.wixy"
version = "0.1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // WireMock
    implementation("org.wiremock:wiremock-standalone:3.13.0")

    // Spring AI MCP
    implementation("org.springframework.ai:spring-ai-starter-mcp-server-webmvc:1.0.0-M8")

    // OpenAPI / Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.8")

    // Config metadata
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.rest-assured:rest-assured:5.5.1")
    testImplementation("org.awaitility:awaitility:4.3.0")
}

// ── Unit tests (tagged @Tag("unit")) ────────────────────────────
tasks.test {
    useJUnitPlatform {
        includeTags("unit")
    }
    finalizedBy(tasks.jacocoTestReport)
}

// ── Integration tests (tagged @Tag("integration")) ─────────────
tasks.register<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    shouldRunAfter(tasks.test)

    useJUnitPlatform {
        includeTags("integration")
    }

    // Allow targeting a remote host
    systemProperty("wixy.test.base-url", System.getProperty("wixy.test.base-url", ""))
    systemProperty("wixy.test.api-key", System.getProperty("wixy.test.api-key", ""))
}

tasks.named("check") {
    dependsOn("integrationTest")
}

// ── JaCoCo coverage (unit tests only) ───────────────────────────
tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required = true
        html.required = true
    }
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.test)
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

tasks.named("check") {
    dependsOn(tasks.jacocoTestCoverageVerification)
}
