package edu.yandex.project.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("items")
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@NoArgsConstructor
@Setter
@ToString
public class Item {

    @Id
    @EqualsAndHashCode.Include
    private Long id;

    private Long price;
    private String title;
    private String description;

    @Column("img_path")
    private String imgPath;

    @Column("created_at")
    private Instant createdAt;
}
