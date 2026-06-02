package com.deposition.infra.api.controller.rights;

import com.deposition.domain.port.in.common.DepositionResult;
import com.deposition.domain.port.in.rights.UpsertRightsStatementInPort;
import com.deposition.domain.port.in.rights.UpsertRightsStatementRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class RightsStatementController {

    private final UpsertRightsStatementInPort upsertRightsStatementInPort;

    @PostMapping(value = "/objects/{objectId}/rights-statement", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DepositionResult> upsertRightsStatement(
            @PathVariable("objectId") UUID objectId,
            @RequestBody @jakarta.validation.Valid UpsertRightsStatementRequest request) {
        var result = upsertRightsStatementInPort.upsertRightsStatement(objectId, request);
        return ResponseEntity.ok(result);
    }

    @PatchMapping(value = "/objects/{objectId}/rights-statements/{rightsStatementId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DepositionResult> updateRightsStatement(
            @PathVariable("objectId") UUID objectId,
            @PathVariable("rightsStatementId") String rightsStatementId,
            @RequestBody @jakarta.validation.Valid UpsertRightsStatementRequest request) {
        var result = upsertRightsStatementInPort.updateRightsStatement(objectId, rightsStatementId, request);
        return ResponseEntity.ok(result);
    }
}
