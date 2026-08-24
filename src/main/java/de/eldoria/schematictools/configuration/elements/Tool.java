/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */
package de.eldoria.schematictools.configuration.elements;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.eldoria.eldoutilities.localization.MessageComposer;
import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.schematicbrush.storage.Storage;
import de.eldoria.schematicbrush.storage.brush.Brush;
import de.eldoria.schematictools.util.Colors;
import de.eldoria.schematictools.util.I18n;
import de.eldoria.schematictools.util.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@SerializableAs("stTool")
public class Tool implements ConfigurationSerializable {
    private static final UUID GLOBAL = new UUID(0L, 0L);
    /**
     * The unique numeric id of the brush
     */
    private int id;
    /**
     * The owner of the underlying brush
     */
    private UUID owner;
    /**
     * The name of this brush tool
     */
    private String name;
    /**
     * The name of the underlying brush
     */
    private String brushName;
    @Nullable
    private String permission;
    private int usages;
    /**
     * Whether the tool is bound to a vanilla or an {@code ItemsAdder} item.
     */
    private ItemSource itemSource = ItemSource.VANILLA;
    /**
     * The {@code ItemsAdder} namespaced id (e.g. {@code namespace:id}) for custom items.
     */
    @Nullable
    private String iaId;
    /**
     * The base material used as fallback when the bound item is unavailable.
     */
    private Material material = Material.AIR;
    /**
     * A custom display name, supporting hex ({@code &#RRGGBB}) and legacy colour codes.
     */
    @Nullable
    private String displayName;
    /**
     * Custom lore lines, supporting hex ({@code &#RRGGBB}) and legacy colour codes.
     */
    private List<String> lore = Collections.emptyList();
    /**
     * An optional list of commands, each carrying its own {@link CommandType}.
     * <p>
     * Each entry is a single-entry map in the form {@code {type: "command"}}
     * (e.g. {@code {op: "say hi"}}), where the key is the case-insensitive
     * {@link CommandType} name and the value the command text.
     * <p>
     * This field replaces the legacy single {@code command}/{@code commandType}
     * fields and is a Fork extension.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Map<String, String>> commands = Collections.emptyList();

    /**
     * Default command template written to new tools so that operators can fill in
     * commands for each execution type. Empty strings act as editable placeholders.
     */
    private static final List<Map<String, String>> DEFAULT_COMMANDS = List.of(
            Map.of(CommandType.CONSOLE.name(), ""),
            Map.of(CommandType.OP.name(), ""),
            Map.of(CommandType.PLAYER.name(), ""));

    public Tool() {
    }

    public Tool(UUID owner, String name, String brushName, int id, String permission, int usages) {
        this.owner = owner;
        this.name = name;
        this.brushName = brushName;
        this.id = id;
        this.permission = permission;
        this.usages = usages;
    }

    public Tool(Map<String, Object> objectMap) {
        var map = SerializationUtil.mapOf(objectMap);
        name = map.getValue("name");
        owner = UUID.fromString(map.getValue("owner"));
        brushName = map.getValue("brushName");
        id = map.getValue("id");
        permission = map.getValue("permission");
        usages = map.getValue("usages");
        itemSource = map.getValueOrDefault("itemSource", ItemSource.VANILLA, ItemSource.class);
        iaId = map.<String>getValue("iaId");
        material = map.getValueOrDefault("material", Material.AIR, this::parseMaterial);
        displayName = map.<String>getValue("displayName");
        lore = map.getValueOrDefault("lore", Collections.emptyList());
        commands = map.getValueOrDefault("commands", Collections.emptyList());
    }

    @Override
    @NotNull
    public Map<String, Object> serialize() {
        return SerializationUtil.newBuilder()
                .add("name", name)
                .add("owner", owner.toString())
                .add("brushName", brushName)
                .add("id", id)
                .add("permission", permission)
                .add("usages", usages)
                .add("itemSource", itemSource)
                .add("iaId", iaId)
                .add("material", material == null || material == Material.AIR ? null : material.name())
                .add("displayName", displayName)
                .add("lore", lore)
                .add("commands", commands)
                .build();
    }

    public String name() {
        return name;
    }

    public void name(String name) {
        this.name = name;
    }

