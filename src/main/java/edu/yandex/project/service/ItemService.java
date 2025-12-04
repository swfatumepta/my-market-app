package edu.yandex.project.service;

import edu.yandex.project.controller.dto.ItemDto;
import edu.yandex.project.controller.dto.ItemsPageDto;
import edu.yandex.project.controller.dto.ItemsPageableRequestDto;
import org.springframework.lang.NonNull;

public interface ItemService {

    ItemsPageDto findAll(@NonNull ItemsPageableRequestDto pageableRequest);

    ItemDto findOne(long itemId);
}
