/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */
package de.eldoria.schematictools.commands.schematictools;

import de.eldoria.eldoutilities.commands.command.AdvancedCommand;
import de.eldoria.eldoutilities.commands.command.CommandMeta;
import de.eldoria.eldoutilities.commands.command.util.Arguments;
import de.eldoria.eldoutilities.commands.exceptions.CommandException;
import de.eldoria.eldoutilities.commands.executor.ITabExecutor;
import de.eldoria.eldoutilities.localization.MessageComposer;
import de.eldoria.schematictools.configuration.Configuration;
import de.eldoria.schematictools.util.I18n;
import de.eldoria.schematictools.util.Permissions;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// Manually persist configuration files.
// Usage: /sbt save tools
public class Save extends AdvancedCommand implements ITabExecutor {
    private final Configuration configuration;

    public Save(Plugin plugin, Configuration configuration) {
        super(plugin, CommandMeta.builder("save")
                .addUnlocalizedArgument("scope", true)
                .withPermission(Permissions.SAVE)
                .build());
        this.configuration = configuration;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String alias, @NotNull Arguments args) throws CommandException {
        String scope = args.asString(0).toLowerCase();
        switch (scope) {
            case "tools" -> {
                configuration.saveTools();
                messageSender().sendMessage(sender, MessageComposer.create()
                        .text(I18n.get().resolveFormatted("commands.save.saved", "tools")));
            }
            default -> messageSender().sendMessage(sender, MessageComposer.create()
                    .text(I18n.get().resolveFormatted("commands.save.invalid_scope")));
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull Arguments args) throws CommandException {
        if (args.sizeIs(1)) {
            String current = args.asString(0).toLowerCase();
            return List.of("tools").stream().filter(scope -> scope.startsWith(current)).toList();
        }
        return List.of();
    }
}
