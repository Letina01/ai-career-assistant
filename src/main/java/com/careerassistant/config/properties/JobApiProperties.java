package com.careerassistant.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "jobs")
public class JobApiProperties {
    private String provider;
    private RapidApi rapidapi = new RapidApi();

    @Getter
    @Setter
    public static class RapidApi {
        private String key;
        private String host;
        private String url;
    }
}
