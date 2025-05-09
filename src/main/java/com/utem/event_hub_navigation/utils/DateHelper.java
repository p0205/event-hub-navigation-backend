package com.utem.event_hub_navigation.utils;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateHelper {

    // Standard date and time formats
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter HUMAN_READABLE_FORMAT = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm");
    private static final DateTimeFormatter SHORT_DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    /**
     * Formats a LocalDateTime to a standard date-time string.
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(DATE_TIME_FORMAT);
    }

    /**
     * Extracts the date from a LocalDateTime.
     */
    public static String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(DATE_FORMAT);
    }

    /**
     * Extracts the time from a LocalDateTime.
     */
    public static String formatTime(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(TIME_FORMAT);
    }

    /**
     * Formats a LocalDateTime to a more human-readable format.
     */
    public static String formatHumanReadableDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(HUMAN_READABLE_FORMAT);
    }

    /**
     * Formats a LocalDateTime to a short date string.
     */
    public static String formatShortDate(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(SHORT_DATE_FORMAT);
    }

    /**
     * Parses a date-time string into a LocalDateTime.
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            return LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMAT);
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Checks if a date-time string is valid.
     */
    public static boolean isValidDateTime(String dateTimeStr) {
        try {
            LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMAT);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
