package edu.yandex.project.controller;

import ch.qos.logback.core.model.Model;
import edu.yandex.project.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderWebController {

    private final OrderService orderService;

    @GetMapping("/{id}")
    public String getOrder(@PathVariable Long id, @RequestParam Boolean newOrder, Model model) {
        log.info("OrderWebController::getOrder begins");

        log.info("OrderWebController::getOrders ends. Result: {}", model);
        return "orders";
    }


    @PostMapping("/place-an-order")
    public String placeAnOrder(RedirectAttributes redirectAttributes) {
        log.info("OrderWebController::placeAnOrder begins");
        var createdOrderId = orderService.create();

        redirectAttributes.addAttribute("id", createdOrderId);
        redirectAttributes.addAttribute("newOrder", true);
        log.info("OrderWebController::placeAnOrder ends. Redirecting ...");
        return "redirect:/orders/{id}";
    }
}
