package edu.yandex.project.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.yandex.project.domain.Item;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, Item> itemReactiveRedisTemplate(ReactiveRedisConnectionFactory connectionFactory,
                                                                         ObjectMapper objectMapper) {
        var redisSerializationContext = RedisSerializationContext.<String, Item>newSerializationContext(new StringRedisSerializer())
                .value(new Jackson2JsonRedisSerializer<>(objectMapper, Item.class))
                .build();
        return new ReactiveRedisTemplate<>(connectionFactory, redisSerializationContext);
    }
}
