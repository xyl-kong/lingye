# 浮窗布局与定位规范（阶段 A/B）

审计日期：2026-11-01。范围：对象上下文工具条、底部参数条、下拉菜单、对话框、提示浮层；不修改代码。

> **2026-11-01 阶段 C 更新**：10 个对象上下文工具条已迁移到 `ContextualToolbarHost` + `resolveToolbarPlacement`（对象锚点、左右/底边 clamp、IME 上沿）；下文 1.1/3 中“固定 offset、无底边/IME clamp”的描述是迁移前审计记录。菜单、参数条、属性绑定和视觉 token 仍按现状。

## 0. 证据等级

- 官方 BlockSuite 工具栏定位源码不在本机可读，AFFiNE 产品无法浏览器操作；官方精确锚点/偏移 = **UNVERIFIED**。
- 既有审计的工具栏结构和少量产品观察 = **PRIOR**。
- 当前实现 = **CUR**，行号均指 `MainActivity.kt`，另有 `CanvasLayout.kt`、`VisualDesign.kt`。

## 1. 当前定位机制

### 1.1 对象上下文工具条

- 所有对象工具条是画布 `Box` 内的 `AnimatedVisibility` + `Modifier.offset(x.dp, y.dp)`，位置由 `viewport.toScreen(Point(shape.x, shape.y))` 每帧重算（B：3089,3115,3188,3327,3360,3403,3456,3597,3656,3730,4207）。
- 横向：多数使用 `CanvasLayout.toolbarX(atX, viewportWidthDp, toolbarWidth)`，clamp 到 `[12, viewportWidth - toolbarWidth - 12]`（`CanvasLayout.kt:7-13`）。
- 纵向：统一 `at.y - 54..58f`，只 `coerceAtLeast(8.dp)`，**没有下边界 clamp**（3089-3091,3116,3188 等）。
- 卡片工具条：`x = at.x.dp.coerceAtLeast(12.dp)`，没有右侧 clamp（3116）。
- 连接线：`x = midpoint.x - 90f`，`coerceAtLeast(12.dp)`，无右侧 clamp（3730-3731）。
- 思维节点：`CanvasLayout.toolbarX(..., 372f)`（3361-3363）。
- 缩放/平移：位置随 `scale/pan` 重算，但工具条自身尺寸不随缩放变化，按钮仍是固定 dp。

### 1.2 底部参数条

- 位于 `Column(Modifier.align(BottomCenter).padding(bottom=28.dp))`（3806）。
- 每个参数条上方 `Spacer(8.dp)`（如 3876）；Frame 条在宽视口 `frameBarShift = -110.dp`（3804,3865）。
- 参数条 `CanvasParamSurface`：圆角 14dp、98% SurfaceHigh、阴影 8dp、1dp 边框（2145-2153）。
- Frame 条与工具栏的垂直间距由 Spacer 8dp 固定；任务要求只横向移动，已满足。

### 1.3 下拉菜单

- `LingyeDropdownMenu` 包装 Material3 `DropdownMenu`，默认 `offset = DpOffset(0,0)`（106-130）。
- 底部三个菜单（思维导图/内容/更多）`modifier = Modifier.offset(y = 24.dp)`（4058,4075,4105），使菜单底缘与工具栏约 16dp；菜单仍由 Compose 的窗口定位器放置。
- 对象工具条内的菜单（形状类型/填充/边框、卡片图层、文字字号/颜色、图片/笔迹图层、连线路径/marker、Frame 背景、思维布局、多选对齐）都未设置统一 offset，均为默认定位（B 表格）。
- 侧面栏菜单使用 `DpOffset(10.dp,6.dp)`（307,1080,1223,1235）。

### 1.4 对话框与提示

- 连接线标签、思维导图父级/删除、链接选择器、Frame 导航/重命名、模板管理、裁剪等使用 `AlertDialog`（3773-3795,4250-4385）。
- 工具提示使用自定义 `AboveAnchorPositionProvider`：`x` 居中并 clamp ≥4，`y = anchor.top - height - 4`（`VisualDesign.kt:303-309`），`PopupProperties(focusable=false, dismissOnClickOutside=false, ...)`（364）。
- 系统 Insets：只有 `SystemBarsEffect` 在沉浸式画布隐藏/恢复系统栏（2253-2270）；对象工具条/菜单没有 Insets 或 IME 避让。

