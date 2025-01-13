/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */
package de.eldoria.schematictools.listener;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.command.tool.BrushTool;
import com.sk89q.worldedit.command.tool.InvalidToolBindException;
import com.sk89q.worldedit.extension.platform.Actor;
import de.eldoria.eldoutilities.messages.MessageSender;
import de.eldoria.schematicbrush.SchematicBrushReborn;
import de.eldoria.schematicbrush.brush.SchematicBrush;
import de.eldoria.schematicbrush.util.WorldEditBrush;
import de.eldoria.schematictools.configuration.Configuration;
import de.eldoria.schematictools.util.SchematicTool;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class BrushBindListener implements Listener {
    private static final WorldEdit WORLD_EDIT = WorldEdit.getInstance();
    private final Plugin plugin;
    private final SchematicBrushReborn sbr;
    private final Configuration configuration;
    private final MessageSender messageSender;

    public BrushBindListener(Plugin plugin, SchematicBrushReborn sbr, Configuration configuration, MessageSender messageSender) {
        this.plugin = plugin;
        this.sbr = sbr;
        this.configuration = configuration;
        this.messageSender = messageSender;
    }

    private static LocalSession getLocalSession(Player player) {
        Actor actor = BukkitAdapter.adapt(player);

        return WORLD_EDIT.getSessionManager().get(actor);
    }

    @EventHandler
    public void onItemSwap(PlayerItemHeldEvent event) {
        var previousSlot = event.getPreviousSlot();
        var newSlot = event.getNewSlot();

        var player = event.getPlayer();

        var inventory = player.getInventory();
        var previousItem = inventory.getItem(previousSlot);
        var newItem = inventory.getItem(newSlot);

        unbindTool(player, previousItem);
        bindTool(player, newItem);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent event) {
        unbindTool(event.getPlayer(), event.getItemDrop().getItemStack());
    }

    public void unbindTool(Player player, ItemStack stack) {
        var toolId = SchematicTool.getToolId(stack);
        if (toolId.isEmpty()) return;

        try {
            if (getLocalSession(player).getTool(BukkitAdapter.adapt(stack).getType()) instanceof BrushTool tool
                && tool.getBrush() instanceof SchematicBrush) {
                getLocalSession(player).setTool(BukkitAdapter.adapt(stack).getType(), null);
                messageSender.sendActionBar(player, "<gold>Deactivated schematic tool");
            }
        } catch (InvalidToolBindException e) {

        }
    }

    public void bindTool(Player player, ItemStack stack) {
        var optMeta = SchematicTool.getTool(stack);
        if (optMeta.isEmpty()) return;

        var toolMeta = optMeta.get();
        var optTool = configuration.tools().byId(toolMeta.id());

        if (optTool.isEmpty()) {
            plugin.getLogger().warning("Brush Tool with ID " + toolMeta + " of player" + player.getName() + "does not exist anymore.");
            if (configuration.toolRemoval().isRemoveInvalidTools()) {
                messageSender.sendActionBar(player, "<dark_red>Your tool broke.");
                stack.setAmount(0);
            }
            return;
        }

        var tool = optTool.get();

        if (tool.hasUsage() && toolMeta.usages() >= tool.usages()) {
            if (configuration.toolRemoval().isRemoveUsed()) {
                messageSender.sendActionBar(player, "<dark_red>The usages of your brush tool are exhausted.");
                stack.setAmount(0);
            }
            return;
        }

        if (!player.hasPermission(tool.permission())) return;


        tool.getBrush(sbr.storageRegistry().activeStorage())
                .thenAccept(brush -> {
                    if (brush.isEmpty()) {
                        plugin.getLogger().warning("Tool " + tool + " of player " + player.getName() + "has an invalid brush.");
                        if (configuration.toolRemoval().isRemoveInvalidBrushes()) {
                            messageSender.sendActionBar(player, "<dark_red>Your tool broke.");
                            stack.setAmount(0);
                        }
                        return;
                    }

                    var build = brush.get().snapshot().load(player, sbr.brushSettingsRegistry(), sbr.schematics()).build(plugin, player);
                    WorldEditBrush.setBrush(player, stack, build, tool.permission());
                    messageSender.sendActionBar(player, "<gold>Activated schematic tool");
                });
    }
}
