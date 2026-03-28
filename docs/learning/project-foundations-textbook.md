# Liuyi Trainer 项目基础知识教材

## 适用对象

这份文档写给“几乎没有软件开发基础，但想真正看懂并逐步参与这个项目”的读者。

它不是一份零散备忘录，而是按照教材思路编排：

- 先讲项目目标
- 再讲实现它需要的知识地图
- 再讲每一类知识在当前仓库里是怎样落地的
- 最后给出适合新手的学习顺序与练习路线

如果你是第一次接触编程，不要试图一次性记住所有内容。正确方式是：

1. 先通读，建立全局认识。
2. 再按后文的“学习顺序”一点点回看。
3. 每读完一章，就回到对应代码文件中对照。

---

## 第 1 章 先搞清楚：这个项目到底是什么

### 1.1 项目目标

这个项目是一个运行在 Android 手机上的本地训练助手，围绕“六艺十式”训练体系，帮助用户完成以下事情：

- 选择某一艺、某一式
- 按固定节奏进行训练
- 在训练过程中实时显示阶段、时间和次数
- 支持多组训练
- 支持组间休息倒计时与超时计时
- 训练结束后保存历史记录
- 后续支持浏览动作标准内容

这不是一个“只有几个页面”的静态展示项目，而是一个有明确业务规则的软件工程项目。

### 1.2 这个项目为什么分成两条线

当前仓库里其实有两条实现线：

1. Android 正式实现
2. 浏览器轻量原型

原因不是“重复开发”，而是为了降低验证成本。

在当前阶段，本机没有完整 Android 开发环境，因此先用浏览器原型验证训练逻辑和页面流程；逻辑稳定后，再将同样的规则落实到 Android 应用中。

所以你会在仓库中同时看到：

- `app/`：正式 Android 工程
- `prototype/`：轻量 HTML/CSS/JavaScript 原型

### 1.3 这个项目的核心，不是页面，而是规则

真正决定项目复杂度的，不是按钮长什么样，而是这些规则：

- 什么叫“开始一组”
- 什么叫“结束一组”
- 如何根据 `2-1-2` 节奏实时推进阶段
- 什么情况下算完成了一次完整动作
- 什么时候进入休息态
- 什么时候从休息倒计时转成休息超时正计时
- 一次训练结束后如何归档保存

这说明一个重要事实：

软件开发不是“把界面画出来”那么简单，而是先把业务规则定义清楚，再让界面去表达这些规则。

### 1.4 当前仓库的核心目录

先建立目录感非常重要。

- `README.md`：项目总览
- `docs/product/srs.md`：需求规格说明，定义产品要做什么
- `docs/architecture/architecture.md`：架构说明，定义系统怎么分层
- `docs/architecture/data-model.md`：数据模型与计次规则
- `docs/process/current-status.md`：当前进度和路线约束
- `app/`：Android 正式代码
- `prototype/`：浏览器原型代码
- `content/`：内容种子文件
- `.github/`：远程构建与自动化流程

如果你以后迷路了，优先回看这几个文档，而不是一头扎进代码。

---

## 第 2 章 实现这个项目需要哪些基础知识

很多新手会问：“我要学什么？”

更准确的问题是：“为了写这个项目，我至少要掌握哪些层面的知识？”

答案可以分成九层。

### 2.1 编程通识

你需要理解：

- 变量
- 数据类型
- 条件判断
- 循环
- 函数
- 类和对象
- 列表与集合
- 状态
- 输入与输出

这是所有编程语言共同的地基。

### 2.2 Kotlin 语言基础

Android 代码当前使用 Kotlin 编写，因此你需要会：

- `val` 和 `var`
- 函数定义
- `data class`
- `enum class`
- `object`
- `sealed interface`
- 空安全基础
- 集合操作，如 `map`、`sumOf`、`firstOrNull`

### 2.3 Android 应用基础

你需要知道：

- 什么是 `Application`
- 什么是 `Activity`
- 什么是 `AndroidManifest.xml`
- 什么是资源文件 `res/`
- 一个 Android App 是如何启动的

### 2.4 Jetpack Compose 界面开发

这个项目没有使用传统 XML 页面，而是使用 Compose。因此你要理解：

- 什么是声明式 UI
- 什么是 `@Composable`
- 什么是状态驱动界面
- 什么是 `Modifier`
- 什么是组件拆分

### 2.5 应用状态管理

这个项目里，训练过程不是静态页面，而是动态状态机，所以你必须理解：

- 什么是页面状态
- 什么是应用状态
- 什么是 `ViewModel`
- 什么是单向数据流
- 为什么“界面显示什么”应由状态决定

### 2.6 领域建模与状态机

这是本项目最重要的一层。你需要学会：

- 如何把业务概念抽象成数据结构
- 如何定义状态
- 如何定义状态之间的转换
- 如何把规则写成纯函数

### 2.7 本地数据持久化

训练记录不能只停留在内存里，所以你要懂：

- 数据库表是什么
- 表与表的关系是什么
- 什么是实体 `Entity`
- 什么是数据访问对象 `Dao`
- 什么是仓库 `Repository`
- 什么是 Room

### 2.8 协程与异步

训练页面要实时刷新，历史记录要异步读取，所以你要知道：

- 什么是协程
- 什么是 `launch`
- 什么是 `delay`
- 什么是 `Flow`
- 什么是持续观察数据变化

### 2.9 工程化与文档

这个项目不是“只写代码”，而是完整工程。因此你还需要知道：

- 什么是需求文档
- 什么是架构文档
- 什么是版本管理 Git
- 什么是 Gradle 构建系统
- 什么是远程构建

请注意：

你现在不需要把这些都学到专家级，但必须知道它们分别解决什么问题。

---

## 第 3 章 编程最基础的地基

这一章故意不讲 Android，先讲最底层概念。

### 3.1 变量

变量可以理解为“有名字的盒子”，用来保存数据。

例如：

