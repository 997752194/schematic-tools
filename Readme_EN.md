![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/eldoriarpg/schematic-tools/verify.yml?branch=main&style=for-the-badge&label=Building)
![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/eldoriarpg/schematic-tools/publish_to_nexus.yml?branch=main&style=for-the-badge&label=Publishing) \
![Sonatype Nexus (Releases)](https://img.shields.io/nexus/maven-releases/de.eldoria/schematic-tools?label=Release&logo=Release&server=https%3A%2F%2Feldonexus.de&style=for-the-badge)
![Sonatype Nexus (Development)](https://img.shields.io/nexus/maven-dev/de.eldoria/schematic-tools?label=DEV&logo=Release&server=https%3A%2F%2Feldonexus.de&style=for-the-badge)
![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/de.eldoria/schematic-tools?color=orange&label=Snapshot&server=https%3A%2F%2Feldonexus.de&style=for-the-badge)

[![wakatime](https://wakatime.com/badge/github/eldoriarpg/schematic-tools.svg)](https://wakatime.com/badge/github/eldoriarpg/schematic-tools)

## What is Schematic Tools

Schematic tools is an add on for [Schematic Brush Reborn 2](https://github.com/eldoriarpg/SchematicBrushReborn).

It allows to bind previously saved brushes on any tool. The tools can be used by any player with a specific or default
permission. The tools can also have a usage limit. Tools can be also invalidated or changed after creation, allowing
easy modification afterwards.

## Features

- Bind saved brushes on a tool
- Restrict usage count of a tool
- Restrict usage of a tool by permission
- Easily change created tools afterwards
- Bind a tool on any custom item
- Allow to preview pastes via schematic brush preview

## Creating a brush tool

1. Create your brush with Schematic Brush Reborn and save it. It doesn't matter if you save it as a global or private brush.
2. Use the `/sbt create` command to create a new tool.
3. You can set a permission by using the `-p` flag. If no permission will be set the required permission will be `schematictools.use`.
4. You can select a saved brush of another player by setting his name via the `-o` flag
5. You can define the usage count with the `-u` flag. If not defined the usage will be unlimited.
6. Get the item you want to bind the tool on. This can be a custom item with a custom lore and more or just a normal item.
7. Use `/sbt bind <tool-name>` to bind the tool on this item. If a usage is defined a new line will be added to the lore showing the remaining uses of the tool.

## Use a brush tool

Simply take the tool into your main hand. The tool will activate itself automatically.

Players with the permission `schematicbrush.brush.preview` will also get a preview when the default state of the preview is set to `true` in the schematic brush config. Otherwise they will need to enable it by themselves by using `/sbrs preview true`. They still need the permission.

## Important notices

- Changing a brush will also change the brush on the brush tools which use this brush.
- Removing a brush will render all brush tools useless.
- Removing a tool brush will render all brush tools useless.
- Changing the usage or permission of a brush tool will change it for existing tools as well.

## Configuring Schematic Tools

There is not much to configure, but you have a few options:

```yaml
# This handles when tools are removed from the player inventory
toolRemoval:
  ==: stToolRemoval
  # Set to true to remove tools which reached the max usage
  removeUsed: false
  # Set to true to remove tools which were removed
  removeInvalidTools: false
  # Set to true to remove tools where the used brush got removed.
  removeInvalidBrushes: false
```

## Permissions

### `schematictools.use`
Allows to use a schematic tool brush if no other permission was defined.

### `schematictools.info.all`
Allows to get information about all existing tools.

### `schematictools.info.current`
Allows to get information about the current used tool.

### `schematictools.list`
Allows to get a list of all existing tools.

### `schematictools.modify`
Allows to create, modify and remove tools.

### `schematictools.bind`
Allows to bind tools via `/sbt bind`.

### `schematictools.give`
Allows to distribute tools via `/sbt give`. Default: OP only.

### `schematictools.manage` (alias: `schematictools.modify`)
Allows to create, modify, remove tools, and reload configuration via `/sbt reload`.

## Commands

### `/sbt bind <tool name>`
Bind a tool on an item.

### `/sbt create <tool-name> <brush-name> [-o brush-owner] [-p custom.permission] [-u <usages>]`
Create a new tool.

### `/sbt list`
Lists all existing tools.

### `/sbt info [tool-name]`
Shows information about the current tool or a specific tool.
This overview also allows to modify the tool if the user has the required permission.

### `/sbt remove <tool-name>`
Removes the tool with this name.

### `/sbt give <player> <tool-name> [amount]`
Distributes pre-bound tool items to a player. Usable from console and by OP players.

### `/sbt reload [all|tools]`
Reloads configuration from disk without restarting the server.
- `/sbt reload` or `/sbt reload all` — reload all configuration files.
- `/sbt reload tools` — reload only `tools.yml`.

---

## Modifications (v1.1.2)

### ItemsAdder Integration

- **Custom Item Binding**: Tools can now be bound to ItemsAdder custom items via reflection bridge. No hard dependency required — vanilla logic runs unaffected when IA is absent.
- **ItemKey System**: Composite identifier (Material + IA ID) prevents brush conflicts between custom items sharing the same base material.
- **Session Cache**: Per-player `BrushToolSessionCache` tracks active IA tools to ensure correct brush binding when switching items.
- **Fallback**: When an IA item is unavailable, the tool falls back to its recorded base material.

### Give Command

- **`/sbt give <player> <tool-name> [amount]`**: Distributes pre-bound tool items. Usable from console and by OP players. Default permission: `schematictools.give` (OP).

### Tool Metadata in tools.yml

- **`itemSource`**: Records whether a tool is bound to `VANILLA` or `ITEMS_ADDER` item.
- **`iaId`**: Stores the ItemsAdder namespace:id for custom items.
- **`material`**: Stores the base material for fallback.
- **`displayName` / `lore`**: Customizable display name and lore with hex color (`&#RRGGBB`) and legacy code (`&a`) support.

### Localization

- **Full i18n**: All player-facing and console messages moved to language files. No hardcoded strings remain.
- **zh_CN Support**: Simplified Chinese translation bundled. Auto-detection from system locale via `config.yml` (`language: auto`).
- **Console Localization**: Custom `I18n` utility resolves locale keys directly from user-editable properties files, with jar-internal fallback.
- **Player Message Localization**: `I18n.resolveFormatted()` pre-resolves parameter placeholders while preserving MiniMessage style tags (`<value>`, `<default>`), then sends via `MessageComposer.text()` for rich formatting.
- **eldoutilities Defaults**: All 28 library-default keys pre-translated to prevent English injection at runtime.

### Reload Command

- **`/sbt reload [all|tools]`**: Reloads configuration from disk without server restart. Supports reloading all configs or only `tools.yml`.

### Build

- Java 21 cross-compilation via `options.release.set(21)`.
- Gradle 9.2.1 wrapper.
- Plugin name: `SchematicToolsFork`.
