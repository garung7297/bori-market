package com.borimarket.domain;

import com.garung.domain.Role;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users") // 실제 DB 테이블명과 매칭
public class User {

    @Id
    @Column(name = "user_login_id")
    private String userLoginId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String email;

    @ManyToOne // Role과의 관계 설정
    @JoinColumn(name = "role_sn")
    private Role role;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now(); // 가입일 자동 설정

    // 기본 생성자
    public User() {}

    // Getter & Setter (가령아, 여기는 나중에 Lombok @Data 쓰면 더 편해!)
    public String getUserLoginId() { return userLoginId; }
    public void setUserLoginId(String userLoginId) { this.userLoginId = userLoginId; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    // ... 생략 ...
}