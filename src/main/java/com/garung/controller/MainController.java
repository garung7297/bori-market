package com.garung.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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

        return "index"; // index.html로 이동
    }

}