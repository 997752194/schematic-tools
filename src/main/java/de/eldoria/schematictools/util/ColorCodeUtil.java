/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */
package de.eldoria.schematictools.util;

import org.bukkit.ChatColor;

import java.util.List;

/**
 * Utility to translate colour codes found in configuration strings into the legacy
 * {@code §} format understood by the Bukkit item meta.
 * <p>
 * Both the traditional single-char codes ({@code &a}, {@code &b}, ...) and the modern
 * hexadecimal notation ({@code &#RRGGBB}) are supported and can be mixed freely within
 * the same string.
 */
public final class ColorCodeUtil {
    private ColorCodeUtil() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    /**
     * Translates all supported colour codes of the given string into legacy {@code §}
     * codes. Any other {@code &} sequence is left untouched.
     *
     * @param input the string containing colour codes
     * @return the translated string
     */
    public static String translate(String input) {
        if (input == null || input.isEmpty()) return input;
        return ChatColor.translateAlternateColorCodes('&', translateHex(input));
    }

    /**
     * Translates the colour codes of every string in the given list.
     *
     * @param lines the list of strings
     * @return a new list with all codes translated
     */
    public static List<String> translate(List<String> lines) {
        if (lines == null) return null;
        return lines.stream().map(ColorCodeUtil::translate).toList();
    }

    /**
     * Converts {@code &#RRGGBB} sequences into their legacy {@code §x§R§R§G§G§B§B}
     * representation so that Bukkit item meta renders true-colour text.
     *
     * @param input the string containing hexadecimal codes
     * @return the string with all hexadecimal codes converted
     */
    private static String translateHex(String input) {
        var result = new StringBuilder(input.length());
        int index = 0;
        while (index < input.length()) {
            char c = input.charAt(index);
            if (c == '&' && index + 7 <= input.length() && input.charAt(index + 1) == '#'
                && isHex(input, index + 2, index + 8)) {
                appendHex(result, input, index + 2);
                index += 8;
            } else {
                result.append(c);
                index++;
            }
        }
        return result.toString();
    }

    private static boolean isHex(String input, int from, int to) {
        for (int i = from; i < to; i++) {
            char c = input.charAt(i);
            boolean isDigit = c >= '0' && c <= '9';
            boolean isUpper = c >= 'A' && c <= 'F';
            boolean isLower = c >= 'a' && c <= 'f';
            if (!isDigit && !isUpper && !isLower) return false;
        }
        return true;
    }

    private static void appendHex(StringBuilder result, String input, int from) {
        result.append(ChatColor.COLOR_CHAR).append('x');
        for (int i = 0; i < 6; i++) {
            char digit = input.charAt(from + i);
            result.append(ChatColor.COLOR_CHAR).append(digit);
        }
    }
}
