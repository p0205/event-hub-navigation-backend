package com.utem.event_hub_navigation.repo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import com.utem.event_hub_navigation.model.Role;

public interface RoleRepo extends JpaRepository<Role, Integer> {

    boolean existsByName(String name);

    List<Role> findByNameContains(String name);

    Page<Role> findAll(@NonNull Pageable pageable);
}
