# 底部工具栏核对与统一（2026-10-31）

参考基线：AFFiNE 产品 0.27.5；BlockSuite commit `5cb5cb68471ca692f3c162258f0087cb22fcb82d`；AFFiNE 官网发布页（GitHub Releases API）。

## 1. 核对来源

| 来源 | 获取方式 | 用途 |
|---|---|---|
| BlockSuite 官方源码 | 本地 shallow 克隆 `deepseek\.tmp\blocksuite-reference`（commit `5cb5cb6`）+ `raw.githubusercontent.com/toeverything/blocksuite/<commit>/…` 拉取文件 | 确认底栏 senior tool / quick tool 注册方式、Frame 与 Connector 菜单、Pen/Eraser 同槽、Link 直接动作 |
| AFFiNE 官方源码副本 | `deepseek\.tmp\affine-src` frontend 包内 `editor-config/toolbar/index.ts` | 确认元素工具条 More 菜单的复制/链接分组与“Open doc”结构 |
| AFFiNE 发布页 | `https://api.github.com/repos/toeverything/AFFiNE/releases` | 最新稳定版 `v0.27.5-beta.2`（2026-10-26），发布说明中没有工具栏专项改动条目；最新 canary `v2026.10.27-canary.909` |
| 产品行为与截图 | 审计基线文档、用户提供的 AFFiNE 工具栏截图 | 视觉分组与图标语义参考 |

已读取的官方文件（BlockSuite `5cb5cb6`）：

- `packages/affine/widgets/edgeless-toolbar/src/edgeless-toolbar.ts`：底栏容器；senior tools 主按钮、quick tools 参数区、溢出“More Tools”、密集模式滚动导航、撤销/重做由编辑器其他部分提供。
- `.../src/extension/index.ts`：`SeniorToolExtension` / `QuickToolExtension` 注册接口；quick tool 带 `menu`、`priority`。
- `packages/affine/gfx/brush/src/toolbar/senior-tool.ts`：`Pen` 与 `Eraser` 在同一个 senior 槽位（`pen-and-eraser`）。
- `packages/affine/gfx/shape/src/toolbar/senior-tool.ts`、`.../mindmap/.../senior-tool.ts`、`.../note/.../senior-tool.ts`、`.../template/.../senior-tool.ts`：Shape / Mind Map / Note / Template 主按钮。
- `packages/affine/blocks/frame/src/edgeless-toolbar/*`：Frame 按钮带 slide 菜单（Custom + Slide 1:1 / 4:3 / 16:9 / 2:1）；quick tool 优先级 90。
- `packages/affine/gfx/connector/src/toolbar/*`：Connector 按钮图标随当前模式（直线/折线/曲线）变化，点击展开连接线菜单；quick tool 优先级 80。
- `packages/affine/gfx/link/src/toolbar/link-tool-button.ts`：Link 是直接动作按钮，打开快捷搜索（已有文档/书签/嵌入），没有参数子集。
- `packages/affine/blocks/edgeless-text/src/edgeless-toolbar/config.ts`：文本工具是元素，底部参数条只在选中文本时出现。

## 2. 官方底栏结构（核对结论）

1. 主按钮（senior）：手形/选择（导航）· Frame · Connector · Link · Text · Pen+Eraser · Shape · Mind Map · Note · Template · More（仅参数溢出）· 撤销/重做。
2. 参数子集（quick tools）只有 Frame（优先级 90）与 Connector（优先级 80）；其余工具的参数从按钮自身的菜单/面板打开。
3. Frame 菜单：`Custom` + `Slide 1:1 / 4:3 / 16:9 / 2:1`，选择预设会在视口中心直接创建 Frame。
4. Connector 菜单：模式（直线/折线/曲线）、线宽、颜色、端点；图标反映当前模式。
5. 没有独立的“文字参数条”或“画笔参数条”——参数跟随工具按钮或选中元素出现。
6. “More Tools”是 quick tools 的溢出菜单，不是内容插入菜单。

## 3. 当前实现盘点（统一后）

