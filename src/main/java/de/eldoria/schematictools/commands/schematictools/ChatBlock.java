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
import de.eldoria.eldoutilities.commands.executor.IPlayerTabExecutor;
import de.eldoria.messageblocker.blocker.MessageBlocker;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ChatBlock extends AdvancedCommand implements IPlayerTabExecutor {
    private final MessageBlocker messageBlocker;

    public ChatBlock(Plugin plugin, MessageBlocker messageBlocker) {
        super(plugin, CommandMeta.builder("chatblock")
                .addUnlocalizedArgument("state", true)
                .hidden()
                .build());
        this.messageBlocker = messageBlocker;
    }

    @Override
    public void onCommand(@NotNull Player player, @NotNull String alias, @NotNull Arguments args) throws CommandException {
        if (args.asBoolean(0)) {
            messageBlocker.unblockPlayer(player);
        } else {
            messageBlocker.blockPlayer(player);
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Player player, @NotNull String alias, @NotNull Arguments args) throws CommandException {
        return IPlayerTabExecutor.super.onTabComplete(player, alias, args);
    }
}
