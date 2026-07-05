package com.flightmonitor.interfaces.web.docs;

import com.flightmonitor.interfaces.web.dto.CreateRouteHttpRequest;
import com.flightmonitor.interfaces.web.dto.RouteResponse;
import com.flightmonitor.interfaces.web.dto.RouteSummaryHttpResponse;
import com.flightmonitor.interfaces.web.dto.SetStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Routes", description = "Create and manage travel routes")
public interface RouteControllerDocs {

    @Operation(summary = "Create a travel route")
    @ApiResponse(responseCode = "201", description = "Route created")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    ResponseEntity<RouteResponse> createRoute(@Valid @RequestBody CreateRouteHttpRequest request);

    @Operation(summary = "List all routes")
    @ApiResponse(responseCode = "200", description = "List of routes (may be empty)")
    ResponseEntity<List<RouteResponse>> listRoutes();

    @Operation(summary = "Delete a route",
            description = "Deletes the route, all its segments, and all price records.")
    @ApiResponse(responseCode = "204", description = "Route deleted")
    @ApiResponse(responseCode = "404", description = "Route not found")
    ResponseEntity<Void> deleteRoute(@Parameter(description = "Route ID") @PathVariable Long id);

    @Operation(summary = "Update route status",
            description = "Valid values: WATCHING, BOOKED, CANCELLED.")
    @ApiResponse(responseCode = "200", description = "Route updated")
    @ApiResponse(responseCode = "404", description = "Route not found")
    ResponseEntity<RouteResponse> setStatus(
            @Parameter(description = "Route ID") @PathVariable Long id,
            @Valid @RequestBody SetStatusRequest request);

    @Operation(summary = "Get price summary per segment")
    @ApiResponse(responseCode = "200", description = "Per-segment price summary")
    @ApiResponse(responseCode = "404", description = "Route not found")
    ResponseEntity<RouteSummaryHttpResponse> getPriceSummary(
            @Parameter(description = "Route ID") @PathVariable Long id);
}
