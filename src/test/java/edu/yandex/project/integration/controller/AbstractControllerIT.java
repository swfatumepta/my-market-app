package edu.yandex.project.integration.controller;

import edu.yandex.project.integration.AbstractDbIT;
import edu.yandex.project.mapper.ItemMapper;
import edu.yandex.project.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest
public class AbstractControllerIT extends AbstractDbIT {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ItemRepository itemRepository;

    @Autowired
    protected ItemMapper itemMapper;
}
