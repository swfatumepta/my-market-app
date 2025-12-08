package edu.yandex.project.integration.repository;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CartItemRepositoryIT")
public class CartItemRepositoryIT extends AbstractRepositoryIT {

    @Test
    void findAllByCartId_inCaseNoCartFound() {
        // given
        // when
        var actualResult = cartItemRepository.findAllByCartId(NON_EXISTENT_ID);
        // then
        assertNotNull(actualResult);
        assertTrue(actualResult.isEmpty());
    }
}
