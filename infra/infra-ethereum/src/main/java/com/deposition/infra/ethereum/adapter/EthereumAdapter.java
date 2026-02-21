package com.deposition.infra.ethereum.adapter;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.utils.Numeric;

import com.deposition.domain.models.AnchorRecord;
import com.deposition.domain.models.SnapshotPointer;
import com.deposition.domain.port.out.BlockchainOutPort;
import com.deposition.infra.ethereum.config.EthereumProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class EthereumAdapter implements BlockchainOutPort {

    private final Web3j web3j;
    private final ObjectMapper objectMapper;
    private final EthereumProperties properties;

    @Override
    public AnchorRecord persistAnchorRecord(AnchorRecord anchorRecord) {
        if (anchorRecord == null) {
            throw new IllegalArgumentException("AnchorRecord must not be null");
        }

        String payload = toJson(anchorRecord);
        String txHash = publishAsTransaction(payload);
        log.info("AnchorRecord persisted in Ethereum tx={} payloadId={}", txHash, anchorRecord.getId());
        return anchorRecord;
    }

    @Override
    public SnapshotPointer persistSnapshotPoint(SnapshotPointer snapshotPointer) {
        if (snapshotPointer == null) {
            throw new IllegalArgumentException("SnapshotPointer must not be null");
        }

        String payload = toJson(snapshotPointer);
        String txHash = publishAsTransaction(payload);

        if (snapshotPointer.getId() == null || snapshotPointer.getId().isBlank()) {
            snapshotPointer.setId(txHash);
        }

        log.info("SnapshotPointer persisted in Ethereum tx={} pointerId={}", txHash, snapshotPointer.getId());
        return snapshotPointer;
    }

    private String publishAsTransaction(String payload) {
        Credentials credentials = resolveCredentials();
        String fromAddress = credentials.getAddress();
        String data = Numeric.toHexString(payload.getBytes(StandardCharsets.UTF_8));

        try {
            var nonceResponse = web3j.ethGetTransactionCount(fromAddress, DefaultBlockParameterName.PENDING).send();
            if (nonceResponse.hasError()) {
                throw new IllegalStateException("Ethereum RPC returned an error while fetching nonce: "
                        + nonceResponse.getError().getMessage());
            }

            BigInteger nonce = nonceResponse.getTransactionCount();
            BigInteger gasPrice = properties.getGasPriceWei() == null ? BigInteger.ZERO : properties.getGasPriceWei();
            BigInteger gasLimit = properties.getGasLimitInteger();

            RawTransaction rawTransaction = RawTransaction.createTransaction(
                    nonce,
                    gasPrice,
                    gasLimit,
                    fromAddress,
                    BigInteger.ZERO,
                    data);

            byte[] signedMessage = signTransaction(rawTransaction, credentials);
            String signedTransactionHex = Numeric.toHexString(signedMessage);

            var ethSendTransaction = web3j.ethSendRawTransaction(signedTransactionHex).send();
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

    private Credentials resolveCredentials() {
        String privateKey = properties.getPrivateKey();
        if (privateKey == null || privateKey.isBlank()) {
            throw new IllegalStateException("integration.ethereum.private-key must be configured");
        }

        return Credentials.create(privateKey);
    }

    private byte[] signTransaction(RawTransaction rawTransaction, Credentials credentials) {
        Long chainId = properties.getChainId();
        if (chainId == null) {
            return TransactionEncoder.signMessage(rawTransaction, credentials);
        }

        return TransactionEncoder.signMessage(rawTransaction, chainId, credentials);
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize payload for Ethereum transaction", ex);
        }
    }
}
