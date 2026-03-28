package com.deposition.domain.service;

import com.deposition.domain.models.PremisSnapshot;
import com.deposition.domain.models.enums.AgentIdentifierType;
import com.deposition.domain.port.out.ObjectIndexDocument;

public final class ObjectVisibilityResolver {

    private static final ObjectIndexDocument.Visibility DEFAULT_VISIBILITY = ObjectIndexDocument.Visibility.PRIVATE;
    private static final String PUBLIC_AGENT_VALUE = "PUBLIC";

    private ObjectVisibilityResolver() {
    }

    public static ObjectIndexDocument.Visibility resolve(PremisSnapshot snapshot) {
        if (snapshot == null || snapshot.getRightsStatements() == null) {
            return DEFAULT_VISIBILITY;
        }

        for (var rs : snapshot.getRightsStatements()) {
            if (rs == null || rs.getLinkingAgentIdentifiers() == null) {
                continue;
            }

            boolean linkedToPublic = rs.getLinkingAgentIdentifiers().stream()
                    .filter(java.util.Objects::nonNull)
                    .map(link -> link.getAgentIdentifier())
                    .filter(java.util.Objects::nonNull)
                    .anyMatch(id -> id.getType() == AgentIdentifierType.PUBLIC
                            && id.getValue() != null
                            && PUBLIC_AGENT_VALUE.equalsIgnoreCase(id.getValue()));

            if (!linkedToPublic) {
                continue;
            }
            if (rs.getRightsGranted() == null) {
                continue;
            }
            for (var g : rs.getRightsGranted()) {
                if (g == null || g.getAct() == null || g.getAct().isBlank()) {
                    continue;
                }
                try {
                    return ObjectIndexDocument.Visibility.valueOf(g.getAct().toUpperCase());
                } catch (RuntimeException ex) {
                    return DEFAULT_VISIBILITY;
                }
            }
        }

        return DEFAULT_VISIBILITY;
    }
}
