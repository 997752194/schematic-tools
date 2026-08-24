/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */
package de.eldoria.schematictools.configuration.elements;

/**
 * The way a tool's {@code command} is executed after a schematic paste.
 */
public enum CommandType {
    /**
     * The command is executed by the server console.
     */
    CONSOLE,
    /**
     * The command is executed by the player themselves.
     */
    PLAYER,
    /**
     * The command is executed by the player while temporarily granted operator
     * permissions. The player is de-opped again immediately afterwards.
     */
    OP
}