## 2. 当前度量（CUR）

| Token | 当前值 | 证据 |
|---|---|---|
| 主工具栏按钮 | 40dp 高；线型宽 40/60dp；插画 40dp | `VisualDesign.kt:391-402,561-577` |
| 主工具条内边距/间距 | 7dp 横向、5dp 纵向；2dp 间距 | 4010-4013 |
| 主工具条 Surface | 圆角 20dp，阴影 12dp，1dp 边框 | 3994-4000 |
| 参数条 Surface | 圆角 14dp，阴影 8dp，1dp 边框 | 2145-2153 |
| 参数 chip | 高 30dp，水平 8dp | 2169-2188 |
| 参数宽度点 | 28dp | 2156-2165 |
| 对象工具条 | 继承 `Surface`/RoundedCornerShape 12dp 左右；按钮 `CompactIconAction` 34dp | 3111-3178 等 |
| 底部工具条底部距屏幕 | 28dp | 3806 |
| 缩放条 | 右侧 16dp、底部 36dp | 4164-4167 |
| 菜单下移 | 24dp；菜单底部≈工具栏上方 16dp | 4058,4075,4105 |
| Frame 参数条水平位移 | 宽视口 -110dp；窄视口 0 | 3804,3865 |
| 工具提示偏移 | 上方 4px，水平居中 clamp | `VisualDesign.kt:303-309` |

> 这些是当前实现 token，不是官方测量。官方尺寸/颜色/阴影因证据不足标 UNVERIFIED；阶段 C 不得把它们宣称为官方一致。

## 3. 当前问题清单

| ID | 问题 | 证据 | 影响 |
|---|---|---|---|
| POP-01 | 对象工具条使用固定屏幕 dp 偏移，非统一锚点解析 | 3089-3091,3116,3730 | 缩放/平移/不同对象尺寸下错位 |
| POP-02 | 只有上/左 clamp，无右/下 clamp | 3116,3188,3730 | 靠近右/下边缘被裁切 |
| POP-03 | 卡片/连接线工具条未使用 `floatingToolbarX` | 3116,3731 | 右边缘溢出 |
| POP-04 | 工具条尺寸固定 dp，不随画布缩放 | 全部 offset 工具条 | 大缩放时相对对象显得过大/过小 |
| POP-05 | 菜单分散在对象工具条内部，无宿主互斥 | 3127-3166,3203-3290,3735-3755 | 菜单与工具条/参数条可能叠加 |
| POP-06 | 底部三个菜单靠 `modifier offset` 经验值定位 | 4058,4075,4105 | 依赖 Compose 定位实现，非结构化锚点 |
| POP-07 | 无 IME Insets/软键盘避让 | 2253-2270（仅系统栏） | 内联编辑时工具条可能被键盘遮挡 |
| POP-08 | 无焦点/关闭状态机（菜单、编辑、选择） | 分散的 `dismissToolMenus`/`closeCanvasOverlays` | 多浮层关闭顺序不一致 |
| POP-09 | 对话框使用 Material 默认宽度/位置 | 3773-3795 等 | 平板横屏下对话框可能过窄/位置固定 |
| POP-10 | 参数条与对象工具条无统一 Z-order 规则 | 3994 vs 3088-3798 | 选中对象在底部时可能被参数条覆盖 |

## 4. 目标统一规范（建议）

### 4.1 锚点类型

```text
sealed class Anchor {
  data class Object(ref, placement = AboveCenter, offset = ...)
  data class Trigger(rect, placement = AboveEnd, offset = ...)
  data class Screen(alignment, margin)
}
```

- `Object`：由世界坐标 + 当前 viewport 计算屏幕 rect；工具条与对象选中框使用同一坐标系。
- `Trigger`：底部按钮/工具条内菜单按钮；菜单与触发按钮的边距由 token 决定。
- `Screen`：对话框、全屏提示等；仍走 Insets/IME 避让。

