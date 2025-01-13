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
import de.eldoria.eldoutilities.utils.Consumers;
import de.eldoria.eldoutilities.utils.Futures;
import de.eldoria.schematicbrush.SchematicBrushReborn;
import de.eldoria.schematictools.commands.schematictools.util.BrushLoader;
import de.eldoria.schematictools.configuration.Configuration;
import de.eldoria.schematictools.configuration.builder.SchematicToolBuilder;
import de.eldoria.schematictools.util.Permissions;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

// Create a new schematic tool configuration
public class Create extends AdvancedCommand implements IPlayerTabExecutor {
    private final SchematicBrushReborn sbr;
    private final Configuration configuration;

    public Create(Plugin plugin, SchematicBrushReborn sbr, Configuration configuration) {
        super(plugin, CommandMeta.builder("create")
                                 .addUnlocalizedArgument("name", true)
                                 .addUnlocalizedArgument("brush", true)
                                 .addUnlocalizedArgument("-p permission.node", false)
                                 .addUnlocalizedArgument("-u usages", false)
                                 .addUnlocalizedArgument("-o owner", false)
                                 .withPermission(Permissions.MANAGE)
                                 .build());
        this.sbr = sbr;
        this.configuration = configuration;
    }


    @Override
    public void onCommand(@NotNull Player player, @NotNull String alias, @NotNull Arguments args) throws CommandException {
        args.parseQuoted();

        var toolName = args.get(0).asString();
        CommandAssertions.isFalse(configuration.tools().byName(toolName).isPresent(), "error.nametaken");

        var brushName = args.get(1).asString();
        var storage = sbr.storageRegistry().activeStorage();
        var brushContainer = BrushLoader.getContainer(player, storage, brushName, args);

        brushContainer.get(brushName).thenAccept(brush -> {
            SchematicToolBuilder builder;
            builder = configuration.tools().create(brushContainer.owner(), toolName, brushName);

            args.flags().getIfPresent("p").ifPresent(input -> builder.withPermission(input.asString()));

            if (args.flags().hasValue("u")) {
                var usages = args.flags().get("u").asInt();
                CommandAssertions.min(usages, 1);
                builder.withUsages(usages);
            }

            configuration.tools().add(builder.build());
            messageSender().sendMessage(player, "commands.create.created");
            configuration.save();
        }).whenComplete(Futures.whenComplete(Consumers.emptyConsumer(), err -> handleCommandError(player, err)));
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
        args.parseQuoted();
        if ("p".equalsIgnoreCase(args.flags().lastFlag())) {
            return Collections.singletonList("permission");
        }

        if ("u".equalsIgnoreCase(args.flags().lastFlag())) {
            return Completion.completeMinInt(args.flags().getIfPresent("u").map(Input::asString).orElse(""), 1);
        }

        if ("o".equalsIgnoreCase(args.flags().lastFlag())) {
            return Completion.completePlayers(args.flags().getIfPresent("o").map(Input::asString).orElse(""));
        }

        if (args.sizeIs(1)) {
            var name = args.get(0).asString();
            CommandAssertions.isFalse(configuration.tools().byName(name).isPresent(), "error.nametaken");
            return Completion.completeFreeInput(name, 32, "name");
        }

        if (args.sizeIs(2)) {
            return sbr.storageRegistry().activeStorage().brushes().complete(player, args.get(1).asString());
        }

        return Collections.emptyList();
    }
}
