package com.deposition.domain.models;

import java.util.List;
import java.util.UUID;

import com.deposition.domain.models.enums.ObjectCategory;
import com.deposition.domain.models.valueobject.Characteristics;
import com.deposition.domain.models.valueobject.Identifier;
import com.deposition.domain.models.valueobject.Storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObjectMetadata {

    private UUID id;
    private ObjectCategory category;
    private String originalName;
    private List<Characteristics> characteristics;
    private List<Storage> storages;
    private List<Identifier> identifiers;
}
