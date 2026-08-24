/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */
package de.eldoria.schematictools.configuration;

import de.eldoria.eldoutilities.configuration.EldoConfig;
import de.eldoria.schematictools.configuration.elements.ToolRemoval;
import de.eldoria.schematictools.configuration.elements.Tools;
import org.bukkit.plugin.Plugin;

public interface Configuration {

    void save();
    Tools tools();
    ToolRemoval toolRemoval();

    /**
     * Reloads all configuration files from disk.
     * <p>
     * Additive capability used by the {@code /sbt reload all} command. Backends that
     * do not support reloading throw an {@link UnsupportedOperationException} so that
     * the behaviour stays explicit instead of silently doing nothing.
     */
    default void reloadAll() {
        throw new UnsupportedOperationException("Reloading all configuration is not supported by this backend.");
    }

    /**
     * Reloads only {@code tools.yml} from disk.
     * <p>
     * Additive capability used by the {@code /sbt reload tools} command.
     */
    default void reloadTools() {
        throw new UnsupportedOperationException("Reloading tools.yml is not supported by this backend.");
    }

    /**
     * Persists the current in-memory {@code tools.yml} to disk.
     * <p>
     * Additive capability used by the {@code /sbt save tools} command.
     */
    default void saveTools() {
        save();
    }
}