| 主按钮 | 行为 | 参数子集 |
|---|---|---|
| 手形/选择 | 单击在拖拽与选择间切换 | — |
| Frame（框选） | 启用 Frame 工具 | 浮动条：自定义、1:1、4:3、16:9、2:1（与官方选项一致，保留浮动条以适配平板点按） |
| Connector（连接） | 启用连线工具 | 浮动条：直线/折线/曲线、四档线宽、十色；终点与标签在选中连线的工具条中编辑 |
| Link（链接） | 直接打开“链接已有文档”选择器（官方同为直接动作） | — |
| Text（文本） | 打开文本参数条 | 浮动条：链接/附件（未实现，置灰）、正文、H1–H6、代码、引用、项目符号、编号、待办；各项直接创建画布文本对象，已移除“关闭” |
| Pen（画笔） | 画笔 | 浮动条：画笔/高亮、十色、六档粗细（画笔 2–12，高亮 10–30）、仅手写笔 |
| Eraser（橡皮） | 橡皮 | 浮动条：整笔/区域、仅手写笔 |
| Shape（形状） | 矩形 | 浮动条：五种形状、十种填充色 |
| Mind Map（思维导图） | 菜单（每项带布局预览图标）：气泡图/向右树/向下树/组织图/大纲 | — |
| Content & data（内容与数据） | 菜单：图片、数据库、五种卡片预设、工作区/旧页面模板、管理模板 | — |
| More（更多） | 菜单：选择操作（多选、网格吸附）、视图（全屏、复位视图、Frame 导航、演示 Frame） | — |
| 撤销 / 重做 | 页面历史 | — |

## 4. 已清理的重复功能

| 位置 | 原重复行为 | 处理 |
|---|---|---|
| More → 基础图形 | 矩形/圆角矩形/圆形/连线，与形状工具、连接工具重复 | 删除该分区 |
| More → 内容 | 文本框/图片/卡片，与文本工具、内容与数据菜单重复 | 删除该分区，More 只保留选择操作与视图 |
| 思维导图菜单 | 第一项“文字”实际创建文本框，与文本工具重复 | 删除，只保留五种结构 |
| 链接菜单 | “添加页面卡片”打开的是卡片预设菜单，与内容与数据菜单重复 | 删除；链接按钮改为直接打开“链接已有文档”选择器 |
| 内容与数据菜单 | “文本框”与文本参数条的“正文”重复 | 删除；文本工具为文本对象唯一入口 |
| 文本参数条 | “图片”插入文档图片块，与内容与数据菜单的画布图片语义不同但同名易混 | 删除；画布图片统一由内容与数据菜单插入 |
| 文本参数条 | “T”图标 + 标签“T”视觉重复 | 标签改为“正文/Body”，与 H 级别并列 |

## 5. 风格统一

新增共享组件（`MainActivity.kt`）：

- `CanvasParamSurface`：所有参数条统一为圆角 18dp、白色 98%、1dp 边框、12dp 阴影、内边距 7/5。
- `CanvasParameterChip`：文字与比例选项统一 34dp 高、9dp 圆角；选中 PrimarySoft + 主色 + SemiBold。
- `CanvasParamWidthDot`：连接线与画笔粗细统一 32dp 触控槽位、选中 PrimarySoft、点径 `5 + 2×宽度`。
- `CanvasParameterColor`：形状、连接线、画笔颜色统一 32dp 槽位 + 18dp 圆点，选中 2dp 主色描边。
- 主工具栏按钮：线型按钮与插画按钮统一 44×44dp 触控区、12dp 圆角、选中 PrimarySoft 背景（插画按钮原为下划线选中态，已改为同一语义）；线型图标 22dp，插画 32dp。

对参数条的改动：Frame 比例条改为 `CanvasParameterChip`（原为自定义灰色选中块）；Connector、Shape、Pen、多选工具条改用 `CanvasParamSurface`（原圆角 12/14、内边距 8/6 或 14/10 不等）；Pen 颜色圆点改用共享组件（原 30dp 圆形 3dp 描边）；“仅手写笔”改用 `CanvasParameterChip`。

## 6. 保留差异与边界（PARTIAL）

- 官方把参数放在工具按钮的滑出菜单里；本实现使用独立浮动参数条，便于平板点按与窄窗口横向滑动。选项内容已与官方核对一致（Frame 四种比例 + Custom；Connector 三种路径与端点；画笔粗细/颜色）。
- 官方 Text 工具在底栏没有参数条；本实现的文本参数条包含 H1–H6 与文档块类型，属于此前按用户参考图对齐的结果，本次只做去重与风格统一。
- 未实现官方 Note（便签）工具、Template 面板、框选“Slide 预设直接在视口中心创建”、连接线端点菜单全部选项（三角/菱形已支持，官方菜单中的“无端点”等已覆盖），不伪装为已完成。
- More 菜单的“选择操作/视图”为当前产品能力，不与官方 More Tools（参数溢出）混同。

