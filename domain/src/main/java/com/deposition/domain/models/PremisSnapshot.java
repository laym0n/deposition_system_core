package com.deposition.domain.models;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PremisSnapshot {

    @Default
    private List<AbstractObjectMetadata> objects = new ArrayList<>();

    @Default
    private List<EventMetadata> events = new ArrayList<>();

    @Default
    private List<AgentMetadata> agents = new ArrayList<>();

    @Default
    private List<RightsStatementMetadata> rightsStatements = new ArrayList<>();
}
