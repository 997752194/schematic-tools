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
import de.eldoria.eldoutilities.commands.executor.ITabExecutor;
import de.eldoria.eldoutilities.localization.MessageComposer;
import de.eldoria.schematictools.configuration.Configuration;
import de.eldoria.schematictools.configuration.elements.ItemSource;
import de.eldoria.schematictools.configuration.elements.Tool;
import de.eldoria.schematictools.util.I18n;
import de.eldoria.schematictools.util.ItemsAdderBridge;
import de.eldoria.schematictools.util.Permissions;

import java.util.Map;
import de.eldoria.schematictools.util.SchematicTool;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

// Hand a pre-bound Schematic Tool item to a player.
// Usage: /sbt give <player> <tool-name> [amount]
public class Give extends AdvancedCommand implements ITabExecutor {
    private final Configuration configuration;

    public Give(Plugin plugin, Configuration configuration) {
        super(plugin, CommandMeta.builder("give")
                .addUnlocalizedArgument("player", true)
                .addUnlocalizedArgument("tool", true)
                .addArgument("amount", false)
                .withPermission(Permissions.GIVE)
                .build());
        this.configuration = configuration;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String alias, @NotNull Arguments args) throws CommandException {
        Player target = args.asPlayer(0);
        String toolName = args.asString(1);

        var byName = configuration.tools().byName(toolName);
        CommandAssertions.isTrue(byName.isPresent(), "error.toolnotfound");
        Tool tool = byName.get();

        ItemStack stack = createToolItem(tool);
        CommandAssertions.isFalse(stack.getType().isAir(), "error.cannotbind");

        int amount = args.asInt(2, 1);
        if (amount < 1) amount = 1;
        stack.setAmount(Math.min(amount, stack.getMaxStackSize()));

        SchematicTool.initTool(stack, tool);

        // Drop leftovers (full inventory) next to the player instead of losing them.
        target.getInventory().addItem(stack).values()
                .forEach(rest -> target.getWorld().dropItemNaturally(target.getLocation(), rest));

        messageSender().sendMessage(sender, MessageComposer.create()
                .text(I18n.get().resolveFormatted("commands.give.given",
                        Map.of("p", target.getName(), "tool", tool.name()))));
    }

    /**
     * Builds the base item for the tool.
     * <p>
     * For {@link ItemSource#ITEMS_ADDER} tools the custom item is resolved through the
     * {@code ItemsAdder} bridge; if the custom item is no longer available the recorded
     * fallback material is used instead. For vanilla tools the recorded {@code material}
     * is used directly.
     */
    private ItemStack createToolItem(Tool tool) {
        if (tool.itemSource() == ItemSource.ITEMS_ADDER) {
            Optional<String> iaId = Optional.ofNullable(tool.iaId());
            if (iaId.isPresent()) {
                Optional<ItemStack> custom = ItemsAdderBridge.getItemStack(iaId.get());
                if (custom.isPresent() && !custom.get().getType().isAir()) return custom.get();
            }
        }
        Material material = tool.material().orElse(Material.AIR);
        return new ItemStack(material);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull Arguments args) throws CommandException {
        if (args.sizeIs(1)) {
            return null; // Bukkit player name completion
        }
        if (args.sizeIs(2)) {
            return configuration.tools().complete(args.asString(1));
        }
        return Collections.emptyList();
    }
}
