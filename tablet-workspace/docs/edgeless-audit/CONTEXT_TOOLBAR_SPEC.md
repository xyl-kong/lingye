# 上下文工具栏规范（阶段 A/B）

审计日期：2026-11-01。目标：先定义统一 Contextual Toolbar 架构与差异，阶段 A/B 不修改代码。

> **2026-11-01 阶段 C 更新**：已新增 `CanvasObjectKind`/`CanvasCapability` 能力模型与 `ContextualToolbarHost`，10 个对象工具条统一使用宿主；按钮仍按对象各自实现，尚未全部改为 capability 驱动（C4–C7 待做）。详见 `STAGE_C_IMPLEMENTATION.md`。

## 0. 证据与限制

- 官方工具栏源码（BlockSuite `edgeless-toolbar`、`gfx/*/toolbar`）不在本机可读；既有审计引用的 commit `5cb5cb6` 只能作为 PRIOR。
- 本地 AFFiNE 前端可证的对象设置与工具栏动作：`affine-src/.../edgeless/*.tsx`、`.../editor-config/toolbar/index.ts`。
- 官方产品按钮顺序、图标、尺寸、二级菜单完整内容：UNVERIFIED（`docs.affine.pro` 403；无浏览器自动化；官方 Android App 未安装）。
- 当前实现证据均为 `MainActivity.kt`（行号）与 `VisualDesign.kt`。

## 1. 当前上下文工具栏盘点

所有对象工具条都是 `AnimatedVisibility` + `Modifier.offset` 的独立实现，锚定在画布 `Box` 内，随 `viewport.toScreen` 更新。

| 工具条 | 触发条件 | 位置/宽度 | 按钮/菜单（当前） | 代码 |
|---|---|---|---|---|
| 页面卡 | `pageCardSelected && tool=="选择"` | 卡左上，`x=at.x`, `y=at.y-54` | 编辑（仅聚焦）、移动、关闭 | 3088-3110 |
| 笔记卡片 | `selectedCardIndex` 且单选 | `x=at.x`（仅左 clamp），`y=at.y-54` | 编辑、勾选（todo）、模板、复制、只读缩放%、蓝/黄、图层与更多（4 图层 + 3 阴影 + 5 圆角）、插入文档、锁定、删除 | 3111-3178 |
| 几何形状 | `selectedShapeIndex` | `floatingToolbarX`，`y=at.y-54` | 类型（rect/roundRect/ellipse）、填充、边框色、边框样式、边框宽度循环、复制、图层、锁定、删除 | 3179-3321 |
| 链接卡 | `selectedPageLinkIndex` | 同上 | 打开、复制、删除 | 3322-3354 |
| 思维节点 | `selectedMindNodeIndex` | `CanvasLayout.toolbarX(...,372f)` | 添加子/同级、编辑、布局菜单、改接、删除 | 3355-3397 |
| Frame | `selectedFrameIndex` | 同上 | 重命名、背景、定位、导航、演示、复制、删除 | 3398-3448 |
| 文本 | `selectedTextIndex` | 同上 | 编辑、字号菜单+/-、颜色、对齐、复制、锁定、更多（图层/删除） | 3449-3591 |
| 图片 | `selectedImageIndex` | 同上 | 裁剪、旋转、水平/垂直翻转、复制、图层、删除 | 3592-3650 |
| 笔迹 | `selectedStrokeIndex` | 同上 | 颜色、减细、宽度、加粗、图层、删除 | 3651-3707 |
| 连接线 | `selectedConnectorIndex`（未校验单选） | 连线中点 `x-90`，`y-46`，仅左 clamp | 路径、起点、终点、标签、翻转、动画、蓝/灰、删除 | 3708-3798 |
| 多选 | `tool=="选择" && selectedItems.size>1` | 底部参数区上方？实际在 Canvas 底部 Column | 复制、对齐、组合、取消组合、绑定、保存模板、删除 | 3959-3999 |

