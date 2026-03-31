# Liuyi Trainer

面向 Android 手机的本地训练助手，服务于“六艺十式”体系化练习。项目目标不是简单计时器，而是把训练节奏、组数记录、本次训练归档、动作标准内容浏览放进一个长期可维护的软件工程体系中。

## 本轮已完成

- 建立 Git 仓库与 Android 工程骨架。
- 定义首版需求规格、架构、数据模型、迭代路线。
- 落下六艺十式内容模型的种子文件。
- 补入原型阶段可复用的训练计次规则。

## 当前约束

- 当前机器缺少 Android SDK 与本地 Gradle 安装，因此不适合把“本机构建 APK”作为近期主路径。
- “动作标准”“示意图”“书中原文节奏说明”涉及版权与来源问题，当前仓库只保留结构与占位，不内置受保护书籍内容。
- 用户不希望安装 Chrome，也没有 Android 开发基础，因此测试策略必须避开 Chrome 和本机 Android 开发环境依赖。

## 技术方向

- 原生 Android
- Kotlin
- Jetpack Compose
- Room
- DataStore
- 离线优先，本地存储

## 当前交付策略

项目目标仍然是 Android 手机上的可用软件，但交付路径调整为两阶段：

1. 先用轻量原型验证训练逻辑与页面流程。
2. 再通过远程构建或最小化 Android 环境产出 APK。

这样做的原因：

- 不要求你现在安装 Android Studio。
- 不要求你安装 Chrome。
- 可以先把“功能是否符合你的意思”验证清楚。
- 避免在错误方向上投入大量 Android 工程成本。

## 目录

- `docs/product/srs.md`：需求规格说明
- `docs/architecture/architecture.md`：架构说明
- `docs/architecture/data-model.md`：数据模型与计次规则
- `docs/learning/project-foundations-textbook.md`：新手教材，系统讲解写这个项目要用到的基础知识
- `docs/roadmap/milestones.md`：迭代计划
- `docs/process/working-rhythm.md`：长期协作方式
- `docs/process/testing-strategy.md`：测试与验证策略
- `docs/architecture/adr-001-lightweight-delivery.md`：轻量交付架构决策
- `content/exercises_seed.json`：六艺十式内容种子
- `app/`：Android 应用骨架
- `prototype/`：轻量可视化原型

## 后续建议启动顺序

1. 先把训练引擎状态机与记录规则在轻量原型中做扎实。
2. 再把同样的规则收敛到可构建的 Android 应用层。
3. 通过远程构建产出首个可安装 APK。
4. 最后接入动作标准内容页与素材资产流程。

## 本地构建前置

原生 Android 本地构建不是当前主路径。等功能原型稳定后，再二选一：

1. 远程 CI 构建 APK。
2. 只安装最小 Android Command-line Tools，本机构建 APK。

当前你只需要能打开：

- `OPEN_DEMO.bat`
- `prototype/index.html`
- `docs/process/start-here.md`
- `docs/learning/project-foundations-textbook.md`

## APK 签名与升级

远程工作流现在支持两种产物：

- 配置固定 keystore 后，CI 产出稳定签名的 `release` APK，可覆盖安装同包名应用。新建一把长期保存的发布 keystore 以后所有正式 APK 都用它签名，不能再丢。可以生成：
    ```powershell
    keytool -genkeypair -v -keystore release-keystore.jks -alias
    [Convert]::ToBase64String([IO.File]::ReadAllBytes("release-keystore.jks")) > release-keystore.base64.txt
    ```

- 未配置 keystore 时，CI 只产出 `debug` APK，并使用独立包名 `com.liuyi.trainer.debug`，避免与正式安装包签名冲突。

要让 GitHub Actions 一直产出可覆盖安装的 APK，需要在仓库 `Secrets and variables > Actions` 中配置 secret 和对应的值：

- `ANDROID_KEYSTORE_BASE64`: `release-keystore.base64.txt` 里的整串内容
- `ANDROID_KEYSTORE_PASSWORD`: 生成 keystore 时输入的 store password
- `ANDROID_KEY_ALIAS`: 创建 keystore 时用的 alias
- `ANDROID_KEY_PASSWORD`: 该 alias 对应的 key password

其中关键的是：后续用于发布的 keystore 必须和手机里已安装那一版 APK 的签名私钥相同。若当前安装包的私钥已经丢失，Android 不允许无损覆盖安装，只能先备份应用数据，再卸载重装。

若需要做一次性数据迁移，请在 GitHub Actions 页面手动运行工作流，并把 `build_mode` 选择为 `migration`。CI 会产出独立的 `migration` APK：它仍使用正式签名，但会显式开启 `debuggable`，且工作流会在发布前校验 manifest 中确实存在 `android:debuggable`。日常 push 默认产出稳定 `release`；若缺少签名 secrets，则回退为 `debug`。迁移完成后，再手动运行一次并把 `build_mode` 选回 `release`。
