package com.utem.event_hub_navigation.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.utem.event_hub_navigation.model.Role;

public interface RoleRepo extends JpaRepository<Role, Integer> {

    boolean existsByName(String name);
}
