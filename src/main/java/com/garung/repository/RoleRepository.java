package com.garung.repository;

import com.garung.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    // JpaRepository를 상속받으면 findById, save, findAll 같은 기본 기능을 바로 쓸 수 있어!
}