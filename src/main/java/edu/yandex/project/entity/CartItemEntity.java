package edu.yandex.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

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

    @Column(name = "items_count",
            nullable = false)
    private Integer itemCount;

    @Column(name = "total_cost",
            nullable = false)
    private Integer totalCost;

    @Column(name = "created_at",
            nullable = false,
            updatable = false)
    private Instant createdAt;

    @ManyToOne
    @MapsId("itemId")
    @JoinColumn(
            name = "item_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_cart_item_item")
    )
    private ItemEntity item;

    @ManyToOne
    @MapsId("cartId")
    @JoinColumn(
            name = "cart_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_cart_item_cart")
    )
    private CartEntity cart;

    // composite key
    @Embeddable

    @AllArgsConstructor
    @EqualsAndHashCode
    @Getter
    @NoArgsConstructor
    @ToString
    static class CartItemCompositeId {
        private Long cartId;
        private Long itemId;
    }
}
