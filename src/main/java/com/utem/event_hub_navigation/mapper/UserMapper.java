package com.utem.event_hub_navigation.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.utem.event_hub_navigation.dto.UserDTO;
import com.utem.event_hub_navigation.model.Users;

@Mapper(componentModel = "spring", uses = { EventMapperHelper.class })
public interface UserMapper {

    UserDTO toUserDTO(Users user);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastUpdatedAt", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    Users toUser(UserDTO userDTO);

    List<UserDTO> toUserDTOs(List<Users> users);
}
