package com.flightmonitor.infrastructure.scheduler;

import com.flightmonitor.application.usecase.FetchAndStoreDailyPricesUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PriceFetchScheduler {

    private static final Logger log = LoggerFactory.getLogger(PriceFetchScheduler.class);

    private final FetchAndStoreDailyPricesUseCase useCase;

    public PriceFetchScheduler(FetchAndStoreDailyPricesUseCase useCase) {
        this.useCase = useCase;
    }

    @Scheduled(cron = "0 0 8 * * *", zone = "America/Sao_Paulo")
    public void fetchDailyPrices() {
        log.info("Starting daily price fetch job");
        useCase.execute();
        log.info("Daily price fetch job completed");
    }
}
