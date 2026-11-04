# 阶段 C 实施记录：统一对象能力 / 上下文工具栏宿主 / 浮窗定位

日期：2026-11-01。范围：C1–C7；页面格式保持 24，未新增/修改持久化字段。

## 1. 交付内容

| 工作包 | 状态 | 说明 |
|---|---|---|
| C1 对象类型/能力模型 | DONE（基础） | 新增 `CanvasObjectKind`、`CanvasCapability`、`CanvasObjectSpecs.kindOf/capabilities/supports`；覆盖 Text/Note/Shape/Image/Connector/Stroke/Frame/Mindmap/Link/PageCard。生产代码用 `primaryObjectKind` 为工具条打 `testTag`；按钮仍未全部改为 capability 驱动。 |
| C2 统一工具栏宿主 | DONE | 10 个对象上下文工具条（页面卡、卡片、形状、链接卡、思维节点、Frame、文本、图片、笔迹、连接线）全部改用 `ContextualToolbarHost`，统一 Surface 圆角/阴影/颜色/边框参数。 |
| C3 浮窗定位 | DONE（对象锚点） | `CanvasToolbarAnchor` + `resolveToolbarPlacement`：按对象上/下、左右边界、底部工具栏保留区、IME 上沿定位；首次测量前 alpha=0，避免闪烁。 |
| C4 菜单宿主/互斥 | DONE（底部菜单） | 新增 `ContextualMenuHost`，思维导图/内容/更多三个触发菜单统一 gap；打开任一工具菜单时隐藏对象上下文工具条。对象工具条内部菜单仍使用原 DropdownMenu。 |
| C5 属性绑定 | PARTIAL | 新增 `CanvasPropertyEditor`（shape/image/connector/stroke 统一读取当前对象 + 单次 changed）；Text 字号/颜色/对齐、Note 颜色/阴影/圆角、Shape 类型/填充/边框、Frame 背景已迁移；其余属性按钮仍直接赋值。 |
| C6 锁定/分组统一 | DONE（Shape 系对象） | `CanvasObjectSpecs.isLocked/hasLocked/unlocked`；移动/缩放/删除/对齐/分组/取消组合/复制均按锁定规则处理；复制锁定对象会得到未锁定副本。图片/连线/笔迹在格式 24 无 locked 字段，保持不可锁定。 |
| C7 视觉 token/UI 测试 | DONE（基础） | `CanvasUiTokens` 统一按钮/参数条/底部 chrome；新增 3 项 Compose UI 测试（位置、翻转、IME），在模拟器 `connectedDebugAndroidTest` 通过。 |

## 2. 新增文件

- `app/src/main/java/com/example/tabletworkspace/CanvasObjectModel.kt`
  - `CanvasObjectKind`（10 种，含 `PAGE_CARD`）。
  - `CanvasCapability`（编辑、字体、填充、边框、裁剪、路径、端点、画笔、Frame、思维导图、链接、通用对象操作等）。
  - `CanvasObjectSpecs.kindOf(page, selection)`：从 `Shape.type`/`cardKind` 和独立列表推导语义类型，不读/写新字段。
  - `capabilities(kind)` / `supports(kind, capability)`：阶段 D/E 的按钮可见性与禁用规则基础。
- `app/src/main/java/com/example/tabletworkspace/ContextualToolbar.kt`
  - `CanvasToolbarAnchor(x, y, bottom, align)`。
  - `ContextualToolbarTokens`：margin 12dp、gap 8dp、bottomReserve 92dp、radius 17dp、shadow 13dp。
  - `resolveToolbarPlacement(...)`：纯函数，可单测。
  - `ContextualToolbarHost(...)`：测量内容 → 计算位置 → 统一 Surface；支持 Start/Center 对齐、软键盘上沿、底部工具栏避让、`testTag("object-toolbar-<kind>")`。

## 3. 修改点

- `MainActivity.kt`
  - 在 `CanvasEditor` 计算 `canvasWidthDp`、`canvasHeightDp`、`imeBottomDp`（`WindowInsets.ime`）和 `primaryObjectKind`。
  - 10 个对象工具条从各自的 `Surface(Modifier.offset(...))` 改为 `ContextualToolbarHost`；锚点使用对象 bounds 的 top/bottom，连接线使用中点 + Center 对齐。
  - 文本工具条保留细边框；其余使用宿主统一无边框表面。
  - 删除未再使用的 `floatingToolbarX`。
  - `selectedConnectorIndex` 增加单选条件，避免多选时出现单选连接线工具条。
- `ContextualToolbarHost` 不改变对象字段、不触碰 `Page.encode/decode`。

## 4. 定位行为

1. 水平：`Start` 对齐对象左缘；`Center` 对齐连接线中点；按测量宽度 clamp 到 `[12dp, viewport-宽度-12dp]`。
2. 垂直：优先放在对象上方 `gap=8dp`；上方空间不足时翻到对象下方；底部保留 92dp 给主工具栏。
3. IME：`imeBottom > 0` 时，最大 y 取 `viewportHeight - imeBottom - 12dp - toolbarHeight`，避免被软键盘遮挡。
4. 首次组合：尺寸未测量时 transparent；测量后立即定位，避免初始闪烁。
5. 页面卡没有 `CanvasSelection`，工具条仍由 `pageCardSelected` 显示；`objectKind` 为 null，testTag 为通用 `object-toolbar`。

