package edu.yandex.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "order_item")
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@NoArgsConstructor
@Setter
@ToString
public class OrderItemEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "order_item_id_seq"
    )
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(
            name = "order_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_order_item_order")
    )
    @ToString.Exclude
    private OrderEntity order;

    @Column(name = "item_id",
            nullable = false)
    private Long itemId;

    @Column(name = "item_title",
            nullable = false)
    private String itemTitle;

    @Column(name = "item_count",
            nullable = false)
    private Long itemCount;

    @Column(name = "item_price_at_order_time",
            nullable = false)
    private Long itemPriceAtOrderTime;

    @Column(name = "subtotal",
            nullable = false)
    private Long subtotal;

    @Column(name = "created_at",
            nullable = false,
            updatable = false,
            insertable = false)
    private Instant createdAt;
}
