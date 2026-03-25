package com.deposition.domain.models;

import com.deposition.domain.models.enums.AgentType;
import com.deposition.domain.models.valueobject.AgentIdentifier;
import lombok.AllArgsConstructor;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AgentMetadata {
    private String id;
    private String name;
    private AgentType type;
    @Default
    private List<AgentIdentifier> identifiers = new ArrayList<>();
}
