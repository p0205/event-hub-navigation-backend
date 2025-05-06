package com.utem.event_hub_navigation.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.utem.event_hub_navigation.model.Event;
import com.utem.event_hub_navigation.model.EventBudget;
import com.utem.event_hub_navigation.model.EventBudgetKey;

@Repository
public interface EventBudgetRepo extends JpaRepository<EventBudget, EventBudgetKey> {


    List<EventBudget> findByEvent(Event event);

}
