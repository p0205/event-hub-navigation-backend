package com.utem.event_hub_navigation.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamMemberDTO {

    private Integer userId;
    private String name;
    private String email;
    private String role;
}
