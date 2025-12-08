package edu.yandex.project.integration.controller;

import edu.yandex.project.controller.dto.CartItemAction;
import edu.yandex.project.controller.dto.CartView;
import edu.yandex.project.controller.dto.ItemView;
import edu.yandex.project.controller.dto.enums.CartAction;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.ui.ModelMap;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("ItemWebControllerIT")
public class CartWebControllerIT extends AbstractControllerIT {
    private final static String CART_ROOT = "/cart/items";

    @Test
    void getEmptyCart_thenFillItWithItems_success() {
        // GET EMPTY CART
        var cartModelMap = this.validateAndGetCart();
        assertThat(cartModelMap.get(CartView.Fields.total)).isEqualTo(0L);
        assertThat((Collection<?>) cartModelMap.get(CartView.Fields.items)).isEmpty();
        // THEN ADD 3 DIFFERENT ITEMS TO THE CART (cart must be created automatically)
        this.updateCartAndValidateResponse(CartAction.PLUS, 1L);
        this.updateCartAndValidateResponse(CartAction.PLUS, 2L);
        this.updateCartAndValidateResponse(CartAction.PLUS, 3L);
        // THEN ADD 3 IDENTICAL ITEMS TO THE CART
        this.updateCartAndValidateResponse(CartAction.PLUS, 4L);
        this.updateCartAndValidateResponse(CartAction.PLUS, 4L);
        this.updateCartAndValidateResponse(CartAction.PLUS, 4L);
        // THEN CHECK IF ITEMS BEEN ADDED TO THE CART
        cartModelMap = this.validateAndGetCart();
        var itemViews = (Collection<?>) cartModelMap.get(CartView.Fields.items);
        assertThat(itemViews).hasSize(4);

        var totalPrice = new AtomicLong(0L);
        itemViews.forEach(item -> {
            assertThat(item).isInstanceOf(ItemView.class);
            var itemView = (ItemView) item;
            assertThat(itemView.id()).isBetween(1L, 4L);
            assertThat(itemView.title()).isNotEmpty();
            assertThat(itemView.description()).isNotEmpty();
            assertThat(itemView.imgPath()).isNotEmpty();
            assertThat(itemView.count()).isGreaterThan(0);
            if (itemView.id() == 4) {
                assertThat(itemView.title()).isEqualTo("Беспроводная компьютерная мышь");
                assertThat(itemView.description()).isEqualTo("Эргономичная беспроводная мышь с Bluetooth 5.0, подсветкой RGB и длительным временем работы от батареи");
                assertThat(itemView.imgPath()).isEqualTo("/images/mouse.jpeg");
                assertThat(itemView.price()).isEqualTo(4000);
                assertThat(itemView.count()).isEqualTo(3);
                totalPrice.getAndUpdate(current -> current + itemView.price() * itemView.count());
            } else {
                assertThat(totalPrice.addAndGet(itemView.price())).isGreaterThan(0);
            }
        });
        assertThat(cartModelMap.get(CartView.Fields.total)).isEqualTo(totalPrice.get());
    }

    @Test
    void removeOneItemFromCart_inCaseItemCountIs1_andTheItemIsNotTheOnlyOneInTheCart_success() {
        // ADD 2 DIFFERENT ITEMS TO THE CART (cart must be created automatically)
        this.updateCartAndValidateResponse(CartAction.PLUS, 5L);
        this.updateCartAndValidateResponse(CartAction.PLUS, 6L);
        // THEN CHECK IF ITEMS BEEN ADDED TO THE CART
        var cartModelMap = this.validateAndGetCart();
        var itemViews = (Collection<?>) cartModelMap.get(CartView.Fields.items);
        assertThat(itemViews).hasSize(2);
        // CHECK ITEMS BEFORE REMOVAL
        var totalPrice = new AtomicLong(0L);
        itemViews.forEach(item -> {
            assertThat(item).isInstanceOf(ItemView.class);
            var itemView = (ItemView) item;
            assertThat(itemView.id()).isBetween(5L, 6L);
            assertThat(itemView.title()).isNotEmpty();
            assertThat(itemView.description()).isNotEmpty();
            assertThat(itemView.imgPath()).isNotEmpty();
            assertThat(itemView.count()).isGreaterThan(0);
            assertThat(totalPrice.addAndGet(itemView.price())).isGreaterThan(0);
        });
        assertThat(cartModelMap.get(CartView.Fields.total)).isEqualTo(totalPrice.get());
        // THEN REMOVE ITEM WITH ID = 5L
        this.updateCartAndValidateResponse(CartAction.MINUS, 5L);
        // CHECK ITEMS AFTER REMOVAL
        cartModelMap = this.validateAndGetCart();
        itemViews = (Collection<?>) cartModelMap.get(CartView.Fields.items);
        assertThat(itemViews).hasSize(1);

        var itemView = (ItemView) itemViews.stream().findFirst().orElseThrow();
        assertThat(itemView.title()).isEqualTo("Плюшевый заяц");
        assertThat(itemView.description()).isEqualTo("Мягкая плюшевая игрушка ручной работы, высотой 40 см, изготовлена из гипоаллергенного материала");
        assertThat(itemView.imgPath()).isEqualTo("/images/rabbit.jpeg");
        assertThat(itemView.price()).isEqualTo(1800);
        assertThat(itemView.count()).isEqualTo(1);
        assertThat(cartModelMap.get(CartView.Fields.total)).isEqualTo(itemView.price());
    }

