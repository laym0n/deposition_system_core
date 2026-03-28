package com.deposition.infra.api.controller;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class DeponeMultipartForm {

    @Schema(description = "JSON часть с метаданными")
    public DeponeMultipartRequest request;

    @ArraySchema(
            arraySchema = @Schema(description = "Файлы для депонирования"),
            schema = @Schema(type = "string", format = "binary")
    )
    public List<MultipartFile> files;
}
