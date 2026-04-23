package com.garung.repository; // 위치 주의!

import com.garung.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

// JpaRepository를 상속받는 순간 findAll(), save() 같은 기능을 공짜로 쓰게 돼.
public interface MemberRepository extends JpaRepository<Member, String> {
}