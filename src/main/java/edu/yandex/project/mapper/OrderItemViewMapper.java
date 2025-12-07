package edu.yandex.project.mapper;

import edu.yandex.project.controller.dto.OrderItemView;
import edu.yandex.project.entity.OrderItemEntity;
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
    OrderItemView from(OrderItemEntity source);

    List<OrderItemView> from(Collection<OrderItemEntity> source);
}
