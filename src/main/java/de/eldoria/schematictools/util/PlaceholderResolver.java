/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */
package de.eldoria.schematictools.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Resolves placeholders inside command strings.
 * <p>
 * If the {@code PlaceholderAPI} plugin is present it is used through reflection (no hard
 * dependency), otherwise a small set of built-in placeholders is applied as a fallback.
 */
public final class PlaceholderResolver {
    private static final String PLACEHOLDER_API_CLASS = "me.clip.placeholderapi.PlaceholderAPI";

    private static boolean placeholderResolved;
    private static boolean placeholderAvailable;
    private static Method setPlaceholders;

    private PlaceholderResolver() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    /**
     * Resolves all supported placeholders for the given player.
     * <p>
     * Built-in placeholders:
     * <ul>
     *     <li>{@code %player%} / {@code %name%} — the player name</li>
     *     <li>{@code %world%} — the current world name</li>
     *     <li>{@code %x%}, {@code %y%}, {@code %z%} — the player's block coordinates</li>
     *     <li>{@code %uuid%} — the player's unique id</li>
     * </ul>
     * If {@code PlaceholderAPI} is loaded its placeholders are resolved additionally.
     *
     * @param player  the player to resolve placeholders for
     * @param command the command string potentially containing placeholders
     * @return the resolved command string
     */
    public static String resolve(Player player, String command) {
        if (command == null || command.isEmpty()) return command;
        String result = command;
        result = result.replace("%player%", player.getName())
                .replace("%name%", player.getName())
                .replace("%world%", player.getWorld().getName())
                .replace("%uuid%", player.getUniqueId().toString())
                .replace("%x%", Integer.toString(player.getLocation().getBlockX()))
                .replace("%y%", Integer.toString(player.getLocation().getBlockY()))
                .replace("%z%", Integer.toString(player.getLocation().getBlockZ()));
        if (usePlaceholderApi()) {
            try {
                Object resolved = setPlaceholders.invoke(null, player, result);
                if (resolved instanceof String s) result = s;
            } catch (IllegalAccessException | InvocationTargetException | RuntimeException ignored) {
                // fall back to the built-in resolution
            }
        }
        return result;
    }

    private static boolean usePlaceholderApi() {
        if (!placeholderResolved) {
            placeholderResolved = true;
            try {
                Class<?> clazz = Class.forName(PLACEHOLDER_API_CLASS);
                setPlaceholders = clazz.getMethod("setPlaceholders", Player.class, String.class);
                placeholderAvailable = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
            } catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
                placeholderAvailable = false;
            }
        }
        return placeholderAvailable;
    }
}
