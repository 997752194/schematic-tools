/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */
package de.eldoria.schematictools.configuration;

import de.eldoria.eldoutilities.config.ConfigKey;
import de.eldoria.eldoutilities.config.JacksonConfig;
import de.eldoria.schematictools.configuration.elements.ToolRemoval;
import de.eldoria.schematictools.configuration.elements.Tools;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class JacksonConfiguration extends JacksonConfig<ConfigFile> implements Configuration {
    public static final ConfigKey<Tools> TOOLS = ConfigKey.of("Tools", Path.of("tools.yml"), Tools.class, Tools::new);

    public JacksonConfiguration(@NotNull Plugin plugin) {
        super(plugin, ConfigKey.defaultConfig(ConfigFile.class, ConfigFile::new));
    }

    @Override
    public Tools tools() {
        return secondary(TOOLS);
    }

    @Override
    public ToolRemoval toolRemoval() {
        return main().toolRemoval();
    }

    @Override
    public void reloadAll() {
        reload();
    }

    @Override
    public void reloadTools() {
        reload(TOOLS);
    }

    @Override
    public void saveTools() {
        save(TOOLS);
    }
}
