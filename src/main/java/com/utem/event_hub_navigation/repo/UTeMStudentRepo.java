package com.utem.event_hub_navigation.repo;

import org.springframework.data.jpa.repository.JpaRepository;


import com.utem.event_hub_navigation.model.UTeMStudent;

public interface UTeMStudentRepo extends JpaRepository<UTeMStudent,Integer>{

    UTeMStudent findByEmail(String email);
}
