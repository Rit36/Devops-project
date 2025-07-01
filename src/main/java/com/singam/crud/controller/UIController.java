package com.singam.crud.controller;

import com.singam.crud.entity.Order;
import com.singam.crud.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UIController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("orders", orderService.getOrders());
        model.addAttribute("newOrder", new Order());
        return "index";
    }

    @PostMapping("/addOrder")
    public String addOrder(@ModelAttribute("newOrder") Order order) {
        orderService.addOrder(order);
        return "redirect:/";
    }

    @GetMapping("/order/{id}")
    public String getOrder(@PathVariable int id, Model model) {
        model.addAttribute("order", orderService.getOrderById(id));
        return "order-details";
    }
}
