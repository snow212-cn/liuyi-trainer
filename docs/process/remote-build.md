# 远程构建 APK

日期：2026-03-27

## 这份文件是干什么的

这份说明专门给“本机没有 Android 开发环境的人”使用。

本项目已经补上 GitHub Actions 工作流。只要把仓库放到 GitHub，就可以让 GitHub 的云端机器帮你构建 APK。

当前为了缩短等待时间，工作流已经改成：

- `main`、`develop` push 都会自动触发；`develop` 会同步更新调试版下载页
- 同一分支新的构建会自动取消旧构建
- 启用 Gradle 官方缓存动作
- 允许非默认分支也写入 Gradle 缓存
- 构建命令显式开启 `build cache` 和 `parallel`
- 只在失败时上传构建报告

## 当前已准备好的内容

- 仓库已包含 `gradlew`、`gradlew.bat`
- 仓库已包含 `gradle/wrapper/gradle-wrapper.jar`
- 仓库已包含 GitHub Actions 工作流：
  - `.github/workflows/android-debug-apk.yml`

## 你最终要得到什么

目标是让 GitHub 自动产出一个可下载的调试版 APK：

- 文件位置通常是 `app/build/outputs/apk/debug/`
- 会同时出现在：
  - `Actions` 里的 `Artifacts`
  - `Releases` 里的公开下载资产

## 如果你不想登录 GitHub 下载

不要走 `Artifacts`。

更合适的路线是：

1. 把仓库设为公开仓库
2. 让工作流构建成功一次
3. 直接在 GitHub 的 `Releases` 页面下载 APK

公开仓库下，`Releases` 资产可以不登录直接下载。

如果仓库是私有仓库，则 GitHub 不支持匿名下载。

## 调试版 Release 会不会越堆越多

当前不会按每次构建新增一个正式版本页。

现在工作流固定使用：

- tag: `debug-latest`
- name: `Liuyi Trainer Debug APK`

并且会覆盖同名文件。

也就是说，调试分发始终只维护一个“最新调试版”入口。

以后如果需要正式发版，再单独按版本号创建：

- `v0.1.0`
- `v0.1.1`
- `v0.2.0`

## 最简单的操作顺序

1. 创建一个 GitHub 仓库
2. 把当前项目文件上传到那个仓库
3. 打开 GitHub 仓库里的 `Actions`
4. 运行 `Android Debug APK`
5. 等待构建完成
6. 打开仓库的 `Releases`
7. 下载最新的调试版 APK
8. 把 APK 传到安卓手机安装测试

## 如果构建失败，先看什么

先看 GitHub Actions 日志里这三类问题：

1. Kotlin/Compose 编译错误
2. Room 注解处理错误
3. Android SDK 或依赖下载错误

## 当前提速不靠改触发条件

这轮提速没有修改 workflow 的 `on:` 条件。

当前提速来自：

- 同分支旧任务自动取消
- Gradle 缓存复用
- 非默认分支也允许写缓存
- 构建命令显式启用缓存和并行

## 对你来说最重要的结论

你不需要在自己电脑上安装 Android Studio 才能继续推进。

当前更合理的路线就是：

- 我继续把仓库代码和工作流准备好
- 你把仓库放到 GitHub
- GitHub 云端帮我们构建 APK
