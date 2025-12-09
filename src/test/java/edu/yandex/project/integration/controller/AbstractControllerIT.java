package edu.yandex.project.integration.controller;

import edu.yandex.project.integration.AbstractDbIT;
import edu.yandex.project.mapper.ItemViewMapper;
import edu.yandex.project.mapper.OrderItemViewMapper;
import edu.yandex.project.repository.CartItemRepository;
import edu.yandex.project.repository.CartRepository;
import edu.yandex.project.repository.ItemRepository;
import edu.yandex.project.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(value = "classpath:/sql/clean-env.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@SpringBootTest
public class AbstractControllerIT extends AbstractDbIT {
    protected final static String CART_ROOT = "/cart/items";

    protected final Long CART_ID = 555_555L;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected CartRepository cartRepository;
    @Autowired
    protected CartItemRepository cartItemRepository;
    @Autowired
    protected ItemRepository itemRepository;
    @Autowired
    protected OrderRepository orderRepository;

    @Autowired
    protected ItemViewMapper itemViewMapper;
    @Autowired
    protected OrderItemViewMapper orderItemViewMapper;
}
