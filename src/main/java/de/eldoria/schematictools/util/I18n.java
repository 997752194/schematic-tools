/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */
package de.eldoria.schematictools.util;

import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

/**
 * Lightweight, standalone internationalization support.
 * <p>
 * Messages are resolved from a user-editable {@code properties} file inside the plugin
 * data folder (e.g. {@code localization/messages_zh_CN.properties}). The file bundled
 * inside the jar acts as the fallback and is copied to disk on first start so admins can
 * tweak or translate it without rebuilding the plugin.
 * <p>
 * - Console/plain messages: {@link #resolve(String, Object...)}
 * - Player messages with MiniMessage styling: {@link #resolveFormatted(String, Object...)}
 *   keeps the colour/style tags intact so the result can be handed to
 *   {@code MessageComposer.text(...)}.
 */
public final class I18n {
    private static I18n instance;

    private final Plugin plugin;
    private final String language;
    private final Map<String, String> bundle = new ConcurrentHashMap<>();
    private final Properties fallback;

    private I18n(Plugin plugin, String language) {
        this.plugin = plugin;
        this.language = language;
        this.fallback = new Properties();
        this.fallback.put("_key", "value");
        loadFallback(language);
        copyToDisk(language);
        loadDisk(language);
        instance = this;
    }

    /**
     * Initializes the global {@link I18n} instance. Should be called once during
     * {@code onPluginEnable}.
     * <p>
     * If {@code language} is {@code auto} (or null/blank), the server system language is
     * detected from {@link Locale#getDefault()} and matched against the bundled
     * translations. A fallback of {@code en_US} is used when no match is found.
     *
     * @param plugin   the plugin instance
     * @param language the locale to load, e.g. {@code en_US} or {@code zh_CN}, or
     *                 {@code auto} for system detection
     */
    public static void init(Plugin plugin, String language) {
        new I18n(plugin, resolveLanguage(plugin, language));
    }

    /**
     * Returns the global instance.
     *
     * @return the initialized {@link I18n}
     */
    public static I18n get() {
        return instance;
    }

    private static String resolveLanguage(Plugin plugin, String language) {
        String lang = language == null ? "auto" : language.trim();
        if (!lang.equalsIgnoreCase("auto")) {
            return normalize(lang);
        }
        String system = Locale.getDefault().toLanguageTag().replace('-', '_');
        // Match either full tag (zh_CN) or the bare language part (zh).
        if (plugin.getResource("messages_" + system + ".properties") != null) {
            return normalize(system);
        }
        String bare = Locale.getDefault().getLanguage();
        if (plugin.getResource("messages_" + bare + ".properties") != null) {
            return normalize(bare);
        }
        return "en_US";
    }

    private static String normalize(String language) {
        return language == null || language.isBlank() ? "en_US" : language.trim();
    }

