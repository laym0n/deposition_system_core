package com.deposition.domain.models.valueobject;

import com.deposition.domain.models.enums.ObjectIdentifierType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ObjectIdentifier {

    private ObjectIdentifierType type;
    private String value;
}
