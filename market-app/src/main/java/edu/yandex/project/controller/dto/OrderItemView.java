package edu.yandex.project.controller.dto;

public record OrderItemView(Long id,
                            String title,
                            Long price,
                            Long count,
                            Long subtotal) {
}
