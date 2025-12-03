package edu.yandex.project.controller.dto;

public record ItemDto(Long id,
                      String title,
                      String description,
                      String imgPath,
                      Long price,
                      Integer count) {
}
