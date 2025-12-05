package edu.yandex.project.controller.dto;

import edu.yandex.project.controller.dto.enums.ItemSort;
import lombok.Builder;

import java.util.List;

@Builder
public record ItemListPageView(String search,
                               ItemSort sort,
                               PageInfo pageInfo,
                               List<List<ItemView>> items) {
}
