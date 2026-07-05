package com.flightmonitor.interfaces.web.docs;

import com.flightmonitor.interfaces.web.dto.CreateSegmentHttpRequest;
import com.flightmonitor.interfaces.web.dto.SegmentHttpResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Segments", description = "Manage transport legs within a route")
public interface SegmentControllerDocs {

    @Operation(summary = "Add a segment to a route",
            description = "A segment represents one transport leg (FLIGHT, BUS, CAR, BOAT, or OTHER).")
    @ApiResponse(responseCode = "201", description = "Segment created")
    @ApiResponse(responseCode = "404", description = "Route not found")
    ResponseEntity<SegmentHttpResponse> createSegment(
            @Parameter(description = "Route ID") @PathVariable Long routeId,
            @Valid @RequestBody CreateSegmentHttpRequest request);

    @Operation(summary = "List segments for a route")
    @ApiResponse(responseCode = "200", description = "List of segments (may be empty)")
    @ApiResponse(responseCode = "404", description = "Route not found")
    ResponseEntity<List<SegmentHttpResponse>> listSegments(
            @Parameter(description = "Route ID") @PathVariable Long routeId);

    @Operation(summary = "Delete a segment",
            description = "Deletes the segment and all its price records.")
    @ApiResponse(responseCode = "204", description = "Segment deleted")
    @ApiResponse(responseCode = "404", description = "Route or segment not found")
    ResponseEntity<Void> deleteSegment(
            @Parameter(description = "Route ID") @PathVariable Long routeId,
            @Parameter(description = "Segment ID") @PathVariable Long segmentId);
}