    private void loadFallback(String lang) {
        String resource = "messages_" + lang + ".properties";
        try (InputStream in = plugin.getResource(resource)) {
            if (in != null) {
                fallback.load(new java.io.InputStreamReader(in, StandardCharsets.UTF_8));
                return;
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to read bundled resource " + resource);
        }
        // Fall back to the default English bundle bundled with the plugin.
        try (InputStream in = plugin.getResource("messages_en_US.properties")) {
            if (in != null) {
                fallback.load(new java.io.InputStreamReader(in, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to read bundled resource messages_en_US.properties");
        }
    }

    /**
     * Copies the bundled language file(s) into the data folder so they can be edited by
     * the server operator. Existing files are kept (operators may have customized them).
     * <p>
     * The English default and the active language are both placed under
     * {@code localization/}, so {@code messages_en_US.properties} and
     * {@code messages_<lang>.properties} always live side by side in one folder.
     */
    private void copyToDisk(String lang) {
        Path folder = plugin.getDataFolder().toPath().resolve("localization");
        try {
            Files.createDirectories(folder);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not create localization folder: " + e.getMessage());
            return;
        }
        copyOne(folder, "messages_en_US.properties");
        if (!lang.equals("en_US")) {
            copyOne(folder, "messages_" + lang + ".properties");
        }
    }

    private void copyOne(Path folder, String fileName) {
        Path target = folder.resolve(fileName);
        if (Files.exists(target)) {
            return;
        }
        try (InputStream in = plugin.getResource(fileName)) {
            if (in == null) {
                return;
            }
            Files.copy(in, target);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not copy localization file " + fileName + ": " + e.getMessage());
        }
    }

    private void loadDisk(String lang) {
        Path target = plugin.getDataFolder().toPath().resolve("localization").resolve("messages_" + lang + ".properties");
        if (!Files.exists(target)) {
            return;
        }
        Properties props = new Properties();
        try (Reader reader = Files.newBufferedReader(target, StandardCharsets.UTF_8)) {
            props.load(reader);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not read localization file: " + e.getMessage());
            return;
        }
        for (String key : props.stringPropertyNames()) {
            bundle.put(key, props.getProperty(key));
        }
    }

    private String raw(String key) {
        String value = bundle.get(key);
        if (value == null) {
            value = fallback.getProperty(key);
        }
        return value == null ? key : value;
    }

    /**
     * Resolves a plain message key and formats the given arguments using
     * {@code %s} placeholders. Suitable for console output where MiniMessage tags are
     * not needed.
     *
     * @param key  the message key
     * @param args the formatting arguments. Positional ({@code %s}, {@code %d}, ...)
     *             and, optionally, a trailing {@link Map} of named placeholders
     *             (e.g. {@code %p}, {@code %tool}) that are replaced before formatting.
     * @return the resolved and formatted message
     */
    public String resolve(String key, Object... args) {
        String template = raw(key);
        if (args.length == 0) {
            return template;
        }
        return format(template, args, false);
    }

    /**
     * Resolves a message key, keeping any MiniMessage tags ({@code <value>},
     * {@code <default>}, colour tags, ...) intact, and pre-formats the {@code %s}
     * placeholder arguments. The result is meant to be passed to
     * {@code MessageComposer.text(resolved)} so the rich text format is preserved.
     * <p>
     * Since {@code MessageComposer.text(String, Object...)} applies
     * {@link String#format} again, any literal {@code %} produced by the substitution
     * is escaped to {@code %%} so it survives the second formatting pass unchanged.
     *
     * @param key  the message key
     * @param args the placeholder arguments ({@code %s}, ...), plus an optional
     *             trailing {@link Map} of named placeholders (e.g. {@code %p},
     *             {@code %tool}) for readability
     * @return the resolved, formatted string with MiniMessage tags preserved
     */
    public String resolveFormatted(String key, Object... args) {
        String template = raw(key);
        if (args.length == 0) {
            return template;
        }
        return format(template, args, true);
    }

    /**
     * Applies named placeholder substitution followed by positional formatting.
     *
     * @param template      the raw message template
     * @param args          positional args plus an optional trailing named-placeholder map
     * @param escapePercent whether literal {@code %} must be escaped for a second
     *                      {@link String#format} pass (used by formatted messages)
     * @return the final message text
     */
    private String format(String template, Object[] args, boolean escapePercent) {
        Map<?, ?> named = null;
        int positional = args.length;
        if (args[args.length - 1] instanceof Map<?, ?> map) {
            named = map;
            positional--;
        }

        String result = template;
        if (named != null) {
            for (Map.Entry<?, ?> e : named.entrySet()) {
                if (e.getValue() == null) continue;
                result = result.replace("%" + e.getKey(),
                        Matcher.quoteReplacement(String.valueOf(e.getValue())));
            }
        }

        if (positional == 0) {
            return escapePercent ? result.replace("%", "%%") : result;
        }
        Object[] pos = Arrays.copyOf(args, positional);
        result = String.format(result, pos);
        return escapePercent ? result.replace("%", "%%") : result;
    }

    /**
     * The active language code, e.g. {@code en_US}.
     */
    public String language() {
        return language;
    }
}
