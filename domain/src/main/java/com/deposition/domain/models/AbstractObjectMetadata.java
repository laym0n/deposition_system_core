package com.deposition.domain.models;

import com.deposition.domain.models.valueobject.ObjectIdentifier;
import com.deposition.domain.models.valueobject.Relationship;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
public abstract class AbstractObjectMetadata {
    private UUID id;
    private String originalName;
    @Default
    private List<ObjectIdentifier> identifiers = new ArrayList<>();
    @Default
    private List<Relationship> relationships = new ArrayList<>();
}
