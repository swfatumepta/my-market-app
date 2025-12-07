package edu.yandex.project.controller;

import edu.yandex.project.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderWebController {

    private final OrderService orderService;

    @GetMapping()
    public String getOrders(Model model) {
        log.info("OrderWebController::getOrders begins");
        var orderViews = orderService.findAll();

        model.addAttribute("orders", orderViews);
        log.info("OrderWebController::getOrders ends. Result: {}", model);
        return "orders";
    }

    @GetMapping("/{id}")
    public String getOrder(@PathVariable Long id, @RequestParam(defaultValue = "false") Boolean newOrder, Model model) {
        log.info("OrderWebController::getOrder begins");
        var orderView = orderService.findOne(id);

        model.addAttribute("order", orderView);
        model.addAttribute("newOrder", newOrder);
        log.info("OrderWebController::getOrder ends. Result: {}", model);
        return "order";
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
