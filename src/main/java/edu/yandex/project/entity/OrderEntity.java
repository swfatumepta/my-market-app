package edu.yandex.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

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
}
