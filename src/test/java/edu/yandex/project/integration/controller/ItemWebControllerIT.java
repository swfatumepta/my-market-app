package edu.yandex.project.integration.controller;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("ItemWebControllerIT")
public class ItemWebControllerIT extends AbstractControllerIT {
    private final static String ITEMS_ROOT = "/items";

    @Test
    void getItem_itemFound_success() throws Exception {
        // given
        var persistedItemEntity = itemRepository.findById(1L).orElseThrow();

        var expectedModel = itemMapper.from(persistedItemEntity);
        var expectedView = "item";
        // when
        var response = mockMvc.perform(
                        get(ITEMS_ROOT + "/" + persistedItemEntity.getId())
                )
                // then
                .andExpect(status().isOk())
                .andExpect(view().name(expectedView))
                .andReturn().getModelAndView();

        assertThat(response).isNotNull();
        assertThat(response.getModel().get(expectedView)).isNotNull();
        assertThat(response.getModel().get(expectedView)).isEqualTo(expectedModel);
    }

    @Test
    void getItem_itemNotFound_fail() throws Exception {
        // given
        var expectedView = "forward:/error/stub_404.html";
        // when
        mockMvc.perform(get(ITEMS_ROOT + "/" + 404))
                // then
                .andExpect(status().isNotFound())
                .andExpect(view().name(expectedView));
    }
}
