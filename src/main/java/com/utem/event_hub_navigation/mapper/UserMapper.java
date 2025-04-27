package com.utem.event_hub_navigation.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.utem.event_hub_navigation.dto.UserDTO;
import com.utem.event_hub_navigation.model.User;

@Mapper(componentModel = "spring", uses = {EventMapperHelper.class})
public interface UserMapper {

    UserDTO toUserDTO(User user);

    User toUser(UserDTO userDTO);

    List<UserDTO> toUserDTOs(List<User> users);
}
