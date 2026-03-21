package com.deposition.infra.api.controller.event;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.deposition.domain.port.in.RecordObjectEventInPort;
import com.deposition.domain.port.in.dto.DepositionResult;
import com.deposition.domain.port.in.dto.RecordObjectEventRequest;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

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
