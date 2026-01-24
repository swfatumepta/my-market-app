package edu.yandex.project.controller.dto;

import edu.yandex.project.controller.dto.enums.CartAction;
import jakarta.validation.constraints.NotNull;
import lombok.experimental.FieldNameConstants;

@FieldNameConstants
public record CartActionRequest(@NotNull CartAction action) {
}
