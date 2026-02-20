package com.deposition.infra.api.controller;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.deposition.domain.models.ObjectMetadata;
import com.deposition.domain.models.valueobject.Storage;

public class DeponeMultipartRequest {

    public ObjectMetadata intellectualEntityMetadata;

    public List<Storage> storages;

    public List<MultipartFile> files;
}
