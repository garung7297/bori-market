package com.garung.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
//
@Controller
public class LoginController {
    // 1. JdbcTemplate을 사용할 수 있도록 선언해줘
    private final JdbcTemplate jdbcTemplate;
    // 2. 생성자를 통해 스프링이 jdbcTemplate을 자동으로 넣어주게 해 (생성자 주입)
    public LoginController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @PostMapping("/login_process")
    public String login_process(
            @RequestParam String user_login_id,
            @RequestParam String password,
            HttpSession session, // 로그인 정보를 저장할 바구니
            Model model
    ) {
        try {
            // 1. DB에서 아이디로 유저 정보 가져오기
            // 가령이가 만든 users 테이블의 컬럼명(user_login_id)을 그대로 썼어!
            Map<String, Object> user = jdbcTemplate.queryForMap(
                    "SELECT * FROM users WHERE user_login_id = ?", user_login_id
            );

            // 2. 비밀번호 비교
            String dbPassword = (String) user.get("password");

            if (dbPassword != null && dbPassword.equals(password)) {
                // [로그인 성공]
                // 세션에 로그인한 사람의 닉네임과 ID를 저장해두자!
                session.setAttribute("userId", user.get("id"));
                session.setAttribute("user_login_id", user.get("user_login_id"));
                session.setAttribute("userNickname", user.get("nickname"));

                // 특정 아이디인 경우에만 'isAdmin' 권한을 true로 설정
                if ("eco2sumin".equals(user.get("user_login_id"))) {
                    session.setAttribute("isAdmin", true);
                } else {
                    session.setAttribute("isAdmin", false);
                }

                // DB에서 가져온 role_name을 세션에 그대로 저장 (예: "본사", "판매사", "배송사", "고객")
                String userRole = "본사";//(String) user.get("role_name");
                session.setAttribute("userRole", userRole);

                // 개발자(본사)인지 체크하는 로직도 이 값 하나로 해결!
                System.out.println("접속 권한 userRole : " + userRole);



                System.out.println("로그인 성공! 환영합니다, " + user.get("nickname") + "님 id :  "+user.get("id"));

                return "redirect:/"; // 메인 페이지로 이동
            } else {
                // [비밀번호 틀림]
                model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
                return "layout/login"; // 다시 로그인 페이지로
            }

        } catch (EmptyResultDataAccessException e) {
            // [아이디가 없음]
            model.addAttribute("error", "존재하지 않는 아이디입니다.");
            return "layout/login";
        } catch (Exception e) {
            // [기타 에러]
            model.addAttribute("error", "로그인 중 오류가 발생했습니다.");
            return "layout/login";
        }
    }


    // 화면 상단이나 메뉴에서 '로그인' 링크를 누르면 /login으로 요청이 와
    @GetMapping("/login")
    public String loginPage() {
        // return "login"; 은 login.html 파일을 찾아서 보여주라는 뜻이야!

        return "layout/login";
    }


    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // 세션을 무효화해서 저장된 모든 정보를 지워버려
        session.invalidate();

        // 로그아웃 후에는 다시 메인 페이지로 보냄
        return "redirect:/";
    }//logout


    @GetMapping("/join")
    public String joinPage() {
        return "layout/join";
    }//joinPage

    @PostMapping("/join_process")
    public String joinProcess(
            @RequestParam String user_login_id,
            @RequestParam String password,
            @RequestParam String nickname,
            HttpSession session, // 자동 로그인을 위해 필요
            Model model
    ) {
        try {
            // 1. 중복 가입 체크
            String checkSql = "SELECT count(*) FROM users WHERE user_login_id = ?";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, user_login_id);

            if (count != null && count > 0) {
                model.addAttribute("error", "이미 사용 중인 아이디입니다.");
                return "layout/join";
            }

            // 2. 회원 정보 저장
            String insertUserSql = "INSERT INTO users (user_login_id, password, nickname) VALUES (?, ?, ?)";
            jdbcTemplate.update(insertUserSql, user_login_id, password, nickname);



            // 3. 기본 권한 부여 (고객 역할 ID: 4)
            // [수정] UUID를 받기 위해 Integer 대신 String을 사용함
            String userIdSql = "SELECT id FROM users WHERE user_login_id = ?";
            String newUserId = jdbcTemplate.queryForObject(userIdSql, String.class, user_login_id);

            System.out.println("newUserId (UUID) : " + newUserId);

            // [수정] user_id 컬럼도 UUID(String)를 받도록 처리
            jdbcTemplate.update("INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)", newUserId, 4);


            return "redirect:/login"; // 가입 성공 시 로그인으로
        } catch (Exception e) {
            // 에러 로그 출력 (디버깅용)
            e.printStackTrace();
            model.addAttribute("error", "가입 도중 오류가 발생했습니다.<p>" + e);

            return "layout/join";
        }
    }//joinProcess
}//class
