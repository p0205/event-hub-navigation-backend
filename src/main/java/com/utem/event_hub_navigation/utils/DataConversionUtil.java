package com.utem.event_hub_navigation.utils;

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

}
