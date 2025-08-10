package com.utem.event_hub_navigation.dto;

import lombok.Data;

@Data
public class UpdateVenueRequest {

    private Integer id;
    private String name;
    private String fullName;
    private Integer capacity;
}
