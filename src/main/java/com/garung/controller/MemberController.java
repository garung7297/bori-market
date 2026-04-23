package com.garung.controller;

import com.garung.domain.Member;
import com.garung.domain.Role;
import com.garung.repository.MemberRepository;
import com.garung.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;


    /**
     * 2. 전체 회원 리스트 조회 (Role 정보 포함)
     */
    @GetMapping("/check-data")
    public List<Member> checkData() {
        return memberRepository.findAll();
    }

    /**
     * 3. 새 회원 저장 (아이디, 역할SN, 등급 기반)
     * 예시: /save-member?id=garung&roleSn=1&level=1
     */
    @GetMapping("/save-member")
    public String saveMember(
            @RequestParam String id,
            @RequestParam Long roleSn,
            @RequestParam int level) {
        try {
            // 역할 정보 조회
            Role role = roleRepository.findById(roleSn)
                    .orElseThrow(() -> new RuntimeException("해당 역할 번호(sn)가 존재하지 않습니다."));

            Member member = new Member();
            member.setUserId(id);
            member.setRole(role); // 연관 관계 설정
            member.setLevel(level);

            memberRepository.save(member);
            return "회원 등록 성공: " + id + " [" + role.getRoleName() + " / " + level + "등급]";
        } catch (Exception e) {
            return "회원 등록 실패: " + e.getMessage();
        }
    }

    /**
     * 4. 아이디 기반 권한 체크 (유스케이스 반영)
     * 예시: /check-auth?id=garung
     */
    @GetMapping("/check-auth")
    public String checkAuth(@RequestParam String id) {
        return memberRepository.findById(id)
                .map(m -> {
                    String roleName = m.getRole().getRoleName();
                    int level = m.getLevel();

                    if (level == 1 && roleName.equals("본사")) {
                        return "마스터 계정 확인: 모든 시스템 권한(테이블 제어 등)이 허용됩니다.";
                    } else {
                        return "[" + roleName + "] 계정입니다. 해당 등급(" + level + ")에 맞는 메뉴만 표시됩니다.";
                    }
                })
                .orElse("존재하지 않는 사용자 아이디입니다.");
    }



}