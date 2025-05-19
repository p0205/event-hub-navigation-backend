package com.utem.event_hub_navigation.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.utem.event_hub_navigation.dto.UserDTO;
import com.utem.event_hub_navigation.dto.UserSignUpDTO;
import com.utem.event_hub_navigation.model.UTeMStaff;
import com.utem.event_hub_navigation.model.UTeMStudent;
import com.utem.event_hub_navigation.model.User;

@Mapper(componentModel = "spring", uses = { EventMapperHelper.class })
public interface UserMapper {

    UserDTO toUserDTO(User user);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastUpdatedAt", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    User toUser(UserDTO userDTO);

    List<UserDTO> toUserDTOs(List<User> users);

    @Mapping(target = "course", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "phoneNo", ignore = true)
    @Mapping(target = "year", ignore = true)
    @Mapping(target = "role", constant = "EVENT_ORGANIZER")
    UserDTO toUserDTO(UTeMStaff staff);

    @Mapping(target = "role", constant = "PARTICIPANT")
    @Mapping(target = "phoneNo", ignore = true)
    @Mapping(target = "id", ignore = true)
    UserDTO toUserDTO(UTeMStudent student);

    UserSignUpDTO toUserSignUpDTO(UTeMStudent student);

    UserSignUpDTO toUserSignUpDTO(UTeMStaff staff);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastUpdatedAt", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", constant = "PARTICIPANT")
    @Mapping(target = "phoneNo", ignore = true)
    User toUser(UTeMStudent student);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastUpdatedAt", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", constant = "EVENT_ORGANIZER")
    @Mapping(target = "phoneNo", ignore = true)
    @Mapping(target = "year", ignore = true)
    @Mapping(target = "course", ignore = true)
    User toUser(UTeMStaff staff);
}
