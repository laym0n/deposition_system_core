package com.deposition.infra.ethereum.config;

import java.math.BigInteger;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@ConfigurationProperties(prefix = "integration.ethereum")
@Data
@NoArgsConstructor
public class EthereumProperties {

    private String rpcUrl;
    private BigInteger gasLimitInteger;
    private Long chainId;
    private String fromAddress;
    private BigInteger gasPriceWei;
}
