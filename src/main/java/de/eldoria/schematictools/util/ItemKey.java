/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */
package de.eldoria.schematictools.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * A composite identifier for an item consisting of its base {@link Material} and an
 * optional {@code ItemsAdder} namespaced id.
 * <p>
 * Two custom {@code ItemsAdder} items that share the same base material (for example
 * two different items both built on {@code STICK}) would otherwise collide when the
 * underlying brush tool is bound per material by WorldEdit. The {@code ItemsAdder} id
 * disambiguates them so switching between them can be tracked precisely.
 *
 * @param material the base {@link Material} of the item
 * @param iaId     the {@code ItemsAdder} namespaced id, or {@code null} for vanilla items
 */
public record ItemKey(Material material, @Nullable String iaId) {

    /**
     * Builds an {@link ItemKey} from an item stack. If the stack is a custom
     * {@code ItemsAdder} item its namespaced id is included.
     *
     * @param stack the item stack
     * @return the composite key
     */
    public static ItemKey of(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) {
            return new ItemKey(Material.AIR, null);
        }
        return new ItemKey(stack.getType(), ItemsAdderBridge.getNamespacedId(stack).orElse(null));
    }

    /**
     * Whether this key refers to a custom {@code ItemsAdder} item.
     *
     * @return {@code true} if an {@code ItemsAdder} id is present
     */
    public boolean isCustom() {
        return iaId != null && !iaId.isBlank();
    }

    /**
     * Whether this key refers to an empty/air item.
     *
     * @return {@code true} if the material is air
     */
    public boolean isAir() {
        return material == Material.AIR;
    }

    /**
     * Whether the other key shares the same base material with this key.
     *
     * @param other the other key
     * @return {@code true} if both use the same base material
     */
    public boolean sameMaterial(ItemKey other) {
        return other != null && material == other.material;
    }

    @Override
    public String toString() {
        return isCustom() ? material + ":" + iaId : material.toString();
    }
}
