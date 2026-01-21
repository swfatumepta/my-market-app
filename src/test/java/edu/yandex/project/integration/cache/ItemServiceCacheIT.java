package edu.yandex.project.integration.cache;

import edu.yandex.project.controller.dto.*;
import edu.yandex.project.domain.CartItem;
import edu.yandex.project.domain.Item;
import edu.yandex.project.repository.util.view.ItemJoinCartPageView;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Tag("ItemServiceCacheIT")
public class ItemServiceCacheIT extends AbstractCacheIT {
    private final static String SHOWCASE_CACHE_NAME = "showcase";
    private final static String ITEM_VIEW_CACHE_NAME = "itemView";

    @SuppressWarnings("unchecked")
    @Test
    void findAll_shouldSaveMethodResultToCache() {
        // given
        var pageableRequest = new ItemsPageableRequest(null, null, null, null);

        var dbResponse = new PageImpl<>(List.of(new ItemJoinCartPageView(null, null, null, null, null, null)));
        var shouldBeCached = ItemListPageView.builder()
                .items(List.of(List.of(getItemView())))
                .search(pageableRequest.search())
                .sort(pageableRequest.sort())
                .paging(new PageInfo(1, 1, false, true))
                .build();
        // when
        when(itemPageableRepository.findAllWithCartCount(any(), any(), any())).thenReturn(Mono.just(dbResponse));
        when(itemListPageViewFactory.create(any(Tuple2.class))).thenReturn(shouldBeCached);

        this.assertThatCacheIsEmpty(SHOWCASE_CACHE_NAME);
        var itemsFromCache = itemService.findAll(pageableRequest).block();   // must be added to cache on first call
        // then
        itemsFromCache = itemService.findAll(pageableRequest).block();   // must be extracted from cache

        verify(itemPageableRepository, times(1)).findAllWithCartCount(
                eq(pageableRequest.search()),
                eq(pageableRequest.sort().name()),
                eq(PageRequest.of(pageableRequest.pageNumber(), pageableRequest.pageSize()))
        );
        verify(itemListPageViewFactory, times(1)).create(any(Tuple2.class));

        assertThat(itemsFromCache).isNotNull();
        assertThat(itemsFromCache.items()).isEqualTo(shouldBeCached.items());
        assertThat(itemsFromCache.paging()).isEqualTo(shouldBeCached.paging());
        assertThat(itemsFromCache.search()).isEqualTo(pageableRequest.search());
        assertThat(itemsFromCache.sort()).isEqualTo(pageableRequest.sort());
    }

    @Test
    void findOne_shouldSaveMethodResultToCache() {
        // given
        var requestedItemId = 1L;
        var itemFromDb = Item.builder().id(requestedItemId).build();
        // when
        when(itemRepository.findById(requestedItemId)).thenReturn(Mono.just(itemFromDb));
        when(cartItemRepository.findCartItemByItemId(requestedItemId)).thenReturn(Mono.just(
                CartItem.builder().itemCount(2L).build()
        ));
        when(itemViewMapper.fromTuple(any())).thenReturn(getItemView());

        this.assertThatCacheIsEmpty(ITEM_VIEW_CACHE_NAME);
        var fromCache = itemService.findOne(requestedItemId).block();   // must be added to cache on first call
        // then
        fromCache = itemService.findOne(requestedItemId).block();   // must be extracted from cache

        verify(itemRepository, times(1)).findById(requestedItemId);
        verify(cartItemRepository, times(1)).findCartItemByItemId(requestedItemId);
        verify(itemViewMapper, times(1)).fromTuple(any());

        assertThat(fromCache).isNotNull();
        assertThat(fromCache).isEqualTo(getItemView());
    }

    private static ItemView getItemView() {
        return ItemView.builder()
                .id(1L)
                .price(5000L)
                .title("item")
                .description("test item")
                .imgPath("/root")
                .count(1L)
                .build();
    }
}
