/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */
package de.eldoria.schematictools.configuration.elements;

/**
 * Identifies the source of the item a tool is bound to.
 * <p>
 * {@link #VANILLA} describes a regular Minecraft item, while {@link #ITEMS_ADDER}
 * describes a custom item provided by the {@code ItemsAdder} plugin. The value is
 * recorded per tool in {@code tools.yml} and drives the fallback behaviour when a
 * custom item is no longer available.
 */
public enum ItemSource {
    /**
     * A regular vanilla Minecraft item.
     */
    VANILLA,
    /**
     * A custom item provided by the {@code ItemsAdder} plugin.
     */
    ITEMS_ADDER
}
