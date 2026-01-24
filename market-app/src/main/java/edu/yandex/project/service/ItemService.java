package edu.yandex.project.service;

import edu.yandex.project.controller.dto.ItemListPageView;
import edu.yandex.project.controller.dto.ItemView;
import edu.yandex.project.controller.dto.ItemsPageableRequest;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;

public interface ItemService {

    Mono<ItemListPageView> findAll(@NonNull ItemsPageableRequest pageableRequest);

    Mono<ItemView> findOne(@NonNull Long itemId);
}
