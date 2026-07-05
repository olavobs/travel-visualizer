package com.flightmonitor.infrastructure.fare;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "serpapi")
public class SerpApiProperties {

    private String apiKey = "";

    public String getApiKey()              { return apiKey; }
    public void setApiKey(String apiKey)   { this.apiKey = apiKey; }
    public boolean isConfigured()          { return apiKey != null && !apiKey.isBlank(); }
}
