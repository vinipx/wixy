package io.github.vinipx.wixy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import io.github.vinipx.wixy.config.WixyProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(WixyProperties.class)
@EnableScheduling
public class WixyApplication {

    public static void main(String[] args) {
        SpringApplication.run(WixyApplication.class, args);
    }
}
