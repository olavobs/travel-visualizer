package com.flightmonitor.interfaces.web;

import com.flightmonitor.application.dto.CreateRouteRequest;
import com.flightmonitor.application.usecase.*;
import com.flightmonitor.domain.model.Route;
import com.flightmonitor.interfaces.web.docs.RouteControllerDocs;
import com.flightmonitor.interfaces.web.dto.CreateRouteHttpRequest;
import com.flightmonitor.interfaces.web.dto.RouteResponse;
import com.flightmonitor.interfaces.web.dto.RouteSummaryHttpResponse;
import com.flightmonitor.interfaces.web.dto.SetStatusRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/routes")
public class RouteController extends BaseController implements RouteControllerDocs {

    private final CreateRouteUseCase createRouteUseCase;
    private final ListRoutesUseCase listRoutesUseCase;
    private final DeleteRouteUseCase deleteRouteUseCase;
    private final ToggleRouteBookedUseCase toggleRouteBookedUseCase;
    private final GetRoutePriceSummaryUseCase getRoutePriceSummaryUseCase;

    public RouteController(CreateRouteUseCase createRouteUseCase,
                           ListRoutesUseCase listRoutesUseCase,
                           DeleteRouteUseCase deleteRouteUseCase,
                           ToggleRouteBookedUseCase toggleRouteBookedUseCase,
                           GetRoutePriceSummaryUseCase getRoutePriceSummaryUseCase) {
        this.createRouteUseCase = createRouteUseCase;
        this.listRoutesUseCase = listRoutesUseCase;
        this.deleteRouteUseCase = deleteRouteUseCase;
        this.toggleRouteBookedUseCase = toggleRouteBookedUseCase;
        this.getRoutePriceSummaryUseCase = getRoutePriceSummaryUseCase;
    }

    @Override
    @PostMapping
    public ResponseEntity<RouteResponse> createRoute(@Valid @RequestBody CreateRouteHttpRequest request) {
        CreateRouteRequest appRequest = new CreateRouteRequest(currentUserId(), request.origin(), request.destination(), request.travelDate());
        Route route = createRouteUseCase.execute(appRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(RouteResponse.from(route));
    }

    @Override
    @GetMapping
    public ResponseEntity<List<RouteResponse>> listRoutes() {
        return ResponseEntity.ok(listRoutesUseCase.execute(currentUserId()).stream()
                .map(RouteResponse::from).toList());
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoute(@PathVariable Long id) {
        deleteRouteUseCase.execute(id, currentUserId());
        return ResponseEntity.noContent().build();
    }

    @Override
    @PatchMapping("/{id}/status")
    public ResponseEntity<RouteResponse> setStatus(@PathVariable Long id,
                                                   @Valid @RequestBody SetStatusRequest request) {
        Route route = toggleRouteBookedUseCase.execute(id, currentUserId(), request.status());
        return ResponseEntity.ok(RouteResponse.from(route));
    }

    @Override
    @GetMapping("/{id}/prices/summary")
    public ResponseEntity<RouteSummaryHttpResponse> getPriceSummary(@PathVariable Long id) {
        RouteSummaryHttpResponse result = RouteSummaryHttpResponse.from(getRoutePriceSummaryUseCase.execute(id, currentUserId()));
        return ResponseEntity.ok(result);
    }
}
