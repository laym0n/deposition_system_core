package com.deposition.infra.api.controller.depositionjob;

import com.deposition.domain.port.in.depositionjob.CreateDepositionJobInPort;
import com.deposition.domain.port.in.depositionjob.GetDepositionJobStatusInPort;
import com.deposition.domain.port.in.depositionjob.ListMyDepositionJobsInPort;
import com.deposition.domain.port.in.depositionjob.SubmitDepositionJobInPort;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class DepositionJobController {

    private final CreateDepositionJobInPort createDepositionJobInPort;
    private final SubmitDepositionJobInPort submitDepositionJobInPort;
    private final GetDepositionJobStatusInPort getDepositionJobStatusInPort;
    private final ListMyDepositionJobsInPort listMyDepositionJobsInPort;

    @PostMapping(value = "/depone/jobs", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<CreateDepositionJobInPort.CreateDepositionJobResult> createJob(
            @RequestHeader(name = "Idempotency-Key", required = false) @Nullable String idempotencyKey,
            @RequestBody @Valid CreateJobRequest request) {

        var cmd = new CreateDepositionJobInPort.CreateDepositionJobCommand(
                idempotencyKey,
                request.intellectualEntityTypeName,
                request.intellectualEntityMetadata,
                request.descriptiveMetadata,
                request.representations);

        var result = createDepositionJobInPort.create(cmd);
        return ResponseEntity
                .created(URI.create("/depone/jobs/" + result.jobId()))
                .body(result);
    }

    @PostMapping(value = "/depone/jobs/{jobId}/submit")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> submit(@PathVariable("jobId") UUID jobId) {
        submitDepositionJobInPort.submit(jobId);
        return ResponseEntity.accepted()
                .location(URI.create("/depone/jobs/" + jobId))
                .build();
    }

    @GetMapping(value = "/depone/jobs/{jobId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<GetDepositionJobStatusInPort.DepositionJobStatusResponse> status(
            @PathVariable("jobId") UUID jobId) {
        var status = getDepositionJobStatusInPort.getStatus(jobId);
        return ResponseEntity.ok(status);
    }

    @GetMapping(value = "/depone/jobs", produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<java.util.List<ListMyDepositionJobsInPort.DepositionJobListItem>> listMyJobs() {
        var jobs = listMyDepositionJobsInPort.listMyJobs();
        return ResponseEntity.ok(jobs);
    }

    public static class CreateJobRequest {
        @NotNull
        public String intellectualEntityTypeName;
        public com.deposition.domain.port.in.object.IntellectualEntityMetadataParam intellectualEntityMetadata;
        public String descriptiveMetadata;

        @NotEmpty
        @Valid
        public java.util.List<CreateDepositionJobInPort.DepositionJobRepresentationUploadParam> representations;
    }
}
