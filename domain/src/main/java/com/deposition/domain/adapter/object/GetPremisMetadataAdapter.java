package com.deposition.domain.adapter.object;

import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.deposition.domain.exception.ResourceNotFoundException;
import com.deposition.domain.models.acl.AclPermission;
import com.deposition.domain.models.statistics.StatisticsEventType;
import com.deposition.domain.port.in.GetPremisMetadataInPort;
import com.deposition.domain.port.out.FileStorageOutPort;
import com.deposition.domain.port.out.UserOutPort;
import com.deposition.domain.service.StatisticsEventReporter;
import com.deposition.domain.service.acl.AccessValidatorService;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Validated
public class GetPremisMetadataAdapter implements GetPremisMetadataInPort {

    private final FileStorageOutPort fileStorage;
    private final AccessValidatorService accessValidatorService;
    private final StatisticsEventReporter statisticsEventReporter;
    private final UserOutPort userService;

    @Override
    public Resource getPremisMetadata(UUID objectId, @Nullable String versionId) {
        accessValidatorService.validateCurrentUserHasPermission(objectId, AclPermission.READ);

        try {
            var premis = fileStorage.loadPremisMetadataByObjectId(objectId, versionId);

            userService.getOptinalCurrentUserId()
                    .ifPresent(userId -> statisticsEventReporter.report(
                    StatisticsEventType.OBJECT_VIEW,
                    objectId,
                    versionId,
                    userId));

            return premis;
        } catch (IllegalArgumentException ex) {
            throw new ResourceNotFoundException("Object", objectId.toString());
        }
    }
}
