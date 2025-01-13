/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */
package de.eldoria.schematictools.listener;

import de.eldoria.eldoutilities.messages.MessageSender;
import de.eldoria.schematicbrush.event.PostPasteEvent;
import de.eldoria.schematicbrush.event.PrePasteEvent;
import de.eldoria.schematictools.configuration.Configuration;
import de.eldoria.schematictools.util.SchematicTool;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class BrushPasteListener implements Listener {
    private Plugin plugin;
    private final Configuration configuration;
    private final MessageSender messageSender;

    public BrushPasteListener(Plugin plugin, Configuration configuration, MessageSender messageSender) {
        this.plugin = plugin;
        this.configuration = configuration;
        this.messageSender = messageSender;
    }

    @EventHandler
    public void onPrePaste(PrePasteEvent event) {
        var player = event.player();
        var currentTool = SchematicTool.getCurrentTool(player);
        if (currentTool.isEmpty()) return;

        var toolMeta = currentTool.get();

        var optionalTool = configuration.tools().byId(toolMeta.id());
        if (optionalTool.isEmpty()) return;

        var tool = optionalTool.get();
        if (!tool.hasUsage()) return;

        if (toolMeta.usages() >= tool.usages()) {
            event.setCancelled(true);
            messageSender.sendErrorActionBar(player, "The usages of your brush tool are exhausted.");
            toolMeta.updateUsage(tool);
            if (configuration.toolRemoval().isRemoveUsed()) {
                plugin.getServer().getScheduler().runTask(plugin,
                        () -> SchematicTool.getPlayerItem(player).setAmount(0));
            }
        }
    }

    @EventHandler
    public void onPostPaste(PostPasteEvent event) {
        var player = event.player();
        var optToolMeta = SchematicTool.getCurrentTool(player);
        if (optToolMeta.isEmpty()) return;

        var toolMeta = optToolMeta.get();
        toolMeta.incrementUsage();

        var optionalTool = configuration.tools().byId(toolMeta.id());
        if (optionalTool.isEmpty()) return;
        var tool = optionalTool.get();
        toolMeta.updateUsage(tool);

        if (tool.hasUsage() && toolMeta.usages() >= tool.usages()) {
            messageSender.sendErrorActionBar(player, "The usages of your brush tool are exhausted.");
            if (configuration.toolRemoval().isRemoveUsed()) {
                plugin.getServer().getScheduler().runTask(plugin,
                        () -> SchematicTool.getPlayerItem(player).setAmount(0));
            }
        }
    }
}