## 7. 证据

`evidence/toolbar-unified-main.png`、`toolbar-frame-bar.png`、`toolbar-connector-bar.png`、`toolbar-shape-bar.png`、`toolbar-pen-bar.png`、`toolbar-text-bar.png`、`toolbar-more-menu-dedupe.png`、`toolbar-mindmap-menu.png`、`toolbar-content-menu.png`、`toolbar-link-direct.png`、`toolbar-multiselect.png`。

## 8. 插画图标重设计（2026-10-31，方向 A）

问题：原六个插画按钮混用白纸片、纯色块、彩色贴纸和高光渐变，与线型图标不成体系，“思维导图”甚至复用了名为 `canvas_tool_link` 的彩色素材。

方向稿：`icon-directions-2026-10-31.html` 提供三个方向（A 纸片 + 单点强调、B 彩色贴纸、C 墨线 + 单点填色），均按工具栏 34dp 实际尺寸与放大稿预览；用户选定方向 A。

落地：

- 新资源 `res/drawable/canvas_tool_text_v2.xml`、`canvas_tool_pen_v2.xml`、`canvas_tool_eraser_v2.xml`、`canvas_tool_shape_v2.xml`、`canvas_tool_mindmap_v2.xml`、`canvas_tool_content_v2.xml`（VectorDrawable，64 视口，34dp）。
- 统一语言：白纸片 + `#D9DCE3` 浅灰描边 + 1.5 描边宽度；每枚只用一个强调色（黄 `#F5C64F` 主强调，蓝 `#6D8FF7`，珊瑚 `#F0A7A0`，薄荷 `#9BD4AE`）；纸片阴影用 10% 深色偏移形状模拟（VectorDrawable 不支持模糊滤镜）。
- 选中态完全交给按钮的 `PrimarySoft` 背景，插画本身不再画下划线或底色。
- 旧 PNG（`drawable-nodpi/canvas_tool_*.png`）保留但不再引用，便于回退；`canvas_tool_link.png` 与思维导图按钮的错名问题由新资源名解决。

证据：`evidence/toolbar-icons-paper.png`（工具栏实际尺寸放大）、`evidence/toolbar-icons-paper-active.png`（文本工具选中态）。README 与 TOOLBAR_PARITY 的素材来源说明已同步。

## 9. 键盘快捷键与提示（2026-10-31）

官方快捷键（BlockSuite `5cb5cb6` 工具提示）：

| 工具 | 键 | 来源 |
|---|---|---|
| 手形 | H | `gfx/pointer/quick-tool/default-tool-button.ts` |
| 选择 | V | 同上 |
| 框选 | F | `blocks/frame/edgeless-toolbar/frame-tool-button.ts` |
| 连接 | C | `gfx/connector/toolbar/connector-tool-button.ts` |
| 链接 | @ | `gfx/link/toolbar/link-tool-button.ts` |
| 画笔 | P | `gfx/brush/toolbar/components/pen/consts.ts` |
| 橡皮 | E | `gfx/brush/.../eraser-tool-button.ts` |
| 形状 | S | `gfx/shape/src/draggable/shape-tool-button.ts` |
| 便签 | N | `gfx/note/toolbar/note-senior-button.ts` |

本实现补充：T 文本、M 思维导图、L 链接（键盘无 @ 时），Ctrl+Z / Ctrl+Shift+Z / Ctrl+Y / Ctrl+A / Delete / Esc / 方向键 / Ctrl+=/-/0。便签工具尚未实现，因此 N 不绑定。

提示：`VisualDesign.kt` 的 `ShortcutTooltipHost` 在悬停 320ms 或长按后显示深色浮层与等宽键帽；按钮使用 `combinedClickable`，长按仅提示、不触发点击。官方提示组件为 `affine-tooltip-content-with-shortcut`，视觉规格与本实现一致。

验证限制：模拟器 `input motionevent` 不支持 HOVER 动作、注入悬停被权限策略阻止，鼠标/手写笔悬停路径未做设备验证；工具键、撤销/重做、全选、Delete、Esc 与长按提示均已实测（`evidence/shortcut-*.png`、`tooltip-long-press.png`）。

