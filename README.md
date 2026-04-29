# Liuyi Trainer

面向 Android 手机的本地训练助手，服务于囚徒健身/ Convict Conditioning 的六艺十式训练体系。项目目标不是做一个单纯计时器，而是围绕原书对应的训练路径、节奏引导、动作标准查阅和训练记录整理而设计的配套软件，一个可长期维护的原生 Android 工程。

## 项目状态

- 当前阶段：公开预览 / 可用软件 / 进入正式版本发布流程
- 目标平台：Android
- 技术栈：Kotlin、Jetpack Compose、Room、DataStore
- 交付策略：轻量原型验证 + Android 正式实现 + GitHub Actions 远程构建 APK

仓库现在以公开协作和后续发布为目标维护：

- `develop`：持续开发与日常集成
- `main`：稳定主线与正式版本发布

## 项目定位

- 软件的主要使用场景是辅助六艺十式训练
- 项目本身是独立软件工程，不代表图书作者、出版社或任何官方机构

## 当前能力

- 六艺十式的动作族与等级结构
- 动作标准、说明与示意图浏览
- 训练准备、执行、组间休息、总结、历史等核心页面
- 固定节奏引导与语音提示
- 本地训练历史保存、导出与导入

## 内容策略

按当前项目基线，仓库会保留构建 APK 所需的训练正文、插图与动作结构内容。

- 当前内容来源于项目已采用的公开传播电子书整理材料
- 动作标准页直接使用这些内容参与构建与展示
- 后续若继续扩展素材，同步维护来源说明与分发边界记录

内容来源登记入口见 `docs/process/content-sources.md`。

## 快速开始

如果只想了解项目：

1. 阅读 `docs/process/start-here.md`
2. 双击 `OPEN_DEMO.bat` 打开本地原型
3. 双击 `OPEN_UI_REDESIGN.bat` 查看当前 UI 方向

如果你要参与代码开发：

1. 安装 JDK 17
2. 安装 Android SDK 36 对应命令行组件
3. 运行 `./gradlew assembleDebug`

## 版本与发布

仓库现在采用更接近正式开源软件的发布方式：

- 版本号在仓库内固定维护，或绑定 CI 运行次数
- `develop` 和 PR 会执行持续集成构建验证，其中 `develop` push 还会更新最新调试版下载入口
- `main` push 会执行候选构建，并按实际产物刷新最新下载入口；只有 `vX.Y.Z` Git tag 才发布正式版本页
- 只有 `vX.Y.Z` 形式的 Git tag 才发布正式版本

具体步骤见 `docs/process/release-process.md`。

## 仓库结构

- `app/`：Android 应用源码
- `content/`：动作结构与种子数据
- `prototype/`：轻量原型
- `docs/architecture/`：架构与数据设计
- `docs/product/`：需求与体验设计文档
- `docs/process/`：协作、测试、发布与流程文档
- `.github/workflows/`：CI/CD 工作流

## 贡献

欢迎提 issue 和 pull request。提交前请先阅读 `CONTRIBUTING.md`，尤其注意：

- 保持内容来源说明清晰，不要提交来源不明的替换素材
- 优先提交可复现的问题、最小改动和必要测试说明
- 涉及正式版本发布时，遵守 `develop` / `main` / Git tag 的分工

## 安全

如发现会影响用户数据、APK 分发或签名流程的安全问题，请先阅读 `SECURITY.md`，不要直接公开披露细节。

## 许可证

本仓库采用 `Apache-2.0` 许可证。详见根目录的 `LICENSE` 文件。
