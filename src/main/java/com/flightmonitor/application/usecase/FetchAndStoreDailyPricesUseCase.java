package com.flightmonitor.application.usecase;

import com.flightmonitor.domain.model.Money;
import com.flightmonitor.domain.model.PriceRecord;
import com.flightmonitor.domain.model.Route;
import com.flightmonitor.domain.model.RouteStatus;
import com.flightmonitor.domain.model.Segment;
import com.flightmonitor.domain.model.TransportType;
import com.flightmonitor.domain.port.FareFetcherPort;
import com.flightmonitor.domain.port.PriceCachePort;
import com.flightmonitor.domain.repository.PriceRecordRepository;
import com.flightmonitor.domain.repository.RouteRepository;
import com.flightmonitor.domain.repository.SegmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class FetchAndStoreDailyPricesUseCase {

    private static final Logger log = LoggerFactory.getLogger(FetchAndStoreDailyPricesUseCase.class);

    private final RouteRepository routeRepository;
    private final SegmentRepository segmentRepository;
    private final PriceRecordRepository priceRecordRepository;
    private final FareFetcherPort fareFetcherPort;
    private final PriceCachePort priceCachePort;

    public FetchAndStoreDailyPricesUseCase(RouteRepository routeRepository,
                                           SegmentRepository segmentRepository,
                                           PriceRecordRepository priceRecordRepository,
                                           FareFetcherPort fareFetcherPort,
                                           PriceCachePort priceCachePort) {
        this.routeRepository       = routeRepository;
        this.segmentRepository     = segmentRepository;
        this.priceRecordRepository = priceRecordRepository;
        this.fareFetcherPort       = fareFetcherPort;
        this.priceCachePort        = priceCachePort;
    }

    public void execute() {
        List<Route> routes = routeRepository.findAllByStatus(RouteStatus.WATCHING);
        log.info("Daily price fetch: {} WATCHING route(s) found", routes.size());

        for (Route route : routes) {
            List<Segment> segments = segmentRepository.findByRouteId(route.getId());

            for (Segment segment : segments) {
                if (segment.getTransportType() != TransportType.FLIGHT) continue;

                try {
                    Optional<Money> result = fareFetcherPort.fetchLowestFare(route);
                    if (result.isEmpty()) {
                        log.warn("No fare returned for segment {} (route {} {}-{})",
                                segment.getId(), route.getId(), route.getOrigin(), route.getDestination());
                        continue;
                    }
                    PriceRecord record = new PriceRecord(segment.getId(), result.get(), Instant.now());
                    priceRecordRepository.save(record);
                    priceCachePort.evict(segment.getId());
                    log.info("Saved fare {} for segment {} ({}-{})",
                            result.get().formatted(), segment.getId(),
                            route.getOrigin(), route.getDestination());
                } catch (Exception e) {
                    log.error("Error fetching fare for segment {} on route {}: {}",
                            segment.getId(), route.getId(), e.getMessage(), e);
                }
            }
        }
    }
}
