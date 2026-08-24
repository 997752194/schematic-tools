/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */
package de.eldoria.schematictools.listener;

import de.eldoria.schematicbrush.util.WorldEditBrush;
import de.eldoria.schematictools.util.BrushToolSessionCache;
import de.eldoria.schematictools.util.ItemKey;
import de.eldoria.schematictools.util.SchematicTool;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Extends the vanilla binding logic with {@code ItemsAdder} awareness.
 * <p>
 * The original {@link BrushBindListener} binds a tool to the base material of an item,
 * which is sufficient as long as each material carries at most one tool. When several
 * custom {@code ItemsAdder} items share the same base material, switching between them
 * needs to release the previous binding explicitly. This listener tracks the active
 * tool per player and provides a fallback that cleans up the recorded base material
 * binding whenever the corresponding {@code ItemsAdder} item is no longer available.
 * <p>
 * All logic is additive and runs alongside the original listener; if {@code ItemsAdder}
 * is not installed this listener simply becomes a lightweight session tracker and never
 * interferes with vanilla behaviour.
 */
public class ItemsAdderBindListener implements Listener {
    private final BrushToolSessionCache sessionCache;

    public ItemsAdderBindListener(BrushToolSessionCache sessionCache) {
        this.sessionCache = sessionCache;
    }

    /**
     * Tracks the active tool and releases bindings when switching away from a custom
     * {@code ItemsAdder} item that shares its base material with the new item.
     * <p>
     * Runs before the original {@link BrushBindListener#onItemSwap} so the session cache
     * is up to date when the actual binding happens.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemSwap(PlayerItemHeldEvent event) {
        var player = event.getPlayer();
        var inventory = player.getInventory();
        var previous = ItemKey.of(inventory.getItem(event.getPreviousSlot()));
        var current = ItemKey.of(inventory.getItem(event.getNewSlot()));

        // The player moved off an active IA tool towards a non-tool item on the same base
        // material: make sure any leftover binding on that material is released.
        if (previous.isCustom() && !current.isCustom() && previous.sameMaterial(current)) {
            fallback(player, previous);
        }

        // Remember the new held item only if it actually carries a schematic tool.
        if (SchematicTool.getToolId(inventory.getItem(event.getNewSlot())).isPresent()) {
            sessionCache.activate(player, current);
        } else {
            sessionCache.clear(player);
        }
    }

    /**
     * Releases the binding recorded for the player when the custom item that owned the
     * tool is dropped.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent event) {
        var dropped = ItemKey.of(event.getItemDrop().getItemStack());
        if (!dropped.isCustom()) return;

        var player = event.getPlayer();
        sessionCache.clear(player);

        // If the player still holds another item on the same base material that is not a
        // tool, do not leave a stale binding behind.
        var held = ItemKey.of(SchematicTool.getPlayerItem(player));
        if (dropped.sameMaterial(held) && !held.isCustom() && !held.isAir()) {
            fallback(player, dropped);
        }
    }

    /**
     * Cleans up all per-player state when a player leaves the server.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        sessionCache.clear(event.getPlayer());
    }

    /**
     * Removes the brush binding on the base material recorded in the given key.
     * <p>
     * This is the fallback used when a custom {@code ItemsAdder} item is no longer
     * available: the brush was bound per material, so we release it from that material.
     * The operation is idempotent — if no brush is bound to that material nothing happens.
     *
     * @param player the affected player
     * @param key    the key holding the base material to release
     */
    private void fallback(Player player, ItemKey key) {
        if (key == null || key.material() == Material.AIR) return;
        WorldEditBrush.removeBrush(player, new ItemStack(key.material()));
    }
}
