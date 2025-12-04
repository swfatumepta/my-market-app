package edu.yandex.project.controller.dto;

public record ItemDto(Long id,
                      String title,
                      String description,
                      String imgPath,
                      Long price,
                      Integer count) {

    public static ItemDto createStub() {
        return new ItemDto(-1L, null, null, null, null, null);
    }
}
