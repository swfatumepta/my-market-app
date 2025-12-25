package edu.yandex.project.domain;

import edu.yandex.project.exception.InconsistentCartItemIdException;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.Instant;

@Table("cart_item")
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@NoArgsConstructor
@Setter
@ToString
public class CartItem {

    @Id
    private CartItemCompositeId id;

    private Long totalCost;

    @Column("items_count")
    private Long itemCount;

    @Column("created_at")
    private Instant createdAt;

    public void incrementCount() {
        var oneItemPrice = this.getOneItemPrice();
        ++itemCount;
        this.totalCost = this.totalCost + oneItemPrice;
    }

    public void decrementCount() {
        var oneItemPrice = this.getOneItemPrice();
        --itemCount;
        this.totalCost = this.totalCost - oneItemPrice;
    }

    private Long getOneItemPrice() {
        return this.totalCost / this.itemCount;
    }

    public static CartItem createNew(@NonNull Cart cart, @NonNull Item item, @Nullable Long itemCount) {
        var computedCount = itemCount != null ? itemCount : 1L;
        return CartItem.builder()
                .id(new CartItemCompositeId(cart.getId(), item.getId()))
                .itemCount(computedCount)
                .totalCost(item.getPrice() * computedCount)
                .build();
    }

    /**
     * Composite id description class for {@link CartItem}
     */
    @Builder
    public record CartItemCompositeId(Long cartId, Long itemId) {
        public CartItemCompositeId {
            if (cartId == null || itemId == null) {
                throw new InconsistentCartItemIdException();
            }
        }
    }
}
