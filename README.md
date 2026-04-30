# Carpet CRS Addition

[English](#english) | [中文](#中文)

---

## English

A [Carpet mod](https://github.com/gnembon/fabric-carpet) extension for Minecraft 1.21.1 Fabric that backports projectile logic and ender pearl chunk loading from **Minecraft 1.21.2**.

### Features

- **Use 1.21.2 Projectile Logic** (`UseV1212ProjectileLogic`)  
  Replace the projectile logic of vanilla 1.21.1 with that of version 1.21.2, allowing you to use the pearl cannon from 1.21.2+.

- **Pearl Can Load Chunks** (`PearlCanLoadingChunks`)  
  Ender pearls can load chunks they pass through while in flight, just like in version 1.21.2.

### Installation

1. Install [Fabric Loader](https://fabricmc.net/) 0.16.0 or later for Minecraft 1.21.1.
2. Install [Fabric Carpet](https://github.com/gnembon/fabric-carpet) (1.4.147 or later).
3. Download the latest mod from [Releases](https://github.com/Eason120806/CarpetCRSAddition/releases) and place it in your `mods` folder.
4. Start the server or client.

### Usage

- `UseV1212ProjectileLogic` (default: `false`) — set to `true` to enable 1.21.2 projectile logic.
- `PearlCanLoadingChunks` (default: `false`) — set to `true` to enable ender pearl chunk loading.

Examples:
/carpet-crs-addition UseV1212ProjectileLogic true

To persist the setting across restarts:
/carpet-crs-addition setDefault UseV1212ProjectileLogic true

### Dependencies

- Minecraft 1.21.1
- Fabric Loader >= 0.16.0
- Carpet Mod >= 1.4.147
- Conditional Mixin (included)


### License

This project is licensed under the **GNU General Public License v3.0 (GPL-3.0)**. See [LICENSE](LICENSE) for details.

---

## 中文

一个为 Minecraft 1.21.1 Fabric 设计的 [Carpet mod](https://github.com/gnembon/fabric-carpet) 扩展，将 **Minecraft 1.21.2** 的弹射物逻辑与末影珍珠区块加载功能移植至 1.21.1。

### 功能

- **使用1.21.2投掷物逻辑** (`UseV1212ProjectileLogic`)  
  将原版 1.21.1 的投掷物工作逻辑替换为 1.21.2 版本，你可以使用1.21.2+的珍珠炮。

- **末影珍珠可加载区块** (`PearlCanLoadingChunks`)  
  末影珍珠在飞行过程中可以加载途经的区块，就像 1.21.2 版本一样。

### 安装

1. 为 Minecraft 1.21.1 安装 [Fabric Loader](https://fabricmc.net/) 0.16.0 或更高版本。
2. 安装 [Fabric Carpet](https://github.com/gnembon/fabric-carpet)（1.4.147 或更高版本）。
3. 从 [发布页面](https://github.com/Eason120806/CarpetCRSAddition/releases) 下载最新的模组文件，放入 `mods` 文件夹。
4. 启动服务器或客户端。

### 使用方法

- `UseV1212ProjectileLogic`（默认：`false`）——设为 `true` 启用 1.21.2 弹射物逻辑。
- `PearlCanLoadingChunks`（默认：`false`）——设为 `true` 启用珍珠加载区块功能。

示例：
/carpet-crs-addition UseV1212ProjectileLogic true

如需持久化设置（重启后仍生效）：
/carpet-crs-addition setDefault UseV1212ProjectileLogic true

### 依赖

- Minecraft 1.21.1
- Fabric Loader >= 0.16.0
- Carpet Mod >= 1.4.147
- Conditional Mixin（已包含）


### 许可证

本项目基于 **GNU General Public License v3.0 (GPL-3.0)** 许可。详情请参阅 [LICENSE](LICENSE) 文件。