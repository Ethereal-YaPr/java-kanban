package ru.common.util;

import java.time.format.DateTimeFormatter;

public class CustomDateTimeFormatter {
    public static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public static DateTimeFormatter getFormatter() {
        return DATE_TIME_FORMATTER;
    }
}