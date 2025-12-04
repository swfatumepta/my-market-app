package edu.yandex.project.controller.dto;

import edu.yandex.project.controller.dto.enums.ItemSort;
import lombok.Builder;

import java.util.List;

@Builder
public record ItemsPageDto(String search,
                           ItemSort sort,
                           PageInfoDto pageInfoDto,
                           List<List<ItemDto>> items) {
}
