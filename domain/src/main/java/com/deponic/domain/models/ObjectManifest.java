package com.deponic.domain.models;

import com.deponic.domain.models.valueobject.ObjectEventLink;
import com.deponic.domain.models.valueobject.ObjectRightStatementLink;
import com.deponic.domain.models.valueobject.ObjectsLink;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObjectManifest {

    private String id;
    private Long version;
    private OffsetDateTime createdAt;
    private String sourceObjectId;
    private List<ObjectsLink> objectsLinks;
    private String objectMetadataCID;
    private List<ObjectEventLink> objectEventLinks;
    private List<ObjectRightStatementLink> objectRightStatementLinks;
}
