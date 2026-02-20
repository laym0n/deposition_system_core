package com.deposition.domain.models.valueobject;
import java.util.UUID;

import com.deposition.domain.models.enums.EventObjectLinkRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventObjectLink {

    private UUID objectId;
    private EventObjectLinkRole role;
}
