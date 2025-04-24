package com.utem.event_hub_navigation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.utem.event_hub_navigation.dto.EventBudgetDTO;
import com.utem.event_hub_navigation.model.EventBudget;

@Mapper(componentModel = "spring")
public interface EventBudgetMapper {

    @Mapping(source = "id.budgetId", target = "budgetCategoryId")
    EventBudgetDTO toDto(EventBudget entity);

    @Mapping(source = "budgetCategoryId", target = "id.budgetId")
    EventBudget toEntity(EventBudgetDTO dto);
}