    @Test
    void removeOneItemFromCart_inCaseItemCountIs2_andCartDoNotContainAnotherItems_success() {
        // ADD 2 IDENTICAL ITEMS TO THE CART (cart must be created automatically)
        this.updateCartAndValidateResponse(CartAction.PLUS, 6L);
        this.updateCartAndValidateResponse(CartAction.PLUS, 6L);
        // THEN CHECK IF ITEMS BEEN ADDED TO THE CART
        var cartModelMap = this.validateAndGetCart();
        var itemViews = (Collection<?>) cartModelMap.get(CartView.Fields.items);
        assertThat(itemViews).hasSize(1);
        // CHECK ITEMS BEFORE REMOVAL
        var totalPrice = new AtomicLong(0L);
        itemViews.forEach(item -> {
                    assertThat(item).isInstanceOf(ItemView.class);
                    var itemView = (ItemView) item;
                    assertThat(itemView.id()).isEqualTo(6L);
                    assertThat(itemView.price()).isEqualTo(1800);
                    assertThat(itemView.count()).isEqualTo(2);
                    totalPrice.getAndUpdate(current -> current + itemView.price() * itemView.count());
                }
        );
        assertThat(cartModelMap.get(CartView.Fields.total)).isEqualTo(totalPrice.get());
        // THEN REMOVE ONE ITEM
        this.updateCartAndValidateResponse(CartAction.MINUS, 6L);
        // CHECK ITEMS AFTER REMOVAL
        cartModelMap = this.validateAndGetCart();
        itemViews = (Collection<?>) cartModelMap.get(CartView.Fields.items);
        assertThat(itemViews).hasSize(1);

        var itemView = (ItemView) itemViews.stream().findFirst().orElseThrow();
        assertThat(itemView.id()).isEqualTo(6L);
        assertThat(itemView.price()).isEqualTo(1800);
        assertThat(itemView.count()).isEqualTo(1);
        assertThat(cartModelMap.get(CartView.Fields.total)).isEqualTo(itemView.price());
    }

    @Test
    void deleteItemsFromCart_inCaseItemCountIs2_success() {
        // ADD 2 IDENTICAL ITEMS TO THE CART (cart must be created automatically)
        this.updateCartAndValidateResponse(CartAction.PLUS, 10L);
        this.updateCartAndValidateResponse(CartAction.PLUS, 10L);
        // THEN CHECK IF ITEMS BEEN ADDED TO THE CART
        var cartModelMap = this.validateAndGetCart();
        var itemViews = (Collection<?>) cartModelMap.get(CartView.Fields.items);
        assertThat(itemViews).hasSize(1);
        // CHECK ITEMS BEFORE REMOVAL
        var totalPrice = new AtomicLong(0L);
        itemViews.forEach(item -> {
                    assertThat(item).isInstanceOf(ItemView.class);
                    var itemView = (ItemView) item;
                    assertThat(itemView.id()).isEqualTo(10L);
                    assertThat(itemView.price()).isEqualTo(4200);
                    assertThat(itemView.count()).isEqualTo(2);
                    totalPrice.getAndUpdate(current -> current + itemView.price() * itemView.count());
                }
        );
        assertThat(cartModelMap.get(CartView.Fields.total)).isEqualTo(totalPrice.get());
        // THEN DELETE ALL ITEMS WITH ONE ACTION
        this.updateCartAndValidateResponse(CartAction.DELETE, 10L);
        // CHECK ITEMS AFTER REMOVAL
        cartModelMap = this.validateAndGetCart();
        assertThat(cartModelMap.get(CartView.Fields.total)).isEqualTo(0L);
        assertThat((Collection<?>) cartModelMap.get(CartView.Fields.items)).isEmpty();
    }

    @SneakyThrows
    private ModelMap validateAndGetCart() {
        // when
        var response = mockMvc.perform(get(CART_ROOT))
                // then
                .andExpect(view().name("cart"))
                .andExpect(model().attributeExists(CartView.Fields.items, CartView.Fields.total))
                .andReturn()
                .getModelAndView();

        assertThat(response).isNotNull();
        return response.getModelMap();
    }

    @SneakyThrows
    private void updateCartAndValidateResponse(CartAction cartAction, Long itemId) {
        // when
        mockMvc.perform(post(CART_ROOT)
                        .param(CartItemAction.Fields.action, cartAction.toString())
                        .param("id", itemId.toString())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                // then
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart/items"));
    }
}
