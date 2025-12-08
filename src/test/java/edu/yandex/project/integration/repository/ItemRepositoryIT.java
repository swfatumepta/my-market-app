package edu.yandex.project.integration.repository;

import edu.yandex.project.controller.dto.enums.ItemSort;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("ItemRepositoryIT")
public class ItemRepositoryIT extends AbstractRepositoryIT {

    @Test
    void findAllWithCartCount_inCaseEmptyResult() {
        // given
        var noMatchesTextFilter = "if I should fall in battle..";
        var sortRule = ItemSort.NO.toString();
        var pageable = PageRequest.of(0, 100);
        // when
        var actualResult = itemRepository.findAllWithCartCount(noMatchesTextFilter, sortRule, pageable);
        // then
        assertNotNull(actualResult);
        assertTrue(actualResult.isEmpty());
    }
}
