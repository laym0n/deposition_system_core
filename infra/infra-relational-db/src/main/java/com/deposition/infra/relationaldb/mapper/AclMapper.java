package com.deposition.infra.relationaldb.mapper;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import com.deposition.domain.models.acl.AclEntry;
import com.deposition.domain.models.acl.AclPermission;
import com.deposition.domain.models.acl.AclPrincipal;
import com.deposition.domain.models.acl.AclPrincipalType;
import com.deposition.domain.models.acl.ObjectAcl;
import com.deposition.infra.relationaldb.entity.ObjectAclEntity;
import com.deposition.infra.relationaldb.entity.ObjectAclEntryEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public abstract class AclMapper {

    @Mapping(target = "entries", ignore = true)
    public abstract ObjectAcl toDomain(ObjectAclEntity entity);

    @AfterMapping
    protected void fillDomainEntries(ObjectAclEntity entity, @MappingTarget ObjectAcl target) {
        if (entity == null || entity.getEntries() == null) {
            return;
        }

        Map<String, AclEntry> grouped = new LinkedHashMap<>();
        for (var row : entity.getEntries()) {
            if (row == null) {
                continue;
            }
            var principalType = AclPrincipalType.valueOf(row.getPrincipalType());
            var principalId = row.getPrincipalId();
            var permission = AclPermission.valueOf(row.getPermission());
            var key = principalType + ":" + principalId;

            var existing = grouped.get(key);
            if (existing == null) {
                grouped.put(key, AclEntry.builder()
                        .principal(AclPrincipal.builder().type(principalType).id(principalId).build())
                        .permissions(EnumSet.of(permission))
                        .build());
            } else {
                existing.getPermissions().add(permission);
            }
        }

        target.setEntries(new java.util.ArrayList<>(grouped.values()));
    }

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "entries", ignore = true)
    public abstract ObjectAclEntity toEntity(ObjectAcl domain);

    @AfterMapping
    protected void fillEntityEntries(ObjectAcl domain, @MappingTarget ObjectAclEntity target) {
        if (target.getEntries() != null) {
            target.getEntries().clear();
        }

        if (domain == null || domain.getEntries() == null) {
            return;
        }

        for (var entry : domain.getEntries()) {
            if (entry == null || entry.getPrincipal() == null || entry.getPermissions() == null) {
                continue;
            }
            for (var permission : entry.getPermissions()) {
                var row = new ObjectAclEntryEntity();
                row.setObjectAcl(target);
                row.setPrincipalType(entry.getPrincipal().getType().name());
                row.setPrincipalId(entry.getPrincipal().getId());
                row.setPermission(permission.name());
                target.getEntries().add(row);
            }
        }
    }

    @Named("toPrincipalType")
    protected AclPrincipalType toPrincipalType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return AclPrincipalType.valueOf(value);
    }
}
