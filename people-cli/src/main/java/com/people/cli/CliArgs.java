package com.people.cli;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CliArgs {
    private CliArgs() {
    }

    public static Map<String, String> parse(List<String> args) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < args.size(); i++) {
            String token = args.get(i);
            if (!token.startsWith("--")) {
                continue;
            }
            String key = token.substring(2);
            String value = null;
            int eqIdx = key.indexOf('=');
            if (eqIdx >= 0) {
                value = key.substring(eqIdx + 1);
                key = key.substring(0, eqIdx);
            } else if (i + 1 < args.size()) {
                value = args.get(++i);
            }
            if (value == null) {
                throw new IllegalArgumentException("Missing value for --" + key);
            }
            map.put(key, value);
        }
        return map;
    }

    public static String require(Map<String, String> args, String key) {
        String value = args.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing required --" + key);
        }
        return value;
    }

    public static String optional(Map<String, String> args, String key) {
        return args.get(key);
    }

    public static boolean parseBoolean(Map<String, String> args, String key) {
        return Boolean.parseBoolean(require(args, key));
    }

    public static int parseInt(Map<String, String> args, String key) {
        return Integer.parseInt(require(args, key));
    }

    public static BigDecimal parseMoney(Map<String, String> args, String key) {
        return new BigDecimal(require(args, key));
    }

    public static LocalDate parseDate(Map<String, String> args, String key) {
        String value = require(args, key);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
        try {
            return LocalDate.parse(value, formatter);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date format for --" + key + "; expected MM-dd-yyyy");
        }
    }

    public static <T extends Enum<T>> T parseEnum(Class<T> enumClass, String value) {
        String normalized = value.trim().replace('-', '_').replace(' ', '_').toUpperCase();
        return Enum.valueOf(enumClass, normalized);
    }
}
