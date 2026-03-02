package com.deposition.infra.api.controller;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.deposition.domain.port.in.FileMetadataParam;
import com.deposition.domain.port.in.IntellectualEntityMetadataParam;
import com.deposition.domain.port.in.RepresentationMetadataParam;

public class DeponeMultipartRequest {

    public IntellectualEntityMetadataParam intellectualEntityMetadata;

    public RepresentationMetadataParam representationMetadata;

    public List<FileMetadataParam> fileMetadata;

    public List<MultipartFile> files;
}
