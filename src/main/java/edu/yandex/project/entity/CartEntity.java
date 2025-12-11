package edu.yandex.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

@NamedEntityGraph(
        name = "CartJoinItems",
        attributeNodes = @NamedAttributeNode(
                value = "addedItems",
                subgraph = "JoinItems"
        ),
        subgraphs = @NamedSubgraph(
                name = "JoinItems",
                type = CartItemEntity.class,
                attributeNodes = @NamedAttributeNode("item")
        )
)
@Entity
@Table(name = "carts")
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@NoArgsConstructor
@Setter
@ToString
public class CartEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "carts_id_seq"
    )
    @SequenceGenerator(
            name = "carts_id_seq",
            sequenceName = "carts_id_seq",
            allocationSize = 1
    )
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "created_at",
            nullable = false,
            updatable = false,
            insertable = false)
    private Instant createdAt;

    @OneToMany(
            mappedBy = "cart",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @ToString.Exclude
    private List<CartItemEntity> addedItems;
}