```kotlin
val familyId = "pushup"
var restSeconds = 90
```

这里：

- `familyId` 保存当前动作家族编号
- `restSeconds` 保存休息时长

`val` 表示值确定后不再重新赋值。
`var` 表示之后还允许修改。

对新手来说，一个很重要的习惯是：

能用 `val` 就先用 `val`，因为不变的数据更安全、更容易推理。

### 3.2 数据类型

程序不是只处理“文字”，而是处理不同类型的数据。

在本项目里常见的数据类型有：

- `String`：字符串，如动作编号、标题
- `Int`：整数，如第几式、休息秒数
- `Long`：长整数，常用于毫秒时间
- `Boolean`：真假值，如语音是否开启
- `List<T>`：列表，如已完成的训练组集合

如果你分不清类型，程序就很容易在“时间”“文本”“次数”这些概念之间混乱。

### 3.3 函数

函数就是“接收输入，执行规则，返回结果”的代码单元。

例如：

```kotlin
fun selectStep(stepLevel: Int) {
    selectedStepLevel = stepLevel
}
```

这个函数做的事很简单：

- 接收一个 `stepLevel`
- 把它写入当前状态

复杂函数也是一样，只不过规则更多。

### 3.4 条件判断

程序要根据不同情况执行不同逻辑。

例如：

```kotlin
if (currentState !is TrainingSessionState.SetRunning) {
    return
}
```

意思是：

- 如果当前不是“正在训练一组”的状态
- 那么就不要继续执行“结束本组”的逻辑

这就是业务规则的守门员。

### 3.5 列表与聚合

训练不是只记录一组，而是多组，因此要使用列表。

例如：

- `completedSets`：已经完成的组列表
- `recentSessions`：最近训练记录列表

你还需要学会对列表做统计：

```kotlin
val totalReps = completedSets.sumOf { it.completedRepCount }
```

这里的意思是：

- 遍历每一组
- 取出每一组的完成次数
- 把它们加总

### 3.6 类与对象

如果一段数据总是一起出现，就应该把它们组织成类。

例如一个节奏配置包含：

- 编号
- 标签
- 下落秒数
- 底部停顿秒数
- 起身秒数
- 顶部停顿秒数
- 来源说明

这就很适合写成一个 `CadenceProfile` 类。

类的意义，不只是“把字段装在一起”，更重要的是让业务概念明确。

### 3.7 状态

新手很容易忽略“状态”这个概念。

所谓状态，就是“系统当前处于什么阶段”。

在本项目里，至少有这些状态：

- 空闲
- 正在做一组
- 组间休息
- 休息超时
- 本次训练已完成

不同状态下：

- 允许的按钮不同
- 显示的信息不同
- 能执行的函数不同

所以，状态不是附属品，而是系统行为的中心。

---

## 第 4 章 Kotlin 基础：当前项目实际在用什么

下面开始进入语言层。

### 4.1 `data class`

在 `app/src/main/java/com/liuyi/trainer/model/TrainingDomain.kt` 中，你会看到这样的结构：

```kotlin
data class CadenceProfile(
    val id: String,
    val label: String,
    val eccentricSeconds: Int,
    val bottomPauseSeconds: Int,
    val concentricSeconds: Int,
    val topPauseSeconds: Int,
    val source: String,
)
```

`data class` 适合表示“主要用来装数据”的对象。

它的特点是：

- 写法简洁
- 自动生成常用能力
- 非常适合领域模型

在这个项目里，很多核心概念都适合用 `data class`：

- 节奏配置
- 动作步骤
- 训练组结果
- 页面预览数据

### 4.2 计算属性

`CadenceProfile` 里还有一个属性：

```kotlin
val cycleDurationMs: Long
    get() = (...) * 1000L
```

这叫计算属性。

它不是把结果硬编码存起来，而是每次访问时根据已有字段算出来。

这很适合用于“可以由其他数据推导出来”的值。

本项目里的完整动作周期时长，就是由各阶段秒数相加后转换成毫秒得到的。

### 4.3 `enum class`

在 `WorkoutScoring.kt` 中有：

```kotlin
enum class CadencePhase {
    Lowering,
    BottomHold,
    Rising,
}
```

枚举适合表示“固定且有限的取值集合”。

这里的阶段只有三种：

- 下落
- 底部停顿
- 起身

当一个概念的可能取值很少，而且这些取值是预先定义好的，用枚举通常比字符串更安全。

### 4.4 `object`

在 `TrainingDomain.kt` 中有：

```kotlin
object ExerciseCatalog {
    ...
}
```

`object` 表示单例对象。你可以把它理解为“整个应用只需要一份的对象”。

这里很适合用它来放动作目录，因为动作目录本身不是运行时频繁创建的临时对象，而是全局共享的静态数据。

### 4.5 `sealed interface`

在 `TrainingSessionEngine.kt` 中，训练状态被定义为：

```kotlin
sealed interface TrainingSessionState
```

再往下有多个具体状态：

- `Idle`
- `SetRunning`
- `RestRunning`
- `RestOvertime`
- `Completed`

`sealed` 的意义是：

- 这个状态体系是封闭的
- 所有可能状态都在当前文件中列出来
- 编译器可以帮助你检查分支是否处理完整

这非常适合状态机建模。

### 4.6 不可变数据思想

在这个项目中，一个明显的设计倾向是：

- 尽量用不可变数据表达状态
- 通过“生成新状态”而不是“到处修改旧状态”来推进流程

例如，结束当前组时，不是随便改几个字段，而是调用：

- `finishCurrentSet(...)`

它会根据旧状态和结束时间，返回新的休息状态。

这让代码更容易验证，也更不容易出现“字段改了一半”的错误。

### 4.7 集合操作

Kotlin 很常用集合函数。

例如：

- `map`：把一个列表转换成另一个列表
- `mapIndexed`：转换时顺便拿到索引
- `sumOf`：把列表中的某个数值字段加总
- `firstOrNull`：寻找第一个满足条件的元素，找不到就返回空
- `chunked`：把列表按指定数量分组

