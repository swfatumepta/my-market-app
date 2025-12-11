package edu.yandex.project.service;

import edu.yandex.project.controller.dto.ItemView;
import edu.yandex.project.controller.dto.ItemListPageView;
import edu.yandex.project.controller.dto.ItemsPageableRequest;
import org.springframework.lang.NonNull;

public interface ItemService {

    ItemListPageView findAll(@NonNull ItemsPageableRequest pageableRequest);

    ItemView findOne(long itemId);
}
