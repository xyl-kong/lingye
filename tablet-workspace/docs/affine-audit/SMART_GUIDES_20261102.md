# 智能参考线与边缘吸附（2026-11-02）

## 1. 需求

拖动白板中的矩形/文本框时，将其左/右/上/下边缘与水平/垂直中心与未选中元素比较；距离小于阈值（默认 5px）时吸附，并绘制紫色参考线；拖拽结束或失焦时清除参考线。

## 2. 纯逻辑算法

源码：`app/src/main/java/com/example/tabletworkspace/SmartGuides.kt`

```kotlin
data class SnapOutcome(val bounds: CanvasBounds, val dx: Float, val dy: Float, val guides: List<SnapGuide>)
data class SnapGuide(val vertical: Boolean, val position: Float, val start: Float, val end: Float)

object SmartGuides {
    fun snap(moving: CanvasBounds, others: List<CanvasBounds>, threshold: Float = 5f): SnapOutcome
}
```

输入：被拖拽包围盒 `CanvasBounds(left, top, right, bottom)`、其他未选中元素包围盒列表、阈值（世界单位）。
输出：吸附后的包围盒、实际位移修正 `(dx, dy)`、参考线列表（世界坐标）。

规则：

1. 每个轴独立计算三组候选：左/上边缘、中心、右/下边缘。
2. 与每个其他元素的同类三组候选两两比较，取 `|delta|` 最小的组合。
3. `|delta| <= threshold` 才生效；阈值包含边界（等于 5 也吸附）。
4. 两轴独立，因此可同时产生一条竖线和一条横线（角对齐）。
5. 参考线位置取对齐后的坐标；长度取被拖拽元素与所有“落在同一条线上的元素”的并集范围，因此多个元素共线时只画一条长线。
6. 无其他元素、阈值为 0 或包围盒无效时原样返回，不产生参考线。

复杂度：O(N × 9 × 2)，N 为其他元素数量；每次拖拽事件只计算一次，不构造空间索引。

## 3. 应用内实现

- 状态：`snapGuides`、`dragSnapStart`、`dragSnapOthers`、`dragSnapAccum`（`MainActivity.kt` CanvasEditor）。
- 拖拽开始（选择工具、非缩放/非端点/非框选）：记录当前选区包围盒，并用 `Page.snapTargetBounds(exclude)` 一次性收集未选中形状与图片的包围盒。
- 拖拽中：把累计位移加到起始包围盒，调用 `SmartGuides.snap(candidate, others, 5f / scale)`；用“目标包围盒 − 当前包围盒”移动选区，因此元素会被磁吸在参考线上。
- 阈值：`5f / scale`，即屏幕上约 5px；画布缩放时吸附手感保持一致。
- 绘制：在画布绘制完形状/连线后，用紫色 `#8B5CF6`、1dp 线宽绘制参考线；参考线位于元素之上、选中手柄之下。
- 清除：`onDragEnd`、`onDragCancel`、工具切换、画布失去焦点（`onFocusChanged`）都会清空 `snapGuides` 与拖拽记忆。

## 4. 性能

- Compose 的 `detectDragGestures` 事件由手势系统按帧合并，无需额外 `requestAnimationFrame`；每次移动事件只计算一次 O(N) 吸附。
- 其他元素包围盒在拖拽开始时缓存，拖拽过程中不重复遍历页面。
- 只在“单选/多选整体拖动”时启用；缩放、端点拖动、框选、连线工具不参与计算。
- 网格吸附（`snapToGrid`）开启时跳过智能吸附，避免两套吸附规则互相拉扯。

## 5. 测试

`SmartGuidesTest` 10 项：

- 左边吸附并生成竖线（含范围）。
- 阈值边界（距离正好 5 吸附）。
- 超过阈值不吸附。
- 多个候选取最近。
- 中心点对齐（中心到中心）。
- 水平边吸附并生成横线。
- 两轴同时吸附（角对齐）。
- 多个元素共线时一条参考线覆盖并集。
- 空列表原样返回。
- 阈值为 0 不吸附。

总计 319 项 JVM 测试 0 失败。

## 6. 验证与发布

- `sg5.png`：拖动文本框时出现紫色竖线，左边缘保持吸附在参考线上。
- `sg6.png`：松手后参考线清除。
- `sg3-end.png`：另一次拖动中参考线覆盖被拖拽元素与对齐元素的范围。

发布：`releases/lingye-v0.15-smart-guides-release-20261102.apk`，2,594,497 字节，SHA-256 `F73F14A93557E8146390C75DB629FE86D719E7B2765A5CB5C7DC52D3CE1BEB0F`。

## 7. 限制

- 只对形状与图片的轴对齐包围盒生效；旋转元素（当前仅图片有旋转）未按旋转后的外接盒计算。
- 参考线不区分“边缘对齐/中心对齐”的颜色或图标，统一为紫色实线。
- 缩放元素、拖动连线端点、框选不显示参考线。
- 网格吸附开启时智能参考线关闭。
- 未在真机与深色主题下逐项核对紫色对比度。
