package edu.yandex.project.repository.util;

import edu.yandex.project.domain.CartItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.mapping.OutboundRow;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.lang.NonNull;
import org.springframework.r2dbc.core.Parameter;

@WritingConverter
@Slf4j
public class CartItemWriteConverter implements Converter<CartItem, OutboundRow> {

    @Override
    @SuppressWarnings("deprecation")
    public OutboundRow convert(@NonNull CartItem source) {
        log.debug("CartItemWriteConverter::convert {} in", source);
        var sourceId = source.getId();
        var row = new OutboundRow()
                .append(SqlIdentifier.unquoted("cart_id"), Parameter.from(sourceId.cartId()))
                .append(SqlIdentifier.unquoted("item_id"), Parameter.from(sourceId.itemId()))
                .append(SqlIdentifier.unquoted("total_cost"), Parameter.from(source.getTotalCost()))
                .append(SqlIdentifier.unquoted("items_count"), Parameter.from(source.getItemCount()))
                .append(SqlIdentifier.unquoted("created_at"), Parameter.from(source.getCreatedAt()));
        log.debug("CartItemWriteConverter::convert {} out. Result: {}", source, row);
        return row;
    }
}
