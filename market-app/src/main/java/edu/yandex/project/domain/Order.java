package edu.yandex.project.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.NonNull;

import java.time.Instant;
import java.util.Collection;

@Table("orders")
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@NoArgsConstructor
@Setter
@ToString
public class Order {

    @Id
    @EqualsAndHashCode.Include
    private Long id;

    @Column("total_cost")
    private Long totalCost;

    @Column("created_at")
    private Instant createdAt;

    public static Order createNew(@NonNull Collection<OrderItem> from) {
        var cartTotalCost = from.stream()
                .map(OrderItem::getSubtotal)
                .reduce(0L, Long::sum);
        var order = new Order();
        order.setTotalCost(cartTotalCost);
        return order;
    }
}
