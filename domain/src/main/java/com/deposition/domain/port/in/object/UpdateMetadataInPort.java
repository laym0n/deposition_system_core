package com.deposition.domain.port.in.object;

import java.util.UUID;

import org.springframework.validation.annotation.Validated;

import com.deposition.domain.port.in.common.DepositionResult;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Validated
public interface UpdateMetadataInPort {

    DepositionResult updateMetadata(@NotNull UUID objectId, @NotNull @Valid UpdateMetadataParams params);
}