这些操作在 UI 和数据处理代码中大量出现。

例如首页把六艺按两列显示时，就用到了 `chunked(2)`。

### 4.8 时间 API

项目里大量使用 `Instant` 和 `Duration`。

这说明开发者没有把时间简单地当字符串处理，而是使用正式的时间对象。

比如：

- `Instant.now()`：当前时刻
- `Duration.between(start, end)`：两个时刻之间的时长

这样做的好处是：

- 语义清晰
- 不容易算错
- 更适合做训练计时这种严肃逻辑

---

## 第 5 章 Android 应用基础：一个 App 是怎么启动的

### 5.1 `AndroidManifest.xml` 是总声明文件

文件位置：`app/src/main/AndroidManifest.xml`

这是 Android 应用的基础声明文件。它告诉系统：

- 应用的入口在哪
- 应用名是什么
- 使用哪个 `Application`
- 哪个 `Activity` 是启动页

当前项目里可以看到：

- `android:name=".app.LiuyiTrainerApplication"`
- `android:name=".MainActivity"`
- `MAIN` 和 `LAUNCHER` 意味着它是桌面点击后首先启动的页面

### 5.2 `Application` 是应用级入口

文件位置：`app/src/main/java/com/liuyi/trainer/app/LiuyiTrainerApplication.kt`

`Application` 对象在整个应用生命周期里通常只会有一份。

它适合放：

- 全局单例依赖
- 数据库实例
- 仓库实例

当前项目中它负责：

- 创建数据库 `LiuyiTrainerDatabase`
- 创建仓库 `TrainingHistoryRepository`

这说明项目已经在做基础依赖管理，而不是把数据库创建逻辑散落到界面里。

### 5.3 `Activity` 是 Android 界面宿主

文件位置：`app/src/main/java/com/liuyi/trainer/MainActivity.kt`

`MainActivity` 的职责并不复杂：

- 接收系统启动
- 设置 Compose 内容
- 应用主题
- 挂载根界面 `LiuyiTrainerApp()`

这说明当前项目把真正的业务和界面逻辑尽量放在 Compose 层，而不是塞进 Activity。

这是现代 Android 项目比较清晰的写法。

### 5.4 资源文件 `res/`

Android 项目中的资源不只包括图片，也包括：

- 字符串
- 主题
- 颜色
- 图标
- 布局资源

当前项目里至少有：

- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values/themes.xml`

它们负责：

- 应用名称
- 基础系统主题配置

### 5.5 Android 生命周期为什么重要

即使你暂时没有深入学习生命周期，也要先建立概念：

手机应用不会像命令行程序一样“启动一次就一直稳定运行”。

系统可能会：

- 切后台
- 回前台
- 重建页面
- 杀掉进程

所以训练记录和 UI 状态不能只靠屏幕上的变量硬撑，而要有清晰的状态管理和持久化策略。

这也是为什么这个项目使用了：

- `ViewModel`
- Room
- 文档化架构

---

## 第 6 章 Gradle 构建系统：项目为什么能被编译

很多新手害怕 Gradle，但你至少要知道它在这个项目里做什么。

### 6.1 根构建文件

文件：`build.gradle.kts`

这里主要声明顶层插件，例如：

- Android Application 插件
- Kapt 相关插件
- Compose 编译器插件

这说明整个工程采用 Kotlin DSL 管理构建，而不是旧式 Groovy 脚本。

### 6.2 `settings.gradle.kts`

这个文件定义：

- 仓库来源
- 依赖解析策略
- 工程名
- 包含哪些模块

当前项目包含：

- `:app`

这表示目前是单模块应用。

### 6.3 `app/build.gradle.kts`

这是最核心的模块构建脚本。它定义了：

- 应用包名
- `compileSdk`
- `minSdk`
- `targetSdk`
- 构建类型
- Java 版本
- Compose 开关
- 依赖项

这里你可以把它理解成：

“告诉编译系统，这个 Android 模块要怎样被构建，以及它依赖哪些库。”

### 6.4 依赖管理

当前项目引入了这些关键库：

- `androidx.activity.compose`
- `androidx.navigation.compose`
- `androidx.room.runtime`
- `androidx.room.ktx`
- `androidx.datastore.preferences`
- Compose BOM
- Material3
- Lifecycle 相关库

这意味着项目具备：

- Compose UI
- 导航
- 数据库存储
- 生命周期集成
- 偏好存储扩展空间

### 6.5 版本目录 `libs.versions.toml`

文件：`gradle/libs.versions.toml`

这是现代 Gradle 常见写法。它把版本号集中管理，而不是把具体版本散落在每个构建文件里。

优点是：

- 版本更容易统一修改
- 依赖管理更清晰
- 可读性更强

### 6.6 你现在要掌握到什么程度

你暂时不需要会自己写复杂 Gradle 脚本，但至少要知道：

- 构建失败时，问题不一定出在业务代码
- 插件、SDK、依赖版本都可能导致构建问题
- `app/build.gradle.kts` 是 Android 模块配置中心

---

## 第 7 章 声明式 UI：为什么当前项目不用 XML 页面

### 7.1 什么是声明式 UI

传统 Android 页面常用 XML 描述布局，再在代码里查找控件、修改控件。

Compose 则采用声明式写法：

- 你不先“摆控件再操控控件”
- 你直接描述“在当前状态下，界面应该长什么样”

这是一种非常重要的思想变化。

### 7.2 `@Composable` 的意义

例如 `HomeScreen.kt` 里的：

```kotlin
@Composable
fun HomeScreen(...) {
    ...
}
```

这表示它是一个可组合的界面函数。

你可以把它理解为：

- 输入：当前状态和事件回调
- 输出：应该渲染出的界面

### 7.3 Compose 为什么适合这个项目

因为这个项目界面高度依赖状态变化：

- 训练中显示什么
- 休息中显示什么
- 已完成显示什么
- 语音是否开启
- 当前是第几组
- 当前累计多少次

如果继续采用“找到控件然后一个个改”的写法，状态会变得很散。

Compose 的优势就在于：

当状态变化时，界面自动根据新状态重新组合。

### 7.4 组件拆分

在 `HomeScreen.kt` 中，你会看到：

- `CompactHeaderCard`
- `ChoiceCard`
- `RestPresetSelector`
- `StepSelector`
- `SectionTitle`

这说明页面不是一个超大函数，而是拆成多个小组件。

组件拆分的好处是：

- 结构更清晰
- 复用更容易
- 修改局部样式时影响更可控

### 7.5 `Modifier` 的作用

Compose 里大量使用 `Modifier`，例如：

- `fillMaxWidth()`
- `padding(16.dp)`
- `weight(1f)`

你可以把 `Modifier` 理解为“给组件附加布局、尺寸、间距、背景、交互等能力的链式工具”。

这是 Compose 的基本功之一。

### 7.6 状态提升

看 `HomeScreen` 的函数参数可以发现：

- 它不自己保存最终业务状态
- 它接收外部传入的状态和值
- 它只通过回调把用户操作反馈出去

例如：

- `selectedFamilyId`
- `onSelectFamily`
- `onStartTraining`

这种做法叫状态提升。它的核心思想是：

- 页面负责展示
- 更高层负责管理状态

这正是“界面”和“业务”分离的体现。

### 7.7 预览数据构建

在 `TrainingFlowScreens.kt` 中，有很多 `buildXxxPreview(...)` 函数，例如：

- `buildRunningPreview`
- `buildRestPreview`
- `buildSummaryPreview`
- `buildHistoryPreview`

这是一种很实用的中间层设计：

- 底层是真实状态对象
- 界面真正消费的是更适合展示的预览数据

这样做有几个好处：

- UI 层不用直接处理过多底层细节
- 格式化时间、拼接文案等逻辑有地方安放
- 更利于后续测试和重构

---

## 第 8 章 导航：多个页面如何组织成一个应用

### 8.1 根导航入口

文件：`app/src/main/java/com/liuyi/trainer/app/LiuyiTrainerApp.kt`

这里使用了 `NavHost` 和多个 `composable(...)` 路由。

你可以把它理解为：

- 整个应用有哪些页面
- 每个页面的名字是什么
- 从一个页面如何跳到另一个页面

### 8.2 当前项目的页面路线

当前路由包括：

- `Home`
- `Training`
- `Rest`
- `Summary`
- `History`
- `HistoryDetail`
- `Standards`

这说明应用已经不是单页原型，而是有完整流程。

### 8.3 导航不只是“跳页面”

新手容易把导航理解成“点击按钮打开另一个页面”。

更严谨地说，导航要解决的是：

- 当前用户位于哪一步
- 返回时应回到哪里
- 某些页面是否应清理旧页面栈

例如当前项目里，在开始新一组时会使用：

- `popUpTo(...)`
- `inclusive = true`

这说明开发者在控制回退栈，避免页面层级错乱。

### 8.4 导航与状态配合

导航本身不负责创造业务状态。

例如：

- 真正开始训练，是 `ViewModel.beginTraining()` 的职责
- 导航只是在逻辑成功后切换到相应页面

这是一种正确分工：

- 状态属于业务层
- 页面跳转属于界面流程层

---

## 第 9 章 ViewModel：为什么项目不把状态直接写在页面里

### 9.1 `LiuyiTrainerViewModel` 的地位

文件：`app/src/main/java/com/liuyi/trainer/app/LiuyiTrainerViewModel.kt`

这个类是当前应用层的中心之一。它管理：

- 当前选择的艺和式
- 当前休息预设
- 当前训练会话状态
- 当前时间
- 最近历史记录
- 语音开关
- 总结页草稿
- 当前选中的历史记录

这说明它不是“一个随手写的工具类”，而是整个界面状态的协调者。

### 9.2 为什么需要 ViewModel

因为页面会重组、Activity 会重建，而训练状态不能随便丢。

ViewModel 的意义是：

- 把状态从具体页面中抽离出来
- 让业务状态在页面重组时保持稳定
- 让界面函数尽量纯粹

### 9.3 可观察状态

在 ViewModel 里，你会看到：

- `mutableStateOf(...)`
- `mutableIntStateOf(...)`
- `mutableLongStateOf(...)`

这表示这些值和 Compose 界面是联动的。

当它们变化时，读取这些值的界面会自动刷新。

这就是“状态驱动界面”的核心实现方式。

### 9.4 事件函数

ViewModel 中定义了很多动作函数，例如：

- `selectFamily()`
- `selectStep()`
- `beginTraining()`
- `finishSet()`
- `beginNextSet()`
- `completeTraining()`
- `saveCompletedTraining()`

这些函数代表用户行为在应用层的正式入口。

好处是：

- 界面不直接篡改复杂业务状态
- 逻辑集中
- 更容易检查流程是否合法

### 9.5 派生属性

ViewModel 中还有：

- `selectedContext`
- `activeContext`
- `selectedHistorySession`

这些不是最原始的存储字段，而是根据当前状态推导出的便捷结果。

这很重要，因为并不是所有值都值得单独存储。有些值应该“按需推导”。

### 9.6 Ticker 机制

训练和休息页面都需要实时刷新，因此 ViewModel 中有 `ensureTicker()`。

它的逻辑是：

- 如果当前处于训练中或休息中，就启动一个协程循环
- 每 100 毫秒更新一次当前时间
- 如果处于休息倒计时，就检查是否已进入超时状态

这说明实时界面更新不是靠页面自己乱刷，而是有明确的统一时钟驱动。

---

## 第 10 章 协程与 Flow：项目为什么能一边计时一边读数据

### 10.1 协程是什么

协程可以理解为 Kotlin 提供的一种更轻量、更结构化的异步处理方式。

它适合做：

- 数据库读写
- 定时刷新
- 持续监听数据流

### 10.2 `viewModelScope.launch`

在 ViewModel 里可以看到：

```kotlin
viewModelScope.launch {
    ...
}
```

这表示在 ViewModel 生命周期范围内启动一个协程任务。

这样做的好处是：

- 不需要自己粗暴创建线程
- 生命周期结束时任务更容易被管理
- 写法更直观

### 10.3 `delay` 与循环

Ticker 中使用了：

- `while (isActive)`
- `delay(100)`

意思是：

- 只要协程仍处于活跃状态，就持续循环
- 每次循环后暂停 100 毫秒

这是一个非常典型的“周期性刷新状态”写法。

### 10.4 `Flow` 的意义

数据库观察使用了：

```kotlin
trainingHistoryRepository.observeRecentSessions().collect { sessions ->
    recentSessions = sessions
}
```

`Flow` 可以理解为“持续发出数据的流”。

与“一次性查询”不同，`Flow` 更适合这样的场景：

- 数据以后还会变化
- 变化后希望自动通知界面

这对于历史记录列表特别合适。

### 10.5 新手容易犯的错误

很多新手会把异步理解为“哪里需要就随便开一个线程”。

这是不严谨的。

更好的思路是：

- 长期任务放到明确作用域里
- UI 刷新和数据观察走受控机制
- 定时器、数据库监听、界面状态更新不要互相缠绕

当前项目的协程写法，正是在往这个方向靠拢。

---

## 第 11 章 领域建模：把训练系统翻译成代码

这是全书最关键的一章。

### 11.1 什么叫领域模型

所谓领域模型，就是把业务世界中的概念翻译成代码中的明确结构。

在这个项目里，真实业务概念包括：

- 六艺
- 每艺十式
- 节奏配置
- 当前一组训练
- 组间休息
- 已完成训练
- 训练历史

如果这些概念不先建模，后面的界面、数据库和交互都会混乱。

### 11.2 `ExerciseCatalog`

在 `TrainingDomain.kt` 中，`ExerciseCatalog` 提供了动作目录。

它定义了：

- 六个动作家族
- 每个家族的中英文标题
- 简介
- 十式列表
- 预览节奏

这说明当前项目已经把“动作目录”视为正式数据结构，而不是临时字符串。

### 11.3 为什么节奏要建模成 `CadenceProfile`

节奏不是一句文案，而是一组参数：

- 下落秒数
- 底部停顿秒数
- 起身秒数
- 顶部停顿秒数
- 来源说明

一旦把节奏建模成对象，后面很多事情就都能统一：

- 计算周期长度
- 计算当前阶段
- 记录训练组使用的是哪种节奏

### 11.4 为什么训练状态必须建模

训练过程不是一个布尔值 `isTraining = true/false` 就能表达清楚的。

因为至少还存在：

- 休息态
- 休息超时态
- 已完成态

如果你只用几个零散布尔值，很快就会出现互相矛盾的情况。

例如：

- 既在训练，又在休息
- 已完成，但还能开始下一组
- 未开始，却显示已完成组数

所以使用 `TrainingSessionState` 这种封闭状态体系，是必要的工程做法。

### 11.5 状态机思维

状态机的核心思想是：

- 系统在任一时刻只能处于一种状态
- 不同状态允许的操作不同
- 操作会把系统推进到下一个合法状态

在本项目中，大致流程是：

- `Idle`
- `SetRunning`
- `RestRunning`
- `RestOvertime`
- `Completed`

并不是所有状态之间都能直接互跳。

例如：

- 只有休息相关状态才能开始下一组
- 只有训练中状态才能结束当前组
- 只有活动中的会话才能完成本次训练

这正是状态机在保护业务逻辑。

### 11.6 纯函数的价值

项目中的这些函数都很重要：

- `startTrainingSession()`
- `finishCurrentSet()`
- `snapshotRestState()`
- `updateRestState()`
- `startNextSet()`
- `completeTrainingSession()`

这些函数的共同特点是：

- 输入清楚
- 输出清楚
- 不依赖界面细节
- 规则可单独测试

这就是纯函数思维的价值：

先把业务规则抽成稳定函数，再让界面调用它们。

---

## 第 12 章 训练计次算法：项目真正的核心逻辑

### 12.1 为什么本项目不能只看“总时长”

训练计次最容易偷懒的写法是：

- 记录总时长
- 再除以动作周期
- 得到次数

但本项目要求更清晰：

- 训练过程中实时显示次数
- 只把完整周期计为一次
- 未完成的当前周期不计入完整次数

所以项目里采用的是“实时节奏推进 + 完整周期计次”的模型。

### 12.2 `trackLiveCadence()` 在做什么

文件：`app/src/main/java/com/liuyi/trainer/model/WorkoutScoring.kt`

它接收：

- 节奏配置
- 已经过的毫秒数

然后计算：

- 当前处于哪个阶段
- 当前阶段已经过去多少毫秒
- 已完成了多少次完整动作

这是训练执行页最关键的算法之一。

### 12.3 完整周期为什么等于一次动作

当前规则是：

- 一个完整周期由多个阶段构成
- 只有当整个周期完成，次数才加一

以 `2-1-2` 为例：

- 下落 2 秒
- 底部停顿 1 秒
- 起身 2 秒
- 顶部停顿 0 秒

那么一个完整周期总共 5 秒。

如果用户只做到了第 4.6 秒就结束，那这一轮不算完整次数。

### 12.4 阶段判断怎么做

函数内部的核心思想是：

1. 先算已经完成了多少个完整周期
2. 再用“总经过时间对周期取余”，得到当前周期中的位置
3. 再根据这个位置判断是在下落、停顿还是起身阶段

这是一种非常典型的“周期性时间轴”算法。

### 12.5 结束一组时如何生成结果

`finishWorkoutSet(...)` 会把一组训练整理为正式结果对象，其中包括：

- 动作信息
- 开始时间
- 结束时间
- 总时长
- 使用的节奏配置编号
- 完整次数
- 最后阶段

这意味着：

训练结果不是临时显示数据，而是可以持久保存的正式业务结果。

### 12.6 休息逻辑怎么建模

休息不是只有一个倒计时数字。

项目里通过 `snapshotRestState(...)` 计算：

- 剩余毫秒数
- 超时毫秒数
- 当前是否已超时

然后 `updateRestState(...)` 决定是否从 `RestRunning` 转换为 `RestOvertime`。

这说明组间休息本身也是正式业务规则，而不是简单 UI 装饰。

---

## 第 13 章 数据库与 Room：训练记录为什么能保存下来

### 13.1 为什么需要数据库

如果所有训练数据都只存在内存里，那么：

- 关闭应用后就没了
- 历史记录也不存在
- 无法做详情页

所以这个项目必须有持久化方案。

### 13.2 Room 是什么

Room 是 Android 官方推荐的本地数据库访问框架之一。它建立在 SQLite 之上，但提供了更清晰的 Kotlin/注解式写法。

它通常由三部分组成：

- `Entity`：定义表结构
- `Dao`：定义访问方法
- `Database`：定义数据库入口

### 13.3 `Entity`：表结构建模

文件：`app/src/main/java/com/liuyi/trainer/data/TrainingHistoryEntities.kt`

当前有两张核心表：

- `training_sessions`
- `training_sets`

这两张表分别对应：

- 一次完整训练
- 这次训练中的每一组

这说明数据模型已经明确区分“训练会话”和“训练组”。

### 13.4 主键与外键

`TrainingSessionEntity` 中：

- `sessionId` 是主键

`TrainingSetEntity` 中：

- `setId` 是主键
- `sessionOwnerId` 是外键，指向所属训练会话

这意味着：

- 一次训练可以包含多组
- 每组都知道自己属于哪次训练

这就是关系型数据库的基本思想。

### 13.5 `@Relation`

`TrainingSessionWithSets` 使用了：

- `@Embedded`
- `@Relation`

它的意义是：

把“一次训练”和“它下面的多组”组合成一个更方便使用的对象。

对于界面来说，直接拿这种组合结果比自己手工拼表更轻松。

### 13.6 `Dao`

文件：`TrainingHistoryDao.kt`

`Dao` 是数据库访问接口。当前包含：

- 插入训练会话
- 插入多组记录
- 事务性插入完整训练
- 观察最近训练记录

这里很重要的一点是：

插入完整训练时使用事务逻辑，避免“会话插入了，但组没有插进去”的不一致问题。

### 13.7 `Repository`

文件：`TrainingHistoryRepository.kt`

仓库层的作用是：

- 把底层数据库细节包装起来
- 对上层提供更符合业务语义的方法

例如：

- `saveCompletedSession(...)`
- `observeRecentSessions()`

这说明 ViewModel 不直接组装 SQL，而是通过仓库与数据层交互。

### 13.8 `Database`

文件：`LiuyiTrainerDatabase.kt`

这里声明了：

- 包含哪些实体
- 数据库版本号
- 如何构建数据库实例

同时通过：

- `Room.databaseBuilder(...)`

创建出正式数据库对象。

---

## 第 14 章 从数据到页面：这个项目的数据流是怎样走的

新手经常会“每个文件都看懂一点，但不知道它们怎么串起来”。

这一章专门讲整体数据流。

### 14.1 启动流程

应用启动后，大致流程是：

1. Android 系统根据 `AndroidManifest.xml` 启动 `MainActivity`
2. `MainActivity` 通过 `setContent` 挂载 `LiuyiTrainerApp`
3. `LiuyiTrainerApp` 获取 `LiuyiTrainerViewModel`
4. 导航系统决定显示哪个页面
5. 页面从 ViewModel 读取状态并显示

### 14.2 开始训练一组时的数据流

当用户点击“开始本组”时，大致发生：

1. 页面按钮触发 `onStartSet`
2. 回调进入 `ViewModel.beginTraining()`
3. ViewModel 调用 `startTrainingSession(...)`
4. `sessionState` 变成 `SetRunning`
5. Ticker 开始刷新当前时间
6. 训练页根据新状态重新组合
7. `buildRunningPreview(...)` 计算页面展示数据
8. 页面显示当前阶段、秒数和次数

### 14.3 结束当前组时的数据流

1. 用户点击“结束本组”
2. 进入 `ViewModel.finishSet()`
3. 调用 `finishCurrentSet(...)`
4. 生成当前组结果并追加到已完成列表
5. `sessionState` 变为 `RestRunning`
6. 导航切到休息页
7. 休息页根据当前时间显示倒计时

### 14.4 完成本次训练时的数据流

1. 用户结束本次训练
2. `ViewModel.completeTraining()` 把状态转成 `Completed`
3. 总结页展示所有组的汇总数据
4. 用户可编辑每组次数草稿
5. 点击保存后，ViewModel 调用仓库保存到数据库
6. 历史记录流更新
7. 历史页自动获得最新数据

这就是典型的单向数据流：

- 用户操作触发事件
- 事件进入 ViewModel
- ViewModel 更新状态
- 页面根据状态刷新
- 持久化结果进入数据库
- 数据流再反馈给界面

---

## 第 15 章 浏览器原型：为什么仓库里还有 HTML/CSS/JS

### 15.1 原型的职责

文件目录：`prototype/`

这个原型不是为了替代 Android，而是为了：

- 快速验证训练逻辑
- 让非技术用户直接打开查看
- 在不依赖 Android SDK 的前提下确认需求

### 15.2 `index.html`

这个文件定义了页面结构。你可以把它看成：

- 页面上有哪些区域
- 有哪些按钮、下拉框、信息卡片、表格

它解决的是“界面骨架”问题。

### 15.3 CSS 的作用

`index.html` 里内嵌了 CSS。它负责：

- 配色
- 间距
- 卡片样式
- 网格布局
- 响应式适配

这说明即使只是原型，也不只是“把按钮丢上去”，而是有明确的视觉层级设计。

### 15.4 `app.js`

这个文件负责：

- 初始化动作列表
- 处理按钮点击事件
- 维护原型状态
- 使用 `setInterval` 周期刷新
- 计算当前阶段与次数
- 渲染训练台和表格内容

从学习角度讲，它非常适合拿来理解：

- 什么是前端状态
- 什么是事件驱动
- 什么是 DOM 更新
- 什么是定时器

### 15.5 原型和 Android 代码的关系

你会发现，原型中的很多规则和 Android 版是一致的，例如：

- `2-1-2` 节奏
- 实时计次
- 组间休息
- 休息超时转正计时
- 多组累计

这说明原型不是“另一个独立产品”，而是 Android 版本的逻辑实验场。

---

## 第 16 章 文档先行：为什么这个项目有这么多 `docs/`

很多新手会误以为：“真正开发就是写代码，文档都是可有可无的。”

这是错误认识。

### 16.1 `srs.md` 在干什么

文件：`docs/product/srs.md`

SRS 是需求规格说明。它定义的是：

- 产品目标
- 用户是谁
- 要解决哪些场景
- 功能需求有哪些
- 非功能要求有哪些
- 业务规则有哪些

如果没有这份文档，代码就很容易边写边漂移。

### 16.2 架构文档在干什么

文件：`docs/architecture/architecture.md`

它回答的是：

- 系统分几层
- 每层职责是什么
- 为什么用这些技术
- 当前有哪些风险

架构文档不是为了显得专业，而是为了防止代码失控。

### 16.3 数据模型文档在干什么

文件：`docs/architecture/data-model.md`

它专门定义：

- 核心实体
- 实体关系
- 计次规则
- 内容实体扩展方向

这份文档对本项目尤其重要，因为训练计次逻辑是核心业务规则。

### 16.4 过程文档在干什么

例如：

- `docs/process/start-here.md`
- `docs/process/current-status.md`
- `docs/process/testing-strategy.md`
- `docs/process/remote-build.md`

它们分别负责：

- 新人如何快速进入项目
- 当前真实进度是什么
- 用什么方式测试项目
- 怎样在没有本地 Android 环境时远程构建 APK

### 16.5 这对新手意味着什么

以后你如果觉得代码看不懂，先不要硬啃实现细节。

更高效的方法通常是：

1. 先看需求文档
2. 再看架构文档
3. 再看当前状态文档
4. 最后再看代码

因为代码只是实现，文档先回答“为什么这样实现”。

---

## 第 17 章 软件工程习惯：写这个项目不只是学语法

### 17.1 Git 是版本管理，不是备份文件夹

仓库里有 `.git/`，说明项目已纳入 Git 管理。

Git 的核心作用不是“多存一份文件”，而是：

- 记录历史变化
- 比较修改差异
- 回溯问题来源
- 配合远程仓库协作

### 17.2 工程开发讲求可验证

本项目文档反复强调：

- 先验证逻辑
- 再推进 Android 正式实现
- 先确认用户需求是否理解正确
- 再投入更重的开发成本

这体现的是工程上的节制，而不是犹豫。

### 17.3 不要把“页面变多”当成“功能完成”

`docs/process/current-status.md` 明确提醒：

- 不要继续堆纯静态页面
- 不要把页面数量增加误判为功能完成
- 不要在未构建验证前宣称可交付

这是非常重要的软件工程观念。

### 17.4 先建结构，再补内容

动作标准页目前还是占位内容，这不是失败，而是有意控制范围。

因为内容接入涉及：

- 来源合法性
- 版权边界
- 内容结构设计
- 图片资源管理

所以工程上先搭结构、再接正式内容，是合理顺序。

---

## 第 18 章 按文件读代码：新手推荐阅读路线

下面给你一个更具体的阅读顺序。

### 18.1 第一轮：只建立全局认识

按这个顺序看：

1. `README.md`
2. `docs/process/start-here.md`
3. `docs/product/srs.md`
4. `docs/architecture/architecture.md`
5. `docs/architecture/data-model.md`
6. `docs/process/current-status.md`

目标不是学会写代码，而是知道项目要做什么、做到哪一步了。

### 18.2 第二轮：先看最容易理解的原型

按这个顺序看：

1. `prototype/index.html`
2. `prototype/app.js`

原因是前端原型比 Android 正式代码更直观，适合用来理解：

- 动作选择
- 开始一组
- 结束一组
- 休息倒计时
- 次数统计

### 18.3 第三轮：看领域模型和算法

按这个顺序看：

1. `app/src/main/java/com/liuyi/trainer/model/TrainingDomain.kt`
2. `app/src/main/java/com/liuyi/trainer/model/WorkoutScoring.kt`
3. `app/src/main/java/com/liuyi/trainer/model/TrainingSessionEngine.kt`

这是整个项目最值得认真读的部分，因为它们决定业务规则。

### 18.4 第四轮：看应用层状态管理

按这个顺序看：

1. `app/src/main/java/com/liuyi/trainer/app/LiuyiTrainerViewModel.kt`
2. `app/src/main/java/com/liuyi/trainer/app/LiuyiTrainerApp.kt`

目标是理解：

- 状态放在哪里
- 用户操作如何进入系统
- 页面之间如何跳转

### 18.5 第五轮：看具体界面

按这个顺序看：

1. `app/src/main/java/com/liuyi/trainer/ui/HomeScreen.kt`
2. `app/src/main/java/com/liuyi/trainer/ui/TrainingFlowScreens.kt`

这一轮重点不是样式，而是：

- 每个页面接收什么数据
- 每个页面暴露什么事件回调
- 预览数据是怎样被组织出来的

### 18.6 第六轮：看数据持久化

按这个顺序看：

1. `app/src/main/java/com/liuyi/trainer/data/TrainingHistoryEntities.kt`
2. `app/src/main/java/com/liuyi/trainer/data/TrainingHistoryDao.kt`
3. `app/src/main/java/com/liuyi/trainer/data/TrainingHistoryRepository.kt`
4. `app/src/main/java/com/liuyi/trainer/data/LiuyiTrainerDatabase.kt`

目标是理解训练结果如何落盘。

---

## 第 19 章 给新手的学习顺序：不要乱学

如果你现在从零开始，不建议直接全面铺开。更合理的学习顺序如下。

### 第一步：先学最基本的 Kotlin

目标：

- 看懂变量、函数、类、列表
- 能读懂 `data class`
- 能看懂简单 `if` 和 `when`

如果这一步没打牢，后面的 Android 和 Compose 都会变成死记硬背。

### 第二步：学“状态”这个概念

你必须真的理解：

- 一个系统在任意时刻处于什么状态
- 不同状态允许什么操作
- 一个操作如何让状态发生合法变化

这是本项目最核心的思维训练。

### 第三步：学 Compose 基础

先不要追求复杂动画和高级主题。先学会：

- `@Composable`
- `Column`、`Row`
- `Text`、`Button`、`Card`
- `Modifier`
- 参数传递
- 状态驱动刷新

### 第四步：学 ViewModel 和协程

先明白：

- 为什么状态不直接写在页面里
- 为什么需要 `viewModelScope`
- 为什么实时刷新要用协程循环

### 第五步：学 Room

先掌握最基本的：

- 实体是什么
- 表关系是什么
- DAO 干什么
- Repository 干什么

### 第六步：再回头学 Gradle 和工程结构

这是顺序问题。不要一开始就被构建脚本吓住。

对新手来说，更合理的路径是：

先知道业务，再知道代码，再补工程细节。

---

## 第 20 章 你现在可以做的练习

只看不练，很难真正掌握。下面是适合当前仓库的练习。

### 练习 1：手工描述状态机

不要写代码，只用文字回答：

- 系统有哪些状态
- 每个状态下允许做什么
- 哪些状态不能直接互相跳转

如果你能说清楚，说明你已经开始理解本项目核心。

### 练习 2：自己算一次计次

假设节奏是 `2-1-2`，请手工回答：

- 4 秒结束，算几次
- 5 秒结束，算几次
- 11 秒结束，算几次
- 14 秒结束，算几次

如果你能算对，说明你理解了“完整周期计次”。

### 练习 3：读懂一条数据流

试着用自己的话描述：

“点击开始本组之后，状态是如何从页面一路传到算法，再回到页面显示的？”

### 练习 4：对照原型和 Android 实现

找出：

- 原型里有哪些规则
- Android 里哪些规则已经正式实现
- 哪些部分仍然是占位

### 练习 5：自己补一条简单需求

例如你可以尝试只做纸面设计，不一定立即改代码：

- 增加一个新的休息预设
- 给总结页增加一个提示文案
- 给历史详情页增加“训练总时长”说明

练习目标不是“马上实现”，而是练习从需求到代码位置的映射能力。

---

## 第 21 章 当前项目里，你最应该先真正学会的五件事

如果时间有限，优先级如下。

### 21.1 第一优先级：状态机

这是本项目的灵魂。你必须理解：

- 为什么不能只用几个布尔值
- 为什么状态转换要受约束
- 为什么结束一组、开始下一组、完成训练都属于状态迁移

### 21.2 第二优先级：Kotlin 数据建模

你必须能看懂：

- `data class`
- `enum class`
- `sealed interface`
- 集合操作

因为这决定你能否理解业务对象和流程。

### 21.3 第三优先级：Compose 状态驱动界面

你必须理解：

- 页面不是自己保存最终业务状态
- 界面应根据状态重绘
- 事件通过回调回到 ViewModel

### 21.4 第四优先级：Room 持久化

你必须理解：

- 为什么训练记录要拆成会话和组
- 为什么保存时要有关系字段
- 为什么需要 Repository 隔开上层和数据库

### 21.5 第五优先级：工程文档意识

你必须养成习惯：

- 先看需求和架构
- 再看实现
- 修改前先判断是否偏离项目路线

---

## 第 22 章 术语表

### 22.1 领域层

专门表达业务规则和业务概念的那一层，例如节奏、计次、训练状态。

### 22.2 表现层

负责把数据展示给用户，并接收用户输入的那一层，例如 Compose 页面。

### 22.3 数据层

负责本地持久化、读取、保存记录的那一层，例如 Room、DAO、Repository。

### 22.4 状态机

用有限状态和状态转换规则表达系统行为的模型。

### 22.5 组合函数 `@Composable`

Compose 中用于声明 UI 的函数。

### 22.6 仓库 `Repository`

对上层提供业务语义，对下层封装数据来源细节的中间层。

### 22.7 实体 `Entity`

数据库中的表结构定义。

### 22.8 DAO

数据访问对象，定义数据库操作方法。

### 22.9 `Flow`

Kotlin 中用于持续异步发出数据流的机制。

### 22.10 ViewModel

用于管理界面相关状态并承载应用层交互逻辑的组件。

---

## 结语：如何正确面对这个项目

如果你是新手，最容易犯的错误有两个：

1. 以为自己需要一次性掌握所有技术细节
2. 以为只要会写页面，就算会做这个项目

这两个判断都不对。

对这个项目，正确的学习姿势是：

- 先理解产品目标
- 再理解状态和规则
- 再理解这些规则如何落成 Kotlin 数据结构和函数
- 再理解 ViewModel 如何协调界面与数据
- 最后再去补构建、依赖、远程构建这些工程细节

如果你能把这份文档读到“知道每一章在解决什么问题”，你就已经跨过了最难的第一步。

下一步不是急着把所有代码背下来，而是拿着本教材去对照仓库文件，逐个建立对应关系。
