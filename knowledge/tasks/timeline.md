# Timeline

## 2026-06-29 16:51 +08:00 - 基地放置者 owner 绑定修复

- 当前阶段：补齐基地 owner 初始化时机，消除放置后未交互前的认领空档。
- 本段重点：`BoosterBaseBlock#setPlacedBy` 在服务端由 `ServerPlayer` 放置时写入 owner；`ensureOwner` 只作为旧存档或非玩家放置 fallback。
- 已完成：代码补丁与知识库同步。
- 关键决策：不移除首次交互兜底，保持旧存档和自动放置兼容。
- 验证记录：`./gradlew.bat build` 通过。
- 遗留问题：仍需游戏内验证玩家 A 放置后玩家 B 不能接管。
- 下一步：构建验证、提交推送；随后部署 jar 到测试客户端做权限 smoke。

## 2026-06-29 16:39 +08:00 - paste 审查 P0/P1 修复

- 当前阶段：完成源码审查中指出的高风险边界修复，准备提交推送和运行态手测。
- 本段重点：修复绑定工具 shift 解绑权限；绑定时校验选中基地 UUID 并标记 target holder dirty；材料 cost 按 item+NBT 汇总后模拟/扣除；未绑定目标等级不再生效；跨维度第一版代码层强制禁止；MBD2 依赖版本范围收紧。
- 已完成：代码修改、语言键补充、构建依赖支持 `-Plocal_mbd2_jar=<path>` 覆盖、`./gradlew.bat build` 通过。
- 关键决策：不引入不确定的远程 MBD2 Maven 坐标；继续默认使用本机兄弟目录 dev jar，但缺失时给出明确错误和覆盖参数。
- 验证记录：`./gradlew.bat build` 通过；搜索确认旧 `ALLOW_CROSS_DIMENSION` 引用、旧未绑定目标等级效果路径和旧 MBD2 开放版本范围无残留。
- 遗留问题：未做真实游戏内 smoke；KubeJS tags、完整 UI、升级材料和 MBD2Thread 兼容仍是后续项。
- 下一步：scoped commit 并推送；部署新 jar 到 Forge 1.20.1 测试客户端；按权限、持久化、扣费、配方、并行、reload 做 smoke。

## 2026-06-28 22:45 +08:00 - 初始化仓库与知识库

- 当前阶段：MBD2 Booster 第一版代码构建通过后进入项目知识化和交接阶段。
- 本段重点：初始化 Git 仓库；创建 `AGENTS.md`、知识入口、构建验证说明、实现笔记、当前任务快照；保留下一步运行态测试入口。
- 已完成：`git init`；补 `.gitignore`；创建 `knowledge/00-start-here.md`、`knowledge/build-and-verify.md`、`knowledge/implementation-notes.md`、`knowledge/tasks/current-task.md`。
- 关键决策：仓库知识以当前附属模组为真源，MBD2 主仓库仍不作为本任务修改对象。
- 验证记录：文件创建后需要回读确认；代码构建记录来自本轮 `./gradlew.bat build` 通过。
- 遗留问题：还没有真实游戏内 smoke；MBD2 本地 jar 依赖仍有 ForgeGradle deobf 警告。
- 下一步：部署 jar 到测试环境；验证绑定/Buff/配方/并行/KubeJS reload；根据结果更新当前任务和时间轴。

## 2026-06-28 22:39 +08:00 - MBD2 Booster 第一版构建通过

- 当前阶段：独立附属模组第一版后端闭环完成。
- 本段重点：实现基地、绑定工具、目标 capability、SavedData、配方 Before 修改、并行 Mixin、KubeJS DSL、debug 命令。
- 已完成：`./gradlew.bat build` 通过；产物为 `build/libs/mbd2_booster-1.20.1-0.1.0.jar`。
- 关键决策：UI 注入先不硬接 LDLib，保留绑定工具和命令 fallback；运行效果不改正在运行的 recipe，只影响下一轮 setup。
- 验证记录：构建通过，jar 内容包含 mods.toml、Mixin 配置、KubeJS 插件入口和核心类。
- 遗留问题：未做游戏内运行态验证；KubeJS tags 匹配和完整 UI 仍未实现。
- 下一步：建立项目仓库、知识库和时间轴；之后进入本地运行态测试。
