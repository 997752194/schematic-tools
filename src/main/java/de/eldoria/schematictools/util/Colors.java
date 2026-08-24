/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */
package de.eldoria.schematictools.util;

import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Class holding some colors sorted by category.
 * <p>
 * This is a local copy of the {@code Colors} class that was removed from
 * SchematicBrushReborn API in version 2.7.10. The constants are identical.
 */
public final class Colors {
    public static final String HEADING = NamedTextColor.GOLD.toString();
    public static final String NAME = NamedTextColor.DARK_AQUA.toString();
    public static final String VALUE = NamedTextColor.DARK_GREEN.toString();
    public static final String CHANGE = NamedTextColor.YELLOW.toString();
    public static final String REMOVE = NamedTextColor.RED.toString();
    public static final String ADD = NamedTextColor.GREEN.toString();
    public static final String NEUTRAL = NamedTextColor.AQUA.toString();
    public static final String INACTIVE = NamedTextColor.GRAY.toString();

    private Colors() {
        throw new UnsupportedOperationException("This is a utility class.");
    }
}
