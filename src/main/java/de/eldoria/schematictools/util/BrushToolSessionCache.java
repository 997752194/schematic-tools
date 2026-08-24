/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */
package de.eldoria.schematictools.util;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-player cache tracking the item that currently holds an active brush tool.
 * <p>
 * The active brush is bound to the item's base material by WorldEdit, so when a player
 * switches between two custom {@code ItemsAdder} items that share a base material the
 * previous binding must be released before the new one is applied. This cache remembers
 * the exact {@link ItemKey} (material + {@code ItemsAdder} id) that owns the active tool
 * for each player, allowing both the switch handling and the fallback mechanism to
 * decide which binding to touch.
 */
public final class BrushToolSessionCache {
    private final Map<UUID, ItemKey> active = new ConcurrentHashMap<>();

    /**
     * Records the item that currently owns the active tool for a player.
     *
     * @param player the player
     * @param key    the item key owning the tool
     */
    public void activate(Player player, ItemKey key) {
        if (key == null || key.isAir()) {
            active.remove(player.getUniqueId());
            return;
        }
        active.put(player.getUniqueId(), key);
    }

    /**
     * Removes any recorded tool owner for the player.
     *
     * @param player the player
     * @return the previously recorded key, if any
     */
    public Optional<ItemKey> clear(Player player) {
        return Optional.ofNullable(active.remove(player.getUniqueId()));
    }

    /**
     * The item key that currently owns the active tool for the player, if any.
     *
     * @param player the player
     * @return the recorded key, or empty if none
     */
    public Optional<ItemKey> get(Player player) {
        return Optional.ofNullable(active.get(player.getUniqueId()));
    }

    /**
     * Removes all cached entries. Used on plugin disable.
     */
    public void clearAll() {
        active.clear();
    }
}