## 5. 测试

新增：

- `CanvasObjectModelTest` 4 项：对象类型映射、非法/越界选择、能力差异、PageCard/Embed 边界。
- `ContextualToolbarPositionTest` 7 项：上方/下方、左右 clamp、连接线居中、底部工具栏、IME、超宽工具条。

现有 + 新增共 **250 项单元测试，0 失败**。

模拟器回归（debug + R8 release）：

- 页面卡：点击显示编辑/移动/关闭工具条。
- 形状：左下/右侧形状工具条均在对象上方、随选择变化；右侧未溢出。
- Frame：边框选中工具条位于 Frame 上方。
- 文本：工具条位于文本上方，保留边框。
- 连接线/卡片/图片/笔迹/思维节点工具条使用同一 host；没有对象可用的状态未逐一造数据验证，定位逻辑由单测覆盖。
- 页面内容与格式未变（仍为 24）；截图：`evidence/stage-c-pagecard.png`、`stage-c-shape.png`、`stage-c-shape-right.png`、`stage-c-frame.png`、`stage-c-text.png`、`stage-c-final.png`。

构建（C4–C7 最终包）：

- `releases/lingye-v0.15-stage-c2-release-20261101.apk`
  SHA-256 `E4AB5319CC906452BD8EACB2EDFC1371EC29AC9443C2484EE56DEAB17A7E1B66`
- `releases/lingye-v0.15-stage-c2-debug-20261101.apk`
  SHA-256 `0F941B2408BA756F55231464D915F27CE0267CB0667574DBDBA7C187CDEE02F4`

测试：259 项 JVM 单元测试 0 失败；3 项 Compose UI 测试在模拟器通过（`connectedDebugAndroidTest`，位置/翻转/IME）。

> **数据事故**：`connectedDebugAndroidTest` 会卸载应用并清空模拟器数据。本轮测试后已从 `format23-backup.tar`、`current.page`、`prefs-restore.xml` 恢复 195 个页面/13 个历史文件、191 个活动文档和最新 AlphaRenamed 页；`files/images` 为空，恢复页面中 62 页包含 `.img` 引用，图片很可能缺失且无原始文件可恢复。恢复后备份见 `evidence/restored-20261101-backup.tar`（SHA-256 `19D21ED6F947D47A9397EF454A7DB83E5F2B7881AA7822081A61E5780F59D700`）。详见 `DATA_RECOVERY_20261101.md`。后续跑 UI 测试前必须先做完整备份。

## 6. C4–C7 增补

- **C4 菜单宿主/互斥**：新增 `ContextualMenuHost`，底部思维导图/内容/更多三个菜单统一 `bottomMenuGap=24dp`；打开任一工具菜单时对象上下文工具条隐藏（10 个 `AnimatedVisibility` 都加 `!toolbarMenuOpen`）。对象工具条内部菜单未全部迁移，仅保证互斥。
- **C5 属性绑定**：新增 `CanvasPropertyEditor`，统一从当前对象读取并只调用一次 `changed()`；已迁移 Text 字号/颜色/对齐、Note 颜色/阴影/圆角、Shape 类型/填充/边框色/边框样式/边框宽度、Frame 背景。其余属性按钮仍直接赋值，后续逐项迁移。
- **C6 锁定/分组**：`CanvasObjectSpecs.isLocked/hasLocked/unlocked` + `Page` 扩展；移动、缩放、删除、对齐、分组、取消组合、复制均遵守锁定（复制产生未锁定副本；含锁定成员的组不能取消组合）。图片/连线/笔迹在格式 24 无 locked 字段，保持不可锁定。
- **C7 token/UI 测试**：`CanvasUiTokens` 统一主按钮 40dp、插画 40dp、对象按钮 34dp、参数条 14dp/30dp/28dp、底栏 28dp、缩放 36dp；新增 `ContextualToolbarHostTest` 3 项（InsideViewport/FlipBelow/IME），在模拟器通过。

## 7. 未完成 / 风险

- 能力注册表尚未驱动每个按钮的可见/禁用；当前用 `kindOf` 统一类型与 testTag，阶段 D/E 再迁移按钮。
- 对象工具条内部菜单仍用 DropdownMenu 默认 offset；只有底部三个菜单进入 `ContextualMenuHost`。
- 属性绑定迁移是部分覆盖；未迁移按钮仍可能写入陈旧捕获值。
- 图片/连线/笔迹无锁定字段；若需统一锁定语义，需要页面格式 25（本轮未改）。
- IME 定位使用窗口 IME 高度；分屏/悬浮键盘下需进一步验证。
- 对象工具条宽度随内容变化，首次测量前 alpha=0；极快连续切换时可能出现一帧透明，需更多 UI 测试。
- **测试工具链风险**：`connectedDebugAndroidTest` 会卸载应用，已导致一次数据清空；见 `DATA_RECOVERY_20261101.md`。

## 8. 下一步

C4–C7 已有基础；进入 D（Text/Note）前需确认页面格式 25 与完整备份方案。先做一次发布前数据备份（pages/history/images/shared/templates/prefs），再开始 D。
