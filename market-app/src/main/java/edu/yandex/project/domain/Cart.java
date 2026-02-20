package edu.yandex.project.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("carts")
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@NoArgsConstructor
@Setter
@ToString
public class Cart {

    @Id
    @EqualsAndHashCode.Include
    private Long id;

    @Column("created_at")
    private Instant createdAt;
}
