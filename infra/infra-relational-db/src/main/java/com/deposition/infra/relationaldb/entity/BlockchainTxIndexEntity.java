package com.deposition.infra.relationaldb.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "blockchain_tx_index")
@Getter
@Setter
@NoArgsConstructor
public class BlockchainTxIndexEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "object_id", nullable = false)
    private UUID objectId;

    @Column(name = "storage_version_id")
    private String storageVersionId;

    @Column(name = "tx_id", nullable = false)
    private String txId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
