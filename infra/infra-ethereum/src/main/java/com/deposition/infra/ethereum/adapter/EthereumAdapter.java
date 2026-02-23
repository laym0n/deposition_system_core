package com.deposition.infra.ethereum.adapter;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.utils.Numeric;

import com.deposition.domain.models.AnchorRecord;
import com.deposition.domain.port.out.BlockchainOutPort;
import com.deposition.infra.ethereum.config.EthereumProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
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
        String payload = toJson(anchorRecord);
        String txHash = publishAsTransaction(payload);
        if (anchorRecord.getTxId() == null || anchorRecord.getTxId().isBlank()) {
            anchorRecord.setTxId(txHash);
        }
        log.info("AnchorRecord persisted in Ethereum tx={} payloadId={}", txHash, anchorRecord.getTxId());
        return anchorRecord;
    }

    private String publishAsTransaction(String payload) {
        String fromAddress = properties.getFromAddress();
        String data = Numeric.toHexString(payload.getBytes(StandardCharsets.UTF_8));

        try {
            var nonceResponse = web3j.ethGetTransactionCount(fromAddress, DefaultBlockParameterName.PENDING).send();
            if (nonceResponse.hasError()) {
                throw new IllegalStateException("Ethereum RPC returned an error while fetching nonce: "
                        + nonceResponse.getError().getMessage());
            }

            BigInteger nonce = nonceResponse.getTransactionCount();

            var rawTransaction = Transaction.createFunctionCallTransaction(
                    fromAddress,
                    nonce,
                    properties.getGasPriceWei(),
                    properties.getGasLimitInteger(),
                    fromAddress,
                    BigInteger.ZERO,
                    data);

            var ethSendTransaction = web3j.ethSendTransaction(rawTransaction).send();
            if (ethSendTransaction.hasError()) {
                throw new IllegalStateException("Ethereum RPC returned an error: "
                        + ethSendTransaction.getError().getMessage());
            }

            String txHash = ethSendTransaction.getTransactionHash();
            if (txHash == null || txHash.isBlank()) {
                throw new IllegalStateException("No transaction hash returned by Ethereum node");
            }
            return txHash;
        } catch (IOException | IllegalStateException ex) {
            throw new IllegalStateException("Failed to send transaction to Ethereum node", ex);
        }
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize payload for Ethereum transaction", ex);
        }
    }
}
