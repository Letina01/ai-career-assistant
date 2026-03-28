package com.careerassistant.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AiProperties {
    private String provider;
    private OpenAi openai = new OpenAi();
    private Gemini gemini = new Gemini();

    @Getter
    @Setter
    public static class OpenAi {
        private String apiKey;
        private String model;
        private String url;
    }

    @Getter
    @Setter
    public static class Gemini {
        private String apiKey;
        private String model;
        private String url;
    }
}
