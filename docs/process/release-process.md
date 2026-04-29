# 发布流程

仓库现在采用更接近正式开源软件项目的版本与发布方式。

## 1. 版本来源

版本号由仓库内固定维护，而不是由 CI 运行次数临时生成。

当前入口：

- `gradle.properties`
  - `APP_VERSION_NAME`
  - `APP_VERSION_CODE`

建议规则：

- `APP_VERSION_NAME` 使用语义化版本，如 `1.0.0`
- `APP_VERSION_CODE` 保持 Android 要求的单调递增整数

## 2. 分支职责

- `develop`：日常开发与集成
- `main`：准备发布的稳定主线

## 3. CI/CD 规则

- `develop` push：执行构建验证，并更新调试版 GitHub Release 下载页
- `main` push：执行候选构建，并按实际产物更新最新 GitHub Release 下载页
- `vX.Y.Z` tag：执行正式 release 构建，并发布 GitHub Release

## 4. 发版步骤

1. 在 `develop` 完成功能并合并到 `main`
2. 修改 `gradle.properties` 中的 `APP_VERSION_NAME` 和 `APP_VERSION_CODE`
3. 更新 `CHANGELOG.md`
4. 在 `main` 上创建版本标签，例如：
   - `v1.0.0`
   - `v1.0.1`
5. 推送 tag
6. 等待 GitHub Actions 自动发布正式 Release

## 5. 预发布

如果以后需要预发布，可以使用带后缀的 tag，例如：

- `v1.1.0-rc1`
- `v1.1.0-beta1`

工作流会将带连字符后缀的版本识别为 prerelease。
