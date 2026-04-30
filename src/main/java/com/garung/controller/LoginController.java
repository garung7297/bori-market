package com.garung.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LoginController {

    private final JdbcTemplate jdbcTemplate;

    public LoginController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 1. 실시간 아이디 중복 체크 API
    @GetMapping("/check_id")
    @ResponseBody
    public Map<String, Object> checkId(@RequestParam String user_login_id) {
        Map<String, Object> response = new HashMap<>();
        try {
            String sql = "SELECT count(*) FROM users WHERE user_login_id = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, user_login_id);

            if (count != null && count > 0) {
                response.put("available", false);
                response.put("message", "이미 사용 중인 아이디입니다. 😢");
            } else {
                response.put("available", true);
                response.put("message", "멋진 아이디네요! 사용 가능합니다. ✨");
            }
        } catch (Exception e) {
            response.put("available", false);
            response.put("message", "체크 중 오류가 발생했습니다.");
        }
        return response;
    }

    // 2. 통합 회원가입 처리 (비동기 방식)[cite: 3, 4]
    @PostMapping("/join_process")
    @ResponseBody
    public Map<String, Object> joinProcess(
            @RequestParam String user_login_id,
            @RequestParam String password,
            @RequestParam String nickname,
            @RequestParam(required = false) String email,
            @RequestParam String role_name,
            @RequestParam(value = "profile_image", required = false) MultipartFile profileImage,
            HttpSession session
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            // [1] 중복 가입 체크
            String checkSql = "SELECT count(*) FROM users WHERE user_login_id = ?";
            if (jdbcTemplate.queryForObject(checkSql, Integer.class, user_login_id) > 0) {
                response.put("success", false);
                response.put("message", "이미 사용 중인 아이디입니다.");
                return response;
            }

            // [2] 회원 정보 저장 (이메일 추가)[cite: 3, 4]
            String insertUserSql = "INSERT INTO users (user_login_id, password, nickname, email) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(insertUserSql, user_login_id, password, nickname, email);

            // [3] 가입된 유저의 UUID 가져오기[cite: 3, 4]
            String userIdSql = "SELECT id FROM users WHERE user_login_id = ?";
            String newUserId = jdbcTemplate.queryForObject(userIdSql, String.class, user_login_id);

            // [4] 권한 ID 결정 로직 (가령이의 요청 반영)
            int roleId = switch (role_name) {
                case "본사" -> 1;
                case "판매사" -> 2;
                case "배송사" -> 3;
                default -> 4; // 고객
            };

            // [5] 권한 부여 (::uuid 적용)[cite: 3, 4]
            jdbcTemplate.update("INSERT INTO user_roles (user_id, role_id) VALUES (?::uuid, ?)", newUserId, roleId);

            // [6] 이미지 파일 처리 (로그로 확인)
            if (profileImage != null && !profileImage.isEmpty()) {
                System.out.println("업로드된 프로필 파일: " + profileImage.getOriginalFilename());
            }

            // [7] 자동 로그인 처리 (세션 저장)[cite: 3, 4]
            session.setAttribute("userId", newUserId);
            session.setAttribute("user_login_id", user_login_id);
            session.setAttribute("userNickname", nickname);
            session.setAttribute("userRole", role_name);

            response.put("success", true);
            response.put("message", "보리마켓에 오신 것을 환영합니다! 🥕");
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "가입 중 오류가 발생했습니다.");
            return response;
        }
    }

    // 3. 로그인 처리[cite: 4]
    @PostMapping("/login_process")
    public String login_process(
            @RequestParam String user_login_id,
            @RequestParam String password,
            HttpSession session,
            Model model
    ) {
        try {
            Map<String, Object> user = jdbcTemplate.queryForMap(
                    "SELECT * FROM users WHERE user_login_id = ?", user_login_id
            );

            String dbPassword = (String) user.get("password");

            if (dbPassword != null && dbPassword.equals(password)) {
                session.setAttribute("userId", user.get("id"));
                session.setAttribute("user_login_id", user.get("user_login_id"));
                session.setAttribute("userNickname", user.get("nickname"));

                if ("eco2sumin".equals(user.get("user_login_id"))) {
                    session.setAttribute("isAdmin", true);
                } else {
                    session.setAttribute("isAdmin", false);
                }

                session.setAttribute("userRole", "본사");
                return "redirect:/";
            } else {
                model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
                return "layout/login";
            }
        } catch (EmptyResultDataAccessException e) {
            model.addAttribute("error", "존재하지 않는 아이디입니다.");
            return "layout/login";
        } catch (Exception e) {
            model.addAttribute("error", "로그인 중 오류가 발생했습니다.");
            return "layout/login";
        }
    }

    // 4. 단순 페이지 이동 메서드들[cite: 4]
    @GetMapping("/login")
    public String loginPage() {
        return "layout/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/join")
    public String joinPage() {
        return "layout/join";
    }

    @GetMapping("/mypage")
    public String myPage(HttpSession session, Model model) {
        String loginId = (String) session.getAttribute("user_login_id");
        if (loginId == null) return "redirect:/login";

        try {
            String sql = "SELECT id, user_login_id, nickname, created_at FROM users WHERE user_login_id = ?";
            Map<String, Object> user = jdbcTemplate.queryForMap(sql, loginId);
            model.addAttribute("user", user);
            return "layout/mypage";
        } catch (Exception e) {
            model.addAttribute("error", "정보를 불러오는 중 오류가 발생했습니다.");
            return "redirect:/";
        }
    }
}