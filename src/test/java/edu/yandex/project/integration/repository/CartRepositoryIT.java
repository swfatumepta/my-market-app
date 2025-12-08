package edu.yandex.project.integration.repository;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CartRepositoryIT")
public class CartRepositoryIT extends AbstractRepositoryIT {

    @Test
    void findFirstByIdIsNotNull_inCaseNonexistentCartId() {
        // given
        // when
        var actualResult = cartRepository.findFirstByIdIsNotNull();
        // then
        assertNotNull(actualResult);
        assertTrue(actualResult.isEmpty());
    }
}
