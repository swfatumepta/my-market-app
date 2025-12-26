package edu.yandex.project.mapper;

import edu.yandex.project.controller.dto.ItemView;
import edu.yandex.project.domain.Item;
import edu.yandex.project.repository.util.view.ItemJoinCartPageView;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import reactor.util.function.Tuple2;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ItemViewMapper {

    @Mapping(target = "count", source = "count")
    ItemView fromItemWithCount(Item mainSource, Long count);

    @Mapping(target = "count", source = "inCartCount")
    ItemView fromItemJoinCartView(ItemJoinCartPageView source);

    List<ItemView> fromItemJoinCartViews(Collection<ItemJoinCartPageView> source);

    default ItemView fromTuple(Tuple2<Item, Long> itemWithCount) {
        return this.fromItemWithCount(itemWithCount.getT1(), itemWithCount.getT2());
    }
}
