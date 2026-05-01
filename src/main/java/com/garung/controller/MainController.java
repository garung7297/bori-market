package com.garung.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class MainController {

    private final JdbcTemplate jdbcTemplate;

    public MainController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/")
    public String index(Model model) {


        String  sql = """
            SELECT *
            FROM news
            ORDER BY created_at DESC
            LIMIT 6
        """;
        List<Map<String, Object>> userNewsList = jdbcTemplate.queryForList(sql);
        model.addAttribute("userNewsList", userNewsList);


// 📢 기존에 있던 뉴스 리스트
//        model.addAttribute("userNewsList", userService.getRecentNews(5));
//
//        // 🆕 탭 메뉴를 위한 데이터 추가
//        // 1. 최근 가입한 고객 10명
//        model.addAttribute("recentJoinedUsers", userService.getRecentJoinedUsers(10));
//
//        // 2. 최근 로그인한 고객 10명
//        model.addAttribute("recentLoginUsers", userService.getRecentLoginUsers(10));
//
//        // 3. 상품 리스트 (스크롤용)
//        model.addAttribute("productList", productService.getAllProducts());




        // 2. 최근 가입 고객 10명 (가짜 데이터)[cite: 4]
        List<String> joinedUsers = new ArrayList<>();
        for(int i=1; i<=10; i++) joinedUsers.add("신규유저" + i);
        model.addAttribute("recentJoinedUsers", joinedUsers);

        // 3. 최근 로그인 고객 10명 (가짜 데이터)[cite: 4]
        List<String> loginUsers = new ArrayList<>();
        for(int i=1; i<=10; i++) loginUsers.add("로그인유저" + i);
        model.addAttribute("recentLoginUsers", loginUsers);

        // 4. 상품 리스트 (가짜 데이터)[cite: 4]
        model.addAttribute("productList", new ArrayList<>());


        return "index"; // index.html로 이동
    }

}