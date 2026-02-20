package com.deposition.domain.models.valueobject;
import com.deposition.domain.models.enums.ObjectLinkSubType;
import com.deposition.domain.models.enums.ObjectLinkType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObjectsLink {

    private String objectId;
    private ObjectLinkType type;
    private ObjectLinkSubType subType;
}
