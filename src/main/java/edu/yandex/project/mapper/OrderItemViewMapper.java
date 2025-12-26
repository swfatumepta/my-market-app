package edu.yandex.project.mapper;

import edu.yandex.project.controller.dto.OrderItemView;
import edu.yandex.project.domain.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderItemViewMapper {

    @Mapping(target = "title", source = "itemTitle")
    @Mapping(target = "price", source = "itemPriceAtOrderTime")
    @Mapping(target = "count", source = "itemCount")
    OrderItemView from(OrderItem source);

    List<OrderItemView> from(Collection<OrderItem> source);
}
