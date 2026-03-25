package com.deposition.infra.api.controller.event;

import com.deposition.domain.port.in.common.DepositionResult;
import com.deposition.domain.port.in.event.RecordObjectEventInPort;
import com.deposition.domain.port.in.event.RecordObjectEventRequest;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ObjectEventController {

    private final RecordObjectEventInPort recordObjectEventInPort;

    @PostMapping(value = "/objects/{objectId}/events", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<DepositionResult> recordEvent(
            @PathVariable("objectId") UUID objectId,
            @RequestBody @jakarta.validation.Valid RecordObjectEventRequest request) {
        var result = recordObjectEventInPort.recordEvent(objectId, request);
        return ResponseEntity.ok(result);
    }
}
