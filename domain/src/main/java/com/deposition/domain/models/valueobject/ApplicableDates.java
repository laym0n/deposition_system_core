package com.deposition.domain.models.valueobject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.ZonedDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicableDates {

    private ZonedDateTime startDate;
    private ZonedDateTime endDate;
}
