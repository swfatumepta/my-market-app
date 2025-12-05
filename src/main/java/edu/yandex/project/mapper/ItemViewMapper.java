package edu.yandex.project.mapper;

import edu.yandex.project.controller.dto.ItemView;
import edu.yandex.project.repository.view.ItemJoinCartPageView;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ItemViewMapper {

    @Mapping(target = "count", source = "inCartCount")
    ItemView fromItemJoinCartView(ItemJoinCartPageView source);

    List<ItemView> fromItemJoinCartViews(Collection<ItemJoinCartPageView> source);
}
