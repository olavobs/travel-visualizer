package com.flightmonitor.interfaces.web.docs;

import com.flightmonitor.interfaces.web.dto.AddPriceHttpRequest;
import com.flightmonitor.interfaces.web.dto.PriceRecordHttpResponse;
import com.flightmonitor.interfaces.web.dto.UpdatePriceHttpRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Prices", description = "Track price observations for a segment")
public interface PriceRecordControllerDocs {

    @Operation(summary = "Add a price observation")
    @ApiResponse(responseCode = "201", description = "Price record created")
    @ApiResponse(responseCode = "404", description = "Route or segment not found")
    ResponseEntity<PriceRecordHttpResponse> addPrice(
            @Parameter(description = "Route ID") @PathVariable Long routeId,
            @Parameter(description = "Segment ID") @PathVariable Long segmentId,
            @Valid @RequestBody AddPriceHttpRequest request);

    @Operation(summary = "List price history for a segment")
    @ApiResponse(responseCode = "200", description = "Price history (may be empty)")
    @ApiResponse(responseCode = "404", description = "Route or segment not found")
    ResponseEntity<List<PriceRecordHttpResponse>> listPriceHistory(
            @Parameter(description = "Route ID") @PathVariable Long routeId,
            @Parameter(description = "Segment ID") @PathVariable Long segmentId);

    @Operation(summary = "Update a price observation")
    @ApiResponse(responseCode = "200", description = "Price record updated")
    @ApiResponse(responseCode = "404", description = "Route, segment, or price record not found")
    ResponseEntity<PriceRecordHttpResponse> updatePrice(
            @Parameter(description = "Route ID") @PathVariable Long routeId,
            @Parameter(description = "Segment ID") @PathVariable Long segmentId,
            @Parameter(description = "Price record ID") @PathVariable Long priceId,
            @Valid @RequestBody UpdatePriceHttpRequest request);

    @Operation(summary = "Delete a price observation")
    @ApiResponse(responseCode = "204", description = "Price record deleted")
    @ApiResponse(responseCode = "404", description = "Route, segment, or price record not found")
    ResponseEntity<Void> deletePriceRecord(
            @Parameter(description = "Route ID") @PathVariable Long routeId,
            @Parameter(description = "Segment ID") @PathVariable Long segmentId,
            @Parameter(description = "Price record ID") @PathVariable Long priceId);

    @Operation(summary = "Toggle purchased flag",
            description = "Marks as bought (sets route to BOOKED) if not purchased; unmarks if already purchased.")
    @ApiResponse(responseCode = "200", description = "Purchased flag toggled")
    @ApiResponse(responseCode = "404", description = "Route, segment, or price record not found")
    ResponseEntity<PriceRecordHttpResponse> togglePurchased(
            @Parameter(description = "Route ID") @PathVariable Long routeId,
            @Parameter(description = "Segment ID") @PathVariable Long segmentId,
            @Parameter(description = "Price record ID") @PathVariable Long priceId);
}
