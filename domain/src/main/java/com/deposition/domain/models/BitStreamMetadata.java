package com.deposition.domain.models;

import com.deposition.domain.models.valueobject.Characteristics;
import com.deposition.domain.models.valueobject.Storage;
import lombok.AllArgsConstructor;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BitStreamMetadata extends AbstractObjectMetadata {
    @Default
    private List<Characteristics> characteristics = new ArrayList<>();
    @Default
    private List<Storage> storages = new ArrayList<>();
}