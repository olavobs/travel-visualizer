package com.flightmonitor.interfaces.web;

import com.flightmonitor.application.dto.AddPriceRequest;
import com.flightmonitor.application.dto.UpdatePriceRequest;
import com.flightmonitor.application.usecase.*;
import com.flightmonitor.domain.model.PriceRecord;
import com.flightmonitor.interfaces.web.docs.PriceRecordControllerDocs;
import com.flightmonitor.interfaces.web.dto.AddPriceHttpRequest;
import com.flightmonitor.interfaces.web.dto.PriceRecordHttpResponse;
import com.flightmonitor.interfaces.web.dto.UpdatePriceHttpRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/routes/{routeId}/segments/{segmentId}/prices")
public class PriceRecordController extends BaseController implements PriceRecordControllerDocs {

    private final AddPriceRecordUseCase addPriceRecordUseCase;
    private final ListPriceHistoryUseCase listPriceHistoryUseCase;
    private final UpdatePriceRecordUseCase updatePriceRecordUseCase;
    private final DeletePriceRecordUseCase deletePriceRecordUseCase;
    private final MarkPriceAsPurchasedUseCase markPriceAsPurchasedUseCase;

    public PriceRecordController(AddPriceRecordUseCase addPriceRecordUseCase,
                                 ListPriceHistoryUseCase listPriceHistoryUseCase,
                                 UpdatePriceRecordUseCase updatePriceRecordUseCase,
                                 DeletePriceRecordUseCase deletePriceRecordUseCase,
                                 MarkPriceAsPurchasedUseCase markPriceAsPurchasedUseCase) {
        this.addPriceRecordUseCase = addPriceRecordUseCase;
        this.listPriceHistoryUseCase = listPriceHistoryUseCase;
        this.updatePriceRecordUseCase = updatePriceRecordUseCase;
        this.deletePriceRecordUseCase = deletePriceRecordUseCase;
        this.markPriceAsPurchasedUseCase = markPriceAsPurchasedUseCase;
    }

    @Override
    @PostMapping
    public ResponseEntity<PriceRecordHttpResponse> addPrice(@PathVariable Long routeId,
                                                            @PathVariable Long segmentId,
                                                            @Valid @RequestBody AddPriceHttpRequest request) {
        AddPriceRequest appRequest = new AddPriceRequest(currentUserId(), routeId, segmentId, request.money(), request.recordedAt());
        PriceRecord record = addPriceRecordUseCase.execute(appRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(PriceRecordHttpResponse.from(record));
    }

    @Override
    @GetMapping
    public ResponseEntity<List<PriceRecordHttpResponse>> listPriceHistory(@PathVariable Long routeId,
                                                                          @PathVariable Long segmentId) {
        return ResponseEntity.ok(listPriceHistoryUseCase.execute(routeId, segmentId, currentUserId()).stream()
                .map(PriceRecordHttpResponse::from).toList());
    }

    @Override
    @PutMapping("/{priceId}")
    public ResponseEntity<PriceRecordHttpResponse> updatePrice(@PathVariable Long routeId,
                                                               @PathVariable Long segmentId,
                                                               @PathVariable Long priceId,
                                                               @Valid @RequestBody UpdatePriceHttpRequest request) {
        UpdatePriceRequest appRequest = new UpdatePriceRequest(currentUserId(), routeId, segmentId, priceId, request.money(), request.recordedAt());
        PriceRecord record = updatePriceRecordUseCase.execute(appRequest);
        return ResponseEntity.ok(PriceRecordHttpResponse.from(record));
    }

    @Override
    @DeleteMapping("/{priceId}")
    public ResponseEntity<Void> deletePriceRecord(@PathVariable Long routeId,
                                                  @PathVariable Long segmentId,
                                                  @PathVariable Long priceId) {
        deletePriceRecordUseCase.execute(routeId, segmentId, priceId, currentUserId());
        return ResponseEntity.noContent().build();
    }

    @Override
    @PatchMapping("/{priceId}/purchase")
    public ResponseEntity<PriceRecordHttpResponse> togglePurchased(@PathVariable Long routeId,
                                                                   @PathVariable Long segmentId,
                                                                   @PathVariable Long priceId) {
        PriceRecord record = markPriceAsPurchasedUseCase.execute(routeId, segmentId, priceId, currentUserId());
        return ResponseEntity.ok(PriceRecordHttpResponse.from(record));
    }
}
