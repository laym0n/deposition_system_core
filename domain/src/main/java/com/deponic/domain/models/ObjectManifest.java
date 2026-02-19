package com.deponic.domain.models;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.deponic.domain.models.valueobject.ObjectEventLink;
import com.deponic.domain.models.valueobject.ObjectRightStatementLink;
import com.deponic.domain.models.valueobject.ObjectsLink;
import com.deponic.domain.models.valueobject.Storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObjectManifest {

    private UUID id;
    private Long version;
    private OffsetDateTime createdAt;
    private String sourceObjectId;
    private List<ObjectsLink> objectsLinks;
    private List<Storage> objectMetadataCids;
    private List<ObjectEventLink> objectEventLinks;
    private List<ObjectRightStatementLink> objectRightStatementLinks;
}
