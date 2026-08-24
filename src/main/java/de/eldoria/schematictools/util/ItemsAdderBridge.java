/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */
package de.eldoria.schematictools.util;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Reflection based bridge to the {@code ItemsAdder} plugin.
 * <p>
 * The {@code ItemsAdder} API is deliberately accessed via reflection so that this
 * plugin has no hard dependency on it. If {@code ItemsAdder} is not installed, all
 * methods simply fall back to vanilla behaviour and the original binding logic keeps
 * working untouched.
 */
public final class ItemsAdderBridge {
    private static final String PLUGIN_NAME = "ItemsAdder";
    private static final String CUSTOM_STACK_CLASS = "dev.lone.itemsadder.api.CustomStack";

    private static boolean loaded;
    private static boolean resolved;
    private static Class<?> customStackClass;
    private static Method byItemStack;
    private static Method getInstance;
    private static Method getNamespacedID;
    private static Method getItemStack;

    private ItemsAdderBridge() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    /**
     * Resolves the {@code ItemsAdder} API classes and methods lazily.
     * <p>
     * This is intentionally called on every access to detect plugins being loaded
     * after server start (e.g. via plugin manager reloads).
     */
    private static synchronized void resolve() {
        boolean present = Bukkit.getPluginManager().isPluginEnabled(PLUGIN_NAME);
        if (resolved && present == loaded) return;

        resolved = false;
        loaded = present;

        if (!present) {
            customStackClass = null;
            byItemStack = null;
            getInstance = null;
            getNamespacedID = null;
            getItemStack = null;
            return;
        }

        try {
            customStackClass = Class.forName(CUSTOM_STACK_CLASS);
            byItemStack = customStackClass.getMethod("byItemStack", ItemStack.class);
            getInstance = customStackClass.getMethod("getInstance", String.class);
            getNamespacedID = customStackClass.getMethod("getNamespacedID");
            getItemStack = customStackClass.getMethod("getItemStack");
            resolved = true;
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
            loaded = false;
            resolved = true;
            customStackClass = null;
            byItemStack = null;
            getInstance = null;
            getNamespacedID = null;
            getItemStack = null;
        }
    }

    /**
     * Whether the {@code ItemsAdder} plugin and its API are currently available.
     *
     * @return {@code true} if the plugin is loaded and the API could be resolved
     */
    public static boolean isLoaded() {
        resolve();
        return loaded && resolved;
    }

    /**
     * Whether the given item stack is a custom {@code ItemsAdder} item.
     *
     * @param stack the item stack to check
     * @return {@code true} if the stack is a custom ItemsAdder item
     */
    public static boolean isCustomItem(ItemStack stack) {
        if (!isLoaded() || stack == null || stack.getType().isAir()) return false;
        return getNamespacedId(stack).isPresent();
    }

    /**
     * Resolves the {@code ItemsAdder} namespaced id (e.g. {@code namespace:id}) of the
     * given item stack, if it is a custom item.
     *
     * @param stack the item stack to check
     * @return an {@link Optional} holding the namespaced id, or empty if not a custom item
     */
    public static Optional<String> getNamespacedId(ItemStack stack) {
        if (!isLoaded() || stack == null || stack.getType().isAir()) return Optional.empty();
        try {
            Object customStack = byItemStack.invoke(null, stack);
            if (customStack == null) return Optional.empty();
            Object id = getNamespacedID.invoke(customStack);
            if (id == null) return Optional.empty();
            return Optional.of((String) id);
        } catch (IllegalAccessException | InvocationTargetException | RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * Returns a {@code ItemsAdder} custom item stack for the given namespaced id.
     * <p>
     * If the item is no longer available (e.g. removed or unregistered) an empty
     * {@link Optional} is returned.
     *
     * @param namespacedId the namespaced id of the custom item
     * @return the resolved item stack, or empty if unavailable
     */
    public static Optional<ItemStack> getItemStack(String namespacedId) {
        if (!isLoaded() || namespacedId == null || namespacedId.isBlank()) return Optional.empty();
        try {
            Object customStack = getInstance.invoke(null, namespacedId);
            if (customStack == null) return Optional.empty();
            Object itemStack = getItemStack.invoke(customStack);
            if (itemStack instanceof ItemStack stack) return Optional.of(stack);
            return Optional.empty();
        } catch (IllegalAccessException | InvocationTargetException | RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * Returns a friendly display string for the given item stack, preferring the
     * {@code ItemsAdder} namespaced id when available and falling back to the material.
     *
     * @param stack the item stack
     * @return a non-null descriptive string
     */
    public static String describe(@Nullable ItemStack stack) {
        if (stack == null || stack.getType().isAir()) return "air";
        return getNamespacedId(stack).orElseGet(() -> stack.getType().getKey().toString());
    }
}
