package edu.yandex.project.mapper;

import edu.yandex.project.controller.dto.ItemView;
import edu.yandex.project.domain.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ItemViewMapper {

    @Mapping(target = "count", source = "count")
    ItemView fromItemWithCount(Item mainSource, long count);
}
