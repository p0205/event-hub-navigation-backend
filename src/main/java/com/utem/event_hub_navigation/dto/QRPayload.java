package com.utem.event_hub_navigation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QRPayload {
    public int eventId;
    public int eventVenueId;
    public String expiresAt;
    public String sig;
}
