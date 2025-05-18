package com.utem.event_hub_navigation.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import com.utem.event_hub_navigation.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Integer> {
    List<User> findByEmailIn(List<String> emails);

    Optional<User> findByEmail(String email);

    List<User> findByNameContains(String name);

    List<User> findByEmailContains(String email);

    @Query("SELECT u FROM User u WHERE LOWER(u.email) LIKE LOWER(CONCAT(:email, '%')) OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<User> findByEmailOrName(@Param("email") String email, @Param("name") String name);
}
