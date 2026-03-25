package com.deposition.infra.relationaldb.repository.spec;

import com.deposition.infra.relationaldb.entity.DescriptiveMetadataSchemaEntity;
import org.springframework.data.jpa.domain.Specification;

public final class DescriptiveMetadataSchemaSpecifications {

    private DescriptiveMetadataSchemaSpecifications() {

    }

    public static Specification<DescriptiveMetadataSchemaEntity> entityTypeEquals(String entityType) {
        return (root, query, cb) -> entityType == null || entityType.isBlank()
                ? cb.conjunction()
                : cb.equal(root.get("entityType"), entityType);
    }

    public static Specification<DescriptiveMetadataSchemaEntity> activeEquals(Boolean active) {
        return (root, query, cb) -> active == null
                ? cb.conjunction()
                : cb.equal(root.get("active"), active);
    }
}
