package com.garung.domain;

import jakarta.persistence.*;

@Entity
public class Member {

    @Id
    @Column(name = "user_id") // 이미지의 user_id와 일치
    private String userId;

    @ManyToOne
    @JoinColumn(name = "role_id") // 이미지의 role_id와 일치 (int8은 Long과 매칭)
    private Role role;

    @Column(name = "level") // 이미지의 level (int4는 int와 매칭)
    private int level;

    // created_at은 일단 생략하거나 아래처럼 추가 가능
    // @Column(name = "created_at")
    // private LocalDateTime createdAt;

    // Getter, Setter 혹은 @Data


    public Member() {}

    // Getter & Setter
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
}