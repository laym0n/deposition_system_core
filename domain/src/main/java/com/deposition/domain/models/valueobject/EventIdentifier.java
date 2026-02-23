package com.deposition.domain.models.valueobject;

import com.deposition.domain.models.enums.EventIdentifierType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EventIdentifier {

    private EventIdentifierType type;
    private String value;
}
