package com.utem.event_hub_navigation.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.utem.event_hub_navigation.model.UTeMStaff;

public interface UTeMStaffRepo extends JpaRepository<UTeMStaff, Integer>{

    UTeMStaff findByEmail(String email);
}