    public void brush(UUID owner, Brush brush) {
        this.owner = owner;
        brushName = brush.name();
    }

    public int id() {
        return id;
    }

    @NotNull
    public String permission() {
        return permission == null ? Permissions.USE : permission;
    }

    public void permission(String permission) {
        this.permission = permission;
    }

    public int usages() {
        return usages;
    }

    public boolean hasUsage() {
        return usages != -1;
    }

    public void usages(int usages) {
        this.usages = usages;
    }

    public ItemSource itemSource() {
        return itemSource;
    }

    public void itemSource(ItemSource itemSource) {
        this.itemSource = itemSource;
    }

    @Nullable
    public String iaId() {
        return iaId;
    }

    public void iaId(@Nullable String iaId) {
        this.iaId = iaId;
    }

    /**
     * The base material used as fallback for this tool, if configured.
     *
     * @return the fallback material, or empty if none was configured
     */
    public Optional<Material> material() {
        return material == null || material == Material.AIR ? Optional.empty() : Optional.of(material);
    }

    public void material(@Nullable Material material) {
        this.material = material == null ? Material.AIR : material;
    }

    @Nullable
    public String displayName() {
        return displayName;
    }

    public void displayName(@Nullable String displayName) {
        this.displayName = displayName;
    }

    /**
     * The custom lore lines, possibly empty.
     *
     * @return an immutable view of the lore
     */
    public List<String> lore() {
        return Collections.unmodifiableList(lore);
    }

    public void lore(List<String> lore) {
        this.lore = lore == null ? Collections.emptyList() : lore;
    }

    /**
     * The multi-command list, possibly empty.
     * <p>
     * Each entry is a single-entry map {@code {type: "command"}}, e.g.
     * {@code {op: "say hi"}}.
     *
     * @return an immutable view of the multi-command list
     */
    public List<Map<String, String>> commands() {
        return Collections.unmodifiableList(commands);
    }

    public void commands(List<Map<String, String>> commands) {
        this.commands = commands == null ? Collections.emptyList() : normalizeCommands(commands);
    }

    /**
     * Fills the command list with the default per-type placeholder template
     * ({@code CONSOLE}/{@code OP}/{@code PLAYER}, each with an empty command).
     * Used when a new tool is created so the generated config contains editable
     * command slots.
     */
    public void applyDefaultCommands() {
        commands = normalizeCommands(DEFAULT_COMMANDS);
    }

    /**
     * Whether this tool has an actual (non-blank) command configured for execution
     * after a paste. Empty placeholder entries are ignored.
     *
     * @return {@code true} if at least one command with a non-blank value exists
     */
    public boolean hasCommand() {
        for (var entry : commands) {
            for (var kv : entry.entrySet()) {
                if (kv.getValue() != null && !kv.getValue().isBlank()) return true;
            }
        }
        return false;
    }

    /**
     * Whether this tool has a non-empty multi-command list (including empty
     * placeholder entries).
     *
     * @return {@code true} if at least one command entry is configured
     */
    public boolean hasCommands() {
        return !commands.isEmpty();
    }

    /**
     * Normalizes raw multi-command entries into a stable form: keys are uppercased to
     * {@link CommandType} names. {@code null} values are dropped while empty strings
     * are preserved (empty placeholders are written to the config as template entries).
     *
     * @param raw the raw entries
     * @return a defensive copy with normalized keys
     */
    private List<Map<String, String>> normalizeCommands(List<Map<String, String>> raw) {
        List<Map<String, String>> result = new ArrayList<>();
        for (Map<String, String> entry : raw) {
            if (entry == null || entry.isEmpty()) continue;
            Map<String, String> normalized = new LinkedHashMap<>();
            for (Map.Entry<String, String> kv : entry.entrySet()) {
                if (kv.getValue() == null) continue;
                normalized.put(kv.getKey().toUpperCase(Locale.ROOT), kv.getValue());
            }
            if (!normalized.isEmpty()) result.add(normalized);
        }
        return result;
    }

    /**
     * Parses a material from its configured name, tolerating unknown or blank values by
     * falling back to {@link Material#AIR}.
     *
     * @param value the configured material name
     * @return the resolved material, or {@link Material#AIR} if unknown
     */
    private Material parseMaterial(String value) {
        if (value == null || value.isBlank()) return Material.AIR;
        try {
            return Material.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return Material.AIR;
        }
    }

