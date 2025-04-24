package com.utem.event_hub_navigation.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.utem.event_hub_navigation.model.User;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Integer> {
    List<User> findByEmailIn(List<String> emails);
    Optional<User> findByEmail(String email);
}
