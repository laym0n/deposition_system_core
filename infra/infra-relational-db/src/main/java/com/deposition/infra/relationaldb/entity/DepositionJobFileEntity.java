package com.deposition.infra.relationaldb.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "deposition_job_file")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositionJobFileEntity {

    @Id
    @Column(name = "file_id", nullable = false)
    private UUID fileId;

    @Column(name = "job_id", nullable = false)
    private UUID jobId;

    @Column(name = "representation_index", nullable = false)
    private Integer representationIndex;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "size_bytes_expected")
    private Long sizeBytesExpected;

    @Column(name = "object_key", nullable = false)
    private String objectKey;

    @Column(name = "content_location", nullable = false)
    private String contentLocation;
}
