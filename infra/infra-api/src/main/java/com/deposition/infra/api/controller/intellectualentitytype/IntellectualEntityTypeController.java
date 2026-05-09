package com.deposition.infra.api.controller.intellectualentitytype;

import com.deposition.domain.models.IntellectualEntityType;
import com.deposition.domain.port.in.intellectualentitytype.IntellectualEntityTypeCrudInPort;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class IntellectualEntityTypeController {

    private final IntellectualEntityTypeCrudInPort crudInPort;

    @GetMapping(value = "/intellectual-entity-types", produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<IntellectualEntityTypeDto>> list() {
        var items = crudInPort.list().stream().map(IntellectualEntityTypeDto::from).toList();
        return ResponseEntity.ok(items);
    }

    @GetMapping(value = "/intellectual-entity-types/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<IntellectualEntityTypeDto> getById(@PathVariable("id") UUID id) {
        var item = crudInPort.getById(id);
        return ResponseEntity.ok(IntellectualEntityTypeDto.from(item));
    }

    @PostMapping(value = "/intellectual-entity-types", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<IntellectualEntityTypeDto> create(@RequestBody @Valid CreateRequest request) {
        var created = crudInPort.create(new IntellectualEntityTypeCrudInPort.CreateCommand(
                request.name(),
                request.description()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(URI.create("/intellectual-entity-types/" + created.id()))
                .body(IntellectualEntityTypeDto.from(created));
    }

    @PutMapping(value = "/intellectual-entity-types/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<IntellectualEntityTypeDto> update(@PathVariable("id") UUID id,
                                                           @RequestBody @Valid UpdateRequest request) {
        var updated = crudInPort.update(id, new IntellectualEntityTypeCrudInPort.UpdateCommand(
                request.name(),
                request.description()));
        return ResponseEntity.ok(IntellectualEntityTypeDto.from(updated));
    }

    @DeleteMapping(value = "/intellectual-entity-types/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        crudInPort.delete(id);
        return ResponseEntity.noContent().build();
    }

    public record IntellectualEntityTypeDto(
            @NotNull UUID id,
            @NotBlank String name,
            String description) {
        public static IntellectualEntityTypeDto from(IntellectualEntityType t) {
            return new IntellectualEntityTypeDto(t.id(), t.name(), t.description());
        }
    }

    public record CreateRequest(
            @NotBlank String name,
            String description) {
    }

    public record UpdateRequest(
            @NotBlank String name,
            String description) {
    }
}
