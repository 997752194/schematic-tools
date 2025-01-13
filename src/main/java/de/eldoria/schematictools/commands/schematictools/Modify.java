/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */
package de.eldoria.schematictools.commands.schematictools;

import de.eldoria.eldoutilities.commands.Completion;
import de.eldoria.eldoutilities.commands.command.AdvancedCommand;
import de.eldoria.eldoutilities.commands.command.CommandMeta;
import de.eldoria.eldoutilities.commands.command.util.Arguments;
import de.eldoria.eldoutilities.commands.command.util.CommandAssertions;
import de.eldoria.eldoutilities.commands.command.util.Input;
import de.eldoria.eldoutilities.commands.exceptions.CommandException;
import de.eldoria.eldoutilities.commands.executor.IPlayerTabExecutor;
import de.eldoria.eldoutilities.utils.Futures;
import de.eldoria.schematicbrush.SchematicBrushReborn;
import de.eldoria.schematictools.commands.schematictools.util.BrushLoader;
import de.eldoria.schematictools.configuration.Configuration;
import de.eldoria.schematictools.util.Permissions;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

// Modify an existing Schematic Tool
public class Modify extends AdvancedCommand implements IPlayerTabExecutor {
    private final Configuration configuration;
    private final SchematicBrushReborn sbr;
    private final Info info;

    public Modify(Plugin plugin, Configuration configuration, SchematicBrushReborn sbr, Info info) {
        super(plugin, CommandMeta.builder("modify")
                                 .addUnlocalizedArgument("name", true)
                                 .addUnlocalizedArgument("field", true)
                                 .addUnlocalizedArgument("value", true)
                                 .withPermission(Permissions.MANAGE)
                                 .build());
        this.configuration = configuration;
        this.sbr = sbr;
        this.info = info;
    }

    @Override
    public void onCommand(@NotNull Player player, @NotNull String alias, @NotNull Arguments args) throws CommandException {
        args.parseQuoted();
        var optionalTool = configuration.tools().byName(args.asString(0));

        CommandAssertions.isTrue(optionalTool.isPresent(), "error.unknowntoolname");

        var value = args.get(2);

        var tool = optionalTool.get();
        switch (args.asString(1).toLowerCase(Locale.ROOT)) {
            case "brushname" -> {
                var storage = sbr.storageRegistry().activeStorage();
                var container = BrushLoader.getContainer(player, storage, value.asString(), args);
                container.get(value.asString()).thenAccept(brush -> {
                    CommandAssertions.isTrue(brush.isPresent(), "error.nobrushfound");
                    tool.brush(container.owner(), brush.get());
                }).whenComplete(Futures.whenComplete(suc -> {
                    configuration.save();
                    info.showTool(player, tool);
                }, err -> handleCommandError(player, err)));
            }
            case "name" -> {
                CommandAssertions.isFalse(configuration.tools().byName(value.asString()).isPresent(), "Name is already in use.");
                tool.name(value.asString());
                configuration.save();
                info.showTool(player, tool);
            }
            case "usages" -> {
                var usages = value.asInt();
                if (usages != -1) {
                    CommandAssertions.min(usages, 1);
                }
                tool.usages(usages);
                info.showTool(player, tool);
            }
            case "permission" -> {
                tool.permission(value.asString());
                info.showTool(player, tool);
            }
            default -> throw CommandException.message("error.invalidfieldname");
        }

    }

    private Optional<OfflinePlayer> playerByName(String name) {
        for (var offlinePlayer : plugin().getServer().getOfflinePlayers()) {
            if (name.equalsIgnoreCase(offlinePlayer.getName())) {
                return Optional.of(offlinePlayer);
            }
        }
        return Optional.empty();
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Player player, @NotNull String alias, @NotNull Arguments args) throws CommandException {

        if (args.sizeIs(1)) {
            return configuration.tools().complete(args.asString(0));
        }

        if (args.sizeIs(2)) {
            var field = args.asString(1);

            if ("o".equalsIgnoreCase(args.flags().lastFlag())) {
                return Completion.completePlayers(args.flags().getIfPresent("o").map(Input::asString).orElse(""));
            }

            switch (field.toLowerCase()) {
                case "brushname" -> {
                    return sbr.storageRegistry().activeStorage().brushes().complete(player, args.get(1).asString());
                }
                case "name" -> {
                    var name = args.get(0).asString();
                    CommandAssertions.isFalse(configuration.tools().byName(name).isPresent(), "Name is already taken.");
                    return Completion.completeFreeInput(name, 32, "name");

                }
                case "usages" -> {
                    return Completion.completeMinInt(args.flags().getIfPresent("u").map(Input::asString).orElse(""), 1);

                }
                case "permission" -> {
                    return Collections.singletonList("permission");
                }
                default -> {
                    return Collections.singletonList("Invalid field");
                }
            }

        }

        return Collections.emptyList();
    }
}
