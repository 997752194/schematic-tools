/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */
package de.eldoria.schematictools.util;

import de.eldoria.eldoutilities.pdc.DataContainerUtil;
import de.eldoria.schematictools.configuration.elements.Tool;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class SchematicTool {

    public static final NamespacedKey TOOL_ID = new NamespacedKey("schematictools", "tool_id");
    public static final NamespacedKey USED = new NamespacedKey("schematictools", "used");
    public static final NamespacedKey UNIQUE = new NamespacedKey("schematictools", "unique");
    public static final NamespacedKey LORE_INDEX = new NamespacedKey("schematictools", "lore_index");

    private SchematicTool() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    public static ItemStack getPlayerItem(Player player) {
        return player.getInventory().getItemInMainHand();
    }

    public static Optional<ToolMeta> getCurrentTool(Player player) {
        return getTool(getPlayerItem(player));
    }

    public static Optional<Integer> getToolId(ItemStack stack) {
        return DataContainerUtil.get(stack, TOOL_ID, PersistentDataType.INTEGER);
    }

    public static @Nullable Integer getUsed(ItemStack stack) {
        return DataContainerUtil.computeIfAbsent(stack, USED, PersistentDataType.INTEGER, 0);
    }

    public static Optional<ToolMeta> getTool(ItemStack stack) {
        var toolId = getToolId(stack);
        if (toolId.isEmpty()) return Optional.empty();
        var used = getUsed(stack);
        return Optional.of(new ToolMeta(toolId.get(), used, stack));
    }

    public static void makeUnique(ItemStack stack) {
        DataContainerUtil.putValue(stack, UNIQUE, PersistentDataType.STRING, String.valueOf(System.currentTimeMillis()));
    }

    public static void setToolId(ItemStack stack, int id) {
        DataContainerUtil.putValue(stack, TOOL_ID, PersistentDataType.INTEGER, id);
    }

    public static void initTool(ItemStack stack, Tool tool) {
        setToolId(stack, tool.id());
        makeUnique(stack);
        setUsed(stack, 0);
        applyDisplay(stack, tool);
        applyMaterial(stack, tool);
        updateUsage(stack, tool);
    }

    /**
     * Records the base material of the bound item onto the tool if the tool does not
     * have a fallback material configured yet.
     * <p>
     * This is an additive extension of {@link #initTool}. The item's {@link
     * ItemStack#getType()} always reflects the underlying vanilla material — for an
     * {@code ItemsAdder} custom item this is the vanilla material it is built on, which
     * is exactly the fallback base material to store. When the {@link Tool} already
     * declares a material (e.g. pre-configured by an administrator) it is left untouched.
     *
     * @param stack the item stack being bound to the tool
     * @param tool  the tool whose material should be recorded
     */
    public static void applyMaterial(ItemStack stack, Tool tool) {
        if (tool.material().isPresent()) return;
        tool.material(stack.getType());
    }

    /**
     * Applies the tool's custom {@code displayName} and {@code lore} to the given item
     * stack. Both support hex ({@code &#RRGGBB}) and legacy colour codes.
     * <p>
     * This is an additive extension of {@link #initTool}; if no display data is
     * configured on the tool the item stack is left untouched.
     *
     * @param stack the item stack to modify
     * @param tool  the tool whose display data should be applied
     */
    public static void applyDisplay(ItemStack stack, Tool tool) {
        var meta = stack.getItemMeta();
        if (meta == null) return;

        boolean changed = false;
        var displayName = tool.displayName();
        if (displayName != null && !displayName.isBlank()) {
            meta.setDisplayName(ColorCodeUtil.translate(displayName));
            changed = true;
        }

        if (!tool.lore().isEmpty()) {
            meta.setLore(ColorCodeUtil.translate(tool.lore()));
            changed = true;
        }

        if (changed) {
            stack.setItemMeta(meta);
        }
    }

    public static void setUsed(ItemStack stack, int value) {
        DataContainerUtil.putValue(stack, USED, PersistentDataType.INTEGER, value);
    }

    public static void incrementUsage(ItemStack stack) {
        DataContainerUtil.computeIfPresent(stack, USED, PersistentDataType.INTEGER, v -> v + 1);
    }

    public static void updateUsage(ItemStack stack, Tool tool) {
        if (!tool.hasUsage()) {
            DataContainerUtil.get(stack, LORE_INDEX, PersistentDataType.INTEGER).ifPresent(index -> {
                var meta = stack.getItemMeta();
                var lore = meta.getLore();
                lore.remove(index.intValue());
                DataContainerUtil.remove(meta, LORE_INDEX, PersistentDataType.INTEGER);
                stack.setItemMeta(meta);
            });
            return;
        }

        var loreIndex = DataContainerUtil.get(stack, LORE_INDEX, PersistentDataType.INTEGER);
        var meta = stack.getItemMeta();
        List<String> lore;
        if (meta.hasLore()) {
            lore = meta.getLore();
        } else {
            lore = new ArrayList<>();
        }

        var usedLine = String.format(I18n.get().resolve("words.used"), getUsed(stack), tool.usages());
        loreIndex.ifPresentOrElse(index -> lore.set(index, usedLine),
                () -> {
                    DataContainerUtil.putValue(meta, LORE_INDEX, PersistentDataType.INTEGER, lore.size());
                    lore.add(usedLine);
                });
        meta.setLore(lore);
        stack.setItemMeta(meta);
    }

    public record ToolMeta(int id, int usages, ItemStack stack) {

        public void updateUsage(Tool tool) {
            SchematicTool.updateUsage(stack, tool);
        }

        public void incrementUsage() {
            SchematicTool.incrementUsage(stack);
        }
    }
}
