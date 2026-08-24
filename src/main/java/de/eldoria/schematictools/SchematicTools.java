/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */
package de.eldoria.schematictools;

import de.eldoria.eldoutilities.config.template.PluginBaseConfiguration;
import de.eldoria.eldoutilities.localization.ILocalizer;
import de.eldoria.eldoutilities.localization.Localizer;
import de.eldoria.eldoutilities.messages.MessageSender;
import de.eldoria.eldoutilities.plugin.EldoPlugin;
import de.eldoria.messageblocker.MessageBlockerAPI;
import de.eldoria.schematicbrush.SchematicBrushReborn;
import de.eldoria.schematictools.commands.BaseCommand;
import de.eldoria.schematictools.configuration.Configuration;
import de.eldoria.schematictools.configuration.JacksonConfiguration;
import de.eldoria.schematictools.configuration.LegacyConfiguration;
import de.eldoria.schematictools.configuration.elements.Tool;
import de.eldoria.schematictools.configuration.elements.ToolRemoval;
import de.eldoria.schematictools.configuration.elements.Tools;
import de.eldoria.schematictools.listener.BrushBindListener;
import de.eldoria.schematictools.listener.BrushPasteListener;
import de.eldoria.schematictools.listener.ItemsAdderBindListener;
import de.eldoria.schematictools.listener.ToolCommandListener;
import de.eldoria.schematictools.util.BrushToolSessionCache;
import de.eldoria.schematictools.util.ForkUpdateChecker;
import de.eldoria.schematictools.util.I18n;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;

public class SchematicTools extends EldoPlugin {
    private JacksonConfiguration configuration = new JacksonConfiguration(this);
    private final BrushToolSessionCache brushToolSessionCache = new BrushToolSessionCache();

    public Level getLogLevel() {
        return configuration.secondary(PluginBaseConfiguration.KEY).logLevel();
    }

    @Override
    public void onPluginEnable() throws Throwable {
        var sbr = SchematicBrushReborn.instance();
        var messageSender = MessageSender.builder(this).prefix("<gold>[ST]").register();
        var messageBlocker = MessageBlockerAPI.builder(this).addWhitelisted("[ST]").build();
        // Resolve the active language first (auto detects the server system language).
        I18n.init(this, configuration.main().language());
        // Route the eldoutilities Localizer's language files into the same localization/
        // folder used by the standalone I18n utility. The fallback locale follows the
        // configured / auto-detected language and both English + Simplified Chinese are
        // included, so framework messages (e.g. "error.invalidArguments" -> "Invalid
        // arguments" / "无效参数") are localized instead of always falling back to English.
        Localizer.builder(this, I18n.get().language())
                .setIncludedLocales("en_US", "zh_CN")
                .setLocalesPath("localization")
                .build();
        PluginBaseConfiguration base = configuration.secondary(PluginBaseConfiguration.KEY);
        if (base.version() == 0) {
            var legacyConfiguration = new LegacyConfiguration(this);
            getLogger().log(Level.INFO, I18n.get().resolve("console.migrating"));
            configuration.main().toolRemoval(legacyConfiguration.toolRemoval());
            // Fork fix: only migrate the tools list while tools.yml still uses the old
            // Bukkit/ConfigurationSerializable format. Once it has been written in the
            // Jackson format used by this build, overwriting it would reset every stored
            // tool on every restart (Jackson YAML cannot be read back by the Bukkit loader).
            if (isLegacyToolsFile()) {
                configuration.replace(JacksonConfiguration.TOOLS, legacyConfiguration.tools());
            }
            base.version(1);
            base.lastInstalledVersion(this);
            configuration.save();
        }

        // Fork build: the upstream Lyna update station (hard-coded plugin id 6) is no
        // longer used. Instead the update-check URL is defined in config.yml via
        // "update-url". When blank, no update check is performed.
        if (configuration.main().updateCheck()) {
            ForkUpdateChecker.check(this, getDescription().getVersion(), configuration.main().updateUrl());
        }

        registerCommand(new BaseCommand(this, sbr, configuration, messageBlocker));

        registerListener(new BrushBindListener(this, sbr, configuration, messageSender),
                new BrushPasteListener(this, configuration, messageSender),
                new ItemsAdderBindListener(brushToolSessionCache),
                new ToolCommandListener(this, configuration));
    }

    @Override
    public List<Class<? extends ConfigurationSerializable>> getConfigSerialization() {
        return List.of(Tools.class, Tool.class, ToolRemoval.class);
    }

    @Override
    public void onPluginDisable() throws Throwable {
        configuration.save();
    }

    /**
     * Detects whether {@code tools.yml} is still in the legacy Bukkit /
     * {@code ConfigurationSerializable} format (it contains a {@code ==:}
     * serializable marker). When the file is missing (fresh install) it is treated as
     * legacy so the initial migration can create it. A file already written by the
     * Jackson-based {@link JacksonConfiguration} does not contain such a marker and is
     * therefore preserved instead of being overwritten by the migration.
     *
     * @return {@code true} when the tools file may safely be migrated from legacy data
     */
    private boolean isLegacyToolsFile() {
        File toolsFile = new File(getDataFolder(), "tools.yml");
        if (!toolsFile.exists()) {
            return true;
        }
        try {
            String content = Files.readString(toolsFile.toPath(), StandardCharsets.UTF_8);
            return content.contains("==") || content.contains("stTools");
        } catch (IOException e) {
            // If we cannot read it, err on the side of migrating from legacy data.
            return true;
        }
    }
}
