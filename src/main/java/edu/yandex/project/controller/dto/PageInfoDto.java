package edu.yandex.project.controller.dto;

import org.springframework.data.domain.Page;

public record PageInfoDto(int pageSize,
                          int pageNumber,
                          boolean hasPrevious,
                          boolean hasNext) {

    public static PageInfoDto from(Page<?> page) {
        return new PageInfoDto(
                page.getSize(),
                page.getNumber(),
                page.hasPrevious(),
                page.hasNext()
        );
    }
}
