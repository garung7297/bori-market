package com.garung.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class MainController {

    private final JdbcTemplate jdbcTemplate;

    public MainController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/")
    public String index(Model model) {
        // public 스키마에 있는 가령이의 테이블 목록만 가져오기
        String sql = """
            SELECT table_name 
            FROM information_schema.tables 
            WHERE table_schema = 'public' 
            ORDER BY table_name
        """;

        List<String> tables = jdbcTemplate.queryForList(sql, String.class);
       System.out.println(tables);
//        인덱스페이지에 뿌려줄 정보들 여기서 모델에 담아서 인덱스에 리턴시킴
        model.addAttribute("tableList", tables); // 화면으로 전달!
        return "index"; // index.html로 이동
    }
}