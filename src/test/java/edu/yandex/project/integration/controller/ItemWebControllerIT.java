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
    void getItemView_itemViewFound_success() throws Exception {
        // given
        var itemJoinCartPageView = itemRepository.findByIdWithCartCount(1L).orElseThrow();

        var expectedModel = itemViewMapper.fromItemJoinCartView(itemJoinCartPageView);
        var expectedView = "item";
        // when
        var response = mockMvc.perform(
                        get(ITEMS_ROOT + "/" + itemJoinCartPageView.id())
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
    void getItemView_itemViewNotFound_fail() throws Exception {
        // given
        var expectedView = "/error/404";
        // when
        mockMvc.perform(get(ITEMS_ROOT + "/" + 404))
                // then
                .andExpect(status().isNotFound())
                .andExpect(view().name(expectedView));
    }
}
