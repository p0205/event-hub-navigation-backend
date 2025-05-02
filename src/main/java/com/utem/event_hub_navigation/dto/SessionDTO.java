package com.utem.event_hub_navigation.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.utem.event_hub_navigation.model.Venue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SessionDTO {

    private Integer id;
    private String sessionName;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private List<Venue> venues;
    private byte[] qrCodeImage;
}
