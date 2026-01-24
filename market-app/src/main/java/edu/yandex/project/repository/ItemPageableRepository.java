package edu.yandex.project.repository;

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
import reactor.util.function.Tuple3;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ItemPageableRepository {

    private final DatabaseClient databaseClient;

    public Mono<Page<ItemJoinCartPageView>> findAllWithCartCount(@NonNull String textFilter,
                                                                 @NonNull String sortRule,
                                                                 @NonNull Pageable pageable) {
        log.debug("ItemPageableRepository::findAllWithCartCount {}, {}, {} in", textFilter, sortRule, pageable);
        var query = """
                SELECT
                    i.id,
                    i.title,
                    i.description,
                    i.img_path,
                    i.price,
                    COALESCE(SUM(ci.items_count), 0) AS in_cart_count
                FROM items i
                LEFT JOIN cart_item ci ON i.id = ci.item_id
                WHERE i.title ILIKE :textFilter OR i.description ILIKE :textFilter
                GROUP BY i.id, i.title, i.description, i.img_path, i.price
                ORDER BY
                    CASE WHEN :sortRule = 'ALPHA' THEN i.title END,
                    CASE WHEN :sortRule = 'PRICE' THEN i.price END,
                    CASE WHEN :sortRule = 'NO' THEN i.id END
                LIMIT :limit OFFSET :offset
                """;

        var likePattern = "%" + textFilter + "%";
        var items = databaseClient.sql(query)
                .bind("textFilter", likePattern)
                .bind("sortRule", sortRule)
                .bind("limit", pageable.getPageSize())
                .bind("offset", pageable.getOffset())
                .map(this::mapToObject)
                .all();
        return Mono.zip(items.collectList(), Mono.just(pageable), this.getTotalCount(likePattern))
                .map(this::mapToPage)
                .doOnSuccess(result ->
                        log.debug("ItemPageableRepository::findAllWithCartCount {}, {}, {} out. Result: {}",
                                textFilter, sortRule, pageable, result)
                );
    }

    private Mono<Long> getTotalCount(String searchPattern) {
        log.debug("ItemPageableRepository::getTotalCount {} in", searchPattern);
        var countQuery = """
                SELECT COUNT(id) AS cnt
                FROM items i
                WHERE i.title ILIKE :textFilter OR i.description ILIKE :textFilter
                """;
        return databaseClient.sql(countQuery)
                .bind("textFilter", searchPattern)
                .map((row, meta) -> row.get(0, Long.class))
                .one()
                .doOnSuccess(total ->
                        log.debug("ItemPageableRepository::getTotalCount {} out. Result: {}", searchPattern, total)
                );
    }

    private Page<ItemJoinCartPageView> mapToPage(Tuple3<List<ItemJoinCartPageView>, Pageable, Long> tuple) {
        return new PageImpl<>(tuple.getT1(), tuple.getT2(), tuple.getT3());
    }

    private ItemJoinCartPageView mapToObject(Row row, RowMetadata rowMetadata) {
        return ItemJoinCartPageView.builder()
                .id(row.get("id", Long.class))
                .title(row.get("title", String.class))
                .description(row.get("description", String.class))
                .imgPath(row.get("img_path", String.class))
                .price(row.get("price", Long.class))
                .inCartCount(row.get("in_cart_count", Long.class))
                .build();
    }
}
