# 实施路线图（阶段 C–F，待确认）

审计日期：2026-11-01。阶段 A/B 已完成；本文件只提出后续计划，**未修改任何业务代码**，等待用户确认后再执行。

## 0. 前置条件

1. **参考版本锁定**：AFFiNE `8cef09e1dd62b47a3cbc9f352aa10b3a90c75d34`（0.27.5 canary）；BlockSuite `5cb5cb6…` 仅属既有审计，需补齐源码或官方截图后才能作为 SRC。
2. **官方证据**：需获取 AFFiNE Edgeless 对象工具栏/菜单/编辑态截图，或可操作的官方 Android App；否则相关验收保持 UNVERIFIED。
3. **页面格式策略**：新增字段（fontFamily/fontStyle/fontWeight、shape rotation、note 子块等）需要格式 25；必须提供备份、迁移、回滚证明。能通过现有字段表达的能力优先不升格式。
4. **回归基线**：239 项单元测试 + 当前对象工具栏/浮窗截图；阶段 C 先建立 UI 语义/bounds 测试，避免后续重复修改。
5. **约束**：不得删除未确认的桌面功能；Android 适配差异必须记录；未验证项不得标完成。

## 1. 阶段 C：统一对象模型与上下文工具栏架构

**2026-11-01 进度**：C1–C7 已实施基础版本（对象类型/能力模型、统一工具栏宿主、对象锚点定位、`ContextualMenuHost` 与菜单互斥、`CanvasPropertyEditor` 部分属性绑定、锁定/分组守卫、`CanvasUiTokens` 与 3 项 Compose UI 测试）。仍有未迁移按钮/菜单；详见 `STAGE_C_IMPLEMENTATION.md`。阶段 D1 已完成 Text 字体/选区格式，页面格式由 24 升至 25 并完成 195 页迁移；详见 `STAGE_D_IMPLEMENTATION.md` 与 `PAGE_FORMAT_25.md`。D2 首版已完成 Note 完整背景色板、行级待办和插入文档类型保真；Note 块容器未开始，详见 `STAGE_D2_IMPLEMENTATION.md`。

| WP | 内容 | 主要文件 | 依赖 | 风险 | 验收 |
|---|---|---|---|---|---|
| C1 | 定义 `CanvasObjectRef/ObjectKind`（text/note/shape/image/connector/stroke/frame/mindmap/link/embed）与 capability registry | 新增 `CanvasObjects.kt`；`CanvasSelection.kt` | 无 | 低 | 现有对象全部映射；单元测试 |
| C2 | 统一 `ContextualToolbarHost` + `ToolbarSection/Action/Toggle/Menu` 模型 | 新增 `ContextualToolbar.kt`；`VisualDesign.kt` | C1 | 中 | 一个对象工具条迁移后截图/行为不变 |
| C3 | 统一 `PositionResolver`（Object/Trigger/Screen 锚点、Insets/IME、四边 clamp） | 新增 `PopupPosition.kt`；`CanvasLayout.kt` | C2 | 中 | 25–400%、四角、分屏 bounds 测试 |
| C4 | 统一菜单宿主/互斥/焦点/关闭顺序 | `MainActivity.kt` | C2/C3 | 中 | 菜单切换一次点击；Esc/Back 顺序 |
| C5 | 统一属性绑定 `value(ctx)/commit(ctx)` 与历史事务 | `ContextualToolbar.kt`、`PageHistory.kt` | C1/C2 | 中 | 颜色改→撤销→重开测试 |
| C6 | 锁定/分组/图层统一状态与拦截 | `CanvasSelection.kt`、`CanvasGroups.kt`、`MainActivity.kt` | C1 | 中 | 锁定对象所有入口禁用 |
| C7 | 视觉 token（44dp 目标、间距、圆角、阴影）与语义标签 | `VisualDesign.kt`、`ContextualToolbar.kt` | C3 | 低 | golden/bounds 基线 |

**阶段 C 出口**：所有对象工具条由一个宿主渲染；旧 10 套实现删除或降级为 model；现有 239 测试 + 新增 UI 测试通过；未改变对象持久化格式。

## 2. 阶段 D：Text 与 Note 优先

### D1 Text

| WP | 内容 | 依赖 | 格式影响 | 验收 |
|---|---|---|---|---|
| D1.1 | 增加 fontFamily/fontStyle/fontWeight 字段与渲染 | C1/C5 | 页面格式 25（追加字段） | 改字体→撤销→重开保持 |
| D1.2 | 字号范围扩展到 16–64 + 任意值/步进 | D1.1 | 复用 fontSize | 16/20/24/32/40/48/64 |
| D1.3 | 文本范围选择模型（start/end）+ 选区工具栏（粗/斜/下划线） | C1/C5 | 页面格式 25 或独立 span 结构 | 选区改格式；撤销/重开 |
| D1.4 | 自动换行/最大宽度/尺寸策略 | D1.1 | 可能需要字段 | 窄/宽文本不裁切错误 |
| D1.5 | Copy as Image/Markdown（官方 SRC 行为） | C5 | 无 | 选区导出，Mindmap/Frame 边界按 copy-as-image.ts |

### D2 Note

