package edu.yandex.project.controller;

import edu.yandex.project.controller.dto.ItemsPageableRequestDto;
import edu.yandex.project.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/items")

@RequiredArgsConstructor
@Slf4j
public class ItemWebController {

    private final ItemService itemService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public String getItems(@ModelAttribute ItemsPageableRequestDto requestParameters, Model model) {
        log.info("ItemWebController::getItems {} begins", requestParameters);
        var itemsPageDto = itemService.findAll(requestParameters);

        model.addAttribute("items", itemsPageDto.items());
        model.addAttribute("paging", itemsPageDto.pageInfoDto());
        model.addAttribute("search", itemsPageDto.search());
        model.addAttribute("sort", itemsPageDto.sort());
        log.info("ItemWebController::getItems {} ends. Result: {}", requestParameters, model);
        return "items";
    }

    @GetMapping("/{itemId}")
    @ResponseStatus(HttpStatus.OK)
    public String getItem(@PathVariable Long itemId, Model model) {
        log.info("ItemWebController::getItem {} begins", itemId);
        var modelData = itemService.findOne(itemId);
        model.addAttribute("item", modelData);
        log.info("ItemWebController::getItem {} ends. Result: {}", itemId, model);
        return "item";
    }
}
