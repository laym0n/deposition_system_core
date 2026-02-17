package com.deponic.domain.models;

import java.util.List;

import com.deponic.domain.models.enums.AgentType;
import com.deponic.domain.models.valueobject.Identifier;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentMetadata {

    private String id;
    private String name;
    private AgentType type;
    private List<Identifier> identifiers;
}
