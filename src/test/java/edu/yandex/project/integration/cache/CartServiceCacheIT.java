package edu.yandex.project.integration.cache;

import edu.yandex.project.controller.dto.CartItemAction;
import edu.yandex.project.controller.dto.ItemView;
import edu.yandex.project.controller.dto.enums.CartAction;
import edu.yandex.project.domain.Cart;
import edu.yandex.project.domain.CartItem;
import edu.yandex.project.domain.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

@Tag("CartServiceCacheIT")
public class CartServiceCacheIT extends AbstractCacheIT {
    private final static String CACHE_NAME = "cartView";
    private final static CartItem.CartItemCompositeId CART_ITEM_ID = new CartItem.CartItemCompositeId(777L, 1L);

    @BeforeEach
    void initStubs() {
        when(cartRepository.findAll()).thenReturn(Flux.just(new Cart(CART_ITEM_ID.cartId(), Instant.now())));
    }

    @Test
    void getCartContent_shouldSaveMethodResultToCache() {
        // given
        var expectedCachedData = getTestItemView();
        // when
        this.initStubsForGetCartContent(expectedCachedData);

        this.assertThatCacheIsEmpty(CACHE_NAME);
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

    @Test
    void updateCart_shouldDropCache() {
        // prepare test env begins
        var expectedCachedData = getTestItemView();
        this.initStubsForGetCartContent(expectedCachedData);

        this.assertThatCacheIsEmpty(CACHE_NAME);
        cartService.getCartContent().block();   // add data to the cache
        // prepare test env ends
        // given
        var updateCartAction = new CartItemAction(CartAction.DELETE, expectedCachedData.id());
        var cartItemToBeDeleted = CartItem.builder().id(CART_ITEM_ID).itemCount(1L).build();
        // when
        when(itemRepository.findById(updateCartAction.itemId())).thenReturn(Mono.just(
                Item.builder().id(expectedCachedData.id()).build()
        ));
        when(cartItemRepository.findById(eq(CART_ITEM_ID))).thenReturn(Mono.just(cartItemToBeDeleted));
        when(cartItemRepository.delete(cartItemToBeDeleted)).thenReturn(Mono.empty());

        cartService.updateCart(updateCartAction).block();   // cache data must be evicted
        // then
        cartService.getCartContent().block();   // must updated cache (should do request to the repository)

        // 3? => 1st call while env preparations, 2nd in start of cartService::getCartContent(), 3rd after eviction
        verify(cartRepository, times(3)).findAll();
        // 2? => 1st call while env preparations, 2nd after eviction
        verify(cartItemRepository, times(2)).findAllByCartId(any());
        verify(itemRepository, times(2)).findAllById(anyCollection());
        verify(itemViewMapper, times(2)).fromItemWithCount(any(), any());
    }

    @Test
    void deleteCart_shouldDropCache() {
        // given
        var expectedCachedData = getTestItemView();
        this.initStubsForGetCartContent(expectedCachedData);
        // cache warming ---
        for (int i = 0; i < 5; i++) {
            cartService.getCartContent().block();
        }
        // --- cache warming
        // when
        when(cartRepository.deleteAll()).thenReturn(Mono.empty());

        cartService.deleteCart().block();   // cache evict
        // then
        cartService.getCartContent().block();   // cache MUST BE empty => data must be requested from db
        // 2? => 1st call while warming cache, 2nd call after cartService::deleteCart
        verify(cartRepository, times(2)).findAll();
        verify(cartItemRepository, times(2)).findAllByCartId(any());
        verify(itemRepository, times(2)).findAllById(anyCollection());
    }

    private void initStubsForGetCartContent(ItemView expectedCachedData) {
        when(cartItemRepository.findAllByCartId(any())).thenReturn(Flux.just(
                CartItem.builder().id(CART_ITEM_ID).itemCount(1L).build()
        ));
        when(itemRepository.findAllById(anyCollection())).thenReturn(Flux.just(
                Item.builder().id(CART_ITEM_ID.itemId()).build()
        ));
        when(itemViewMapper.fromItemWithCount(any(), any())).thenReturn(expectedCachedData);
    }

    private static ItemView getTestItemView() {
        return ItemView.builder()
                .id(CART_ITEM_ID.itemId())
                .title("title")
                .description("description")
                .imgPath("/path")
                .price(6666L)
                .count(2L)
                .build();
    }
}
