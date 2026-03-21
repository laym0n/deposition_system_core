package com.deposition.domain.port.in;

import java.util.UUID;

import com.deposition.domain.port.in.dto.UpsertRightsStatementRequest;
import com.deposition.domain.port.in.dto.DepositionResult;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface UpsertRightsStatementInPort {

    DepositionResult updateRightsStatement(@NotNull UUID objectId,
            @NotNull String rightsStatementId,
            @NotNull @Valid UpsertRightsStatementRequest request);

    DepositionResult upsertRightsStatement(@NotNull UUID objectId,
            @NotNull @Valid UpsertRightsStatementRequest request);
}
