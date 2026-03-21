package com.deposition.domain.adapter.object;

import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.deposition.domain.adapter.acl.PremisOwnershipValidator;
import com.deposition.domain.exception.ObjectNotFoundException;
import com.deposition.domain.models.acl.AclPermission;
import com.deposition.domain.models.statistics.StatisticsEventType;
import com.deposition.domain.port.in.GetPremisMetadataInPort;
import com.deposition.domain.port.out.FileStorageOutPort;
import com.deposition.domain.port.out.UserService;
import com.deposition.domain.service.StatisticsEventReporter;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Validated
public class GetPremisMetadataAdapter implements GetPremisMetadataInPort {

    private final FileStorageOutPort fileStorage;
    private final PremisOwnershipValidator premisOwnershipValidator;
    private final StatisticsEventReporter statisticsEventReporter;
    private final UserService userService;

    @Override
    public Resource getPremisMetadata(UUID objectId, @Nullable String versionId) {
        premisOwnershipValidator.validateCurrentUserHasPermission(objectId, AclPermission.READ);

        try {
            var premis = fileStorage.loadPremisMetadataByObjectId(objectId, versionId);

            userService.getCurrentUserId()
                    .ifPresent(userId -> statisticsEventReporter.report(
                    StatisticsEventType.OBJECT_VIEW,
                    objectId,
                    versionId,
                    userId));

            return premis;
        } catch (IllegalArgumentException ex) {
            throw new ObjectNotFoundException(objectId);
        }
    }
}
