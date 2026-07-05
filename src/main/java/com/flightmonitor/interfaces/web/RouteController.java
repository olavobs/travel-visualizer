package com.flightmonitor.interfaces.web;

import com.flightmonitor.application.dto.*;
import com.flightmonitor.application.usecase.*;
import com.flightmonitor.domain.model.Currency;
import com.flightmonitor.domain.model.Money;
import com.flightmonitor.domain.model.RouteStatus;
import com.flightmonitor.domain.model.TransportType;
import com.flightmonitor.infrastructure.security.UserAuthentication;
import com.flightmonitor.interfaces.web.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
@Tag(name = "Travel Routes", description = "Manage travel routes, segments (transport legs), and price observations")
public class RouteController {

    private final CreateRouteUseCase createRouteUseCase;
    private final ListRoutesUseCase listRoutesUseCase;
    private final DeleteRouteUseCase deleteRouteUseCase;
    private final ToggleRouteBookedUseCase toggleRouteBookedUseCase;
    private final CreateSegmentUseCase createSegmentUseCase;
    private final ListSegmentsUseCase listSegmentsUseCase;
    private final DeleteSegmentUseCase deleteSegmentUseCase;
    private final AddPriceRecordUseCase addPriceRecordUseCase;
    private final ListPriceHistoryUseCase listPriceHistoryUseCase;
    private final UpdatePriceRecordUseCase updatePriceRecordUseCase;
    private final DeletePriceRecordUseCase deletePriceRecordUseCase;
    private final GetRoutePriceSummaryUseCase getRoutePriceSummaryUseCase;
    private final MarkPriceAsPurchasedUseCase markPriceAsPurchasedUseCase;

    public RouteController(CreateRouteUseCase createRouteUseCase,
                           ListRoutesUseCase listRoutesUseCase,
                           DeleteRouteUseCase deleteRouteUseCase,
                           ToggleRouteBookedUseCase toggleRouteBookedUseCase,
                           CreateSegmentUseCase createSegmentUseCase,
                           ListSegmentsUseCase listSegmentsUseCase,
                           DeleteSegmentUseCase deleteSegmentUseCase,
                           AddPriceRecordUseCase addPriceRecordUseCase,
                           ListPriceHistoryUseCase listPriceHistoryUseCase,
                           UpdatePriceRecordUseCase updatePriceRecordUseCase,
                           DeletePriceRecordUseCase deletePriceRecordUseCase,
                           GetRoutePriceSummaryUseCase getRoutePriceSummaryUseCase,
                           MarkPriceAsPurchasedUseCase markPriceAsPurchasedUseCase) {
        this.createRouteUseCase = createRouteUseCase;
        this.listRoutesUseCase = listRoutesUseCase;
        this.deleteRouteUseCase = deleteRouteUseCase;
        this.toggleRouteBookedUseCase = toggleRouteBookedUseCase;
        this.createSegmentUseCase = createSegmentUseCase;
        this.listSegmentsUseCase = listSegmentsUseCase;
        this.deleteSegmentUseCase = deleteSegmentUseCase;
        this.addPriceRecordUseCase = addPriceRecordUseCase;
        this.listPriceHistoryUseCase = listPriceHistoryUseCase;
        this.updatePriceRecordUseCase = updatePriceRecordUseCase;
        this.deletePriceRecordUseCase = deletePriceRecordUseCase;
        this.getRoutePriceSummaryUseCase = getRoutePriceSummaryUseCase;
        this.markPriceAsPurchasedUseCase = markPriceAsPurchasedUseCase;
    }

    private Long currentUserId() {
        return ((UserAuthentication) SecurityContextHolder.getContext().getAuthentication()).getUserId();
    }

    // ── Routes ────────────────────────────────────────────────────────────────

