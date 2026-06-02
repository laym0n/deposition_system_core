package com.deposition.infra.api.controller.acl;

import com.deposition.domain.port.in.acl.UpsertObjectAclEntryInPort;
import com.deposition.domain.port.in.acl.UpsertObjectAclEntryRequest;
import com.deposition.domain.port.in.common.DepositionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ObjectAclController {

    private final UpsertObjectAclEntryInPort upsertObjectAclEntryInPort;

    @PostMapping(value = "/objects/{objectId}/acl/users", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DepositionResult> upsertUserAclEntry(
            @PathVariable("objectId") UUID objectId,
            @RequestBody @jakarta.validation.Valid UpsertObjectAclEntryRequest request) {
        var result = upsertObjectAclEntryInPort.upsertUserEntry(objectId, request);
        return ResponseEntity.ok(result);
    }
}
