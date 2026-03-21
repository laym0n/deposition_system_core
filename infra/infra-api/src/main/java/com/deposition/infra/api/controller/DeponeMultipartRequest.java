package com.deposition.infra.api.controller;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.deposition.domain.port.in.object.FileMetadataParam;
import com.deposition.domain.port.in.object.IntellectualEntityMetadataParam;
import com.deposition.domain.port.in.object.RepresentationMetadataParam;

public class DeponeMultipartRequest {

    public IntellectualEntityMetadataParam intellectualEntityMetadata;

    public String descriptiveMetadata;

    public RepresentationMetadataParam representationMetadata;

    public List<FileMetadataParam> fileMetadata;

    public List<MultipartFile> files;
}
