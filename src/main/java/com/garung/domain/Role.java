package com.garung.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "role") // 실제 테이블명 확인
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") // 보내준 데이터의 'id'와 일치
    private Long id;

    @Column(name = "role_name", nullable = false) // '본사', '판매사' 등
    private String roleName;

    @Column(name = "level") // 1, 5, 8, 12 등[cite: 6]
    private int level;

    @Column(name = "created_at") // 2026-04-25 ...[cite: 6]
    private LocalDateTime createdAt;

    public Role() {}

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}