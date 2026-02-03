package edu.yandex.project.repository;

import edu.yandex.project.controller.dto.enums.ItemSort;
import edu.yandex.project.repository.util.view.ItemJoinCartPageView;
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

    public Mono<Page<ItemJoinCartPageView>> findAllWithCartCount(@NonNull String textFilter,
                                                                 @NonNull ItemSort sortRule,
                                                                 @NonNull Pageable pageable) {
        log.debug("ItemPageableRepository::findAllWithCartCount {}, {}, {} in", textFilter, sortRule, pageable);
        var query = """
                SELECT
                    i.id,
                    i.title,
                    i.description,
                    i.img_path,
                    i.price,
                    COALESCE(SUM(ci.items_count), 0) AS in_cart_count,
                    COUNT(*) OVER() AS total_count
                FROM items i
                LEFT JOIN cart_item ci ON i.id = ci.item_id
                WHERE i.title ILIKE :textFilter OR i.description ILIKE :textFilter
                GROUP BY i.id, i.title, i.description, i.img_path, i.price
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
                        log.debug("ItemPageableRepository::findAllWithCartCount {}, {}, {} out. Result: {}",
                                textFilter, sortRule, pageable, result)
                );
    }

    private static Page<ItemJoinCartPageView> mapToPage(Pageable pageable, List<ItemsWithTotal> itemsWithTotal) {
        if (itemsWithTotal.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }
        var total = itemsWithTotal.getFirst().total();
        var itemJoinCartPageViews = itemsWithTotal.stream()
                .map(ItemsWithTotal::item)
                .toList();
        return new PageImpl<>(itemJoinCartPageViews, pageable, total);
    }

    private static ItemsWithTotal mapRow(Row row, RowMetadata rowMetadata) {
        return new ItemsWithTotal(
                ItemJoinCartPageView.builder()
                        .id(row.get("id", Long.class))
                        .title(row.get("title", String.class))
                        .description(row.get("description", String.class))
                        .imgPath(row.get("img_path", String.class))
                        .price(row.get("price", Long.class))
                        .inCartCount(row.get("in_cart_count", Long.class))
                        .build(),
                (row.get("total_count", Long.class))
        );
    }

    private record ItemsWithTotal(ItemJoinCartPageView item, Long total) {
    }
}
