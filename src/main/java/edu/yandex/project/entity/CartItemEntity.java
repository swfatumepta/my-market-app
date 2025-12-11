package edu.yandex.project.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.Instant;

@NamedEntityGraph(
        name = "CartItemJoinItem",
        attributeNodes = @NamedAttributeNode("item")
)
@Entity
@Table(name = "cart_item")
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@NoArgsConstructor
@Setter
@ToString
public class CartItemEntity {

    @EmbeddedId
    private CartItemCompositeId id;

    @ManyToOne
    @MapsId("itemId")
    @JoinColumn(
            name = "item_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_cart_item_item")
    )
    @ToString.Exclude
    private ItemEntity item;

    @ManyToOne
    @MapsId("cartId")
    @JoinColumn(
            name = "cart_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_cart_item_cart")
    )
    @ToString.Exclude
    private CartEntity cart;

    @Column(name = "items_count",
            nullable = false)
    private Long itemCount;

    @Column(name = "total_cost",
            nullable = false)
    private Long totalCost;

    @Column(name = "created_at",
            nullable = false,
            updatable = false,
            insertable = false)
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

    public static CartItemEntity createNew(@NonNull CartEntity cartEntity,
                                           @NonNull ItemEntity itemEntity,
                                           @Nullable Long itemCount) {
        var compositeId = new CartItemCompositeId(cartEntity.getId(), itemEntity.getId());
        var computedCount = itemCount != null ? itemCount : 1L;
        return CartItemEntity.builder()
                .id(compositeId)
                .itemCount(computedCount)
                .totalCost(itemEntity.getPrice() * computedCount)
                .cart(cartEntity)
                .item(itemEntity)
                .build();
    }

    /**
     * Composite id description class for {@link CartItemEntity}
     */
    @Embeddable

    @AllArgsConstructor
    @EqualsAndHashCode
    @Getter
    @NoArgsConstructor
    @ToString
    public static class CartItemCompositeId {
        private Long cartId;
        private Long itemId;
    }
}
