package edu.yandex.project.repository;

import edu.yandex.project.entity.ItemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<ItemEntity, Long> {

    @Query("""
            SELECT i
            FROM ItemEntity i
            WHERE i.title ILIKE %:textFilter% OR i.description ILIKE %:textFilter%
            ORDER BY
                    CASE WHEN :sortRule = 'ALPHA' THEN i.title END ASC,
                    CASE WHEN :sortRule = 'PRICE' THEN i.price END ASC,
                    CASE WHEN :sortRule = 'NO' THEN i.id END DESC
            """)
    Page<ItemEntity> findAllByTitleLikeOrDescriptionLike(String textFilter, String sortRule, Pageable pageable);
}
