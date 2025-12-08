package edu.yandex.project.integration.repository;

import edu.yandex.project.integration.AbstractDbIT;
import edu.yandex.project.repository.CartItemRepository;
import edu.yandex.project.repository.CartRepository;
import edu.yandex.project.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
public abstract class AbstractRepositoryIT extends AbstractDbIT {
    protected final Long NON_EXISTENT_ID = Long.MAX_VALUE;

    @Autowired
    protected CartRepository cartRepository;
    @Autowired
    protected CartItemRepository cartItemRepository;
    @Autowired
    protected ItemRepository itemRepository;
}
