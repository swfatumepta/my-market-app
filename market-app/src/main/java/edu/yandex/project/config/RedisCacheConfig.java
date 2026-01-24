package edu.yandex.project.config;

import edu.yandex.project.controller.dto.CartView;
import edu.yandex.project.controller.dto.ItemListPageView;
import edu.yandex.project.controller.dto.ItemView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Value("${spring.cache.redis.time-to-live:PT2M}")
    private Duration defaultTTL;

    @Bean
    public RedisCacheManagerBuilderCustomizer showcaseCacheCustomizer() {
        return builder -> builder.withCacheConfiguration(
                "showcase",
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(defaultTTL)
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                                new Jackson2JsonRedisSerializer<>(ItemListPageView.class)
                        ))
        );
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer itemViewCacheCustomizer() {
        return builder -> builder.withCacheConfiguration(
                "itemView",
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(defaultTTL)
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                                new Jackson2JsonRedisSerializer<>(ItemView.class)
                        ))
        );
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer cartViewCacheCustomizer() {
        return builder -> builder.withCacheConfiguration(
                "cartView",
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(defaultTTL)
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                                new Jackson2JsonRedisSerializer<>(CartView.class)
                        ))
        );
    }
}
