package edu.yandex.project.service.impl;

import edu.yandex.project.controller.dto.ItemDto;
import edu.yandex.project.exception.ItemNotFoundException;
import edu.yandex.project.mapper.ItemMapper;
import edu.yandex.project.repository.ItemRepository;
import edu.yandex.project.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    private final ItemMapper itemMapper;

    @Override
    @Transactional(readOnly = true)
    public ItemDto findOne(long itemId) {
        log.debug("ItemServiceImpl::findOne {} in", itemId);
        var itemDto = itemRepository.findById(itemId)
                .map(itemMapper::from)
                .orElseThrow(() -> {
                    log.error("ItemServiceImpl::findOne ItemEntity.id = {} not found", itemId);
                    return new ItemNotFoundException(itemId);
                });
        log.debug("ItemServiceImpl::findOne {} out. Result: {}", itemId, itemDto);
        return itemDto;
    }
}
