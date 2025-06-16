package com.utem.event_hub_navigation.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.utem.event_hub_navigation.dto.EmailCheckResponse;
import com.utem.event_hub_navigation.dto.EmailCheckResult;
import com.utem.event_hub_navigation.dto.UserDTO;
import com.utem.event_hub_navigation.dto.UserSignUpDTO;
import com.utem.event_hub_navigation.mapper.UserMapper;
import com.utem.event_hub_navigation.model.AccountStatus;
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

    @Override
    public UserDTO updatePhoneNumber(Integer userId, String phoneNo) {

        Optional<User> userOpt = userRepo.findById(userId);
        if (userOpt.isEmpty()) {
            return null;
        }
        User user = userOpt.get();
        user.setPhoneNo(phoneNo);
        userRepo.save(user);
        return userMapper.toUserDTO(user);
    }

    @Override
    public boolean updatePassword(Integer userId, String currentPassword, String newPassword) {
        try {
            Optional<User> userOpt = userRepo.findById(userId);
            if (userOpt.isEmpty()) {
                return false;
            }

            User user = userOpt.get();

            // Verify current password
            if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
                return false;
            }

            // Update to new password
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            userRepo.save(user);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public UserDTO updateUserInfo(Integer userId, UserDTO dto) {
        Optional<User> userOpt = userRepo.findById(userId);
        if (userOpt.isEmpty()) {
            return null;
        }

        User user = userOpt.get();

        if (dto.getName() != null)
            user.setName(dto.getName());
        if (dto.getEmail() != null)
            user.setEmail(dto.getEmail());
        if (dto.getPhoneNo() != null)
            user.setPhoneNo(dto.getPhoneNo());
        if (dto.getGender() != null)
            user.setGender(dto.getGender());
        if (dto.getFaculty() != null)
            user.setFaculty(dto.getFaculty());
        if (dto.getCourse() != null)
            user.setCourse(dto.getCourse());
        if (dto.getYear() != null)
            user.setYear(dto.getYear());
        if (dto.getRole() != null)
            user.setRole(dto.getRole());

        if (dto.getStatus() != null)
            user.setStatus(dto.getStatus());
        user.setLastUpdatedAt(LocalDateTime.now());

        userRepo.save(user);
        return userMapper.toUserDTO(user);
    }

    public User createOutsiderAccount(String name, String email, String phoneNo, Character gender) {
        if (userRepo.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists.");
        }

        // Generate a strong temporary password (could also be sent to the outsider)
        String tempPassword = UUID.randomUUID().toString().substring(0, 10); // 10-char random string
        System.out.println(tempPassword);
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPhoneNo(phoneNo);
        user.setGender(gender);
        user.setStatus(AccountStatus.ACTIVE);
        user.setRole("PARTICIPANT"); // e.g., "PARTICIPANT" or "OUTSIDER"
        user.setPasswordHash(passwordEncoder.encode(tempPassword));
        user.setMustChangePassword(true);
        user.setCreatedAt(LocalDate.now());

        return userRepo.save(user);
    }

    @Override
    public Page<UserDTO> getAllUsers(Pageable pageable) {

        Page<User> usersPage = userRepo.findAll(pageable);
        return usersPage.map(userMapper::toUserDTO);
    }

    @Override
    public void deleteUser(Integer userId) {
        userRepo.deleteById(userId);
    }

}