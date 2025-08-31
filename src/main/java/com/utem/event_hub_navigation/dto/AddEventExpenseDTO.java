package com.utem.event_hub_navigation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddEventExpenseDTO {

    Integer budgetCategoryId;
    Double amount;

}
