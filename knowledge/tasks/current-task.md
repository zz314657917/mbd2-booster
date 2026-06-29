# Current Task

## 背景

用户要求按“MBD2 Booster 附属模组修正版计划”实现独立 Forge 1.20.1 附属模组，而不是修改 MBD2 主仓库。当前仓库为 `F:/mcmod/mbd2-booster`。

## 当前目标

进入运行态手测与后续补齐阶段：验证核心绑定、Buff、配方修改、并行、KubeJS reload 和存档行为。

## 本次已完成

- 初始化独立 Forge 模组工程和 Git 仓库。
- 实现 booster base、binding tool、target capability、SavedData 索引、recipe effect 服务、parallel mixin、debug commands 和 KubeJS DSL。
- 创建项目知识库入口、构建验证说明、实现笔记、当前交接快照和时间轴。
- 已执行 `./gradlew.bat build` 并通过。

## 已确认事实

- 构建产物为 `build/libs/mbd2_booster-1.20.1-0.1.0.jar`。
- 当前 MBD2 依赖来自 `../Multiblocked2-1.20.1/build/libs/multiblocked2-1.20.1-1.0.38.a.jar`。
- 配方效果走 `MachineRecipeModifyEvent.Before`。
- 并行效果走 MBD2 `getMaxParallel` mixin。
- UI 注入尚未实现，当前用绑定工具和命令作为 fallback。

## 待验证点

- 游戏内绑定流程：工具选择基地 -> 绑定目标 -> SavedData 正确记录。
- 配方效果：下一轮 setup 是否正确应用速度、FE、物品/流体输出。
- 并行：`parallelBonus` 是否只提高 maxParallel，不重复乘已并行配方。
- KubeJS：`MBD2BoosterEvents.registry` 在 `/reload` 后是否生效并 dirty 已绑定目标。
- 存档：重启后 base/target 绑定和目标等级是否恢复。
- 兼容：仅 MBD2、MBD2 + MBD2Thread 两组运行态。

## 当前结论

代码级和构建级第一版已完成，可进入本地 Forge 1.20.1 环境手测；尚不能声明运行态通过。

## 下一步

- 部署 jar 到 Forge 1.20.1 测试环境 -> 验证服务端启动无 mixin/KubeJS/capability 报错。
- 做主流程 smoke -> 绑定、升级、激活 buff、跑目标配方并观察效果。
- 做边界 smoke -> `/reload`、解绑、基地拆除、目标拆除、区块卸载。

## 验证记录

- `./gradlew.bat build`: 通过，2026-06-28。
- jar 内容检查：确认包含 `mods.toml`、`mixins.mbd2_booster.json`、`kubejs.plugins.txt`、Mixin、命令、绑定工具和 KubeJS 插件类。
- 未执行真实游戏内 smoke。
