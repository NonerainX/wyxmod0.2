# WYX 技能书（wyxjinengshu）

《我的世界》1.21.1 / NeoForge 技能树模组。核心玩法是把**电力转化为技能点**，并用技能点强化自己与身边的科技机器——联动 **AE2 应用能源2** 与 **通用机械 Mekanism**，可直接加速它们的工作速度。

## 特性

- **独立技能界面**：游戏内按 `N` 键打开技能书，查看技能点、分类页签、等级与升级消耗。界面是纯客户端独立 `Screen`（不依附容器界面），因此**不会**被 JEI 的物品列表覆盖。
- **电量 → 技能点**：放置「技能核心」方块并接入发电机器（FE 能量），电量按固定比例自动转化为技能点；技能等级越高，升一级消耗的技能点越多。
- **技能核心可合成**：配方参考通用机械的「热力发电机」并替换了部分材料——
  ```
  铁锭   铁锭   铁锭
  木板   钻石   木板
  红石   熔炉   红石
  ```
  （木板接受任意 `minecraft:planks` 标签木板）
- **联动加速（装了才可用，软依赖）**
  - AE2：**压印提速**、**分子装配提速**，以及覆盖全部 AE2 机器（含附属模组）的**全域加速**
  - Mekanism：**机械过载**，以及覆盖全部通用机械机器（含附属模组）的**全域加速**
  - 未安装对应联动模组时，该分支显示「不可用」，其余功能不受影响。
- **全域加速技能（新增，永久 + 无范围限制）**：加点后立即对整个世界（所有已加载区块）内的机器生效，不要求玩家待在机器附近，也不随玩家移动失效。
  - `AE2 全域加速`：所有 AE2 机器（含附属模组）每刻额外工作 1 次 / 级
  - `机械全域加速`：所有通用机械机器（含附属模组）每刻额外工作 1 次 / 级
  - `全域超频`：同时提升上面两项
- **通用技能**：能量储量、能量功率、转化效率、影响范围。
- **调试命令**：`/wyx points`、`/wyx addpoint <数量>`、`/wyx setskill <技能id> <等级>`、`/wyx max`。

## 版本要求

| 项目 | 版本 |
| --- | --- |
| Minecraft | 1.21.1 |
| NeoForge | 21.1.x（接受 21.1 全系，测试于 21.1.233） |
| AE2（可选联动） | 19.2.x |
| Mekanism（可选联动） | 10.7.x |

## 安装

1. 为 1.21.1 安装 NeoForge 21.1.x。
2. 把 `wyxjinengshu-1.1.0.jar` 放进 `.minecraft/mods/`。
3. 启动游戏，按 `N` 打开技能界面。

## 构建（开发向）

本项目使用 ModDevGradle 构建。AE2 / Mekanism 的 jar 仅编译期使用，**未包含在本仓库中**（体积原因）：

1. 从 CurseForge / Modrinth 下载 1.21.1 版的 **AE2** 与 **Mekanism** 的 jar，放入项目根目录的 `libs/` 文件夹。
2. 确认本机已安装 JDK 21；若 Gradle 用错了 JDK，可在 `gradle.properties` 里把 `org.gradle.java.home` 指向你的 JDK 21 路径。
3. 执行构建：

```
gradlew build
```

产物位于 `build/libs/wyxjinengshu-1.1.0.jar`。

### 轻量测试环境

`gradlew runTextureCheck` 会使用独立的游戏目录 `run-light/`，只加载 `run-light/mods/` 里的少量模组，启动快、帧率高，适合检查贴图与界面等视觉表现（完整的 260 模组整合包测试仍用 `gradlew runClient`）。

## 运行测试记录

- **v1.0.0**：已在整合包《新时代科技》v1.9.8（共 260 个模组，NeoForge 21.1.233）中完成实测：客户端正常启动并进入单人世界，模组联动识别正常（日志 `AE2: true / Mekanism: true`），未出现崩溃。
- **v1.1.0**：在同一个整合包中重新实测：模组以 `WYX Skill Book 1.1.0 (wyxjinengshu)` 正常加载，联动识别正常（日志 `AE2: true / Mekanism: true`），技能核心配方无解析报错，未出现崩溃。
- **v1.1.0 贴图与界面视觉实测**：使用轻量测试环境（`gradlew runTextureCheck`，仅加载 AE2 / Mekanism / GuideME）进入创造模式世界实测：技能核心的**物品图标、手持模型、放置后的方块六面贴图**均渲染正确（金属外框 + 紫色能量环 + 青蓝辉光 + 金色核心），日志无任何缺失贴图（missing texture）告警；按 `N` 打开的技能书为独立全屏界面，正常显示技能点、分类与技能列表。

## 目录结构

```
src/main/java/com/example/wyxjinengshu/    模组源码
  ├─ energy/       技能核心方块与能量转换
  ├─ skills/       技能定义、存档与效果（范围型 + 全域加速）
  ├─ integration/  AE2 / Mekanism 联动加速（运行时按需加载）
  ├─ network/      客户端-服务端数据同步
  ├─ ui/           技能书界面（N 键，独立 Screen）
  └─ command/      /wyx 调试命令
src/main/resources/
  ├─ assets/wyxjinengshu/   贴图、模型、语言文件
  └─ data/wyxjinengshu/recipe/   技能核心合成配方
src/main/templates/                        mods.toml 模板
```