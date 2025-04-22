package com.utem.event_hub_navigation.utils;

public class NumberParserUtil {
    public static Integer parseInteger(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format for " + fieldName + ": " + value);
        }
    }
}
