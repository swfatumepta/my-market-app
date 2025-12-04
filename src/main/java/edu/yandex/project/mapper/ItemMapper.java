package edu.yandex.project.mapper;

import edu.yandex.project.controller.dto.ItemDto;
import edu.yandex.project.entity.ItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ItemMapper {

    ItemDto from(ItemEntity source);

    List<ItemDto> from(Collection<ItemEntity> source);
}
