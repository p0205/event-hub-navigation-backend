package com.utem.event_hub_navigation.service;

import java.util.List;

import com.utem.event_hub_navigation.dto.UserDTO;

public interface UserService {

    List<UserDTO> getUsersByEmail(List<String> emails);

    UserDTO getUserByEmail(String email);

    List<UserDTO> getUserByNameLike(String name);

}