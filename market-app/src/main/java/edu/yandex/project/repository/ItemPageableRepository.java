package edu.yandex.project.repository;

import edu.yandex.project.controller.dto.enums.ItemSort;
import edu.yandex.project.domain.Item;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ItemPageableRepository {
    private static final Map<ItemSort, String> ORDER_BY = Map.of(
            ItemSort.ALPHA, "i.title",
            ItemSort.PRICE, "i.price",
            ItemSort.NO, "i.id"
    );

    private final DatabaseClient databaseClient;

    public Mono<Page<Item>> findAll(@NonNull String textFilter, @NonNull ItemSort sortRule, @NonNull Pageable pageable) {
        log.debug("ItemPageableRepository::findAll {}, {}, {} in", textFilter, sortRule, pageable);
        var query = """
                SELECT
                    id,
                    title,
                    description,
                    img_path,
                    price,
                    COUNT(*) OVER() AS total_count
                FROM items
                WHERE title ILIKE :textFilter OR description ILIKE :textFilter
                GROUP BY id, title, description, img_path, price
                ORDER BY :sortRule
                LIMIT :limit OFFSET :offset
                """;
        var likePattern = "%" + textFilter + "%";
        return databaseClient.sql(query)
                .bind("textFilter", likePattern)
                .bind("sortRule", ORDER_BY.get(sortRule))
                .bind("limit", pageable.getPageSize())
                .bind("offset", pageable.getOffset())
                .map(ItemPageableRepository::mapRow)
                .all()
                .collectList()
                .map(itemsWithTotal -> mapToPage(pageable, itemsWithTotal))
                .doOnSuccess(result ->
                        log.debug("ItemPageableRepository::findAll {}, {}, {} out. Result: {}",
                                textFilter, sortRule, pageable, result)
                );
    }

    private static Page<Item> mapToPage(Pageable pageable, List<ItemsWithTotal> itemsWithTotal) {
        if (itemsWithTotal.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }
        var total = itemsWithTotal.getFirst().total();
        var items = itemsWithTotal.stream()
                .map(ItemsWithTotal::item)
                .toList();
        return new PageImpl<>(items, pageable, total);
    }

    private static ItemsWithTotal mapRow(Row row, RowMetadata rowMetadata) {
        return new ItemsWithTotal(
                Item.builder()
                        .id(row.get("id", Long.class))
                        .title(row.get("title", String.class))
                        .description(row.get("description", String.class))
                        .imgPath(row.get("img_path", String.class))
                        .price(row.get("price", Long.class))
                        .build(),
                (row.get("total_count", Long.class))
        );
    }

    private record ItemsWithTotal(Item item, Long total) {
    }
}
