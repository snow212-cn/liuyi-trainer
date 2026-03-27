# 当前状态与路线校正

日期：2026-03-27

## 1. 当前结论

路线已经校正回来了，当前不再停留在“纯静态页面骨架”阶段。

目前真实进度是：

- 首页已支持选择艺、式和休息预设
- 训练页、休息页、总结页已经从真实会话状态取数
- 应用层状态已收敛到 `LiuyiTrainerViewModel`
- 历史记录列表页已接入导航，并读取持久化数据
- 动作标准页仍然是正式结构加占位内容

## 2. 当前认可的成果

- 轻量演示已可用于需求确认
- `2-1-2` 实时节奏引导规则已落地到原型与 Android 领域层
- 组间休息倒计时，超时后转正计时的规则已落地
- Android 训练状态机已建立：
  - `Idle`
  - `SetRunning`
  - `RestRunning`
  - `RestOvertime`
  - `Completed`
- 训练完成后保存历史记录的入口已建立
- 历史记录页已能展示最近训练数据
- 历史记录页已细化为每次训练卡片，并展示每组次数、每组时长、结束时间
- Gradle Wrapper 文件已补齐：
  - `gradlew`
  - `gradlew.bat`
  - `gradle/wrapper/gradle-wrapper.jar`
- GitHub Actions 远程调试版 APK 构建流程已建立
- 已修正一个真实编译阻塞点：`Room.databaseBuilder(...)` 命名参数错误

## 3. 当前仍未完成的内容

- 本机没有 Android SDK，尚未真正编译 Android App
- APK 还没有产出
- Gradle 发行包尚未完整下载到本机缓存
- Room 所需的构建链路还未经过真实编译校验
- 动作标准正式内容、示意图、音频素材尚未接入
- 历史记录页还可以继续细化为更完整的详情展示
- GitHub 远程构建还没有经过第一次真实运行

## 4. 当前代码定性

以下文件已不只是“参考骨架”，而是当前正式实现的一部分：

- `app/src/main/java/com/liuyi/trainer/app/LiuyiTrainerApp.kt`
- `app/src/main/java/com/liuyi/trainer/app/LiuyiTrainerViewModel.kt`
- `app/src/main/java/com/liuyi/trainer/model/TrainingSessionEngine.kt`
- `app/src/main/java/com/liuyi/trainer/ui/HomeScreen.kt`
- `app/src/main/java/com/liuyi/trainer/ui/TrainingFlowScreens.kt`
- `app/src/main/java/com/liuyi/trainer/data/TrainingHistoryRepository.kt`

## 5. 当前禁止偏航的方向

- 不要继续扩张只靠样例数据驱动的静态页面
- 不要把“页面数量变多”误判为“功能完成”
- 不要在没有构建验证的情况下宣称 Android 已可交付

## 6. 官方下一步

从现在开始，官方路线统一为：

1. 继续完善历史记录展示与训练数据闭环
2. 优先执行第一次 GitHub 远程构建验证
3. 根据第一次构建日志修正剩余编译问题
4. 构建链路稳定后，再接动作标准正式内容

## 7. 以后如何判断 AI 是否偏离路线

以后只看四个文件：

- `docs/product/srs.md`
- `docs/product/ux-training-screen.md`
- `docs/process/current-status.md`
- `docs/process/handoff.md`

如果某次回复偏离：

- 先以仓库代码与文档为准
- 再判断是保留、修正还是放弃
- 不以聊天里自我描述为准
