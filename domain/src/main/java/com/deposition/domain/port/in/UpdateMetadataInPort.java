package com.deposition.domain.port.in;

import java.util.UUID;

import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Validated
public interface UpdateMetadataInPort {

    UpdateMetadataResult updateMetadata(@NotNull UUID objectId, @NotNull @Valid UpdateMetadataParams params);
}
