package edu.yandex.project.service.impl;

import edu.yandex.project.controller.dto.ItemView;
import edu.yandex.project.controller.dto.ItemListPageView;
import edu.yandex.project.controller.dto.ItemsPageableRequest;
import edu.yandex.project.exception.ItemNotFoundException;
import edu.yandex.project.factory.ItemListPageViewFactory;
import edu.yandex.project.mapper.ItemViewMapper;
import edu.yandex.project.repository.ItemRepository;
import edu.yandex.project.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    private final ItemViewMapper itemViewMapper;
    private final ItemListPageViewFactory itemListPageViewFactory;

    @Override
    @Transactional(readOnly = true)
    public ItemListPageView findAll(@NonNull ItemsPageableRequest pageableRequest) {
        log.debug("ItemServiceImpl::findAll {} in", pageableRequest);
        var itemJoinCartPageView = itemRepository.findAllWithCartCount(
                pageableRequest.search(),
                pageableRequest.sort().name(),
                PageRequest.of(pageableRequest.pageNumber(), pageableRequest.pageSize())
        );
        var itemListPageView = itemListPageViewFactory.create(itemJoinCartPageView, pageableRequest);
        log.debug("ItemServiceImpl::findAll {} out. Result: {}", pageableRequest, itemListPageView);
        return itemListPageView;
    }

    @Override
    @Transactional(readOnly = true)
    public ItemView findOne(long itemId) {
        log.debug("ItemServiceImpl::findOne {} in", itemId);
        var itemView = itemRepository.findByIdWithCartCount(itemId)
                .map(itemViewMapper::fromItemJoinCartView)
                .orElseThrow(() -> {
                    log.error("ItemServiceImpl::findOne ItemEntity.id = {} not found", itemId);
                    return new ItemNotFoundException(itemId);
                });
        log.debug("ItemServiceImpl::findOne {} out. Result: {}", itemId, itemView);
        return itemView;
    }
}
