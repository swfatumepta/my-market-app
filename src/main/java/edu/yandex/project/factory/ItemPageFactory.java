package edu.yandex.project.factory;

import edu.yandex.project.controller.dto.ItemDto;
import edu.yandex.project.controller.dto.ItemsPageDto;
import edu.yandex.project.controller.dto.ItemsPageableRequestDto;
import edu.yandex.project.controller.dto.PageInfoDto;
import edu.yandex.project.entity.ItemEntity;
import edu.yandex.project.mapper.ItemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ItemPageFactory {

    @Value("${items.view.table.size:3}")
    private int itemViewTableSize;

    private final ItemMapper itemMapper;

    public ItemsPageDto create(Page<ItemEntity> itemEntitiesPage, @NonNull ItemsPageableRequestDto pageableRequest) {
        log.debug("ItemPageFactory::create request = {}, fromDb = {} in", pageableRequest, itemEntitiesPage);
        var builder = ItemsPageDto.builder();
        if (!itemEntitiesPage.getContent().isEmpty()) {
            var itemDtoList = itemMapper.from(itemEntitiesPage.getContent());

            var itemWebView = this.splitIntoParts(itemDtoList, itemViewTableSize);
            this.alignIfNeeded(itemWebView);

            builder
                    .items(itemWebView)
                    .pageInfoDto(PageInfoDto.from(itemEntitiesPage))
                    .sort(pageableRequest.sort())
                    .search(pageableRequest.search());
        }
        var built = builder.build();
        log.debug("ItemPageFactory::create request = {}, fromDb = {} out. Result: {}",
                pageableRequest, itemEntitiesPage, built);
        return built;
    }

    /**
     * Дополняет последний список в списке заглушками для правильнгого отображения элементов во view
     * <p>
     * @param itemWebView список списков (таблица) {@link ItemDto}
     */
    private void alignIfNeeded(ArrayList<List<ItemDto>> itemWebView) {
        if (!itemWebView.isEmpty() && itemWebView.getLast().size() < 3) {
            do {
                itemWebView.getLast().add(ItemDto.createStub());
            } while (itemWebView.getLast().size() < 3);
        }
    }

    private ArrayList<List<ItemDto>> splitIntoParts(List<ItemDto> items, int columns) {
        var table = new ArrayList<List<ItemDto>>();
        for (int i = 0; i < items.size(); i += columns) {
            var row = new ArrayList<>(items.subList(i, Math.min(i + columns, items.size())));
            table.add(row);
        }
        return table;
    }
}
