package com.utem.event_hub_navigation.dto;

import java.util.ArrayList;
import java.util.List;

import com.utem.event_hub_navigation.model.EventReport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttendanceReportOverview {
    EventReport attendanceReport;
    @Builder.Default
    private List<SessionAttendanceDTO> sessionAttendances = new ArrayList<>();
}
