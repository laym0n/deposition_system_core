package com.deposition.domain.adapter.intellectualentitytype;

import com.deposition.domain.exception.ResourceNotFoundException;
import com.deposition.domain.models.IntellectualEntityType;
import com.deposition.domain.port.in.intellectualentitytype.IntellectualEntityTypeCrudInPort;
import com.deposition.domain.port.out.IntellectualEntityTypeOutPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Validated
public class IntellectualEntityTypeCrudAdapter implements IntellectualEntityTypeCrudInPort {

    private final IntellectualEntityTypeOutPort outPort;

    @Override
    public List<IntellectualEntityType> list() {
        return outPort.findAll();
    }

    @Override
    public IntellectualEntityType getById(UUID id) {
        return outPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IntellectualEntityType", id.toString()));
    }

    @Override
    public IntellectualEntityType create(CreateCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }

        var type = new IntellectualEntityType(UUID.randomUUID(), command.name(), command.description());
        return outPort.save(type);
    }

    @Override
    public IntellectualEntityType update(UUID id, UpdateCommand command) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }

        outPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IntellectualEntityType", id.toString()));

        var updated = new IntellectualEntityType(id, command.name(), command.description());
        return outPort.save(updated);
    }

    @Override
    public void delete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        outPort.deleteById(id);
    }
}
