package edu.yandex.project.controller.dto;

import edu.yandex.project.controller.dto.enums.ItemSort;

public record ItemsPageableRequest(String search,
                                   Integer pageNumber,
                                   Integer pageSize,
                                   ItemSort sort) {

    public ItemsPageableRequest {
        if (search == null) search = "";
        if (pageNumber == null) pageNumber = 0;
        if (pageSize == null) pageSize = 5;
        if (sort == null) sort = ItemSort.NO;
    }
}
