package com.deponic.domain.models.valueobject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentLocation {

    private String contentLocationType;
    private String contentLocationValue;
}
