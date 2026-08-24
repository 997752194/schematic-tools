/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */
package de.eldoria.schematictools.commands;

import de.eldoria.eldoutilities.commands.command.AdvancedCommand;
import de.eldoria.eldoutilities.commands.command.CommandMeta;
import de.eldoria.messageblocker.blocker.MessageBlocker;
import de.eldoria.schematicbrush.SchematicBrushReborn;
import de.eldoria.schematictools.commands.schematictools.Bind;
import de.eldoria.schematictools.commands.schematictools.ChatBlock;
import de.eldoria.schematictools.commands.schematictools.Create;
import de.eldoria.schematictools.commands.schematictools.Give;
import de.eldoria.schematictools.commands.schematictools.Info;
import de.eldoria.schematictools.commands.schematictools.List;
import de.eldoria.schematictools.commands.schematictools.Modify;
import de.eldoria.schematictools.commands.schematictools.Reload;
import de.eldoria.schematictools.commands.schematictools.Remove;
import de.eldoria.schematictools.commands.schematictools.Save;
import de.eldoria.schematictools.configuration.Configuration;
import org.bukkit.plugin.Plugin;

public class BaseCommand extends AdvancedCommand {
    public BaseCommand(Plugin plugin, SchematicBrushReborn sbr, Configuration configuration, MessageBlocker messageBlocker) {
        super(plugin, CommandMeta.builder("schematictools")
                .buildSubCommands((cmds, builder) -> {
                    cmds.add(new Bind(plugin, configuration));
                    cmds.add(new Create(plugin, sbr, configuration));
                    cmds.add(new List(plugin, messageBlocker, configuration));
                    var info = new Info(plugin, messageBlocker, configuration);
                    cmds.add(info);
                    cmds.add(new Modify(plugin, configuration, sbr, info));
                    cmds.add(new Remove(plugin, configuration));
                    cmds.add(new ChatBlock(plugin, messageBlocker));
                    cmds.add(new Give(plugin, configuration));
                    cmds.add(new Reload(plugin, configuration));
                    cmds.add(new Save(plugin, configuration));
                })
                .build());
    }
}
