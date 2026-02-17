package com.deponic.domain.models.valueobject;
import com.deponic.domain.models.enums.EventObjectLinkRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventObjectLink {

    private String objectId;
    private EventObjectLinkRole role;
}
