/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */
package de.eldoria.schematictools.util;

import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A lightweight update checker for this Fork build.
 * <p>
 * Unlike the upstream SchematicTools which uses the Lyna update station (a hard-coded
 * plugin id), this Fork reads a configurable URL from {@code config.yml}
 * ({@code update-url}) so the server operator controls where updates are fetched from.
 * <p>
 * The URL response is expected to be either a plain version string (e.g.
 * {@code 1.2.0}) or a JSON object exposing a {@code tag_name} / {@code version} field
 * (e.g. the GitHub "latest release" API). Only the first semantic-ish version triple
 * found is used for comparison.
 */
public final class ForkUpdateChecker {
    private static final Pattern VERSION = Pattern.compile("(\\d+\\.\\d+\\.\\d+)");

    private ForkUpdateChecker() {
    }

    /**
     * Asynchronously checks for updates against the configured URL and logs the result.
     *
     * @param plugin      the plugin instance
     * @param currentVersion the locally installed plugin version
     * @param updateUrl   the configured update-check URL (may be blank to skip)
     */
    public static void check(Plugin plugin, String currentVersion, String updateUrl) {
        if (updateUrl == null || updateUrl.isBlank()) {
            plugin.getLogger().info(I18n.get().resolve("console.update.disabled"));
            return;
        }
        final URI uri;
        try {
            uri = new URI(updateUrl.trim());
        } catch (URISyntaxException e) {
            plugin.getLogger().warning(I18n.get().resolve("console.update.failed", updateUrl));
            return;
        }

        plugin.getLogger().info(I18n.get().resolve("console.update.checking", updateUrl));

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(10))
                .header("User-Agent", "SchematicToolsFork/" + currentVersion)
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .thenApply(HttpResponse::body)
                .thenApply(body -> findVersion(body))
                .handle((remoteVersion, throwable) -> {
                    if (throwable != null) {
                        plugin.getLogger().warning(I18n.get().resolve("console.update.failed", updateUrl));
                        return null;
                    }
                    if (remoteVersion == null) {
                        plugin.getLogger().warning(I18n.get().resolve("console.update.failed", updateUrl));
                        return null;
                    }
                    report(plugin, currentVersion, remoteVersion);
                    return null;
                });
    }

    private static void report(Plugin plugin, String current, String remote) {
        int cmp = compare(current, remote);
        if (cmp < 0) {
            plugin.getLogger().info(I18n.get().resolve("console.update.available", remote, current));
        } else if (cmp > 0) {
            plugin.getLogger().info(I18n.get().resolve("console.update.newer", current, remote));
        } else {
            plugin.getLogger().info(I18n.get().resolve("console.update.latest", current));
        }
    }

    /**
     * Extracts the first {@code x.y.z} version triple from a plain text or JSON body,
     * preferring the value of a {@code tag_name} / {@code version} JSON field when present.
     */
    private static String findVersion(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        // Prefer explicit JSON fields (e.g. GitHub releases latest API).
        for (String field : new String[]{"\"tag_name\"", "\"version\"", "\"name\""}) {
            int idx = body.indexOf(field);
            if (idx >= 0) {
                Matcher m = VERSION.matcher(body.substring(idx, Math.min(idx + 200, body.length())));
                if (m.find()) {
                    return m.group(1);
                }
            }
        }
        // Otherwise take the first version triple anywhere in the body.
        Matcher m = VERSION.matcher(body);
        return m.find() ? m.group(1) : null;
    }

    /**
     * Compares two {@code x.y.z} version strings.
     *
     * @return negative if {@code current} &lt; {@code remote}, zero if equal, positive otherwise
     */
    private static int compare(String current, String remote) {
        int[] a = parts(current);
        int[] b = parts(remote);
        for (int i = 0; i < 3; i++) {
            if (a[i] != b[i]) {
                return Integer.compare(a[i], b[i]);
            }
        }
        return 0;
    }

    private static int[] parts(String version) {
        int[] out = new int[3];
        String[] split = version.replace("v", "").split("\\.");
        for (int i = 0; i < 3; i++) {
            try {
                out[i] = i < split.length ? Integer.parseInt(split[i].replaceAll("\\D.*", "")) : 0;
            } catch (NumberFormatException e) {
                out[i] = 0;
            }
        }
        return out;
    }
}
