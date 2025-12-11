package edu.yandex.project.integration.exception.handler;

import edu.yandex.project.controller.CartWebController;
import edu.yandex.project.controller.ItemWebController;
import edu.yandex.project.controller.OrderWebController;
import edu.yandex.project.exception.ItemNotFoundException;
import edu.yandex.project.exception.OrderNotFoundException;
import edu.yandex.project.exception.handler.GlobalWebExceptionHandler;
import edu.yandex.project.factory.ItemListPageViewFactory;
import edu.yandex.project.mapper.ItemViewMapper;
import edu.yandex.project.mapper.OrderItemViewMapper;
import edu.yandex.project.repository.CartItemRepository;
import edu.yandex.project.repository.CartRepository;
import edu.yandex.project.repository.ItemRepository;
import edu.yandex.project.repository.OrderRepository;
import edu.yandex.project.service.impl.CartServiceImpl;
import edu.yandex.project.service.impl.ItemServiceImpl;
import edu.yandex.project.service.impl.OrderServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.util.ReflectionTestUtils.getField;

@Import({CartServiceImpl.class, ItemServiceImpl.class, OrderServiceImpl.class})
@WebMvcTest({CartWebController.class, ItemWebController.class, OrderWebController.class})
public abstract class AbstractGlobalWebExceptionHandlerIT {
    protected static final String ERR_DIR_NAME;
    protected static final String ERR_MESSAGE_KEY;

    protected final Long NON_EXISTENT_ID = 123456L;

    protected static final String ITEM_NOT_FOUND_ERROR_MESSAGE_PATTERN;
    protected static final String ORDER_NOT_FOUND_ERROR_MESSAGE_PATTERN;

    static {
        ERR_DIR_NAME = (String) getField(GlobalWebExceptionHandler.class, "ERR_DIR_NAME");
        ERR_MESSAGE_KEY = (String) getField(GlobalWebExceptionHandler.class, "ERR_MESSAGE_KEY");

        ITEM_NOT_FOUND_ERROR_MESSAGE_PATTERN = (String) getField(ItemNotFoundException.class, "ERROR_MESSAGE_PATTERN");
        ORDER_NOT_FOUND_ERROR_MESSAGE_PATTERN = (String) getField(OrderNotFoundException.class, "ERROR_MESSAGE_PATTERN");
    }

    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected CartItemRepository mockedCartItemRepository;
    @MockitoBean
    protected CartRepository mockedCartRepository;
    @MockitoBean
    protected ItemRepository mockedItemRepository;
    @MockitoBean
    protected OrderRepository mockedOrderRepository;

    @MockitoBean
    protected ItemListPageViewFactory mockedItemListPageViewFactory;

    @MockitoBean
    protected ItemViewMapper mockedItemViewMapper;
    @MockitoBean
    protected OrderItemViewMapper mockedOrderItemViewMapper;
}
