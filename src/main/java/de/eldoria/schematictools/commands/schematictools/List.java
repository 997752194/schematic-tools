/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */
package de.eldoria.schematictools.commands.schematictools;

import de.eldoria.eldoutilities.commands.command.AdvancedCommand;
import de.eldoria.eldoutilities.commands.command.CommandMeta;
import de.eldoria.eldoutilities.commands.command.util.Arguments;
import de.eldoria.eldoutilities.commands.exceptions.CommandException;
import de.eldoria.eldoutilities.commands.executor.IPlayerTabExecutor;
import de.eldoria.eldoutilities.localization.MessageComposer;
import de.eldoria.messageblocker.blocker.MessageBlocker;
import de.eldoria.schematictools.util.Colors;
import de.eldoria.schematictools.configuration.Configuration;
import de.eldoria.schematictools.configuration.elements.Tool;
import de.eldoria.schematictools.configuration.elements.Tools;
import de.eldoria.schematictools.util.I18n;
import de.eldoria.schematictools.util.Permissions;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

// List schematic tool configurations
public class List extends AdvancedCommand implements IPlayerTabExecutor {
    public final int PAGE_SIZE = 15;
    public final String RIGHT_ARROW = "»»»";
    public final String LEFT_ARROW = "«««";
    private final MessageBlocker messageBlocker;
    private final Configuration configuration;

    public List(Plugin plugin, MessageBlocker messageBlocker, Configuration configuration) {
        super(plugin, CommandMeta.builder("list")
                .withPermission(Permissions.LIST)
                .build());
        this.messageBlocker = messageBlocker;
        this.configuration = configuration;
    }

    @Override
    public void onCommand(@NotNull Player player, @NotNull String alias, @NotNull Arguments args) throws CommandException {
        var index = args.asInt(0, 0);
        var tools = configuration.tools();
        var composer = MessageComposer.create();
        addPageHeader(composer, "words.tools");
        addEntries(composer, tools.page(0, PAGE_SIZE), Tool::asListComponent);
        addPageFooter(composer, index, tools);
        send(composer, player);
    }

    protected void addPageHeader(MessageComposer composer, String title) {
        composer.text("<%s>%s", Colors.HEADING, I18n.get().resolve(title)).newLine();
    }

    protected <T> void addEntries(MessageComposer composer, java.util.List<T> entries, Function<T, String> map) {
        var page = entries.stream().map(entry -> String.format("  %s", map.apply(entry))).toList();
        composer.text(page).newLine();
    }

    protected void addPageFooter(MessageComposer composer, int index, Tools paged) {
        var baseCommand = "/" + meta().createCommandCall();
        if (index == 0) {
            composer.text("<%s>%s", Colors.INACTIVE, LEFT_ARROW);
        } else {
            composer.text("<click:run_command:'%s %s'><%s>%s</click>", baseCommand, index - 1, Colors.CHANGE, LEFT_ARROW);
        }
        composer.text(" <%s>%s / %s ", Colors.NEUTRAL, index + 1, Math.max(1, paged.pages(PAGE_SIZE)));

        if (index + 1 >= paged.pages(PAGE_SIZE)) {
            composer.text("<%s>%s", Colors.INACTIVE, RIGHT_ARROW);
        } else {
            composer.text("<click:run_command:'%s %s'><%s>%s</click>", baseCommand, index + 1, Colors.CHANGE, RIGHT_ARROW);
        }
    }

    protected void send(MessageComposer composer, Player player) {
        composer.prependLines(20);
        messageBlocker.blockPlayer(player);
        messageBlocker.ifEnabled(() -> composer.newLine().text("<click:run_command:'/schematictools chatblock false'><%s>[x]</click>", Colors.REMOVE));
        messageBlocker.announce(player, "[x]");
        messageSender().sendMessage(player, composer.build());
    }
}
