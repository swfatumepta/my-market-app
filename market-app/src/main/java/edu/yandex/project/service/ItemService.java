package edu.yandex.project.service;

import edu.yandex.project.controller.dto.ItemListPageView;
import edu.yandex.project.controller.dto.ItemView;
import edu.yandex.project.controller.dto.ItemsPageableRequest;
import edu.yandex.project.domain.Item;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface ItemService {

    Mono<ItemView> findOneAsView(@NonNull Long itemId);

    Mono<ItemListPageView> findAllAsView(@NonNull ItemsPageableRequest pageableRequest);

    Mono<Item> findOne(@NonNull Long itemId);

    Flux<Item> findAll(@NonNull Collection<Long> itemIds);
}
