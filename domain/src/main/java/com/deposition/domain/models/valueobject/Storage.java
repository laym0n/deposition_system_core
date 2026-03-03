package com.deposition.domain.models.valueobject;

import java.net.URI;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Storage {

    private URI contentLocation;

    private String versionId;
}
