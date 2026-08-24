![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/eldoriarpg/schematic-tools/verify.yml?branch=main&style=for-the-badge&label=Building)
![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/eldoriarpg/schematic-tools/publish_to_nexus.yml?branch=main&style=for-the-badge&label=Publishing) \
![Sonatype Nexus (Releases)](https://img.shields.io/nexus/maven-releases/de.eldoria/schematic-tools?label=Release&logo=Release&server=https%3A%2F%2Feldonexus.de&style=for-the-badge)
![Sonatype Nexus (Development)](https://img.shields.io/nexus/maven-dev/de.eldoria/schematic-tools?label=DEV&logo=Release&server=https%3A%2F%2Feldonexus.de&style=for-the-badge)
![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/de.eldoria/schematic-tools?color=orange&label=Snapshot&server=https%3A%2F%2Feldonexus.de&style=for-the-badge)

[![wakatime](https://wakatime.com/badge/github/eldoriarpg/schematic-tools.svg)](https://wakatime.com/badge/github/eldoriarpg/schematic-tools)

## Schematic Tools 是什么

Schematic Tools 是 [Schematic Brush Reborn 2](https://github.com/eldoriarpg/SchematicBrushReborn) 的附加插件.

允许将已保存的画笔绑定到任意物品上. 拥有特定或默认权限的玩家均可使用. 工具可设置使用次数限制, 且创建后可随时修改或失效.

## 功能

- 将已保存的画笔绑定到工具上
- 限制工具的使用次数
- 通过权限限制工具的使用
- 创建后可轻松修改
- 可绑定到任意自定义物品
- 支持 Schematic Brush 预览功能

## 创建画笔工具

1. 用 Schematic Brush Reborn 创建并保存画笔. 全局或私人画笔均可.
2. 使用 `/sbt create` 创建新工具.
3. 通过 `-p` 标志设置权限. 未设置时默认权限为 `schematictools.use`.
4. 通过 `-o` 标志指定其他玩家的已保存画笔.
5. 通过 `-u` 标志定义使用次数. 未定义则不限次数.
6. 将需要绑定工具的物品拿在手中. 可以是带有自定义 lore 的自定义物品或普通物品.
7. 使用 `/sbt bind <tool-name>` 绑定工具. 若设置了使用次数, lore 中会新增一行显示剩余次数.

## 使用画笔工具

将工具拿在主手即可自动激活.

拥有 `schematicbrush.brush.preview` 权限的玩家, 在 Schematic Brush 配置中预览默认状态为 `true` 时会自动获得预览. 否则需通过 `/sbrs preview true` 手动开启. 仍需对应权限.

## 重要提示

- 修改画笔会影响所有使用该画笔的工具.
- 删除画笔会使所有相关工具失效.
- 删除工具画笔会使所有相关工具失效.
- 修改工具的使用次数或权限会影响已存在的工具.

## 配置

```yaml
# 控制工具从玩家背包中移除的时机
toolRemoval:
  ==: stToolRemoval
  # 达到最大使用次数时移除
  removeUsed: false
  # 工具被删除时移除
  removeInvalidTools: false
  # 画笔被删除时移除
  removeInvalidBrushes: false
```

### tools.yml 中 `commands` 的占位符用法

工具在玩家使用并完成画笔粘贴后, 会依次执行 `tools.yml` 中 `commands` 列表里的每条指令. 每条指令格式为 `指令类型: "指令内容"`, 指令类型可为 `CONSOLE` (控制台), `PLAYER` (以玩家身份) 或 `OP` (以管理员身份). **指令文本会在执行前解析占位符**, 因此你可以在其中嵌入占位符.

示例:

```yaml
commands:
- CONSOLE: "give %player% diamond 1"
- OP: "tp %player% 0 100 0"
- PLAYER: "say 我在 %world% 的 %x% %y% %z%"
- PLAYER: "warp %player_name%"   # 需要 PlaceholderAPI
```

#### 内置占位符 (无需 PlaceholderAPI)

任何环境均可使用, 针对"执行粘贴的玩家"生效:

| 占位符 | 含义 |
|--------|------|
| `%player%` / `%name%` | 玩家名 |
| `%world%` | 当前世界名 |
| `%uuid%` | 玩家 UUID |
| `%x%` / `%y%` / `%z%` | 玩家所在方块坐标 |

#### PlaceholderAPI 占位符 (可选)

若服务器安装了 [PlaceholderAPI](https://www.spigotmc.org/resources/6245/), 指令还会额外通过 PlaceholderAPI 解析, 支持所有已注册的 PAPI 占位符 (如 `%player_name%`, `%server_name%`, `%vault_eco_balance%` 等).

#### 注意事项

- 指令开头的 `/` 会被自动去除, 请直接写 `give ...` 而不要写 `/give ...`.
- 占位符在**执行时**解析, 其中 `%x%`/`%y%`/`%z%` 是粘贴那一刻玩家的坐标.
- `%player%` 是使用工具并粘贴的玩家, 而非配置里的 `owner`.
- 某条指令解析后若为空白, 该条会被跳过.

## 权限

### `schematictools.use`
允许使用画笔工具(未设置自定义权限时的默认权限).

### `schematictools.info.all`
查看所有已存在工具的信息.

### `schematictools.info.current`
查看当前使用工具的信息.

### `schematictools.list`
查看所有已存在工具的列表.

### `schematictools.modify`
创建, 修改和删除工具.

### `schematictools.bind`
通过 `/sbt bind` 绑定工具.

### `schematictools.give`
通过 `/sbt give` 下发工具. 默认: 仅 OP.

### `schematictools.manage` (别名: `schematictools.modify`)
创建, 修改, 删除工具, 以及通过 `/sbt reload` 重新加载配置.

## 命令

### `/sbt bind <tool-name>`
将工具绑定到物品上.

### `/sbt create <tool-name> <brush-name> [-o brush-owner] [-p custom.permission] [-u <usages>]`
创建新工具.

### `/sbt list`
列出所有已存在的工具.

### `/sbt info [tool-name]`
显示当前工具或指定工具的信息.
若拥有相应权限, 可在此界面中修改工具.

### `/sbt remove <tool-name>`
删除指定名称的工具.

### `/sbt give <player> <tool-name> [amount]`
向玩家下发预绑定的工具物品. 可从控制台或 OP 玩家执行.

### `/sbt reload [all|tools]`
从磁盘重新加载配置, 无需重启服务器.
- `/sbt reload` 或 `/sbt reload all` — 重新加载全部配置文件.
- `/sbt reload tools` — 仅重新加载 `tools.yml`.

---

## 修改内容 (v1.1.2)

### 支持 Paper 26.2

- **测试服务端**: leaf-26.2-83


### ItemsAdder 集成

- **自定义物品绑定**: 通过反射桥接, 工具可绑定到 ItemsAdder 自定义物品. 无硬依赖 — IA 不存在时原版逻辑正常运行.
- **ItemKey 系统**: 复合标识符 (Material + IA ID), 防止共用基础材质的自定义物品之间画笔冲突.
- **会话缓存**: 每玩家 `BrushToolSessionCache` 跟踪活跃 IA 工具, 确保切换物品时正确绑定画笔.
- **回退机制**: IA 物品不可用时, 自动回退到记录的基础材质.

### 画笔粘贴后执行指令

- **执行指令**: 画笔粘贴本身已回到主线程触发事件
- **指令类型**: 控制台/ 玩家/ 管理员OP

### tools.yml 元数据

- **`itemSource`**: 记录工具绑定的是 `VANILLA` 还是 `ITEMS_ADDER` 物品.
- **`iaId`**: 存储自定义物品的 ItemsAdder namespace:id.
- **`material`**: 存储回退用基础材质.
- **`displayName` / `lore`**: 可自定义显示名称和描述, 支持十六进制颜色 (`&#RRGGBB`) 和传统颜色代码 (`&a`).
- **`command_type`**: 工具右键使用后执行支持占位符的指令类型:控制台, 玩家, 管理员OP
- **`command`**: ""

### Give 命令

- **`/sbt give <player> <tool-name> [amount]`**: 下发预绑定工具物品. 可从控制台或 OP 玩家执行. 默认权限: `schematictools.give` (OP).


### Reload 命令

- **`/sbt reload [all|tools]`**: 从磁盘重新加载配置, 无需重启服务器. 支持重载全部配置或仅 `tools.yml`.


### Save 命令

- **`/sbt save tools`**: 手动保存 `tools.yml`.
- **`修改tools.yml顺序`**: 编辑 `tools.yml` - 重新加载配置 - 保存 .

### 本地化

- **完整 i18n**: 所有玩家可见消息和控制台消息移入语言文件, 无硬编码字符串残留.
- **zh_CN 支持**: 内置简体中文翻译. 通过 `config.yml` (`language: auto`) 自动检测系统语言.
- **控制台本地化**: 自定义 `I18n` 工具类直接从用户可编辑的 properties 文件解析语言键, jar 内置文件作为回退.
- **玩家消息本地化**: `I18n.resolveFormatted()` 预解析参数占位符, 同时保留 MiniMessage 样式标签 (`<value>`, `<default>`), 再通过 `MessageComposer.text()` 发送, 保留富文本格式.
- **eldoutilities 默认值**: 28 个库默认键预置中文翻译, 防止运行时注入英文.
- **语言文件统一目录**: `messages_en_US.properties` 与 `messages_zh_CN.properties` 均放置于 `plugins/SchematicToolsFork/localization/`. 通过 `Localizer.setLocalesPath("localization")` 将 eldoutilities 的语言文件纳入同一目录, `I18n.copyToDisk()` 同时复制英文默认文件与激活语言文件.

### Fork 修复

- **插件名称**: 改为 `SchematicToolsFork` (`plugin.yml` + jar 名), 数据目录随之变为 `plugins/SchematicToolsFork/`. 包名/命令/权限标识保持 `de.eldoria.schematictools` 不变以兼容原插件.
- **更新检查**: 移除硬编码的 Lyna 更新站 (ID=6). 更新链接改由 `config.yml` 的 `update-url` 定义, 通过新增 `util/ForkUpdateChecker` 异步检查 (支持纯文本版本号或 JSON `tag_name`/`version` 字段). 留空则禁用更新检查.
- **tools.yml 重启重置修复**: 根因是启动时旧版迁移逻辑用 `LegacyConfiguration` (Bukkit YAML) 读取本构建以 Jackson YAML 写入的 `tools.yml`, 读取不兼容返回空对象并 `replace` 覆盖, 导致已保存工具被清空. 修复: 迁移前经 `isLegacyToolsFile()` 判断, 仅当 `tools.yml` 仍是旧版 Bukkit/ConfigurationSerializable 格式 (含 `==`/`stTools`) 时才允许迁移覆盖, 已是 Jackson 格式则保留数据.
- **`base_configuration.yml` 结论**: 由 eldoutilities `JacksonConfig.secondary(PluginBaseConfiguration.KEY)` 生成, 仅首次创建; 后续启动从磁盘读取, 不会重置, 也不影响 `config.yml`/`tools.yml`.
- **命令框架报错本地化**: 命令缺参数等框架消息 (`error.invalidArguments`) 由 eldoutilities 命令框架经 MessageSender 解析. 将 `I18n.init` 提前并让 eldoutilities `Localizer` 的 fallback locale 跟随 `config.yml` 的 `language` (auto 检测系统语言), 同时 `setIncludedLocales("en_US", "zh_CN")`, 使中文环境显示 `无效参数` 而非英文 `Invalid arguments`; 文案已在语言文件中可编辑.

### 构建

- 通过 `options.release.set(21)` 实现 Java 21 交叉编译.
- Gradle 9.2.1 wrapper.
- 插件名称: `SchematicToolsFork`.
