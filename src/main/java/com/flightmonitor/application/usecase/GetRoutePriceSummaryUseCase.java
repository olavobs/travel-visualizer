package com.flightmonitor.application.usecase;

import com.flightmonitor.application.dto.RouteSummaryResponse;
import com.flightmonitor.application.dto.RouteSummaryResponse.SegmentSummary;
import com.flightmonitor.domain.exception.RouteNotFoundException;
import com.flightmonitor.domain.model.Money;
import com.flightmonitor.domain.model.PriceRecord;
import com.flightmonitor.domain.model.Route;
import com.flightmonitor.domain.model.Segment;
import com.flightmonitor.domain.port.PriceCachePort;
import com.flightmonitor.domain.repository.PriceRecordRepository;
import com.flightmonitor.domain.repository.RouteRepository;
import com.flightmonitor.domain.repository.SegmentRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class GetRoutePriceSummaryUseCase {

    private final RouteRepository routeRepository;
    private final SegmentRepository segmentRepository;
    private final PriceRecordRepository priceRecordRepository;
    private final PriceCachePort priceCachePort;

    public GetRoutePriceSummaryUseCase(RouteRepository routeRepository,
                                       SegmentRepository segmentRepository,
                                       PriceRecordRepository priceRecordRepository,
                                       PriceCachePort priceCachePort) {
        this.routeRepository       = routeRepository;
        this.segmentRepository     = segmentRepository;
        this.priceRecordRepository = priceRecordRepository;
        this.priceCachePort        = priceCachePort;
    }

    public RouteSummaryResponse execute(Long routeId, Long userId) {
        Route route = routeRepository.findById(routeId)
                .filter(r -> r.getUserId().equals(userId))
                .orElseThrow(() -> new RouteNotFoundException(routeId));

        List<Segment> segments = segmentRepository.findByRouteId(routeId);
        List<SegmentSummary> summaries = new ArrayList<>();

        for (Segment segment : segments) {
            Long segId = segment.getId();

            Money latestMoney;
            Optional<Money> cached = priceCachePort.getLatestPrice(segId);
            if (cached.isPresent()) {
                latestMoney = cached.get();
            } else {
                Optional<PriceRecord> latestRecord = priceRecordRepository.findLatestBySegmentId(segId);
                if (latestRecord.isPresent()) {
                    latestMoney = latestRecord.get().getPrice();
                    priceCachePort.storeLatestPrice(segId, latestMoney);
                } else {
                    latestMoney = null;
                }
            }

            Money lowestMoney = priceRecordRepository.findLowestBySegmentId(segId)
                    .map(PriceRecord::getPrice)
                    .orElse(null);

            Money purchasedMoney = priceRecordRepository.findPurchasedBySegmentId(segId)
                    .map(PriceRecord::getPrice)
                    .orElse(null);

            summaries.add(new SegmentSummary(segId, segment.getTransportType(), segment.getLabel(), latestMoney, lowestMoney, purchasedMoney));
        }

        return new RouteSummaryResponse(
                route.getId(),
                route.getOrigin(),
                route.getDestination(),
                route.getTravelDate(),
                summaries
        );
    }
}
