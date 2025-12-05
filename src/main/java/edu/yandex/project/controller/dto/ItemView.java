package edu.yandex.project.controller.dto;

public record ItemView(Long id,
                       String title,
                       String description,
                       String imgPath,
                       Long price,
                       Long count) {

    public static ItemView createStub() {
        return new ItemView(-1L, null, null, null, null, null);
    }
}
