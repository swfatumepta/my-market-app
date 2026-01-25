package edu.yandex.project.config;

import edu.yandex.project.ApiClient;
import edu.yandex.project.client.WalletApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WalletWebClientConfig {

    @Value("${integration.wallet.url}")
    private String walletUrl;

    @Bean
    public WalletApi walletApi() {
        var apiClient = new ApiClient();
        apiClient.setBasePath(walletUrl);
        return new WalletApi(apiClient);
    }
}
