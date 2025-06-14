package com.utem.event_hub_navigation.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataConversionUtil {
    public static Map<String, Long> convertObjectListToMap(List<Object[]> data) {
        return data
                .stream()
                .filter(row -> row[0] != null) // filter out null keys
                .collect(
                    Collectors.toMap(
                        
                        row -> (String) row[0], 
                    row -> (Long) row[1]));
    }
    public static Map<String, Long> convertBudgetObjectListToMap(List<Object[]> data) {
        Map<String, Long> result = new HashMap<>();

        // Check if data is not null and contains at least one row
        if (data != null && !data.isEmpty()) {
            Object[] row = data.get(0); // Get the first (and likely only) row of aggregate results

            // Safely retrieve and cast the Double values from the array.
            // Provide default 0.0 if values are null or not of expected type.
            Double totalBudgetDouble = (row.length > 0 && row[0] instanceof Double) ? (Double) row[0] : 0.0;
            Double totalExpenseDouble = (row.length > 1 && row[1] instanceof Double) ? (Double) row[1] : 0.0;

            // Convert Double values to Long using longValue().
            // Note: longValue() truncates decimal places. If higher precision is needed,
            // consider changing your DTO fields (totalBudget, totalExpenses) to Double.
            result.put("totalBudget", totalBudgetDouble.longValue());
            result.put("totalExpenses", totalExpenseDouble.longValue());
        } else {
            // If no data is returned (e.g., no budget entries for the event),
            // ensure the map contains default values to prevent NullPointerExceptions later.
            result.put("totalBudget", 0L);
            result.put("totalExpenses", 0L);
        }
        return result;
    }
}
