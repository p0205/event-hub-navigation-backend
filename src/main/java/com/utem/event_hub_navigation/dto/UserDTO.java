package com.utem.event_hub_navigation.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDTO {

    private Integer id;
    private String name;
    private String email;
    private String phoneNo;
    private Character gender;
    private String faculty;
    private String course;
    private String year;
    private String role;
}
