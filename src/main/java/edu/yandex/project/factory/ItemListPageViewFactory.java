package edu.yandex.project.factory;

import edu.yandex.project.controller.dto.ItemView;
import edu.yandex.project.controller.dto.ItemListPageView;
import edu.yandex.project.controller.dto.ItemsPageableRequest;
import edu.yandex.project.controller.dto.PageInfo;
import edu.yandex.project.mapper.ItemViewMapper;
import edu.yandex.project.repository.view.ItemJoinCartPageView;
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
public class ItemListPageViewFactory {

    @Value("${items.view.table.size:3}")
    private int itemViewTableSize;

    private final ItemViewMapper itemViewMapper;

    public ItemListPageView create(Page<ItemJoinCartPageView> itemEntitiesPage, @NonNull ItemsPageableRequest pageableRequest) {
        log.debug("ItemPageFactory::create request = {}, fromDb = {} in", pageableRequest, itemEntitiesPage);
        var builder = ItemListPageView.builder();
        if (!itemEntitiesPage.getContent().isEmpty()) {
            var itemViews = itemViewMapper.fromItemJoinCartViews(itemEntitiesPage.getContent());

            var itemsViewTable = this.splitIntoParts(itemViews, itemViewTableSize);
            this.alignIfNeeded(itemsViewTable);

            builder
                    .items(itemsViewTable)
                    .pageInfo(PageInfo.from(itemEntitiesPage))
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
     * @param itemWebView список списков (таблица) {@link ItemView}
     */
    private void alignIfNeeded(ArrayList<List<ItemView>> itemWebView) {
        if (!itemWebView.isEmpty() && itemWebView.getLast().size() < 3) {
            do {
                itemWebView.getLast().add(ItemView.createStub());
            } while (itemWebView.getLast().size() < 3);
        }
    }

    private ArrayList<List<ItemView>> splitIntoParts(List<ItemView> items, int columns) {
        var table = new ArrayList<List<ItemView>>();
        for (int i = 0; i < items.size(); i += columns) {
            var row = new ArrayList<>(items.subList(i, Math.min(i + columns, items.size())));
            table.add(row);
        }
        return table;
    }
}
