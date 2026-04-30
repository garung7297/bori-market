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

    // 다른 메뉴(예: 회원관리)를 눌렀을 때 예시
    @GetMapping("/users")
    public String userManagement(Model model) {
        model.addAttribute("contentPage", "user_list"); // user_list.html을 던져줌
        return "admin/admin_main";
    }

    @GetMapping("/main")
    public String adminMain(HttpSession session, Model model) {


        String userRole = (String) session.getAttribute("userRole");
        // 보안 체크: 본사 직원이 아니면 접근 불가
        //if (!"본사".equals(userRole)) {return "redirect:/";}

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

        sql = """
        SELECT relname AS table_name
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE n.nspname = 'public' AND c.relkind = 'r'
        ORDER BY c.oid DESC 
    """;
        List<String> tableNames = jdbcTemplate.queryForList(sql, String.class);

        Map<String, List<Map<String, Object>>> tableDataMap = new LinkedHashMap<>();

        for (String tableName : tableNames) {
            try {
                // 1. 작성일(created_at) 기준으로 최신순(DESC) 정렬하고
                // 2. 딱 15개만(LIMIT 15) 가져오기
                sql = "SELECT * FROM " + tableName + " ORDER BY created_at DESC LIMIT 17";
                List<Map<String, Object>> dataList = jdbcTemplate.queryForList(sql);
                tableDataMap.put(tableName, dataList);
                //System.out.println(tableName+" :: "+dataList);
            } catch (Exception e) {
                // 테이블은 있는데 데이터가 없거나 조회 에러가 날 경우를 대비
                tableDataMap.put(tableName, new ArrayList<>());
            }

        }

        model.addAttribute("tableDataMap", tableDataMap);


        // 1. 처음 들어왔을 때 보여줄 '옷(알맹이)'의 이름을 지정해
        // admin/dashboard.html 파일 내의 th:fragment="content"를 불러오게 됨
        model.addAttribute("contentPage", "dashboard");
        // 2. 껍데기(프레임) 파일인 admin_main을 리턴해
        return "admin/main";
    }//adminMain





    // 나중에 추가할 기능들 예시
    // @GetMapping("/stats") ... 매출 통계
    // @GetMapping("/roles") ... 권한 수정
}