package edu.yandex.project.controller.dto;

import org.springframework.data.domain.Page;

public record PageInfo(int pageSize,
                       int pageNumber,
                       boolean hasPrevious,
                       boolean hasNext) {

    public static PageInfo from(Page<?> page) {
        return new PageInfo(
                page.getSize(),
                page.getNumber(),
                page.hasPrevious(),
                page.hasNext()
        );
    }
}
