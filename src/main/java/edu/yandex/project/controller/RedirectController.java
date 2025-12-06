package edu.yandex.project.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
@Slf4j
public class RedirectController {

    @PostMapping("/buy")
    @ResponseStatus(HttpStatus.PERMANENT_REDIRECT)
    public String redirectToPlaceAnOrder() {
        log.info("RedirectController::redirectToPlaceAnOrder ...");
        return "redirect:/orders/place-an-order";
    }
}
