package edu.yandex.project.repository;

import edu.yandex.project.domain.CartItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@Slf4j
@RequiredArgsConstructor
@Repository
public class CartItemRepository {

    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    public Flux<CartItem> findAllByCartId(@NonNull Long cartId) {
        log.debug("CartItemRepository::findAllByCartId {} in", cartId);
        return r2dbcEntityTemplate.select(CartItem.class)
                .matching(query(where("cart_id").is(cartId)))
                .all();
    }

    // т.к. корзина только одна поэтому такой подход работает
    public Mono<CartItem> findCartItemByItemId(@NonNull Long itemId) {
        log.debug("CartItemRepository::findCartItemByItemId {} in", itemId);
        return r2dbcEntityTemplate.select(CartItem.class)
                .matching(query(where("item_id").is(itemId)))
                .one()
                .doOnSuccess(cartItem ->
                        log.debug("CartItemRepository::findCartItemByItemId {} out. Result: {}", itemId, cartItem)
                );
    }

    public Mono<CartItem> findById(@NonNull CartItem.CartItemCompositeId id) {
        log.debug("CartItemRepository::findById {} in", id);
        return r2dbcEntityTemplate.select(CartItem.class)
                .matching(query(
                        where("cart_id").is(id.cartId())
                                .and(where("item_id").is(id.itemId()))
                ))
                .one()
                .doOnSuccess(cartItem ->
                        log.debug("CartItemRepository::findById {} out. Result: {}", id, cartItem)
                );
    }

    public Mono<Void> upsert(@NonNull CartItem toBeUpdated) {
        log.debug("CartItemRepository::upsert {} in", toBeUpdated);
        var upsertQuery = """
                INSERT INTO cart_item (cart_id, item_id, total_cost, items_count)
                VALUES (:cartId, :itemId, :totalCost, :itemCount)
                ON CONFLICT (cart_id, item_id)
                DO UPDATE SET
                    total_cost = :totalCost,
                    items_count = :itemCount
                RETURNING cart_id, item_id, total_cost, items_count, created_at
                """;
        return r2dbcEntityTemplate.getDatabaseClient()
                .sql(upsertQuery)
                .bind("cartId", toBeUpdated.getId().cartId())
                .bind("itemId", toBeUpdated.getId().itemId())
                .bind("totalCost", toBeUpdated.getTotalCost())
                .bind("itemCount", toBeUpdated.getItemCount())
                .fetch()
                .one()
                .doOnSuccess(updated -> log.debug("CartItemRepository::upsert {} out", updated))
                .then();
    }

    public Mono<Void> delete(@NonNull CartItem toBeDeleted) {
        log.debug("CartItemRepository::delete {} in", toBeDeleted);
        return r2dbcEntityTemplate.delete(CartItem.class)
                .matching(query(
                        where("cart_id").is(toBeDeleted.getId().cartId())
                                .and(where("item_id").is(toBeDeleted.getId().itemId()))
                ))
                .all()
                .doOnSuccess(affectedRows -> log.debug("CartItemRepository::delete {} out", affectedRows))
                .then();
    }
}
