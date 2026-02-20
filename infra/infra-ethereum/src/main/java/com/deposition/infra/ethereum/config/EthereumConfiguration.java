package com.deposition.infra.ethereum.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@Configuration
@EnableConfigurationProperties(EthereumProperties.class)
public class EthereumConfiguration {

    @Bean
    public Web3j web3j(EthereumProperties properties) {
        return Web3j.build(new HttpService(properties.getRpcUrl()));
    }
}
