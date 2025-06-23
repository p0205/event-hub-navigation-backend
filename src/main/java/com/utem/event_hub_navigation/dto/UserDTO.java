package com.utem.event_hub_navigation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.LastModifiedDate;

import com.utem.event_hub_navigation.model.AccountStatus;

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
    private Boolean mustChangePassword;
    private AccountStatus status;
    private LocalDate createdAt;

    private LocalDateTime lastUpdatedAt;

}
