package edu.yandex.project.config;

import edu.yandex.project.repository.util.CartItemReadConverter;
import edu.yandex.project.repository.util.CartItemWriteConverter;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.lang.NonNull;

import java.util.List;

@Configuration
public class R2dbcConfiguration extends AbstractR2dbcConfiguration {

    @Value("${spring.r2dbc.url}")
    private String r2dbcUrl;

    @Override
    public @NonNull ConnectionFactory connectionFactory() {
        return ConnectionFactories.get(r2dbcUrl);
    }

    @Override
    protected @NonNull List<Object> getCustomConverters() {
        return List.of(new CartItemReadConverter(), new CartItemWriteConverter());
    }
}
