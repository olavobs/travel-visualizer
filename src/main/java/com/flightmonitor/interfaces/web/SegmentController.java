package com.flightmonitor.interfaces.web;

import com.flightmonitor.application.dto.CreateSegmentRequest;
import com.flightmonitor.application.usecase.CreateSegmentUseCase;
import com.flightmonitor.application.usecase.DeleteSegmentUseCase;
import com.flightmonitor.application.usecase.ListSegmentsUseCase;
import com.flightmonitor.domain.model.Segment;
import com.flightmonitor.interfaces.web.docs.SegmentControllerDocs;
import com.flightmonitor.interfaces.web.dto.CreateSegmentHttpRequest;
import com.flightmonitor.interfaces.web.dto.SegmentHttpResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/routes/{routeId}/segments")
public class SegmentController extends BaseController implements SegmentControllerDocs {

    private final CreateSegmentUseCase createSegmentUseCase;
    private final ListSegmentsUseCase listSegmentsUseCase;
    private final DeleteSegmentUseCase deleteSegmentUseCase;

    public SegmentController(CreateSegmentUseCase createSegmentUseCase,
                             ListSegmentsUseCase listSegmentsUseCase,
                             DeleteSegmentUseCase deleteSegmentUseCase) {
        this.createSegmentUseCase = createSegmentUseCase;
        this.listSegmentsUseCase = listSegmentsUseCase;
        this.deleteSegmentUseCase = deleteSegmentUseCase;
    }

    @Override
    @PostMapping
    public ResponseEntity<SegmentHttpResponse> createSegment(@PathVariable Long routeId,
                                                             @Valid @RequestBody CreateSegmentHttpRequest request) {
        CreateSegmentRequest appRequest = new CreateSegmentRequest(currentUserId(), routeId,
                request.transportType(), request.label());
        Segment segment = createSegmentUseCase.execute(appRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(SegmentHttpResponse.from(segment));
    }

    @Override
    @GetMapping
    public ResponseEntity<List<SegmentHttpResponse>> listSegments(@PathVariable Long routeId) {
        return ResponseEntity.ok(listSegmentsUseCase.execute(routeId, currentUserId()).stream()
                .map(SegmentHttpResponse::from).toList());
    }

    @Override
    @DeleteMapping("/{segmentId}")
    public ResponseEntity<Void> deleteSegment(@PathVariable Long routeId,
                                              @PathVariable Long segmentId) {
        deleteSegmentUseCase.execute(routeId, segmentId, currentUserId());
        return ResponseEntity.noContent().build();
    }
}
