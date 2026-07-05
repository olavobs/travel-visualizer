package com.flightmonitor.infrastructure.fare;

import com.flightmonitor.domain.port.FareFetcherPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FareFetcherConfiguration {

    @Bean
    @ConditionalOnExpression("'${serpapi.api-key:}' != ''")
    public FareFetcherPort serpApiFareFetcher(SerpApiClient client) {
        return new SerpApiFareFetcherAdapter(client);
    }

    @Bean
    @ConditionalOnMissingBean(FareFetcherPort.class)
    public FareFetcherPort stubFareFetcher() {
        return new StubFareFetcherAdapter();
    }
}
