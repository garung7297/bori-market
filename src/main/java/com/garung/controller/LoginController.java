package com.garung.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
public class LoginController {
    // 1. JdbcTemplate을 사용할 수 있도록 선언해줘
    private final JdbcTemplate jdbcTemplate;
    // 2. 생성자를 통해 스프링이 jdbcTemplate을 자동으로 넣어주게 해 (생성자 주입)
    public LoginController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    // 화면 상단이나 메뉴에서 '로그인' 링크를 누르면 /login으로 요청이 와
    @GetMapping("/login")
    public String loginPage() {
        // return "login"; 은 login.html 파일을 찾아서 보여주라는 뜻이야!
        return "layout/login";
    }



    @PostMapping("/login-process") // URL 경로도 명시해주는 게 좋아
    public String login_process(
            @RequestParam String user_login_id,
            @RequestParam String password
    ) throws Exception {

        Map<String, Object> user = jdbcTemplate.queryForMap("SELECT * FROM users WHERE user_login_id = ?", user_login_id);

        System.out.println("user_login_id :"+user_login_id);
        //Long newsId = jdbcTemplate.queryForObject("SELECT lastval()", Long.class);
        return "redirect:/" ;
    }




}
