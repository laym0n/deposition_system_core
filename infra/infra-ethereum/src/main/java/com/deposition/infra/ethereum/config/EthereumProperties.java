package com.deposition.infra.ethereum.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigInteger;

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
