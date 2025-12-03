package edu.yandex.project.controller;

import edu.yandex.project.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping("/items")

@RequiredArgsConstructor
@Slf4j
public class ItemWebController {

    private final ItemService itemService;

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
