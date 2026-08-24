/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */
package de.eldoria.schematictools.configuration;

import de.eldoria.schematictools.configuration.elements.ToolRemoval;

@SuppressWarnings("FieldMayBeFinal")
public class ConfigFile {
    private boolean updateCheck = true;
    private ToolRemoval toolRemoval = new ToolRemoval();
    /**
     * The language code used for player and console messages. Use {@code auto} to
     * detect the server system language automatically, or a code such as
     * {@code en_US} / {@code zh_CN}.
     */
    private String language = "auto";
    /**
     * Optional custom update-check URL for this Fork build. When non-blank it is used
     * instead of the upstream Lyna update station. Leave empty to disable update checks.
     * The URL should return the newest version as plain text (or as a JSON object
     * containing a {@code tag_name} / {@code version} field, e.g. GitHub releases latest).
     */
    private String updateUrl = "";

    public ToolRemoval toolRemoval() {
        return toolRemoval;
    }

    public void toolRemoval(ToolRemoval toolRemoval) {
        this.toolRemoval = toolRemoval;
    }

    public boolean updateCheck() {
        return updateCheck;
    }

    public String language() {
        return language;
    }

    public void language(String language) {
        this.language = language;
    }

    public String updateUrl() {
        return updateUrl;
    }

    public void updateUrl(String updateUrl) {
        this.updateUrl = updateUrl;
    }
}
