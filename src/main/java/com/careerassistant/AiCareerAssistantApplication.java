package com.careerassistant;

import com.careerassistant.config.properties.EmailProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(EmailProperties.class)
public class AiCareerAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiCareerAssistantApplication.class, args);
    }
}
