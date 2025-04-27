package com.utem.event_hub_navigation.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.utem.event_hub_navigation.dto.UserDTO;
import com.utem.event_hub_navigation.mapper.UserMapper;
import com.utem.event_hub_navigation.model.User;
import com.utem.event_hub_navigation.repo.UserRepo;


@Service
public class UserService {

    private UserRepo userRepo;
    private UserMapper userMapper;

    @Autowired
    public UserService(UserRepo userRepo, UserMapper userMapper) {
        this.userRepo = userRepo;
        this.userMapper = userMapper;
    }

    public List<UserDTO> getUsersByEmail(List<String> emails) {

        List<User> users = userRepo.findByEmailIn(emails);
        return userMapper.toUserDTOs(users);
}

    public UserDTO getUserByEmail(String email) {
        Optional<User> user = userRepo.findByEmail(email);
        if (user.isEmpty()) {
            return null;
        }

        return userMapper.toUserDTO(user.get());
    }
}