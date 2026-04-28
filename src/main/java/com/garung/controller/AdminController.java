package com.garung.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin") // 모든 주소 앞에 /admin이 붙음
public class AdminController {

    private final JdbcTemplate jdbcTemplate;

    public AdminController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/main")
    public String adminMain(HttpSession session) {
        // "본사" 권한 체크 로직 포함
        return "admin/main";
    }




    @GetMapping("/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        String userRole = (String) session.getAttribute("userRole");
        // 보안 체크: 본사 직원이 아니면 접근 불가
        if (!"본사".equals(userRole)) {
            return "redirect:/";
        }

        // 전체 회원 조회 (관리자 전용 데이터)
        String sql = "SELECT * FROM users ORDER BY id DESC";
        List<Map<String, Object>> allUsers = jdbcTemplate.queryForList(sql);

        model.addAttribute("allUsers", allUsers);

        // public 스키마에 있는 가령이의 테이블 목록만 가져오기
        sql = """
            SELECT table_name
            FROM information_schema.tables
            WHERE table_schema = 'public'
            ORDER BY table_name
        """;
//        인덱스페이지에 뿌려줄 정보들 여기서 모델에 담아서 인덱스에 리턴시킴
        List<String> tables = jdbcTemplate.queryForList(sql, String.class);

        model.addAttribute("tableList", tables); // 화면으로 전달!

        String tableSql = """
        SELECT relname AS table_name
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE n.nspname = 'public' AND c.relkind = 'r'
        ORDER BY c.oid DESC 
    """;
        List<String> tableNames = jdbcTemplate.queryForList(tableSql, String.class);

        //tableNames = List.of("users", "news",  "test_table");

        //System.out.println("테이블이름 :"+tableNames);
        // 2. 각 테이블의 데이터를 담을 바구니 만들기
        // 결과 예시: { "news": [내용1, 내용2...], "users": [내용1, 내용2...] }
        Map<String, List<Map<String, Object>>> tableDataMap = new LinkedHashMap<>();

        for (String tableName : tableNames) {
            try {
                // 1. 작성일(created_at) 기준으로 최신순(DESC) 정렬하고
                // 2. 딱 15개만(LIMIT 15) 가져오기
                String dataSql = "SELECT * FROM " + tableName + " ORDER BY created_at DESC LIMIT 15";
                List<Map<String, Object>> dataList = jdbcTemplate.queryForList(dataSql);
                tableDataMap.put(tableName, dataList);
                //System.out.println(tableName+" :: "+dataList);
            } catch (Exception e) {
                // 테이블은 있는데 데이터가 없거나 조회 에러가 날 경우를 대비
                tableDataMap.put(tableName, new ArrayList<>());
            }

        }

        model.addAttribute("tableDataMap", tableDataMap);

        return "admin/dashboard"; // templates/admin/dashboard.html
    }





    // 나중에 추가할 기능들 예시
    // @GetMapping("/stats") ... 매출 통계
    // @GetMapping("/roles") ... 권한 수정
}