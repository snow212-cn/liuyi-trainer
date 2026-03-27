# 远程构建 APK

日期：2026-03-27

## 这份文件是干什么的

这份说明专门给“本机没有 Android 开发环境的人”使用。

本项目已经补上 GitHub Actions 工作流。只要把仓库放到 GitHub，就可以让 GitHub 的云端机器帮你构建 APK。

当前为了缩短等待时间，工作流已经改成：

- 只手动触发
- 同一分支新的构建会自动取消旧构建
- 启用 Gradle 官方缓存动作
- 只在失败时上传构建报告

## 当前已准备好的内容

- 仓库已包含 `gradlew`、`gradlew.bat`
- 仓库已包含 `gradle/wrapper/gradle-wrapper.jar`
- 仓库已包含 GitHub Actions 工作流：
  - `.github/workflows/android-debug-apk.yml`

## 你最终要得到什么

目标是让 GitHub 自动产出一个可下载的调试版 APK：

- 文件位置通常是 `app/build/outputs/apk/debug/`
- 在 GitHub 页面里会作为构建产物下载

## 最简单的操作顺序

1. 创建一个 GitHub 仓库
2. 把当前项目文件上传到那个仓库
3. 打开 GitHub 仓库里的 `Actions`
4. 运行 `Android Debug APK`
5. 等待构建完成
6. 下载产物 `liuyi-trainer-debug-apk`
7. 把里面的 APK 传到安卓手机安装测试

## 如果构建失败，先看什么

先看 GitHub Actions 日志里这三类问题：

1. Kotlin/Compose 编译错误
2. Room 注解处理错误
3. Android SDK 或依赖下载错误

## 为什么现在不用每次 push 都自动跑

因为当前阶段主要是在排首次构建问题。

如果每次推送都自动跑：

- 会反复排队
- 会浪费 GitHub Actions 时间
- 你会更难判断哪一次才是最新有效构建

所以当前改成“只手动点一次最新构建”更快、更稳。

## 对你来说最重要的结论

你不需要在自己电脑上安装 Android Studio 才能继续推进。

当前更合理的路线就是：

- 我继续把仓库代码和工作流准备好
- 你把仓库放到 GitHub
- GitHub 云端帮我们构建 APK
