package edu.yandex.project.controller.dto;

import edu.yandex.project.controller.dto.enums.ItemSort;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.experimental.FieldNameConstants;

@Builder
@FieldNameConstants
public record ItemsPageableRequest(String search,

                                   @Min(value = 0, message = "must be >= 0")
                                   Integer pageNumber,

                                   @Min(value = 1, message = "must be > 0")
                                   @Max(value = 100, message = "must be <= 100")
                                   Integer pageSize,

                                   ItemSort sort) {

    public ItemsPageableRequest {
        if (search == null) search = "";
        if (pageNumber == null) pageNumber = 0;
        if (pageSize == null) pageSize = 5;
        if (sort == null) sort = ItemSort.NO;
    }
}
