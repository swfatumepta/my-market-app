package edu.yandex.project.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
@Slf4j
public class RedirectController {

    @PostMapping("/buy")
    public RedirectView redirectToPlaceAnOrder() {
        log.info("RedirectController::redirectToPlaceAnOrder ...");
        var redirectView = new RedirectView("/orders/place-an-order");
        redirectView.setStatusCode(HttpStatus.PERMANENT_REDIRECT);
        return redirectView;
    }
}
