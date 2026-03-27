# 数据模型与计次规则 v0.1

## 1. 核心实体

### MovementFamily

- `id`
- `titleZh`
- `titleEn`

### MovementStep

- `familyId`
- `level`
- `label`
- `contentStatus`

### CadenceProfile

- `id`
- `eccentricSeconds`
- `bottomPauseSeconds`
- `concentricSeconds`
- `topPauseSeconds`
- `source`

### WorkoutBatch

- `id`
- `startedAtUtc`
- `endedAtUtc`
- `status`
- `totalSetCount`
- `totalRepCount`

### WorkoutExerciseEntry

- `id`
- `batchId`
- `familyId`
- `stepLevel`
- `setCount`
- `repCount`

### WorkoutSet

- `id`
- `entryId`
- `startedAtUtc`
- `endedAtUtc`
- `elapsedMs`
- `cadenceProfileId`
- `completedRepCount`
- `lastCompletedPhase`
- `usedVoiceCue`

## 2. 关系

- 一个 `WorkoutBatch` 包含多个 `WorkoutExerciseEntry`
- 一个 `WorkoutExerciseEntry` 包含多个 `WorkoutSet`
- 一个 `WorkoutSet` 对应一个 `CadenceProfile`

## 3. 计次规则

首版采用实时节奏引导计次，而不是结束后统一换算。

设：

- `elapsedMs` 为用户手动开始到手动结束的毫秒数
- `cycleMs` 为一整个动作周期的毫秒数

实时规则：

- 每完成一个完整周期，`completedRepCount += 1`
- 组结束时保存当前 `completedRepCount`

解释：

- 只把完整节奏循环计入次数
- 未完成的循环不算作完整次数
- 计数在训练过程中实时显示

## 4. 示例

- 若节奏采用 `2-1-2`，则完整周期为 5 秒
- 用户完成了 6 个完整周期，并在第 7 个周期中途结束
- 则本组记录为 `6 次`
- 当前未完成周期不计入次数

## 5. 为什么这样设计

- 用户只需专注动作，交互成本低
- 结果与“固定节奏训练”天然一致
- 多组、多动作、多次训练都能统一建模

## 6. 内容实体

后续应补充：

### StandardContent

- `familyId`
- `stepLevel`
- `summary`
- `keyPoints`
- `commonMistakes`
- `imageAssetPath`
- `contentStatus`
- `sourceType`

## 7. 页面位置建议

动作标准页建议放在“进入某一式之后”的独立详情页中，并在同一页提供：

- 动作标准说明
- 动作示意图
- 注意事项
- 开始训练按钮

这样更合理，因为用户可以先看标准，再直接开始本式训练，不需要在多个独立页面之间来回跳转。
