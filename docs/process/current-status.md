# 当前状态与路线校正

日期：2026-04-29

## 1. 当前结论

当前可执行路线仍然是：

1. 先用最小可用版本把 Android 成品继续往前推，优先可训练、可记录、可远程构建
2. 保持 `prototype/ui-redesign.html` 作为界面收敛参考，不把它误当成最终成品
3. 采用远程构建作为默认交付路线，本机尽量不要求安装 Android 开发依赖
4. 在每轮只改少量关键页面的前提下，持续修正 UI

当前 `prototype/ui-redesign.html` 已得到用户确认，属于本轮 Android UI 改造的唯一基准。

这轮已经开始直接修改 Android 真代码，不再只停留在静态稿。本地构建尝试只是一次环境探测，不是对用户路线的改要求。

## 2. 本轮新增成果

- 已按确认后的 HTML 稿开始重写 Android 前端：
  - 新增统一 UI 壳层：`app/src/main/java/com/liuyi/trainer/ui/UiChrome.kt`
  - 已重写首页：`app/src/main/java/com/liuyi/trainer/ui/HomeScreen.kt`
  - 已重写训练流与历史页 UI：`app/src/main/java/com/liuyi/trainer/ui/TrainingFlowScreens.kt`
  - 已重写主题色与字阶：`app/src/main/java/com/liuyi/trainer/ui/theme/`
- 已继续修改 Android 训练页与历史页：
  - 训练执行页补回“已训练”总时长主指标
  - 历史列表分组预览改为更直接的次数串
  - 历史详情显式显示每组的实际组间休息
  - 历史列表改为按日期归档，默认只展开最近一天，避免记录增长后无限摊长
  - 历史条目压缩为总次数主指标 + 每组次数摘要，弱化重复日期文字
  - 历史详情把每组结束时间拆出单独层级，减少一行塞满信息的阅读负担
- 已调整 GitHub Actions 开发线发布规则：
  - `develop` push 现在会和 `main` 一样自动触发 Android 工作流
  - `develop` 线固定刷新 `debug-latest` 调试版下载页，不占用稳定线
- 已同步更新静态参考稿：
  - `prototype/ui-redesign.html`
- 已修正环境检查脚本：
  - `CHECK_ANDROID_ENV.bat` 现在会检查 `javac`
- 已执行一次本机构建探测：
  - `./gradlew.bat assembleDebug`
  - 当前失败原因已明确为缺少 JDK，仅说明“本机不能直接编译”，不改变远程构建主路线
- 已用用户提供的 EPUB 确认六艺章节映射：
  - 俯卧撑：`text00013.html`
  - 深蹲：`text00014.html`
  - 引体向上：`text00015.html`
  - 举腿：`text00016.html`
  - 桥：`text00017.html`
  - 倒立撑：`text00018.html`
- 已从 EPUB 补齐六艺十式正式名称
- 已把正式名称写入：
  - `content/exercises_seed.json`
  - `app/src/main/java/com/liuyi/trainer/model/TrainingDomain.kt`
- 已重写 UI 蓝图为 v2：
  - `docs/product/ui-redesign-blueprint.md`
- 已重做静态审查稿 v2：
  - `prototype/ui-redesign.html`
- 已根据新反馈继续收紧为 v2.1 方向：
  - 首页十式由长列表改为更紧凑的当前式 + 拨盘式切换
  - 训练进行页移除多余说明块、开始时间等低价值信息
  - 历史列表页移除每条记录的三按钮外露布局

## 3. 这轮静态稿的核心变化

- 首页改成更短的“训练指挥台”
- 十式改为正式名称轨道列表，不再使用十个数字按钮
- 组间休息选择改为更紧凑的刻度式表现
- 训练进行页加入图形化节奏引导：
  - 环形节奏盘
  - 相位轨道
  - 呼吸脉冲条
- 历史列表页与详情页显式预留：
  - 编辑
  - 删除
  - 复制训练
- 动作标准页显式标出原书章节来源，并预留正文与示意图位

## 4. 当前没有做的事

- 这轮还没有把全部页面重构到最终满意的 UI
- 这轮还没有产出 APK
- 这轮没有接入正式动作正文、示意图、音频素材

## 5. 当前未完成事项

- 远程构建链路还未实际跑通一次
- 动作标准页还未接入 EPUB 正文与示意图

## 6. 当前禁止偏航的方向

- 不要再回到旧首页和旧视觉风格上修补
- 不要把“Android 页面已有代码”误判为“视觉方向已定稿”
- 不要在没有构建验证的情况下宣称 App 已可交付

## 7. 官方下一步

1. 以 `prototype/ui-redesign.html` 为唯一 UI 基准修改 Android 正式页面
2. 优先重构首页、训练执行页、历史列表页、历史详情页
3. 准备并检查 GitHub Actions 远程构建链路
4. 远程产出 APK 后，让用户先安装这版最小可用成品使用
