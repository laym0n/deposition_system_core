package com.deposition.domain.models.valueobject;

import com.deposition.domain.models.enums.AgentIdentifierType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AgentIdentifier {

    private AgentIdentifierType type;
    private String value;
}
