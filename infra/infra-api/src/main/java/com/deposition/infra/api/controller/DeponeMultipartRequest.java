package com.deposition.infra.api.controller;

import com.deposition.domain.port.in.object.FileMetadataParam;
import com.deposition.domain.port.in.object.IntellectualEntityMetadataParam;
import com.deposition.domain.port.in.object.RepresentationMetadataParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class DeponeMultipartRequest {

    public IntellectualEntityMetadataParam intellectualEntityMetadata;

    public String descriptiveMetadata;

    public RepresentationMetadataParam representationMetadata;

    public List<FileMetadataParam> fileMetadata;

    public List<MultipartFile> files;
}
