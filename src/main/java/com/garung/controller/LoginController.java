package com.garung.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
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
    @ResponseBody // 데이터를 반환하기 위해 필수
    public Map<String, Object> joinProcess( // [수정됨] 리턴 타입을 String에서 Map으로 변경[cite: 3]
                                            @RequestParam String user_login_id,
                                            @RequestParam String password,
                                            @RequestParam String nickname,
                                            HttpSession session,
                                            Model model
    ) {
        Map<String, Object> response = new HashMap<>(); //
        try {
            // 1. 중복 가입 체크
            String checkSql = "SELECT count(*) FROM users WHERE user_login_id = ?";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, user_login_id);

            if (count != null && count > 0) {
                response.put("success", false);
                response.put("message", "이미 사용 중인 아이디입니다.");
                return response; // 바구니(Map)를 그대로 리턴[cite: 3]
            }

            // 2. 회원 정보 저장
            String insertUserSql = "INSERT INTO users (user_login_id, password, nickname) VALUES (?, ?, ?)";
            jdbcTemplate.update(insertUserSql, user_login_id, password, nickname);

            // 3. 기본 권한 부여 및 UUID 캐스팅
            String userIdSql = "SELECT id FROM users WHERE user_login_id = ?";
            String newUserId = jdbcTemplate.queryForObject(userIdSql, String.class, user_login_id);
            jdbcTemplate.update("INSERT INTO user_roles (user_id, role_id) VALUES (?::uuid, ?)", newUserId, 4);

            // 4. 자동 로그인 세션 저장[cite: 3]
            session.setAttribute("userId", newUserId);
            session.setAttribute("user_login_id", user_login_id);
            session.setAttribute("userNickname", nickname);
            session.setAttribute("userRole", "고객");

            // 5. 성공 결과 전송
            response.put("success", true);
            response.put("message", "회원가입 및 로그인이 완료되었습니다! 🥕");
            return response; //[cite: 3]

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "가입 도중 서버 오류가 발생했습니다.");
            return response; //[cite: 3]
        }
    }//joinProcess

    @GetMapping("/mypage")
    public String myPage(HttpSession session, Model model) {
        // 1. 세션에서 로그인한 사용자 정보 확인
        String loginId = (String) session.getAttribute("user_login_id");

        if (loginId == null) {
            return "redirect:/login"; // 로그인 안 되어 있으면 로그인 페이지로
        }

        try {
            // 2. DB에서 최신 사용자 정보 조회
            // UUID를 사용하므로 id를 포함해 필요한 정보들을 가져옴
            String sql = "SELECT id, user_login_id, nickname, created_at FROM users WHERE user_login_id = ?";

            Map<String, Object> user = jdbcTemplate.queryForMap(sql, loginId);

            // 3. 모델에 담아서 뷰로 전달
            model.addAttribute("user", user);

            return "layout/mypage"; // 마이페이지 템플릿 경로

        } catch (Exception e) {
            model.addAttribute("error", "정보를 불러오는 중 오류가 발생했습니다.");
            return "redirect:/";
        }
    }//mypage

}//class
