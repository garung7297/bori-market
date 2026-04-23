package com.garung.domain;

import jakarta.persistence.*;

@Entity
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sn") // 이미지의 sn과 일치
    private Long sn;

    @Column(name = "role_name") // 이미지의 role_name과 일치
    private String roleName;

    @Column(name = "level") // 이미지의 level과 일치
    private int level;

    public Role() {}

    // Getter & Setter
    public Long getSn() { return sn; }
    public void setSn(Long sn) { this.sn = sn; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
}