## 2. 当前实现的结构性问题

| 问题 | 证据 | 影响 |
|---|---|---|
| 每个对象复制一套工具条，按钮顺序/间距/圆角/阴影各自定义 | 3088-3798 的 10 个 `AnimatedVisibility` 块 | 视觉与行为漂移；新增属性需改多处 |
| 按钮直接调用属性复制，没有统一的能力模型/可用性表达式 | 例如 3118-3173、3282-3311、3545-3586 | 无法自动处理“对象不支持”“锁定”“多选混合值” |
| 属性值读取分散（`shape.fontSize`、`shape.color` 等） | 3477-3543 | 双向同步脆弱；菜单打开时值变化需手动重算 |
| 位置使用固定 dp 偏移，且部分工具条未统一 clamp | 3116、3730-3731 vs 2374-2388 | 缩放/平移/边缘/窄屏下错位或裁切 |
| 同一时刻多个浮层缺乏统一宿主 | 下拉菜单分散在各工具条内部 | 菜单与对象工具条、参数条可能叠加 |
| 多选没有混合属性面板 | 3959-3999 | 只能复制/对齐/组合/删除 |
| 锁定状态未在所有按钮中统一禁用 | 2389-2390,2787 vs 2405,2424,2487,2501 | 锁定语义不完整 |
| 只读控件混在工具条（缩放 %） | 3124 | 无效控件占用空间 |
| 页面卡“编辑”只聚焦 | 3097 | 行为不明确 |
| 连接线单选条件不一致 | 2388 | 多选时可能出现单选工具条 |

## 3. 目标架构（建议，不含实现）

### 3.1 对象能力模型

```text
CanvasObjectRef(type, id, kind)            // text/note/shape/image/connector/stroke/frame/mindmap/link/embed
ObjectCapability(
  id, label, icon,
  applies(refs), isVisible(ctx), isEnabled(ctx),
  value(ctx): ToolbarValue,
  commit(ctx, value)
)
ToolbarSection(id, priority, actions: List<Action|Toggle|Menu|Color|Slider>)
```

- `ctx` 至少包含：`selection: List<CanvasObjectRef>`、`primary`、`editState`、`textSelection`、`locked`、`zoom/pan`、`settings`、`canUndo`。
- `isVisible` 与 `isEnabled` 分离；禁用必须提供原因（用于 tooltip/无障碍）。
- 属性控件使用 `value(ctx)` 读实际对象，不在 UI 侧缓存；commit 后调用统一 `PageHistory` 事务包装。
- 多选时：`value` 返回 mixed/null；只有交集能力可见；批量 commit 一次入历史。

### 3.2 统一工具栏宿主

```text
ContextualToolbarHost(anchor, model, onAction)
  -> PositionResolver(anchor, viewport, insets, ime)
  -> Surface(TOKENS.toolbarSurface)
      -> SectionRow(...)
```

禁止每个对象自行 `AnimatedVisibility + offset`。对象工具条只声明 model，宿主负责布局/定位/动画/边缘避让/焦点。

### 3.3 属性双向同步契约

以文本颜色为例，必须满足：

1. 工具栏色板打开时读取 `primary.textColor`，选中项与对象一致。
2. 选择颜色 → `commit(primary, color)` → 对象字段改变、画布重绘、历史记录一次。
3. 撤销 → 历史恢复旧颜色；工具栏若仍打开，值随对象重算。
4. 重开页面 → 颜色来自页面格式字段。
5. 多选时若颜色不同，色板显示“混合”且不虚假标选。

## 4. 对象工具栏目标清单

> “官方”列只列出有 SRC/PRIOR 证据的按钮；UNVERIFIED 项不得作为已完成验收。

### Text

