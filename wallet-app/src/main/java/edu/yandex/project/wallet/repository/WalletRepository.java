package edu.yandex.project.wallet.repository;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class WalletRepository {

    @Value("${wallet.key}")
    private String defaultWalletKey;
    @Value("${wallet.amount}")
    private Long defaultWalletAmount;

    private final Map<String, Long> storage = new ConcurrentHashMap<>();

    @PostConstruct
    protected void init() {
        storage.put(defaultWalletKey, defaultWalletAmount);
    }

    public Long get() {
        return storage.get(defaultWalletKey);
    }

    public Long update(@NonNull Long balance) {
        storage.put(defaultWalletKey, balance);
        return balance;
    }
}
