# Liuyi Trainer

面向 Android 手机的本地训练助手，聚焦六类自重训练动作的节奏引导、训练记录与离线使用体验。项目目标不是做一个单纯计时器，而是把训练流程、历史归档、设置管理和后续内容扩展收敛成一个可长期维护的原生 Android 工程。

## 项目状态

- 当前阶段：公开预览 / Pre-release
- 目标平台：Android
- 技术栈：Kotlin、Jetpack Compose、Room、DataStore
- 交付策略：轻量原型验证 + Android 正式实现 + GitHub Actions 远程构建 APK

仓库现在以公开协作和后续发布为目标维护：

- `main` 用于公开展示与稳定发布
- `develop` 用于持续开发与日常集成

## 当前能力

- 六艺十式的动作族与等级结构
- 训练准备、执行、组间休息、总结、历史等核心页面
- 固定节奏引导与语音提示
- 本地训练历史保存、导出与导入
- 轻量 HTML 原型，用于快速验证流程和界面方向

## 内容策略

按当前项目基线，仓库会保留构建 APK 所需的训练正文、插图与动作结构内容。

- 当前内容来源于项目已采用的公开传播电子书整理材料
- 动作标准页会直接使用这些内容参与构建与展示
- 后续若继续扩展素材，请同步保留来源说明与分发边界记录

内容来源登记入口见 `docs/process/content-sources.md`。

## 快速开始

如果你只想快速了解项目：

1. 阅读 `docs/process/start-here.md`
2. 双击 `OPEN_DEMO.bat` 打开本地原型
3. 双击 `OPEN_UI_REDESIGN.bat` 查看当前 UI 方向

如果你要参与代码开发：

1. 安装 JDK 17
2. 安装 Android SDK 36 对应命令行组件
3. 运行 `./gradlew assembleDebug`

如果你不打算在本机搭 Android 环境，可以直接使用 GitHub Actions 远程构建 APK。

## 仓库结构

- `app/`：Android 应用源码
- `content/`：动作结构与种子数据
- `prototype/`：轻量原型
- `docs/architecture/`：架构与数据设计
- `docs/product/`：需求与体验设计文档
- `docs/process/`：协作、测试、发布与流程文档
- `.github/workflows/`：CI/CD 工作流

## 公开发布建议

建议先阅读 `docs/process/repository-release.md`。这个文档说明了：

- 为什么开发态历史不应该直接当作公开主分支
- 如何整理 `main` / `develop` 分工
- 如何在公开前清理不适合公开分发的历史内容

## 贡献

欢迎提 issue 和 pull request。提交前请先阅读 `CONTRIBUTING.md`，尤其注意：

- 保持内容来源说明清晰，不要提交来源不明的替换素材
- 优先提交可复现的问题、最小改动和必要测试说明
- 涉及公开发布流程时，遵守 `main` / `develop` 分支约定

## 安全

如发现会影响用户数据、APK 分发或签名流程的安全问题，请先阅读 `SECURITY.md`，不要直接公开披露细节。

## 许可证

本仓库采用 `Apache-2.0` 许可证。详见根目录的 `LICENSE` 文件。
