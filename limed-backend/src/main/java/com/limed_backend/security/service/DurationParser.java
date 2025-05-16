package com.limed_backend.security.service;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DurationParser {
    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)\\s*([smhdSMHD])");

    /** Парсер из строки в срок */
    public static Duration parseDuration(String input) {
        Matcher matcher = DURATION_PATTERN.matcher(input);
        if (matcher.matches()) {
            long amount = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2).toLowerCase();
            return switch (unit) {
                case "s" -> Duration.ofSeconds(amount);
                case "m" -> Duration.ofMinutes(amount);
                case "h" -> Duration.ofHours(amount);
                case "d" -> Duration.ofDays(amount);
                default -> throw new IllegalArgumentException("Неподдерживаемая единица измерения: " + unit);
            };
        } else {
            throw new IllegalArgumentException("Неправильный формат длительности. Пример корректного ввода: \"1h\", \"30m\", \"2d\"");
        }
    }
}