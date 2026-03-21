package com.deposition.domain.port.in;

import java.util.UUID;

import com.deposition.domain.port.in.dto.DepositionResult;
import com.deposition.domain.port.in.dto.RecordObjectEventRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Records a PREMIS event for an already deposited object: updates PREMIS XML,
 * anchors updated PREMIS hash in blockchain and updates OpenSearch anchors.
 */
public interface RecordObjectEventInPort {

    DepositionResult recordEvent(@NotNull UUID objectId, @NotNull @Valid RecordObjectEventRequest request);
}
