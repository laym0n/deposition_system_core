package com.deposition.domain.port.in.acl;

import java.util.UUID;

import com.deposition.domain.port.in.common.DepositionResult;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface UpsertObjectAclEntryInPort {

    DepositionResult upsertUserEntry(@NotNull UUID objectId,
            @NotNull @Valid UpsertObjectAclEntryRequest request);

    DepositionResult removeUserEntry(@NotNull UUID objectId,
            @NotNull String userId);
}