    public boolean hasPermission(Player player) {
        if (permission == null) return true;
        return player.hasPermission(permission);
    }

    public CompletableFuture<Optional<Brush>> getBrush(Storage storage) {
        if (owner.equals(GLOBAL)) {
            return storage.brushes().globalContainer().get(brushName);
        }
        return storage.brushes().playerContainer(owner).get(brushName);
    }

    public String asModifyComponent() {
        var base = "/schematictools modify";
        var i18n = I18n.get();

        var composer = MessageComposer.create()
                .text(i18n.resolveFormatted("info.tool.heading", Colors.HEADING, name))
                .newLine()
                .text(i18n.resolveFormatted("info.tool.brush", Colors.NAME, Colors.VALUE, brushName))
                .space()
                .text(i18n.resolveFormatted("info.tool.change", Colors.CHANGE, base, name, "brushName"))
                .newLine()
                .text(i18n.resolveFormatted("info.tool.brushowner", Colors.NAME, Colors.VALUE,
                        hasGlobalBrush() ? i18n.resolve("info.global") : Bukkit.getOfflinePlayer(owner).getName()))
                .newLine()
                .text(i18n.resolveFormatted("info.tool.permission", Colors.NAME, Colors.VALUE, permission()))
                .space()
                .text(i18n.resolveFormatted("info.tool.change", Colors.CHANGE, base, name, "permission"))
                .newLine()
                .text(i18n.resolveFormatted("info.tool.usages", Colors.NAME, Colors.VALUE,
                        hasUsage() ? usages : i18n.resolve("info.unlimited")))
                .space()
                .text(i18n.resolveFormatted("info.tool.change", Colors.CHANGE, base, name, "usages"))
                .space()
                .text(i18n.resolveFormatted("info.tool.unlimited", Colors.CHANGE, base, name));
        appendCommands(composer, i18n);
        return composer.build();
    }

    public String asInfoComponent() {
        var i18n = I18n.get();
        var composer = MessageComposer.create()
                .text(i18n.resolveFormatted("info.tool.heading", Colors.HEADING, name))
                .newLine()
                .text(i18n.resolveFormatted("info.tool.brush", Colors.NAME, Colors.VALUE, brushName))
                .newLine()
                .text(i18n.resolveFormatted("info.tool.brushowner", Colors.NAME, Colors.VALUE,
                        hasGlobalBrush() ? i18n.resolve("info.global") : Bukkit.getOfflinePlayer(owner).getName()))
                .newLine()
                .text(i18n.resolveFormatted("info.tool.permission", Colors.NAME, Colors.VALUE, permission()))
                .newLine()
                .text(i18n.resolveFormatted("info.tool.usages", Colors.NAME, Colors.VALUE,
                        hasUsage() ? usages : i18n.resolve("info.unlimited")));
        appendCommands(composer, i18n);
        return composer.build();
    }

    /**
     * Appends the configured multi-command list to the info message, one line per
     * command with its execution type. Does nothing when no command is set.
     *
     * @param composer the message composer to append to
     * @param i18n     the active localizer
     */
    private void appendCommands(MessageComposer composer, I18n i18n) {
        if (!hasCommands()) return;
        composer.newLine()
                .text(i18n.resolveFormatted("info.tool.commands", Colors.NAME, Colors.VALUE, commands.size()));
        for (var entry : commands()) {
            for (var kv : entry.entrySet()) {
                composer.newLine()
                        .text(i18n.resolveFormatted("info.tool.command",
                                Colors.NAME, Colors.VALUE, kv.getKey(), Colors.NAME, kv.getValue()));
            }
        }
    }

    public String asListComponent() {
        var i18n = I18n.get();
        return MessageComposer.create().text(i18n.resolveFormatted("info.tool.list.name", Colors.NAME, asInfoComponent(), name))
                .space()
                .text(i18n.resolveFormatted("info.tool.list.info", name, Colors.CHANGE))
                .build();
    }

    public boolean hasGlobalBrush() {
        return owner.equals(GLOBAL);
    }

    @Override
    public String toString() {
        return "Tool{owner=%s, name='%s', brushName='%s', id=%d}".formatted(owner, name, brushName, id);
    }
}
