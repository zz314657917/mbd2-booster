# Timeline

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
