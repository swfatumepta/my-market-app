package edu.yandex.project.repository;

import edu.yandex.project.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    @Query("""
            SELECT o
            FROM OrderEntity o
            LEFT JOIN FETCH o.items
            """)
    List<OrderEntity> findAllWithItems();

    @Query("""
            SELECT o
            FROM OrderEntity o
            LEFT JOIN FETCH o.items
            WHERE o.id = :id
            """)
    Optional<OrderEntity> findWithItemsById(@NonNull Long id);
}