| WP | 内容 | 依赖 | 格式影响 | 验收 |
|---|---|---|---|---|
| D2.1 | 完整背景色板（官方 palette） | C5 | 复用 noteBackground | 色板与工具条同步 |
| D2.2 | 插入文档保真（图片/待办/格式） | C5 | 复用 block 模型 | 插入后可编辑、无丢失 |
| D2.3 | Note 块容器评估（子块/显示模式） | C1 | 可能格式 25 | 先补官方证据再实施 |
| D2.4 | 行级待办勾选接入 UI | C5 | 无 | 点击行勾选/撤销 |

**阶段 D 出口**：Text/Note 工具栏由统一宿主渲染；所有新增属性双向同步；格式 25 迁移/备份/回滚通过；官方证据缺失项显式 UNVERIFIED。

## 3. 阶段 E：逐对象修复

| 顺序 | 对象 | 工作包 | 依赖 | 风险 |
|---|---|---|---|---|
| E1 | Shape | 形状内文字（D1.3 选区）、字体、旋转、Scribbled、按类型默认值、圆角 UI | D1 | 高（渲染/格式） |
| E2 | Image | 锁定、替换、边框/圆角/说明（先补官方证据）、剪贴板 | C6 | 中 |
| E3 | Connector | rough/dash、标签字体、完整色板、图层菜单、官方端点全集 | D1.1 | 中 |
| E4 | Brush | 压感/倾斜、平滑/预测、局部擦除、笔迹锁定/分组/缩放 | C6；真机 | 中高 |
| E5 | Frame | 嵌套、删除成员联动、锁定、官方属性 | C1 | 中高 |
| E6 | Mindmap | 官方 LEFT/BALANCE/RIGHT + Style 1–4、拖拽改接、整图选择 | C1/C3 | 高 |
| E7 | Link/Embed | 官方 Link 语义、Embed/Linked Doc/Synced Doc/Reference/iframe | 官方证据 | 高（全新对象） |
| E8 | 多选 | 混合属性面板、批量修改、锁定/类型限制 | C5 | 中 |

## 4. 阶段 F：视觉与功能回归

| WP | 内容 | 验收 |
|---|---|---|
| F1 | 对象级回归：创建/选中/工具条/属性/撤销/重做/移动/缩放/复制/粘贴/保存重载/缩放后操作 | 每对象 12 项测试 |
| F2 | 共享工具栏/浮窗回归：多对象切换、菜单互斥、边缘/缩放/Insets | bounds 断言 |
| F3 | Android 专项：触摸/笔/IME/旋转/分屏/TalkBack | 真机矩阵或明确 UNVERIFIED |
| F4 | 视觉 golden：官方截图到位后逐状态对比；当前截图作为内部基线 | 布局断言 + 像素差阈值 |
| F5 | 性能/可靠性：大页面撤销、快照成本、格式迁移 | 基线报告 |

## 5. 最小风险修复顺序（推荐）

1. **C1–C3**：对象引用/能力模型、工具栏宿主、定位解析器——不改格式，先消除重复实现。
2. **C4–C6**：菜单互斥/焦点、属性绑定、锁定/分组。
3. **D1.1–D1.3 + D2.1/D2.2**：Text 字体/选区、Note 色板/插入保真；格式 25 一次性迁移。
4. **E1 Shape**：形状文字/字体/旋转/默认值。
5. **E2–E3 Image/Connector**：锁定/替换、rough/dash/标签样式。
6. **E4–E5 Brush/Frame**：压感/平滑、嵌套/联动。
7. **E6–E7 Mindmap/Link/Embed**：需官方证据，最后做。
8. **F1–F5**：每阶段结束即跑对象/共享/浮窗回归，避免最后集中返工。

## 6. 建议的格式 25 迁移方案（若执行 D/E）

1. 先备份 pages/history（tar + SHA-256），记录页面数/对象数。
2. 编码版本 25，追加字段：Text 字体族/样式/字重/选区 span；Shape rotation；Note 子块/显示模式（如确认）；Embed 对象。
3. 解码 1–25 兼容；旧页面缺省值与当前渲染一致（字体 Sans/Regular、rotation 0、无 span）。
4. 迁移后跑：旧版 fixture、当前 239+ 测试、回滚到格式 24 release。
5. 文档：`docs/edgeless-audit/PAGE_FORMAT_25.md`（迁移、回滚、备份证明）。

## 7. 风险与开放问题

- **官方证据缺失**：对象工具栏顺序/图标/尺寸、Note 块操作、Embed 行为、Frame 嵌套语义、Image 属性等未验证；不得据此宣称高保真。
- **格式升级**：Text 选区/字体和 Shape 旋转需要格式 25；必须先获得用户确认与备份方案。
- **Android 真机**：压感/掌托/第三方 IME/分屏/旋转未验证。
- **性能**：整页快照撤销在新增文本 span 后可能进一步变大；需评估命令级历史。
- **视觉 token**：44dp/圆角/阴影等为 Android 适配建议，不是官方测量；阶段 F 需官方截图校准。

## 8. 停止点

阶段 A/B 已交付 9 份审计文档。**未修改任何业务代码。** 等待用户确认：
1. 是否按 C→D→E→F 顺序执行；
2. 是否接受页面格式 25（Text/Shape 所需）；
3. 官方截图/产品访问如何补齐；
4. 首批范围是否限定 Text+Note。
