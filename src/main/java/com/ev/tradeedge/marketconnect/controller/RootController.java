package com.ev.tradeedge.marketconnect.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootController {

    @GetMapping("/")
    public String redirectToApp() {
        return "redirect:/app/";
    }
}
