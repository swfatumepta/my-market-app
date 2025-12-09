package edu.yandex.project.integration.controller;

import edu.yandex.project.controller.dto.CartItemAction;
import edu.yandex.project.controller.dto.OrderItemView;
import edu.yandex.project.controller.dto.OrderView;
import edu.yandex.project.controller.dto.enums.CartAction;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.ui.ModelMap;

import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("OrderWebControllerIT")
public class OrderWebControllerIT extends AbstractControllerIT {
    private final static String ORDERS_ROOT = "/orders";

    @Test
    void getOrders_inCaseNoOrders_success() {
        this.validateAndGetOrders();
    }

    @Sql(scripts = "classpath:/sql/order-prepare-env.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Test
    void placeAnOrder_success() throws Exception {
        // given
        // cart created and filled with items (/sql/order-prepare-env.sql)
        // when
        mockMvc.perform(post(ORDERS_ROOT + "/place-an-order"))
                // then
                .andExpect(view().name("redirect:/orders/{id}"))
                .andExpect(model().attribute("id", "1"))
                .andExpect(model().attribute("newOrder", Boolean.TRUE.toString()))
                .andExpect(model().size(2));
        // validate created order
        var orders = (Collection<?>) this.validateAndGetOrders().getAttribute("orders");
        assertThat(orders).hasSize(1);
        orders.forEach(order -> {
            assertThat(order).isInstanceOf(OrderView.class);
            var orderView = (OrderView) order;
            assertThat(orderView.id()).isEqualTo(1L);
            assertThat(orderView.items()).isNotEmpty();

            var orderItemViews = orderView.items();
            orderItemViews.sort(Comparator.comparing(OrderItemView::id));
            OrderItemView orderItemView;
            Long count, price;
            var totalSum = new AtomicLong(0);
            for (int i = 0; i < orderItemViews.size(); i++) {
                orderItemView = orderItemViews.get(i);
                assertThat(orderItemView.id()).isEqualTo(i + 1);
                assertThat(orderItemView.title()).isNotEmpty();

                count = orderItemView.count();
                price = orderItemView.price();
                final long subtotal = orderItemView.subtotal();
                assertThat(count).isEqualTo(i + 1);
                assertThat(price).isGreaterThan(0L);
                assertThat(subtotal).isEqualTo(count * price);
                totalSum.updateAndGet(current -> current + subtotal);
            }
            assertThat(orderView.totalSum()).isEqualTo(totalSum.get());
        });
        // check if cart removed with cart_item links
        assertThat(cartRepository.findById(CART_ID)).isEmpty();
        assertThat(cartItemRepository.findAllByCartId(CART_ID)).isEmpty();
    }

    @Test
    void getOrders_inCaseOrdersHistoryHasTwoRows_success() {
        // given
        // preparations begins
        // make first purchase
        this.addItemsToTheCart(1L);
        this.addItemsToTheCart(1L);
        this.buy();
        // make second purchase
        this.addItemsToTheCart(2L);
        this.addItemsToTheCart(3L);
        this.buy();
        // preparations ends
        // when
        var orders = (Collection<?>) this.validateAndGetOrders().getAttribute("orders");
        assertThat(orders).hasSize(2);

        orders.forEach(order -> {
            assertThat(order).isInstanceOf(OrderView.class);
            var orderView = (OrderView) order;
            if (orderView.id() == 1) {
                assertThat(orderView.items().size()).isEqualTo(1);
                var orderItemView = orderView.items().getFirst();
                assertThat(orderItemView.id()).isEqualTo(1L);
                assertThat(orderItemView.count()).isEqualTo(2);
                assertThat(orderView.totalSum()).isEqualTo(orderItemView.subtotal());
            } else {
                assertThat(orderView.items().size()).isEqualTo(2);
                var orderItemView = orderView.items().getFirst();
                var firstItemSubtotal = orderItemView.subtotal();
                assertThat(orderItemView.id()).isEqualTo(2L);
                assertThat(orderItemView.count()).isEqualTo(1);

                orderItemView = orderView.items().getLast();
                var secondItemSubtotal = orderItemView.subtotal();
                assertThat(orderItemView.id()).isEqualTo(3L);
                assertThat(orderItemView.count()).isEqualTo(1);
                assertThat(orderView.totalSum()).isEqualTo(firstItemSubtotal + secondItemSubtotal);
            }
        });
    }

    @Test
    void getOrder_inCaseNewOrderRequestParameterNotSet_success() throws Exception {
        // given
        // preparations begins
        this.addItemsToTheCart(1L);
        this.addItemsToTheCart(1L);
        this.addItemsToTheCart(2L);
        this.addItemsToTheCart(3L);
        this.buy();

        var orderEntity = orderRepository.findWithItemsById(1L).orElseThrow();
        var expectedOrderItemViews = orderItemViewMapper.from(orderEntity.getItems());
        var expectedOrderView = new OrderView(orderEntity.getId(), orderEntity.getTotalCost(), expectedOrderItemViews);
        // preparations ends
        // when
        var response = mockMvc.perform(get(ORDERS_ROOT + "/1"))
                .andExpect(view().name("order"))
                .andExpect(model().attributeExists("order"))
                .andExpect(model().attribute("newOrder", false))
                .andExpect(model().size(2))
                .andReturn()
                .getModelAndView();

        assertThat(response).isNotNull();
        var actualOrderView = (OrderView) response.getModelMap().get("order");
        assertThat(expectedOrderView).isEqualTo(actualOrderView);
    }

    @SneakyThrows
    private ModelMap validateAndGetOrders() {
        // when
        var response = mockMvc.perform(get(ORDERS_ROOT))
                // then
                .andExpect(view().name("orders"))
                .andExpect(model().attributeExists("orders"))
                .andExpect(model().size(1))
                .andReturn()
                .getModelAndView();

        assertThat(response).isNotNull();
        return response.getModelMap();
    }

    @SneakyThrows
    private void buy() {
        mockMvc.perform(post(ORDERS_ROOT + "/place-an-order"));
    }

    @SneakyThrows
    private void addItemsToTheCart(Long itemId) {
        mockMvc.perform(post(CART_ROOT)
                .param(CartItemAction.Fields.action, CartAction.PLUS.toString())
                .param("id", itemId.toString())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED));
    }
}
