package com.deposition.domain.port.in.object;

import com.deposition.domain.port.in.common.DepositionResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Validated
public interface UpdateMetadataInPort {

    DepositionResult updateMetadata(@NotNull UUID objectId, @NotNull @Valid UpdateMetadataParams params);
}
