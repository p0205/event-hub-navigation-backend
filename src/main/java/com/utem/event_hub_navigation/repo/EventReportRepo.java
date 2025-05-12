package com.utem.event_hub_navigation.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.utem.event_hub_navigation.model.Event;
import com.utem.event_hub_navigation.model.EventReport;

public interface EventReportRepo extends JpaRepository<EventReport, Integer>{

    List<EventReport> findByEvent(Event event);
}
