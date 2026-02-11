package io.github.vinipx.wixy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import io.github.vinipx.wixy.config.WixyProperties;

@SpringBootApplication
@EnableConfigurationProperties(WixyProperties.class)
public class WixyApplication {

    public static void main(String[] args) {
        SpringApplication.run(WixyApplication.class, args);
    }
}
