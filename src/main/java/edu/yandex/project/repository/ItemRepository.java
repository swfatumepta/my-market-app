package edu.yandex.project.repository;

import edu.yandex.project.entity.ItemEntity;
import edu.yandex.project.repository.view.ItemJoinCartPageView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<ItemEntity, Long> {

    @Query("""
            SELECT new edu.yandex.project.repository.view.ItemJoinCartPageView(
                    i.id,
                    i.title,
                    i.description,
                    i.imgPath,
                    i.price,
                    COALESCE(SUM(ci.itemCount), 0)
            )
            FROM ItemEntity i
            LEFT JOIN CartItemEntity ci ON i.id = ci.item.id
            WHERE i.title ILIKE %:textFilter% OR i.description ILIKE %:textFilter%
            GROUP BY i.id, i.title, i.description, i.imgPath, i.price
            ORDER BY
                    CASE WHEN :sortRule = 'ALPHA' THEN i.title END ASC,
                    CASE WHEN :sortRule = 'PRICE' THEN i.price END ASC,
                    CASE WHEN :sortRule = 'NO' THEN i.id END DESC
            """)
    Page<ItemJoinCartPageView> findAllWithCartCount(String textFilter, String sortRule, Pageable pageable);

    @Query("""
            SELECT new edu.yandex.project.repository.view.ItemJoinCartPageView(
                    i.id,
                    i.title,
                    i.description,
                    i.imgPath,
                    i.price,
                    COALESCE(SUM(ci.itemCount), 0)
            )
            FROM ItemEntity i
            LEFT JOIN CartItemEntity ci ON i.id = ci.item.id
            WHERE i.id = :id
            GROUP BY i.id, i.title, i.description, i.imgPath, i.price
            """)
    Optional<ItemJoinCartPageView> findByIdWithCartCount(Long id);
}
