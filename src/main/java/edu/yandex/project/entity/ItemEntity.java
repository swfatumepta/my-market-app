package edu.yandex.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "items")

@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@NoArgsConstructor
@Setter
@ToString
public class ItemEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "items_id_seq"
    )
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "title",
            nullable = false)
    private String title;

    @Column(name = "description",
            nullable = false)
    private String description;

    @Column(name = "img_path")
    private String imgPath;

    @Column(name = "price",
            nullable = false)
    private Integer price;

    @Column(name = "created_at",
            nullable = false,
            updatable = false)
    private Instant createdAt;
}
