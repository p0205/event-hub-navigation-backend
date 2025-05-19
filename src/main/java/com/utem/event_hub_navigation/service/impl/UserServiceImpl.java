package com.utem.event_hub_navigation.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.utem.event_hub_navigation.dto.EmailCheckResponse;
import com.utem.event_hub_navigation.dto.EmailCheckResult;
import com.utem.event_hub_navigation.dto.UserDTO;
import com.utem.event_hub_navigation.dto.UserSignUpDTO;
import com.utem.event_hub_navigation.mapper.UserMapper;
import com.utem.event_hub_navigation.model.User;
import com.utem.event_hub_navigation.repo.UTeMStaffRepo;
import com.utem.event_hub_navigation.repo.UTeMStudentRepo;
import com.utem.event_hub_navigation.repo.UserRepo;
import com.utem.event_hub_navigation.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    private UserRepo userRepo;
    private UserMapper userMapper;
    private PasswordEncoder passwordEncoder;
    private UTeMStaffRepo utemStaffRepo;
    private UTeMStudentRepo utemStudentRepo;

    @Autowired
    public UserServiceImpl(UserRepo userRepo, UserMapper userMapper, PasswordEncoder passwordEncoder,
            UTeMStaffRepo utemStaffRepo, UTeMStudentRepo utemStudentRepo) {
        this.userRepo = userRepo;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.utemStaffRepo = utemStaffRepo;
        this.utemStudentRepo = utemStudentRepo;
    }

    @Override
    public EmailCheckResponse existInUTemDatabase(String email) {

        if (userRepo.findByEmail(email).isPresent()) {
            return new EmailCheckResponse(EmailCheckResult.USER_ALREADY_REGISTERED, null);
        }

        UserSignUpDTO dto = null;
        if (email.contains("student")) {
            dto = userMapper.toUserSignUpDTO(utemStudentRepo.findByEmail(email)); // return UserDTO object
        } else {
            dto = userMapper.toUserSignUpDTO(utemStaffRepo.findByEmail(email));
        }

        if (dto == null) {
            return new EmailCheckResponse(EmailCheckResult.EMAIL_NOT_FOUND, null);
        }

        return new EmailCheckResponse(EmailCheckResult.VALID_EMAIL, dto);
    }

    @Override
    public boolean register(String email, String phoneNo, String rawPassword) {
        try {
            User user = userMapper.toUser(utemStudentRepo.findByEmail(email));
            String hashPassword = passwordEncoder.encode(rawPassword);
            user.setCreatedAt(LocalDate.now());
            user.setPasswordHash(hashPassword);
            user.setPhoneNo(phoneNo);
            System.out.println(user.toString());
            userRepo.save(user);
            return true;
        } catch (Exception e) {

            return false;
        }
    }

    @Override
    public List<UserDTO> getUsersByEmail(List<String> emails) {

        List<User> users = userRepo.findByEmailIn(emails);
        return userMapper.toUserDTOs(users);
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        Optional<User> user = userRepo.findByEmail(email);
        if (user.isEmpty()) {
            return null;
        }

        return userMapper.toUserDTO(user.get());
    }

    @Override
    public List<UserDTO> getUserByNameLike(String name) {
        List<User> user = userRepo.findByNameContains(name);
        if (user.isEmpty()) {
            return null;
        }

        return userMapper.toUserDTOs(user);
    }

    @Override
    public List<UserDTO> findByEmailOrName(String query) {

        List<User> users = userRepo.findByNameContains(query);
        if (users.isEmpty()) {
            users = userRepo.findByEmailContains(query);
            if (users.isEmpty()) {
                return null;
            }
        }
        return userMapper.toUserDTOs(users);
    }

    @Override
    public User getUserById(Integer userId) {
        Optional<User> user = userRepo.findById(userId);
        if (user.isEmpty()) {
            return null;
        }
        return user.get();
    }

}