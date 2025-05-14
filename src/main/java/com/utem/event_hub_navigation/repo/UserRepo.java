package com.utem.event_hub_navigation.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import com.utem.event_hub_navigation.model.Users;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<Users, Integer> {
    List<Users> findByEmailIn(List<String> emails);

    Optional<Users> findByEmail(String email);

    List<Users> findByNameContains(String name);

    List<Users> findByEmailContains(String email);

    @Query("SELECT u FROM Users u WHERE LOWER(u.email) LIKE LOWER(CONCAT(:email, '%')) OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Users> findByEmailOrName(@Param("email") String email, @Param("name") String name);
}
