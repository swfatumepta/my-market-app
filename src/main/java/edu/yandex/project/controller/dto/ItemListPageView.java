package edu.yandex.project.controller.dto;

import edu.yandex.project.controller.dto.enums.ItemSort;
import lombok.Builder;
import lombok.experimental.FieldNameConstants;

import java.util.List;

@Builder
@FieldNameConstants
public record ItemListPageView(String search,
                               ItemSort sort,
                               PageInfo paging,
                               List<List<ItemView>> items) {
}
