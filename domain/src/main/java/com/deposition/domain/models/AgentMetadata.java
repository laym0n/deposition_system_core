package com.deposition.domain.models;

import java.util.ArrayList;
import java.util.List;

import com.deposition.domain.models.enums.AgentType;
import com.deposition.domain.models.valueobject.Identifier;

import lombok.AllArgsConstructor;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AgentMetadata {

    private String id;
    private String name;
    private AgentType type;
    @Default
    private List<Identifier> identifiers = new ArrayList<>();
}
