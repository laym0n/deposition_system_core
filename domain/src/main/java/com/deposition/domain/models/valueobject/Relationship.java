package com.deposition.domain.models.valueobject;

import java.util.List;

import com.deposition.domain.models.enums.ObjectRelationshipSubType;
import com.deposition.domain.models.enums.ObjectRelationshipType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Relationship {

    private ObjectRelationshipType type;
    private ObjectRelationshipSubType subType;
    private List<RelationObjectIdentifier> relatedObjects;
    private List<RelationEventIdentifier> relatedEvents;
}
