package edu.yandex.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

@NamedEntityGraph(
        name = "CartFullState",
        attributeNodes = {
                @NamedAttributeNode("id"),
                @NamedAttributeNode("createdAt"),
                @NamedAttributeNode(
                        value = "addedItems",
                        subgraph = "cartItem.details"
                )
        },
        subgraphs = {
                @NamedSubgraph(
                        name = "cartItem.details",
                        type = CartItemEntity.class,
                        attributeNodes = {
                                @NamedAttributeNode("itemCount"),
                                @NamedAttributeNode("totalCost"),
                                @NamedAttributeNode(
                                        value = "item",
                                        subgraph = "item.details"
                                )
                        }
                ),
                @NamedSubgraph(
                        name = "item.details",
                        type = ItemEntity.class,
                        attributeNodes = {
                                @NamedAttributeNode("id"),
                                @NamedAttributeNode("title"),
                                @NamedAttributeNode("price")
                        }
                )
        }
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
