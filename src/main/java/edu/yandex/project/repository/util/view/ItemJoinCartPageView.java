package edu.yandex.project.repository.util.view;

import lombok.Builder;
import lombok.experimental.FieldNameConstants;

@Builder
@FieldNameConstants
public record ItemJoinCartPageView(Long id,
                                   String title,
                                   String description,
                                   String imgPath,
                                   Long price,
                                   Long inCartCount) {
}
