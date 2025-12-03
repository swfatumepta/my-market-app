package edu.yandex.project.service;

import edu.yandex.project.controller.dto.ItemDto;

public interface ItemService {

    ItemDto findOne(long itemId);
}
