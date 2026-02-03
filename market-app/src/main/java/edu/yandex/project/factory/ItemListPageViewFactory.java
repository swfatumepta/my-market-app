package edu.yandex.project.factory;

import edu.yandex.project.controller.dto.ItemListPageView;
import edu.yandex.project.controller.dto.ItemView;
import edu.yandex.project.controller.dto.ItemsPageableRequest;
import edu.yandex.project.controller.dto.PageInfo;
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

    public ItemListPageView create(@NonNull Page<ItemView> itemViewPage, @NonNull ItemsPageableRequest request) {
        log.debug("ItemPageFactory::create request = {}, fromDb = {} in", request, itemViewPage);
        var builder = ItemListPageView.builder()
                .sort(request.sort())
                .search(request.search())
                .paging(PageInfo.from(itemViewPage));

        var itemViews = itemViewPage.getContent();
        if (!itemViews.isEmpty()) {
            var itemsViewTable = this.splitIntoParts(itemViews, itemViewTableSize);
            this.alignIfNeeded(itemsViewTable);

            builder.items(itemsViewTable);
        } else {
            builder.items(List.of());
        }
        var built = builder.build();
        log.debug("ItemPageFactory::create request = {}, fromDb = {} out. Result: {}", request, itemViewPage, built);
        return built;
    }

    /**
     * Дополняет последний список в списке заглушками для правильнгого отображения элементов во view
     * <p>
     * @param itemWebView список списков (таблица) {@link ItemView}
     */
    private void alignIfNeeded(List<List<ItemView>> itemWebView) {
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
