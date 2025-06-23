package com.utem.event_hub_navigation.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.utem.event_hub_navigation.dto.EmailCheckResponse;
import com.utem.event_hub_navigation.dto.UserDTO;
import com.utem.event_hub_navigation.model.User;

public interface UserService {

    EmailCheckResponse existInUTemDatabase(String email);

    boolean register(String email, String phoneNo, String rawPassword);

    List<UserDTO> getUsersByEmail(List<String> emails);

    UserDTO getUserByEmail(String email);



    List<UserDTO> getUserByNameLike(String name);

    public List<UserDTO> findByEmailOrName(String query);

    User getUserById(Integer userId);

    UserDTO updatePhoneNumber(Integer userId, String phoneNo);

    UserDTO updateUserInfo(Integer userId, UserDTO dto);

    void deleteUser(Integer userId);
    
    boolean updatePassword(Integer userId, String currentPassword, String newPassword);


    User createOutsiderAccount(String name, String email, String phoneNo, Character gender);

    boolean updateOutsiderPassword(Integer userId, String newPassword);

    Page<UserDTO> getAllUsers(Pageable pageable);
}