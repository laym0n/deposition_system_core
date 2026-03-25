package com.deposition.domain.port.in.rights;

import com.deposition.domain.port.in.common.DepositionResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public interface UpsertRightsStatementInPort {

    DepositionResult updateRightsStatement(@NotNull UUID objectId,
                                           @NotNull String rightsStatementId,
                                           @NotNull @Valid UpsertRightsStatementRequest request);

    DepositionResult upsertRightsStatement(@NotNull UUID objectId,
                                           @NotNull @Valid UpsertRightsStatementRequest request);
}
