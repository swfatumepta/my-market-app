package edu.yandex.project.repository;

import edu.yandex.project.entity.CartEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<CartEntity, Long> {

    // с учетом контекста задачи, данный запрос валиден, потму что подразумевается, что в БД может быть только одна корзина
    @EntityGraph(value = "CartFullState", type = EntityGraph.EntityGraphType.FETCH)
    Optional<CartEntity> findFirstByIdIsNotNull();
}
