package edu.yandex.project.integration.cache;

import edu.yandex.project.controller.dto.ItemView;
import edu.yandex.project.domain.Cart;
import edu.yandex.project.domain.CartItem;
import edu.yandex.project.domain.Item;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

@Tag("CartServiceCacheIT")
public class CartServiceCacheIT extends AbstractCacheIT {

    @Test
    void getCartContent_shouldSaveMethodResultToCache() {
        // given
        var cacheName = "cartView";

        var cartItemId = new CartItem.CartItemCompositeId(777L, 1L);
        var expectedCachedData = ItemView.builder()
                .id(cartItemId.cartId())
                .title("title")
                .description("description")
                .imgPath("/path")
                .price(6666L)
                .count(2L)
                .build();
        // when
        when(cartRepository.findAll()).thenReturn(Flux.just(new Cart()));
        when(cartItemRepository.findAllByCartId(any())).thenReturn(Flux.just(
                CartItem.builder()
                        .id(cartItemId)
                        .itemCount(1L)
                        .build()
        ));
        when(itemRepository.findAllById(anyCollection())).thenReturn(Flux.just(
                Item.builder().id(cartItemId.itemId()).build()
        ));
        when(itemViewMapper.fromItemWithCount(any(), any())).thenReturn(expectedCachedData);

        this.assertThatCacheIsEmpty(cacheName);
        var cartViewFromCache = cartService.getCartContent().block();   // must be added to cache on first call
        // then
        cartViewFromCache = cartService.getCartContent().block();   // must be extracted from cache

        verify(cartRepository, times(1)).findAll();
        verify(cartItemRepository, times(1)).findAllByCartId(any());
        verify(itemRepository, times(1)).findAllById(anyCollection());
        verify(itemViewMapper, times(1)).fromItemWithCount(any(), any());

        assertThat(cartViewFromCache).isNotNull();
        assertThat(cartViewFromCache.total()).isEqualTo(expectedCachedData.count() * expectedCachedData.price());
        assertThat(cartViewFromCache.items()).isEqualTo(List.of(expectedCachedData));
    }
}