## 10. 画笔类型与参数（2026-10-31）

官方 `gfx/brush` 的 pen-menu 提供两种类型与两套粗细（BlockSuite `5cb5cb6`）：

| 类型 | 键 | 粗细 | 颜色 |
|---|---|---|---|
| 画笔 Pen | P | `BRUSH_LINE_WIDTHS` = 2/4/6/8/10/12（默认 4） | `StrokeColorShortPalettes` 短色板 |
| 高亮 Highlighter | ⇧P | `HIGHLIGHTER_LINE_WIDTHS` = 10/14/18/22/26/30（默认 22） | 同色板，绘制时 `adjustColorAlpha(color, 0.3)` |

本实现：`BrushSettings.kt` 保存两套粗细、十色短色板（含白色，透明色未提供）和 30% 透明度换算；高亮笔迹把透明度直接写入笔迹颜色的 alpha，沿用页面格式 22，无需迁移。参数条增加“画笔/高亮”chip，颜色与粗细按当前类型独立记忆；`P`/`⇧P` 快捷键切换，画笔按钮提示随类型显示 `P` 或 `⇧P`。

未实现：压感、笔刷平滑/预测、透明色、高亮笔独立的混合模式（当前为普通 alpha 叠加）。证据：`evidence/brush-types-bar.png`、`brush-highlighter-bar.png`、`brush-highlighter-stroke.png`、`brush-shift-p.png`、`brush-reopen.png`。

## 11. 窄窗口参数条适配（2026-10-31）

官方底栏在窄视口下通过按钮滑出菜单收纳参数；本实现的浮动参数条改为受视口约束并在条内滑动，保持平板点按路径不变。

- `CanvasLayout.parameterBarMaxWidth(viewportWidthDp, margin = 8f)`：返回 `viewportWidthDp - 2 × margin`，下限 240dp；单测 2 项。
- 接入 `CanvasParamSurface(Modifier.widthIn(max = paramBarMaxWidth))`：Frame、连线、形状、画笔/橡皮、多选；文本条维持 `< 1040dp` 上限与固定“关闭”。
- 条内 `horizontalScroll`：640dp 下画笔条可滑到“仅手写笔”，连线条可滑到“颜色 10”，形状条恰好完整，文本条只滚动参数区。
- 200% 系统字体（2560×1600）下画笔条与文本条完整，无裁切。

证据：`evidence/narrow-param-pen-start.png`、`narrow-param-pen-scrolled.png`、`narrow-param-connector.png`、`narrow-param-shape.png`、`narrow-param-text.png`、`font200-param-pen.png`、`font200-param-text.png`、`object-toolbar-clamped.png`。

窄窗口非参数条问题已修复（2026-10-31）：640dp 侧栏展开时首页头部改为两行（`narrow-home-header-two-row.png`），文档块类型条改用 `FlowRow` 自动换行（`narrow-document-block-toolbar-wrapped.png`）；宽屏保持单行（`wide-home-header-single-row.png`、`wide-document-block-toolbar-single-row.png`）。

## 12. 底栏与参数条留白统一（2026-10-31）

- 文本参数条去掉 `fillMaxWidth`，参数区使用 `weight(1f, fill = false)`：内容不足时“关闭”紧跟参数，超出时参数区滚动、关闭固定。
- 底部工具栏按内容自适应宽度并始终可横向滚动，删除尾部空白和 More 与撤销之间多余的 4dp 间隔。
- 同类检查覆盖 Frame、连线、形状、画笔/橡皮、多选参数条、对象工具条、文档块条与演示浮条；除编辑顶栏和页面顶栏外均按内容收窄。
- 证据：`evidence/toolbar-content-width-wide.png`、`toolbar-content-width-narrow.png`。

## 13. 底栏状态互斥与思维导图菜单预览（2026-10-31 复检）

复检以用户标注的形状/思维导图按钮与思维导图浮层为起点，逐项打开 Frame、连接、文本、画笔、橡皮、形状、思维导图、内容与更多菜单。

发现问题：

- 形状工具激活时打开思维导图菜单，形状与思维导图按钮同时高亮，形状参数条仍显示在菜单下方；内容、更多菜单也会叠加在工具选中态之上。
- 思维导图五项共用同一条连线图标，无法从图标区分结构。
- 文档曾把连接终点写成绘制参数条选项，实际终点只在选中连线的工具条中编辑。

