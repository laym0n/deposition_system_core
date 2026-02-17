package com.deponic.domain.models.valueobject;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicableDates {

    private LocalDate startDate;
    private LocalDate endDate;
}
