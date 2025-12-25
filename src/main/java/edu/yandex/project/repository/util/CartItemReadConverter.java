package edu.yandex.project.repository.util;

import edu.yandex.project.domain.CartItem;
import io.r2dbc.spi.Row;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.lang.NonNull;

import java.time.Instant;

@ReadingConverter
@Slf4j
public class CartItemReadConverter implements Converter<Row, CartItem> {

    @Override
    public CartItem convert(@NonNull Row source) {
        log.debug("CartItemReadConverter::convert {} in", source);
        var id = new CartItem.CartItemCompositeId(
                source.get("cart_id", Long.class),
                source.get("item_id", Long.class)
        );
        var cartItem = CartItem.builder()
                .id(id)
                .totalCost(source.get("total_cost", Long.class))
                .itemCount(source.get("items_count", Long.class))
                .createdAt(source.get("created_at", Instant.class))
                .build();
        log.debug("CartItemReadConverter::convert {} out. Result: {}", source, cartItem);
        return cartItem;
    }
}
