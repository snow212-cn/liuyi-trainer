# 对话过长时的续做说明

日期：2026-03-29

## 新对话开头直接发这句

请先阅读 `docs/process/start-here.md`、`docs/process/current-status.md`、`docs/process/handoff.md`，然后按当前官方路线继续，不要偏离。

## 当前项目目标

做一款 Android 手机上的本地训练助手，服务“六艺十式”练习。

核心规则：

- 用户手动开始一组
- 用户手动结束一组
- 系统在训练中按 `2-1-2` 节奏实时显示阶段与秒数
- 每完成一个完整节奏循环，当前组次数加一
- 支持多组训练
- 组间休息先倒计时，超时后转正计时
- 训练结束后保存本次训练的日期时间、总组数、总次数、每组次数
- 动作标准页后续接入文字、示意图、注意事项

## 这轮最新结论

- `prototype/ui-redesign.html` 现在已经得到用户确认，属于当前唯一 UI 基准
- 后续 Android 成品必须以这份 HTML 稿为准推进，不允许再回到旧 UI 自由发挥
- 允许直接在现有 Android 代码基础上重构和修正，不需要推倒重写
- 这轮重点仍然是把界面气质拉回《囚徒健身》，不是继续做普通健身 App 风格
- 用户当前最在意的页面是：
  - 首页
  - 训练进行页
  - 历史列表页 / 详情页
  - 动作标准页
- 组间休息页、训练总结页目前是“暂时接近需求”，本轮不应该大改结构

## 本轮已落地成果

- 已开始按确认后的 HTML 稿重写 Android 正式 UI：
  - 新增统一 UI 壳层 `app/src/main/java/com/liuyi/trainer/ui/UiChrome.kt`
  - 首页已重写 `app/src/main/java/com/liuyi/trainer/ui/HomeScreen.kt`
  - 训练准备 / 训练执行 / 组间休息 / 总结 / 历史列表 / 历史详情 / 动作标准已重写到 `app/src/main/java/com/liuyi/trainer/ui/TrainingFlowScreens.kt`
  - 主题已重写 `app/src/main/java/com/liuyi/trainer/ui/theme/`
- 已确认 EPUB 路径：
  - `D:\book\囚徒健身-保罗•威德.epub`
- 已确认六艺章节映射：
  - 俯卧撑：`text00013.html`
  - 深蹲：`text00014.html`
  - 引体向上：`text00015.html`
  - 举腿：`text00016.html`
  - 桥：`text00017.html`
  - 倒立撑：`text00018.html`
- 已从 EPUB 补齐六艺十式正式名称
- 已把十式正式名称写入：
  - `content/exercises_seed.json`
  - `app/src/main/java/com/liuyi/trainer/model/TrainingDomain.kt`
- 已重写 v2 蓝图：
  - `docs/product/ui-redesign-blueprint.md`
- 已重做 v2 静态审查稿：
  - `prototype/ui-redesign.html`
- 已继续按用户反馈收紧为 v2.1：
  - 十式选择更紧凑
  - 训练进行页更克制
  - 历史列表改为主信息优先，管理动作收敛

## v2 静态稿的关键规则

- 首页必须是短而硬的训练指挥台
- 十式必须以“正式名称轨道列表”呈现，不再使用十个数字按钮
- 休息时长要用更紧凑的刻度式表现
- 训练进行页必须图形化表达实时节奏：
  - 环形节奏盘
  - 相位轨道
  - 呼吸脉冲条
- 历史列表页和详情页要显式预留：
  - 编辑
  - 删除
  - 复制训练
- 动作标准页必须显示原书来源章节，并预留正文与示意图区

## 当前未完成事项

- Android Compose 视觉实现还没按已确认的 HTML 稿重做
- 远程构建链路还没有真正产出 APK
- 还没有 APK
- 动作标准正文、示意图、音频素材还没有正式导入

## 当前官方下一步

1. 以 `prototype/ui-redesign.html` 为唯一 UI 基准，开始改 Android 正式页面
2. 优先修改首页、训练进行页、历史列表页、历史详情页
3. 在现有代码基础上持续修正，不要推倒重写
4. 跑通远程构建，产出第一版可安装 APK

## 对新的 AI 的要求

- 不要再回去修旧视觉
- 不要把旧 Android 页面当成当前设计基准
- 优先以 `prototype/ui-redesign.html` 为准继续
- 如果 Android 当前实现与 HTML 稿不一致，应以 HTML 稿为准修正
