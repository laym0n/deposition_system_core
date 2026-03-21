package com.deposition.infra.api.controller.rights;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.deposition.domain.port.in.UpsertRightsStatementInPort;
import com.deposition.domain.port.in.dto.DepositionResult;
import com.deposition.domain.port.in.dto.UpsertRightsStatementRequest;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class RightsStatementController {

    private final UpsertRightsStatementInPort upsertRightsStatementInPort;

    @PostMapping(value = "/objects/{objectId}/rights-statement", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<DepositionResult> upsertRightsStatement(
            @PathVariable("objectId") UUID objectId,
            @RequestBody @jakarta.validation.Valid UpsertRightsStatementRequest request) {
        var result = upsertRightsStatementInPort.upsertRightsStatement(objectId, request);
        return ResponseEntity.ok(result);
    }

    @PatchMapping(value = "/objects/{objectId}/rights-statements/{rightsStatementId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<DepositionResult> updateRightsStatement(
            @PathVariable("objectId") UUID objectId,
            @PathVariable("rightsStatementId") String rightsStatementId,
            @RequestBody @jakarta.validation.Valid UpsertRightsStatementRequest request) {
        var result = upsertRightsStatementInPort.updateRightsStatement(objectId, rightsStatementId, request);
        return ResponseEntity.ok(result);
    }
}
