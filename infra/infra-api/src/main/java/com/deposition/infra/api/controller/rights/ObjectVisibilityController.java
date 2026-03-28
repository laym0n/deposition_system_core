package com.deposition.infra.api.controller.rights;

import com.deposition.domain.port.in.common.DepositionResult;
import com.deposition.domain.port.in.rights.UpdateObjectVisibilityInPort;
import com.deposition.domain.port.in.rights.UpdateObjectVisibilityRequest;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ObjectVisibilityController {

    private final UpdateObjectVisibilityInPort updateObjectVisibilityInPort;

    @PutMapping(value = "/objects/{objectId}/visibility",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<DepositionResult> updateVisibility(
            @PathVariable("objectId") UUID objectId,
            @RequestBody @Valid UpdateObjectVisibilityRequest request) {
        var result = updateObjectVisibilityInPort.updateVisibility(objectId, request);
        return ResponseEntity.ok(result);
    }
}
