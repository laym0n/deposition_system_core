package com.deposition.domain.models.valueobject;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Characteristics {

    private Long size;
    private List<FixityBlock> fixity;
    private List<FormatBlock> format;
}
