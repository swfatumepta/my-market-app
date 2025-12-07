package edu.yandex.project.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.lang.NonNull;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "orders")
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@NoArgsConstructor
@Setter
@ToString
public class OrderEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "orders_id_seq"
    )
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "total_cost",
            nullable = false)
    private Long totalCost;

    @Column(name = "created_at",
            nullable = false,
            updatable = false,
            insertable = false)
    private Instant createdAt;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.PERSIST,
            orphanRemoval = true
    )
    @ToString.Exclude
    private List<OrderItemEntity> items;

    public static OrderEntity createNew(@NonNull CartEntity cartEntity) {
        var orderItemEntities = cartEntity.getAddedItems().stream()
                .map(OrderItemEntity::createWithEmptyOrder)
                .toList();
        var cartTotalCost = orderItemEntities.stream()
                .map(OrderItemEntity::getSubtotal)
                .reduce(0L, Long::sum);
        var orderEntity = OrderEntity.builder()
                .items(orderItemEntities)
                .totalCost(cartTotalCost)
                .build();
        orderItemEntities.forEach(orderItemEntity -> orderItemEntity.setOrder(orderEntity));
        return orderEntity;
    }
}