### 4.2 PositionResolver

输入：`anchorRect`、`popupSize`、`viewport`、`windowInsets`、`imeInsets`、`scale`、`preferredPlacement`。  
输出：`IntOffset` + 最终 placement，保证：

1. 与锚点边缘间距 `gap`（对象工具条 8dp；触发菜单 8dp；参数条 8dp）。
2. 水平 clamp 到 `[safeLeft, safeRight]`，垂直 clamp 到 `[safeTop, safeBottom]`。
3. 上方空间不足时翻转到下方；仍不足则贴边并内部滚动。
4. IME 可见时，底部浮层上移到 `imeTop - gap`。
5. 画布缩放时工具条尺寸按 `scale` 做有限缩放（或保持固定，但锚点与对象中心对齐；需产品决策，默认固定以可点击）。
6. 返回实际 bounds 供焦点/关闭/命中测试。

### 4.3 统一 token（暂定，待官方截图/源码确认）

| Token | 建议值 | 说明 |
|---|---|---|
| `TOOLBAR_HEIGHT` | 44dp（触控） | 与 Android 触控目标一致 |
| `TOOLBAR_RADIUS` | 12–14dp | 沿用现有参数条/对象条 |
| `TOOLBAR_PADDING_H/V` | 6–8dp / 4–5dp | 需与官方截图对齐 |
| `BUTTON_SIZE` | 40–44dp | 现有 34/40；阶段 C 统一 |
| `BUTTON_GAP` | 2dp 组内，8dp 组间 | 现有 2/4/6 混杂 |
| `MENU_GAP` | 8dp | 三个底部菜单目前 16dp |
| `SHADOW` | 6–12dp | 通过层级 token |
| `SAFE_MARGIN` | 12dp + insets | 现有 8/12 |
| `IME_GAP` | 8dp | 新增 |

### 4.4 层级与互斥

- 层级（高→低）：模态对话框 > 工具提示 > 菜单 > 对象工具条 > 参数条 > 选中框/控制柄 > 画布对象 > 背景。
- 同一时间只允许一个“主浮层”：对象工具条、参数条、菜单三者由宿主仲裁；打开菜单时参数条隐藏（当前已部分实现）。
- 菜单打开时点击其他工具按钮：当前 Compose Popup 会吞掉第一次点击；阶段 C 必须在宿主层处理“菜单切换”而不是依赖两次点击。
- 关闭顺序：Esc/Back 先关闭最上层菜单，再关闭工具条，再清选中；内联编辑优先退出并收键盘。

## 5. 定位测试矩阵

| 场景 | 期望 |
|---|---|
| 对象在左上/右上/左下/右下 | 工具条完整可见，不越过 12dp safe margin |
| 25% / 100% / 400% 缩放 | 工具条跟随对象；不遮挡对象控制柄 |
| 平移后打开工具条 | 位置基于世界坐标，不因屏幕固定坐标漂移 |
| 横屏/竖屏/分屏 | 重新计算宽度；不溢出 |
| 软键盘弹出（内联编辑） | 工具条上移或隐藏；输入框可见 |
| 菜单打开后点另一个菜单按钮 | 一次点击完成切换（目标行为） |
| 多浮层叠加 | 仅允许规定层级；关闭顺序正确 |
| 深色/浅色 | 表面/边框/阴影对比度满足可读性 |

## 6. 阶段 C 必做

1. 抽出 `PositionResolver` 与 `ContextualToolbarHost`，全部对象工具条迁移。
2. 移除固定屏幕坐标（`at.x.dp`、`at.y-54`）与未 clamp 的分支。
3. 菜单统一使用 `Trigger` 锚点，禁止 `modifier.offset` 经验值。
4. 引入 Insets/IME 监听；内联编辑时自动避让。
5. 建立浮层互斥状态机与焦点恢复。
6. 用结构化 bounds 断言替代“肉眼观察”；对官方缺失的证据保持 UNVERIFIED。
