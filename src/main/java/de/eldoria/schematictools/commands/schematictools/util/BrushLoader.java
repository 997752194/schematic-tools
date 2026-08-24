/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */
package de.eldoria.schematictools.commands.schematictools.util;

import de.eldoria.eldoutilities.commands.command.util.Arguments;
import de.eldoria.eldoutilities.commands.command.util.CommandAssertions;
import de.eldoria.schematicbrush.storage.Storage;
import de.eldoria.schematicbrush.storage.brush.BrushContainer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

public class BrushLoader {
    private static final Pattern UUID_PATTERN = Pattern.compile("[[:xdigit:]]{8}(-[[:xdigit:]]{4}){3}-[[:xdigit:]]{12}");

    public static BrushContainer getContainer(Player player, Storage storage, String name, Arguments args) {
        // Check for container by name
        var brushContainer = storage.brushes().containerByName(player, name);

        UUID owner = null;
        if (!brushContainer.isGlobalContainer() && args.flags().hasValue("o")) {
            var ownerArg = args.flags().get("o");
            var optOwner = playerByName(ownerArg.asString()).map(OfflinePlayer::getUniqueId).or(() -> getAsUUID(ownerArg.asString()));
            CommandAssertions.isTrue(optOwner.isPresent(), "error.noowner");
            owner = optOwner.get();
            brushContainer = storage.brushes().playerContainer(owner);
        }

        return brushContainer;
    }

    private static Optional<UUID> getAsUUID(String uuid) {
        if (UUID_PATTERN.matcher(uuid).matches()) {
            return Optional.of(UUID.fromString(uuid));
        }
        return Optional.empty();
    }

    private static Optional<OfflinePlayer> playerByName(String name) {
        for (var offlinePlayer : Bukkit.getServer().getOfflinePlayers()) {
            if (name.equalsIgnoreCase(offlinePlayer.getName())) {
                return Optional.of(offlinePlayer);
            }
        }
        return Optional.empty();
    }
}