    @Operation(summary = "Create a travel route",
               description = "Creates a new route identified by origin/destination IATA codes and a travel date.")
    @ApiResponse(responseCode = "201", description = "Route created")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @PostMapping
    public ResponseEntity<RouteResponse> createRoute(@Valid @RequestBody CreateRouteHttpRequest request) {
        var appRequest = new CreateRouteRequest(currentUserId(), request.origin(), request.destination(), request.travelDate());
        var route = createRouteUseCase.execute(appRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(RouteResponse.from(route));
    }

    @Operation(summary = "List all routes")
    @ApiResponse(responseCode = "200", description = "List of routes (may be empty)")
    @GetMapping
    public ResponseEntity<List<RouteResponse>> listRoutes() {
        return ResponseEntity.ok(listRoutesUseCase.execute(currentUserId()).stream()
                .map(RouteResponse::from).toList());
    }

    @Operation(summary = "Delete a route",
               description = "Deletes the route, all its segments, and all price records. Evicts all segment cache entries.")
    @ApiResponse(responseCode = "204", description = "Route deleted")
    @ApiResponse(responseCode = "404", description = "Route not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoute(@Parameter(description = "Route ID") @PathVariable Long id) {
        deleteRouteUseCase.execute(id, currentUserId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update route status",
               description = "Sets the status of a route. Valid values: WATCHING, BOOKED, CANCELLED.")
    @ApiResponse(responseCode = "200", description = "Route updated")
    @ApiResponse(responseCode = "400", description = "Invalid status value")
    @ApiResponse(responseCode = "404", description = "Route not found")
    @PatchMapping("/{id}/status")
    public ResponseEntity<RouteResponse> setStatus(
            @Parameter(description = "Route ID") @PathVariable Long id,
            @Valid @RequestBody SetStatusRequest request) {
        var route = toggleRouteBookedUseCase.execute(id, currentUserId(), RouteStatus.valueOf(request.status()));
        return ResponseEntity.ok(RouteResponse.from(route));
    }

    @Operation(summary = "Get price summary",
               description = "Returns latest and lowest prices per segment for a route.")
    @ApiResponse(responseCode = "200", description = "Per-segment price summary")
    @ApiResponse(responseCode = "404", description = "Route not found")
    @GetMapping("/{id}/prices/summary")
    public ResponseEntity<RouteSummaryHttpResponse> getPriceSummary(
            @Parameter(description = "Route ID") @PathVariable Long id) {
        var result = getRoutePriceSummaryUseCase.execute(id, currentUserId());
        return ResponseEntity.ok(RouteSummaryHttpResponse.from(result));
    }

    // ── Segments ──────────────────────────────────────────────────────────────

    @Operation(summary = "Add a segment to a route",
               description = "A segment represents one transport leg (FLIGHT, BUS, CAR, BOAT, or OTHER with a custom label).")
    @ApiResponse(responseCode = "201", description = "Segment created")
    @ApiResponse(responseCode = "400", description = "Invalid transport type")
    @ApiResponse(responseCode = "404", description = "Route not found")
    @PostMapping("/{id}/segments")
    public ResponseEntity<SegmentHttpResponse> createSegment(
            @Parameter(description = "Route ID") @PathVariable Long id,
            @Valid @RequestBody CreateSegmentHttpRequest request) {
        var appRequest = new CreateSegmentRequest(currentUserId(), id,
                TransportType.valueOf(request.transportType()), request.label());
        var segment = createSegmentUseCase.execute(appRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(SegmentHttpResponse.from(segment));
    }

    @Operation(summary = "List segments for a route")
    @ApiResponse(responseCode = "200", description = "List of segments (may be empty)")
    @ApiResponse(responseCode = "404", description = "Route not found")
    @GetMapping("/{id}/segments")
    public ResponseEntity<List<SegmentHttpResponse>> listSegments(
            @Parameter(description = "Route ID") @PathVariable Long id) {
        var segments = listSegmentsUseCase.execute(id, currentUserId());
        return ResponseEntity.ok(segments.stream().map(SegmentHttpResponse::from).toList());
    }

    @Operation(summary = "Delete a segment",
               description = "Deletes the segment and all its price records. Evicts the segment cache entry.")
    @ApiResponse(responseCode = "204", description = "Segment deleted")
    @ApiResponse(responseCode = "404", description = "Route or segment not found")
    @DeleteMapping("/{id}/segments/{segId}")
    public ResponseEntity<Void> deleteSegment(
            @Parameter(description = "Route ID") @PathVariable Long id,
            @Parameter(description = "Segment ID") @PathVariable Long segId) {
        deleteSegmentUseCase.execute(id, segId, currentUserId());
        return ResponseEntity.noContent().build();
    }

    // ── Prices ────────────────────────────────────────────────────────────────

    @Operation(summary = "Add a price observation to a segment")
    @ApiResponse(responseCode = "201", description = "Price record created")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "404", description = "Route or segment not found")
    @PostMapping("/{id}/segments/{segId}/prices")
    public ResponseEntity<PriceRecordHttpResponse> addPrice(
            @Parameter(description = "Route ID") @PathVariable Long id,
            @Parameter(description = "Segment ID") @PathVariable Long segId,
            @Valid @RequestBody AddPriceHttpRequest request) {
        var money = new Money(request.price(), Currency.valueOf(request.currency()));
        var appRequest = new AddPriceRequest(currentUserId(), id, segId, money, request.recordedDate());
        var record = addPriceRecordUseCase.execute(appRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(PriceRecordHttpResponse.from(record));
    }

    @Operation(summary = "List price history for a segment")
    @ApiResponse(responseCode = "200", description = "Price history (may be empty)")
    @ApiResponse(responseCode = "404", description = "Route or segment not found")
    @GetMapping("/{id}/segments/{segId}/prices")
    public ResponseEntity<List<PriceRecordHttpResponse>> listPriceHistory(
            @Parameter(description = "Route ID") @PathVariable Long id,
            @Parameter(description = "Segment ID") @PathVariable Long segId) {
        return ResponseEntity.ok(listPriceHistoryUseCase.execute(id, segId, currentUserId()).stream()
                .map(PriceRecordHttpResponse::from).toList());
    }

    @Operation(summary = "Update a price observation")
    @ApiResponse(responseCode = "200", description = "Price record updated")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "404", description = "Route, segment, or price record not found")
    @PutMapping("/{id}/segments/{segId}/prices/{priceId}")
    public ResponseEntity<PriceRecordHttpResponse> updatePrice(
            @Parameter(description = "Route ID") @PathVariable Long id,
            @Parameter(description = "Segment ID") @PathVariable Long segId,
            @Parameter(description = "Price record ID") @PathVariable Long priceId,
            @Valid @RequestBody UpdatePriceHttpRequest request) {
        var money = new Money(request.price(), Currency.valueOf(request.currency()));
        var appRequest = new UpdatePriceRequest(currentUserId(), id, segId, priceId, money, request.recordedDate());
        var record = updatePriceRecordUseCase.execute(appRequest);
        return ResponseEntity.ok(PriceRecordHttpResponse.from(record));
    }

    @Operation(summary = "Delete a price observation")
    @ApiResponse(responseCode = "204", description = "Price record deleted")
    @ApiResponse(responseCode = "404", description = "Route, segment, or price record not found")
    @DeleteMapping("/{id}/segments/{segId}/prices/{priceId}")
    public ResponseEntity<Void> deletePriceRecord(
            @Parameter(description = "Route ID") @PathVariable Long id,
            @Parameter(description = "Segment ID") @PathVariable Long segId,
            @Parameter(description = "Price record ID") @PathVariable Long priceId) {
        deletePriceRecordUseCase.execute(id, segId, priceId, currentUserId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Toggle purchased flag on a price record",
               description = "If not purchased, marks it as bought (clears any other purchased flag on the segment and sets route to BOOKED). If already purchased, unmarks it.")
    @ApiResponse(responseCode = "200", description = "Price record marked as purchased")
    @ApiResponse(responseCode = "404", description = "Route, segment, or price record not found")
    @PatchMapping("/{id}/segments/{segId}/prices/{priceId}/purchase")
    public ResponseEntity<PriceRecordHttpResponse> markAsPurchased(
            @Parameter(description = "Route ID") @PathVariable Long id,
            @Parameter(description = "Segment ID") @PathVariable Long segId,
            @Parameter(description = "Price record ID") @PathVariable Long priceId) {
        var record = markPriceAsPurchasedUseCase.execute(id, segId, priceId, currentUserId());
        return ResponseEntity.ok(PriceRecordHttpResponse.from(record));
    }

}
