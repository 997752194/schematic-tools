/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */
package de.eldoria.schematictools.listener;

import de.eldoria.schematicbrush.event.PostPasteEvent;
import de.eldoria.schematictools.configuration.Configuration;
import de.eldoria.schematictools.configuration.elements.CommandType;
import de.eldoria.schematictools.configuration.elements.Tool;
import de.eldoria.schematictools.util.PlaceholderResolver;
import de.eldoria.schematictools.util.SchematicTool;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.Optional;

/**
 * Executes the command configured on a tool after a schematic paste.
 * <p>
 * This listener is intentionally separate from {@link BrushPasteListener} so that the
 * original paste handling is left untouched. It observes {@link PostPasteEvent}, looks
 * up the tool currently bound to the pasting player and, if a command is configured,
 * resolves its placeholders and executes it according to the configured
 * {@link CommandType}.
 */
public class ToolCommandListener implements Listener {
    private final Plugin plugin;
    private final Configuration configuration;

    public ToolCommandListener(Plugin plugin, Configuration configuration) {
        this.plugin = plugin;
        this.configuration = configuration;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPostPaste(PostPasteEvent event) {
        var player = event.player();
        Optional<Tool> tool = currentTool(player);
        if (tool.isEmpty() || !tool.get().hasCommand()) return;

        execute(player, tool.get());
    }

    private Optional<Tool> currentTool(Player player) {
        var meta = SchematicTool.getCurrentTool(player);
        if (meta.isEmpty()) return Optional.empty();
        return configuration.tools().byId(meta.get().id());
    }

    /**
     * Executes the configured command.
     * <p>
     * The {@link PostPasteEvent} is fired from FastAsyncWorldEdit's asynchronous paste
     * queue, so dispatching a command there directly throws
     * {@code "Command Dispatched Async"}. The actual execution is therefore scheduled
     * onto the primary (main) thread; if the event already happens to run on the main
     * thread it is executed inline.
     *
     * @param player the player who pasted
     * @param tool   the tool whose command should be executed
     */
    private void execute(Player player, Tool tool) {
        if (Bukkit.isPrimaryThread()) {
            executeOnMain(player, tool);
            return;
        }
        Bukkit.getScheduler().runTask(plugin, () -> executeOnMain(player, tool));
    }

    private void executeOnMain(Player player, Tool tool) {
        for (var entry : tool.commands()) {
            for (var kv : entry.entrySet()) {
                String typeName = kv.getKey();
                String rawCommand = kv.getValue();
                String command = PlaceholderResolver.resolve(player, rawCommand);
                if (command == null || command.isBlank()) continue;
                dispatchCommand(player, parseType(typeName), command);
            }
        }
    }

    /**
     * Parses a case-insensitive {@link CommandType} name, falling back to
     * {@link CommandType#CONSOLE} for unknown values.
     *
     * @param name the configured type name
     * @return the resolved command type
     */
    private CommandType parseType(String name) {
        if (name == null) return CommandType.CONSOLE;
        try {
            return CommandType.valueOf(name.toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return CommandType.CONSOLE;
        }
    }

    /**
     * Executes a single command according to the given type.
     *
     * @param player  the player who pasted
     * @param type    how the command should be executed
     * @param command the already resolved command text
     */
    private void dispatchCommand(Player player, CommandType type, String command) {
        // Both dispatchCommand and performCommand reject a leading slash.
        String stripped = command.replaceFirst("^/", "");

        switch (type) {
            case CONSOLE -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), stripped);
            case PLAYER -> player.performCommand(stripped);
            case OP -> executeAsOp(player, stripped);
        }
    }

    private void executeAsOp(Player player, String command) {
        boolean wasOp = player.isOp();
        player.setOp(true);
        try {
            player.performCommand(command);
        } finally {
            if (!wasOp) {
                player.setOp(false);
            }
        }
    }
}