处理：

- `CanvasEditor` 新增 `dismissToolMenus` / `chooseTool` / `selectMenuOption` 与 `toolbarMenuOpen`：打开文本、思维导图、内容、更多时先收起其他弹层，画布底层切换到选择工具，隐藏框选、连接、画笔/橡皮、形状、多选参数条，只保留被打开菜单按钮的选中底色。
- 菜单关闭后保持该按钮为当前选项：连点同一个菜单按钮时第一次打开、第二次关闭，第二下不再激活前一个形状/画笔工具，也不跳到选择箭头；思维导图、内容、更多按钮保持高亮，直到选择其他工具或菜单选项。
- 选择思维导图布局后同样保持思维导图按钮为当前项，画布处于选择工具，可立即点选节点。
- 选择工具会清除菜单当前项并关闭其他弹层；文本按钮会清除思维导图/内容/更多的当前项；T/M 快捷键与工具栏按钮使用同一逻辑。
- 思维导图菜单改用五种布局预览图标（`MindmapLayoutPreview`），并实测“向右树”生成向右堆叠分支、插入后可撤销恢复。
- 同步修正本文参数清单：画笔为十色/六档，橡皮含整笔/区域，More 含演示 Frame，连接终点不在绘制参数条。

功能实测（2026-10-31，debug + R8 release）：手形/选择切换、框选 4:3 创建、连接直线/6px/红色绘制、链接选择器、正文插入、高亮笔绘制、整笔橡皮、圆形蓝色填充、气泡/向右/大纲思维导图、笔记卡片插入、多选与网格吸附开关、全屏、Frame 导航、演示 Frame、撤销/重做均通过；测试内容已撤销，页面恢复为 10 个画布对象、16 个文字块，形状色与连线样式已还原。未逐项重测的路径（H1–H6/代码/引用/列表/待办、图片、数据库文档呈现、模板、普通画笔/区域橡皮、其余形状与布局、链接跳转、节点编辑）沿用此前轮次验证和 239 项单元测试，不标记为本轮新验证。

证据：`evidence/toolbar-mindmap-overlap-before.png`、`toolbar-mindmap-exclusive-release.png`、`toolbar-shape-only.png`、`toolbar-text-exclusive.png`、`toolbar-content-exclusive.png`、`toolbar-mindmap-right-tree.png`、`toolbar-mindmap-restored.png`、`toolbar-final-state.png`、`toolbar-doubletap-persist-open.png`、`toolbar-doubletap-persist-closed.png`、`toolbar-doubletap-content.png`，以及 `toolbar-test-*.png`。

## 14. 底栏视觉与交互修正（2026-11-01 用户六项）

- 文本条 H1–H6/代码/引用/项目符号/编号/待办改为创建画布文本对象（`Page.addCanvasTextBlock`），标题按级别设置字号，代码/引用/列表/待办带对应前缀；移除“关闭”，再次点文本工具或选择其他工具收起。
- 思维导图、内容、更多下拉菜单用 `Modifier.offset(y = 24.dp)` 下移，菜单底部与工具栏间距约 16dp，不遮挡工具栏。
- 缩放控件底部内边距 104dp → 36dp，与工具栏水平对齐。
- 主工具栏选中底色由 `PrimarySoft` 改为 `LingyeColors.Subtle` 灰色；参数 chip 与粗细点同步改为灰色。`ToolIconButton`/`IllustratedToolButton` 增加 0.94→1.14→1 的 spring 弹跳，仅三个竖点左侧的主按钮启用。
- 页头文档/白板切换选中底色改为白色（`CompactIconAction.selectedBackground`）。
- Frame 比例浮层在宽视口下向左平移 110dp，贴近框选按钮；垂直间距不变。

验证：debug 与 R8 release 实测文本条六个插入项、菜单间距、缩放对齐、页头切换与 Frame 浮层；239 项单元测试通过。证据：`evidence/toolbar-polish-base.png`、`toolbar-polish-text-bar.png`、`toolbar-polish-text-h1.png`、`toolbar-polish-text-code.png`、`toolbar-polish-text-quote.png`、`toolbar-polish-text-bullets.png`、`toolbar-polish-text-numbered.png`、`toolbar-polish-text-todo.png`、`toolbar-polish-more-gap.png`、`toolbar-polish-frame-popup.png`、`toolbar-polish-final.png`。