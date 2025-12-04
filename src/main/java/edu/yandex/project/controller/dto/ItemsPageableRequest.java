package edu.yandex.project.controller.dto;

public record ItemsPageableRequest(String search,
                                   Integer pageNumber,
                                   Integer pageSize,
                                   CustomSort sort) {

    public ItemsPageableRequest {
        if (search == null) search = "";
        if (pageNumber == null) pageNumber = 1;
        if (pageSize == null) pageSize = 5;
        if (sort == null) sort = CustomSort.NO;
    }

    enum CustomSort {
        NO, ALPHA, PRICE
    }
}
