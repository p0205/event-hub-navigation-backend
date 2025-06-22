package com.utem.event_hub_navigation.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.utem.event_hub_navigation.model.AccountStatus;
import com.utem.event_hub_navigation.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Integer> {
    List<User> findByEmailIn(List<String> emails);

    Optional<User> findByEmail(String email);

    List<User> findByNameContains(String name);

    List<User> findByEmailContains(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE LOWER(u.email) LIKE LOWER(CONCAT(:email, '%')) OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<User> findByEmailOrName(@Param("email") String email, @Param("name") String name);

     Page<User> findAll(@NonNull Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u")
    long countAllUsers();


   @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status")
    long countByStatus(@Param("status") AccountStatus status);

    

}