| 分组 | 控件 | 官方证据 | 当前 |
|---|---|---|---|
| 编辑 | 编辑/退出、文字选区 | 内联编辑 PRIOR | 有编辑，无选区 |
| 字体 | 字号 16–64、字体族、样式、字重、颜色、对齐 | `text.tsx:39-139` SRC | 字号 16–48、颜色、对齐；其余缺 |
| 格式 | 粗体/斜体/下划线（选区或整块） | PRIOR | 无 |
| 布局 | 自动换行/最大宽度/缩放 | `docs/text.json` PRIOR | 固定宽高/8 行裁切 |
| 对象 | 复制、图层、锁定、删除 | PRIOR | 有 |
| More | Copy as Image/Markdown/link | `toolbar/index.ts:277-413` SRC | 无 |

### Note

| 分组 | 控件 | 官方证据 | 当前 |
|---|---|---|---|
| 编辑 | 内联标题/正文、子块 | PRIOR | 标题/正文；无子块 |
| 样式 | 背景色板、圆角 0/8/16/24/32、阴影、边框样式/厚度 | `note.tsx:29-264` SRC | 背景仅蓝/黄、圆角/阴影/边框已有 |
| 待办 | 下一项/行级勾选 | PRIOR | 下一项；行级未接 UI |
| 模板 | 保存/插入 | PRIOR | 有 |
| 转换 | 插入文档 | PRIOR | 标题/正文，丢失格式 |
| 对象 | 复制、图层、锁定、删除 | PRIOR | 有 |

### Shape

| 分组 | 控件 | 官方证据 | 当前 |
|---|---|---|---|
| 类型 | 5 种形状 | `shape.tsx:177-202` SRC | 3 种可切换；圆角/三角/菱形通过创建工具而非选中菜单 |
| 样式 | General/Scribbled | `shape.tsx:81-104` SRC | 无 |
| 填充/边框 | 填充色、边框色、solid/dash/none、宽度 2–12 | `shape.tsx:106-267` SRC | 填充/边框色/样式；宽度 1/2/4 |
| 文字 | 编辑、颜色、字体族/样式/字重/字号 16–64、对齐 | `shape.tsx:269-347,498-513` SRC | 无形状内文字 |
| 对象 | 复制、图层、锁定、删除、旋转 | PRIOR | 除旋转外有 |
| 默认值 | 按 shapeName 独立 | `shape.tsx:96-104` SRC | 仅全局填充色 |

### Image

| 分组 | 控件 | 官方证据 | 当前 |
|---|---|---|---|
| 编辑 | 裁剪、旋转、翻转 | PRIOR | 有 |
| 对象 | 复制、图层、删除 | PRIOR | 有 |
| 锁定/替换/边框/说明 | 官方 Image 属性 | UNVERIFIED | 无 |

### Connector

| 分组 | 控件 | 官方证据 | 当前 |
|---|---|---|---|
| 路径 | straight/step/curve | `connector.tsx:90-124` SRC | 有 |
| 样式 | rough/general、solid/dash、宽度 2–12 | `connector.tsx:63-159` SRC | 有宽度但 1–8；无 rough/dash |
| 颜色 | 色板 | `connector.tsx:161-184` SRC | 参数条十色；选中工具条仅蓝/灰 |
| 端点 | 起点/终点完整 PointStyle | `connector.tsx:186-214` SRC | 五态 |
| 标签 | 文字+字体族/样式/字重/字号 16–64/颜色/对齐 | `connector.tsx:243-353` SRC | 仅文字对话框 |
| 对象 | 翻转、动画、图层、删除 | PRIOR | 除图层外有 |

### Brush

| 分组 | 控件 | 官方证据 | 当前 |
|---|---|---|---|
| 绘制 | 画笔/高亮、颜色、线宽 2–12、压感/平滑 | `pen.tsx:32-60` SRC；高级行为 PRIOR/UNVERIFIED | 画笔/高亮/颜色/线宽/仅手写笔；无压感/平滑 |
| 橡皮 | 整笔/区域/局部 | PRIOR/UNVERIFIED | 整笔/区域；无局部 |
| 对象 | 颜色/宽度、图层、删除 | PRIOR | 有；无缩放/锁定/分组 |

