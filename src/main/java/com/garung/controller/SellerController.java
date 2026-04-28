package com.garung.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/seller")
public class SellerController {
    @GetMapping("/main")
    public String sellerMain(HttpSession session) {
        // "판매사" 권한 체크 로직 포함
        return "seller/main";
    }
}