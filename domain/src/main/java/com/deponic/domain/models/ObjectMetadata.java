package com.deponic.domain.models;

import java.util.List;

import com.deponic.domain.models.enums.ObjectCategory;
import com.deponic.domain.models.valueobject.Characteristics;
import com.deponic.domain.models.valueobject.Identifier;
import com.deponic.domain.models.valueobject.Storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObjectMetadata {

    private String id;
    private ObjectCategory category;
    private String originalName;
    private List<Characteristics> characteristics;
    private Storage storage;
    private List<Identifier> identifiers;
}
