package edu.yandex.project.domain;

import edu.yandex.project.controller.dto.ItemView;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.NonNull;

import java.time.Instant;

@Table("order_item")
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@NoArgsConstructor
@Setter
@ToString
public class OrderItem {

    @Id
    @EqualsAndHashCode.Include
    private Long id;

    private Long orderId;
    private Long itemId;

    private String itemTitle;
    private Long itemCount;
    private Long itemPriceAtOrderTime;
    private Long subtotal;

    @Column("created_at")
    private Instant createdAt;

    public static OrderItem fromItemViewWithEmptyOrder(@NonNull ItemView from) {
        return OrderItem.builder()
                .itemId(from.id())
                .itemTitle(from.title())
                .itemPriceAtOrderTime(from.price())
                .itemCount(from.count())
                .subtotal(from.price() * from.count())
                .build();
    }
}
