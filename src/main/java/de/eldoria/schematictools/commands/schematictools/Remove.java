/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */
package de.eldoria.schematictools.commands.schematictools;

import de.eldoria.eldoutilities.commands.command.AdvancedCommand;
import de.eldoria.eldoutilities.commands.command.CommandMeta;
import de.eldoria.eldoutilities.commands.command.util.Arguments;
import de.eldoria.eldoutilities.commands.command.util.CommandAssertions;
import de.eldoria.eldoutilities.commands.exceptions.CommandException;
import de.eldoria.eldoutilities.commands.executor.IPlayerTabExecutor;
import de.eldoria.schematictools.configuration.Configuration;
import de.eldoria.schematictools.util.Permissions;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

// Remove an schematic tool.
public class Remove extends AdvancedCommand implements IPlayerTabExecutor {
    private final Configuration configuration;

    public Remove(Plugin plugin, Configuration configuration) {
        super(plugin, CommandMeta.builder("remove")
                .addUnlocalizedArgument("name", true)
                .withPermission(Permissions.MANAGE)
                .build());
        this.configuration = configuration;
    }

    @Override
    public void onCommand(@NotNull Player player, @NotNull String alias, @NotNull Arguments args) throws CommandException {
        args.parseQuoted();
        var tool = configuration.tools().byName(args.get(0).asString());
        CommandAssertions.isTrue(tool.isPresent(), "error.toolnotfound");

        configuration.tools().remove(tool.get());
        messageSender().sendMessage(player, "commands.remove.removed");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Player player, @NotNull String alias, @NotNull Arguments args) throws CommandException {
        if (args.sizeIs(1)) return configuration.tools().complete(args.get(0).asString());
        return Collections.emptyList();
    }
}
