package com.utem.event_hub_navigation.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddEventExpenseDTO {

    Integer budgetCategoryId;
    Double amount;

}
