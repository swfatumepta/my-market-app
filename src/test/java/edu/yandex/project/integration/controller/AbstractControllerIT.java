package edu.yandex.project.integration.controller;

import edu.yandex.project.integration.AbstractDbIT;
import edu.yandex.project.mapper.ItemViewMapper;
import edu.yandex.project.repository.ItemRepository;
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
    protected final Long CART_ID = 555_555L;
    protected final Long NON_EXISTENT_ID = 777_777L;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ItemRepository itemRepository;

    @Autowired
    protected ItemViewMapper itemViewMapper;
}