### Frame

| 分组 | 控件 | 官方证据 | 当前 |
|---|---|---|---|
| 创建 | 比例 Custom/1:1/4:3/16:9/2:1 | PRIOR | 有 |
| 属性 | 重命名、背景色 | `frame.tsx:22-52` SRC | 有（背景六色） |
| 视图 | 定位、导航、演示 | PRIOR | 有 |
| 对象 | 复制、锁定、删除 | PRIOR | 复制/删除；无锁定 |
| 结构 | 嵌套、删除成员联动 | PRIOR/UNVERIFIED | 无 |

### Mind Map

| 分组 | 控件 | 官方证据 | 当前 |
|---|---|---|---|
| 创建 | LEFT/BALANCE/RIGHT + Style 1–4 | `mind-map.tsx:20-94` SRC | 5 种结构，无主题 |
| 节点 | 添加子/同级、编辑、改接、删除 | PRIOR | 有（改接为对话框） |
| 布局 | 切换布局 | SRC 3 种 | 5 种结构切换 |
| 交互 | 拖拽改接、整图选择 | PRIOR/UNVERIFIED | 无 |

### 多选

| 分组 | 控件 | 官方证据 | 当前 |
|---|---|---|---|
| 对象操作 | 复制、对齐、组合、取消组合、绑定、模板、删除 | PRIOR | 有 |
| 属性 | 混合值显示/批量修改 | UNVERIFIED | 无 |
| 类型限制 | connector/stroke 是否可组 | UNVERIFIED | 仅 shape/image 可组 |

## 5. 工具栏布局与行为规范（Android 适配）

| 项 | 规范（建议基线） | 当前状态 |
|---|---|---|
| 表面 | 圆角 12–14dp，1dp 边框，阴影 6–8dp | 对象工具条各自 `RoundedCornerShape`/elevation；未统一 |
| 按钮 | 触控目标 ≥44dp；图标 18–20dp；线型/插画两套 | `ToolIconButton` 40dp 高；CompactIconAction 34dp |
| 分组 | 2dp 组内间距、7–8dp 组分隔 | 对象工具条多数 4–6dp；无统一 token |
| 属性控件 | 颜色点 18dp/32dp 槽；chip 30–34dp | 当前分散 |
| 下拉菜单 | 与触发按钮同侧，底部距工具栏 8–16dp | 底部三个菜单用 `offset y=24dp`；对象菜单默认 |
| 屏幕边界 | 左右/上下均避让 + Insets | 仅左/上 clamp，部分未右/下 clamp |
| 焦点 | 工具条打开后不抢文本焦点；Esc/Back 关闭 | 局部实现 |
| 多浮层 | 同一时间一个工具条 + 一个菜单 | 菜单分散，缺少宿主级互斥 |

## 6. 验收清单（供阶段 C/D 使用）

1. 每个对象/状态只渲染一个宿主工具条；按钮来自 capability registry。
2. 打开工具栏读取对象真实值；修改后对象、画布、历史三方一致。
3. 撤销/重做后工具栏值同步；重开后持久化值正确。
4. 多选时只显示交集能力，混合值不显示为已选。
5. 锁定对象：所有修改/删除/方向键/对齐/组合均按规范禁用并给出原因。
6. 工具栏在 25%–400% 缩放、四边、横竖屏、分屏、软键盘下不越界。
7. 所有禁用/缺失能力有明确状态，不以空实现伪装完成。
8. 与官方参考的按钮顺序/分组/图标有截图或源码证据；无证据项保持 UNVERIFIED。

## 7. 结论

当前对象工具栏是“每个对象一套复制实现”，已覆盖较多真实操作，但缺少统一能力模型、属性绑定和定位宿主；这是阶段 C 的首要重构对象。官方精确按钮/视觉无法在当前环境验证，阶段 C 应先建立可测试的统一架构，再逐对象填入有证据的按钮，避免把 PRIOR/UNVERIFIED 项当成已完成。
