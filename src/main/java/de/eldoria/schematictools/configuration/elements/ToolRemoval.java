/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */
package de.eldoria.schematictools.configuration.elements;

import de.eldoria.eldoutilities.serialization.SerializationUtil;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@SerializableAs("stToolRemoval")
public class ToolRemoval implements ConfigurationSerializable {
    private final boolean removeUsed;
    private final boolean removeInvalidTools;
    private final boolean removeInvalidBrushes;

    /**
     * Constructor required by {@link ConfigurationSerializable} in order to deserialize the object.
     */
    @SuppressWarnings("unused")
    public ToolRemoval(Map<String, Object> objectMap) {
        var map = SerializationUtil.mapOf(objectMap);
        removeUsed = map.getValueOrDefault("removeUsed", false);
        removeInvalidTools = map.getValueOrDefault("removeInvalidTools", false);
        removeInvalidBrushes = map.getValueOrDefault("removeInvalidBrushes", false);
    }

    public ToolRemoval() {
        removeUsed = false;
        removeInvalidTools = false;
        removeInvalidBrushes = false;
    }

    @Override
    @NotNull
    public Map<String, Object> serialize() {
        return SerializationUtil.newBuilder()
                .add("removeUsed", removeUsed)
                .add("removeInvalidTools", removeInvalidTools)
                .add("removeInvalidBrushes", removeInvalidBrushes)
                .build();
    }

    public boolean isRemoveUsed() {
        return removeUsed;
    }

    public boolean isRemoveInvalidTools() {
        return removeInvalidTools;
    }

    public boolean isRemoveInvalidBrushes() {
        return removeInvalidBrushes;
    }
}
