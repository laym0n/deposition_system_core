package com.deponic.infra.ethereum.adapter;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.deponic.domain.models.AnchorRecord;
import com.deponic.domain.models.SnapshotPointer;
import com.deponic.domain.port.out.BlockchainOutPort;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.deponic.infra.ethereum.config.EthereumProperties;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.utils.Numeric;

@Component
@RequiredArgsConstructor
@Slf4j
public class EthereumAdapter implements BlockchainOutPort {

    private final Web3j web3j;
    private final ObjectMapper objectMapper;
    private final EthereumProperties properties;

    private volatile String cachedFromAccount;

    @Override
    public AnchorRecord persistAnchorRecord(AnchorRecord anchorRecord) {
        if (anchorRecord == null) {
            throw new IllegalArgumentException("AnchorRecord must not be null");
        }

        String payload = buildAnchorPayload(anchorRecord);
        String txHash = publishAsTransaction(payload);
        log.info("AnchorRecord persisted in Ethereum tx={} payloadId={}", txHash, anchorRecord.getId());
        return anchorRecord;
    }

    @Override
    public SnapshotPointer persistSnapshotPoint(SnapshotPointer snapshotPointer) {
        if (snapshotPointer == null) {
            throw new IllegalArgumentException("SnapshotPointer must not be null");
        }

        String payload = buildSnapshotPointerPayload(snapshotPointer);
        String txHash = publishAsTransaction(payload);

        if (snapshotPointer.getId() == null || snapshotPointer.getId().isBlank()) {
            snapshotPointer.setId(txHash);
        }

        log.info("SnapshotPointer persisted in Ethereum tx={} pointerId={}", txHash, snapshotPointer.getId());
        return snapshotPointer;
    }

    private String publishAsTransaction(String payload) {
        String fromAccount = resolveFromAccount();
        String data = Numeric.toHexString(payload.getBytes());
        Transaction transaction = new Transaction(
                fromAccount,
                null,
                null,
                properties.getGasLimitInteger(),
                fromAccount,
                BigInteger.ZERO,
                data);

        try {
            var ethSendTransaction = web3j.ethSendTransaction(transaction).send();
            if (ethSendTransaction.hasError()) {
                throw new IllegalStateException("Ethereum RPC returned an error: "
                        + ethSendTransaction.getError().getMessage());
            }

            String txHash = ethSendTransaction.getTransactionHash();
            if (txHash == null || txHash.isBlank()) {
                throw new IllegalStateException("No transaction hash returned by Ethereum node");
            }
            return txHash;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to send transaction to Ethereum node", ex);
        }
    }

    private String resolveFromAccount() {
        if (cachedFromAccount != null && !cachedFromAccount.isBlank()) {
            return cachedFromAccount;
        }

        synchronized (this) {
            if (cachedFromAccount != null && !cachedFromAccount.isBlank()) {
                return cachedFromAccount;
            }

            try {
                var ethAccounts = web3j.ethAccounts().send();
                List<String> accounts = ethAccounts.getAccounts();
                if (accounts == null || accounts.isEmpty()) {
                    throw new IllegalStateException("No available Ethereum account returned by eth_accounts");
                }
                cachedFromAccount = accounts.getFirst();
            } catch (Exception ex) {
                throw new IllegalStateException("Failed to resolve Ethereum sender account", ex);
            }

            log.info("Using Ethereum sender account: {}", cachedFromAccount);
            return cachedFromAccount;
        }
    }

    private String buildAnchorPayload(AnchorRecord anchorRecord) {
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("type", "ANCHOR_RECORD");
        payload.put("id", anchorRecord.getId());
        payload.put("snapshotHash", anchorRecord.getSnapshotHash());
        payload.put("timestamp", anchorRecord.getTimestamp() == null ? null : anchorRecord.getTimestamp().toString());
        return toJson(payload);
    }

    private String buildSnapshotPointerPayload(SnapshotPointer snapshotPointer) {
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("type", "SNAPSHOT_POINTER");
        payload.put("id", snapshotPointer.getId());
        payload.put("anchorRecordId", snapshotPointer.getAnchorRecordId());
        payload.put("offChainLocation", snapshotPointer.getOffChainLocation());
        return toJson(payload);
    }

    private String toJson(Map<String, String> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize payload for Ethereum transaction", ex);
        }
    }
}
