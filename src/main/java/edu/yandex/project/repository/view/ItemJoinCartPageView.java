package edu.yandex.project.repository.view;

public record ItemJoinCartPageView(Long id,
                                   String title,
                                   String description,
                                   String imgPath,
                                   Long price,
                                   Long inCartCount) {
}
