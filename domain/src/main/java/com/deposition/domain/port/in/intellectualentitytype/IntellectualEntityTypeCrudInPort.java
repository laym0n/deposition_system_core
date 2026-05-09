package com.deposition.domain.port.in.intellectualentitytype;

import com.deposition.domain.models.IntellectualEntityType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.UUID;

@Validated
public interface IntellectualEntityTypeCrudInPort {

    List<IntellectualEntityType> list();

    IntellectualEntityType getById(@NotNull UUID id);

    IntellectualEntityType create(@NotNull @Valid CreateCommand command);

    IntellectualEntityType update(@NotNull UUID id, @NotNull @Valid UpdateCommand command);

    void delete(@NotNull UUID id);

    record CreateCommand(
            @NotBlank String name,
            String description) {
    }

    record UpdateCommand(
            @NotBlank String name,
            String description) {
    }
}
