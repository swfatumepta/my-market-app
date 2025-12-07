package edu.yandex.project.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
@Slf4j
public class RedirectController {

    @PostMapping("/buy")
    public String redirectToPlaceAnOrder() {
        log.info("RedirectController::redirectToPlaceAnOrder ...");
        return "redirect:/orders/place-an-order";
    }
}
