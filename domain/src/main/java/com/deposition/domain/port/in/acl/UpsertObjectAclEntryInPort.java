package com.deposition.domain.port.in.acl;

import com.deposition.domain.port.in.common.DepositionResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public interface UpsertObjectAclEntryInPort {

    DepositionResult upsertUserEntry(@NotNull UUID objectId,
                                     @NotNull @Valid UpsertObjectAclEntryRequest request);
}
