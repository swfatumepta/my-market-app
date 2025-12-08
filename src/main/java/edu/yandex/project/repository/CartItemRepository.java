package edu.yandex.project.repository;

import edu.yandex.project.entity.CartItemEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItemEntity, CartItemEntity.CartItemCompositeId> {

    @EntityGraph(value = "CartView", type = EntityGraph.EntityGraphType.FETCH)
    List<CartItemEntity> findAllByCartId(@NonNull Long cartId);
}
