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
    public String persistAnchorRecord(AnchorRecord anchorRecord) {
        String payload = toJson(anchorRecord);
        String txHash = publishAsTransaction(payload);
        log.info("AnchorRecord persisted in Ethereum tx={}", txHash);
        return txHash;
    }

    @Override
    public AnchorRecord loadAnchorRecord(String txId) {
        if (txId == null || txId.isBlank()) {
            throw new IllegalArgumentException("txId must not be blank");
        }

        try {
            var response = web3j.ethGetTransactionByHash(txId).send();
            if (response.hasError()) {
                throw new IllegalStateException("Ethereum RPC returned an error while fetching tx: "
                        + response.getError().getMessage());
            }

            var txOptional = response.getTransaction();
            if (txOptional == null || txOptional.isEmpty()) {
                throw new IllegalArgumentException("Transaction not found in blockchain: " + txId);
            }

            var tx = txOptional.get();
            var input = tx.getInput();
            if (input == null) {
                throw new IllegalStateException("Transaction input is null for tx=" + txId);
            }

            var payloadJson = fromHexInputToString(input);
            return objectMapper.readValue(payloadJson, AnchorRecord.class);
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load AnchorRecord from Ethereum tx=" + txId, ex);
        }
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

    private static String fromHexInputToString(String input) {
        var normalized = input;
        if (normalized.startsWith("0x")) {
            normalized = normalized.substring(2);
        }
        if (normalized.isBlank()) {
            return "";
        }
        var bytes = Numeric.hexStringToByteArray("0x" + normalized);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
