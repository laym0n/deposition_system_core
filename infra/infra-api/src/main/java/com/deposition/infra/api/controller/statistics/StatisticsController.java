package com.deposition.infra.api.controller.statistics;

import com.deposition.domain.models.statistics.StatisticsEventType;
import com.deposition.domain.port.in.statistics.GetStatisticsEventsInPort;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class StatisticsController {

    private final GetStatisticsEventsInPort getStatisticsEventsInPort;

    @GetMapping(value = "/statistics/events", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<StatisticsEventResponse>> getEvents(
            @RequestParam(name = "objectId") UUID objectId,
            @RequestParam(name = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(name = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(name = "eventType", required = false) StatisticsEventType eventType) {

        var request = new GetStatisticsEventsInPort.GetStatisticsEventsRequest(objectId, from, to, eventType);
        var result = getStatisticsEventsInPort.getEvents(request);

        var response = result.stream()
                .map(e -> new StatisticsEventResponse(
                        e.id(),
                        e.eventType(),
                        e.objectId(),
                        e.objectVersion(),
                        e.userId(),
                        e.timestamp()))
                .toList();

        return ResponseEntity.ok(response);
    }

    public record StatisticsEventResponse(
            UUID id,
            StatisticsEventType eventType,
            UUID objectId,
            String objectVersion,
            String userId,
            OffsetDateTime timestamp) {
    }
}
