# 无界画布底栏参数对齐（2026-10-30）

参考基线：

- AFFiNE commit `8cef09e1dd62b47a3cbc9f352aa10b3a90c75d34`
- BlockSuite commit `5cb5cb68471ca692f3c162258f0087cb22fcb82d`
- 官方源码路径：`blocksuite/affine/widgets/edgeless-toolbar/`、`blocksuite/affine/blocks/root/src/edgeless/configs/toolbar/`、`blocksuite/affine/gfx/*/src/toolbar/`
- 图标基线：`@blocksuite/icons` 2.2.17

## 当前底栏结构

1. 手形/选择切换。
2. 框选、连接、链接三个工具按钮。
3. 内容组：文本、画笔、橡皮、形状、连接关系、内容与数据。
4. 更多、撤销和重做。

## 参数与交互核对

| 控件 | 当前实现 | 状态 | 说明 |
|---|---|---|---|
| 手形/选择 | 单击在 `移动` 与 `选择` 间切换，图标同步变化 | VERIFIED | 已实测首次点击从选择切到手形 |
| 框选 | 选中后显示自定义、1:1、4:3、16:9、2:1 比例条（与官方 Frame 菜单一致），拖拽创建带标题的 Frame；选中 Frame 后显示重命名、背景色、定位、导航、复制和删除 | PARTIAL | 比例条已与文本/画笔等参数条统一风格；属性、导航与基础层级已实测；成员按“完全包含”几何判定而非持久化父子关系，嵌套 Frame、删除时联动、演示模式和文档大纲入口仍缺失 |
| 连接 | 选中后显示曲线/折线/直线、四档线宽和十种颜色；终点样式在选中连线的工具条中设置（圆点/箭头/三角/菱形/无） | VERIFIED | 参数写入 `WorkspaceSettings` 并应用于新连线；绘制参数条不含终点选项，避免与选中连线工具条重复；已展开菜单实测 |
| 链接 | 按钮直接打开“链接已有文档”选择器（官方 Link 同为直接动作）；创建链接卡片 | PARTIAL | 链接按页面 UUID 稳定保存，标题实时跟随重命名，目标被删除或回收时显示失效状态；双击或工具条打开目标，返回键回到来源页面；尚无反向链接、大纲目录和行内链接 |
| 文本 | 官方插画按钮展开文本框、H1–H6、代码、引用、项目符号、编号、待办和图片菜单；当前白板文本条直接插入画布文本对象，标题按级别设置字号，代码/引用/列表/待办带对应前缀；图片入口已移除 | PARTIAL | 已接入当前画布文本模型；H1–H6、代码、引用、列表和待办都会生成可见文本对象，不再只写入文档块；“关闭”按钮已移除，再次点文本工具或选择其他工具收起。已有文档链接与附件明确禁用。模拟器证据：`evidence/toolbar-polish-text-*.png` |
| 画笔 | 官方插画按钮；参数条提供画笔/高亮两种类型、十色、画笔 2/4/6/8/10/12 与高亮 10/14/18/22/26/30 六档粗细、仅手写笔；⇧P 选高亮 | PARTIAL | 类型、色板和粗细与官方 pen-menu 一致；高亮以 30% 透明度写入笔迹并随页面保存；官方压感、笔刷平滑/预测与透明色未实现 |
| 橡皮 | 官方插画按钮；参数条提供整笔/区域模式与仅手写笔 | PARTIAL | 区域模式与官方一致：拖过即整体删除被覆盖的图形、图片、笔迹和连线，锁定对象除外；官方在拖动时把目标降为 0.3 透明，本实现用灰色擦除轨迹预览；整笔模式是平板的额外选项，只删笔迹 |
| 形状 | 选中后显示矩形、圆形、菱形、三角形、圆角矩形和十种填充色 | VERIFIED | 新建形状使用选定类型与默认色；拖拽创建已接入 |
| 思维导图 | 官方插画按钮提供气泡图、向右树、向下树、组织图、大纲（“文字”已去重移除） | PARTIAL | 五种布局生成 mindNode 节点树并自动排布；节点工具条支持添加子/同级、双击编辑、切换布局、改接上级、删除子树（含确认与撤销）；官方为左/放射/右三种布局、四套主题和拖拽改接，尚未实现 |
| 内容与数据 | 菜单提供文本框、图片、数据库、卡片预设、工作区模板和旧页面模板 | VERIFIED | 当前支持的页面级数据能力均进入同一内容菜单 |
| 更多 | 收纳选择操作（多选、网格吸附）与视图（全屏、复位、Frame 导航） | PARTIAL | 已删除与形状/连接/文本/内容菜单重复的基础图形与内容分区；官方 More Tools 是参数溢出菜单，与本实现含义不同；Embed、AI 等未实现 |
| 撤销/重做 | 直接调用当前页面历史 | VERIFIED | 原有页面历史测试覆盖 |

## 素材来源

- Hand、Frame、Connector、Link、Shape、Text、Pen、Eraser、More、Undo、Redo 的线型路径来自 `@blocksuite/icons`。
- 彩色内容按钮已改为按 AFFiNE 插画语言重绘的白色纸片矢量图标（`res/drawable/canvas_tool_*_v2.xml`，方向 A）；旧 PNG 素材保留在 `drawable-nodpi/` 但不再引用。方向稿与预览见 `icon-directions-2026-10-31.html`。
- 原有官方工具栏实现通过 Web Components 渲染，当前 Android 项目为 Compose，并非直接复用同一 DOM/CSS 组件。

## 边界

- 当前数据模型没有 AFFiNE 的完整 Frame 属性、节点级 Mindmap 和 Linked Doc 关联字段；已实现基础替代行为，未实现项保持禁用或记录为 PARTIAL。
- 已恢复的是当前模型支持的工具布局、可见交互、参数菜单和默认值持久化。
- 画布容器已裁剪到编辑区，画布卡片不会覆盖顶部栏。

## 2026-10-30 H1–H6 复检与修复

复检新增的 H1–H6 二级菜单时发现并修复两个缺陷（修复前证据 `evidence/heading-h3-menu-before.png`、`evidence/heading-slash-crash-before.log`）：

1. 在标题块中通过斜杠菜单转成普通块类型时进程崩溃（`IllegalArgumentException: 不支持的文档块类型`，`Page.changeBlockType` 只接受单一 `heading` 类型）。现在 `heading1`–`heading6` 与其他文字块一样可以转换，也提供更改块类型和文字样式菜单（`evidence/heading-h3-menu-after-fix.png`、`evidence/heading-h3-change-type-menu.png`、`evidence/heading-slash-convert-after-fix.png`）。
2. Markdown 导出与导入只识别单一 `heading`/`##`，H1–H6 级别会丢失。现在按级别导出 `##`–`######`（受 Markdown 六级限制，H6 与 H5 共用最深一级），导入时恢复对应级别；旧 `heading` 类型与 `##` 的既有行为保持不变。

强停重启后各级标题、转换结果和文字均保持（`evidence/heading-reopen-after-fix.png`）；单元测试 `HeadingBlocksTest` 覆盖级别识别、保存/重开、转换、格式与 Markdown 往返。

## 2026-10-31 Frame 属性、导航与层级

按官方 Frame 的产品行为实现当前数据模型可承载的部分（`FrameGeometry.kt`；官方源码副本缺少 gfx/frame 模块，行为以产品实测和基线记录为准）：

- **属性**：Frame 选中后显示重命名、六种浅色背景、定位、Frame 导航、复制和删除；名称与背景色随页面保存（`evidence/frame-selected-toolbar.png`、`evidence/frame-rename-dialog.png`、`evidence/frame-background-menu.png`）。
- **导航**：Frame 导航对话框列出当前页所有 Frame 及完全包含的对象数，点击“定位”把视口按比例缩放并居中到该 Frame；“更多 → 视图”也提供同一入口（`evidence/frame-navigator-dialog.png`、`evidence/frame-navigator-locate.png`、`evidence/frame-more-menu-entry.png`）。
- **层级**：成员判定为“边界完全位于 Frame 内”的图形、图片、笔迹和两端均未绑定的连线；拖动 Frame 时成员同步移动，外部对象不动；Frame 内部空白点击穿透到成员，只有边框带和标题条命中 Frame；Frame 始终绘制在普通对象之下；复制 Frame 时成员一并复制并保持相对位置（`evidence/frame-moved-member-follows.png`、`evidence/frame-body-click-through.png`、`evidence/frame-copy-with-members.png`）。
- **持久化**：强停重开后 Frame 名称、背景色、成员位置和导航列表均保持（`evidence/frame-reopen-after-fix.png`）。

边界：成员是几何结果而不是持久化的父子关系，嵌套 Frame、删除 Frame 时联动删除成员、官方演示模式（Presentation）、文档大纲/侧栏中的 Frame 目录以及 Frame 内对象被拖出后立即失效等语义尚未实现，保持 PARTIAL。

## 2026-10-31 链接已有文档

- 画布“链接 → 链接已有文档”打开页面选择器，创建 `pageLink` 形状；目标页面的 UUID 保存在形状 `body`，标题在渲染时从当前页面列表实时解析（`PageLinks.kt`）。
- 目标重命名后卡片标题跟随；目标被移入回收站或无法读取时显示“目标页面不存在”；目标恢复后自动恢复显示。
- 双击链接卡片或选中后点击工具条的链接按钮打开目标：有文字内容的页面按文档打开，只有画布对象的页面按白板打开；返回键回到来源页面及其原视图（`openLinkedPage`/`returnToOriginPage`）。
- 链接形状随现有页面格式 22 保存，不需要迁移；旧版本会把未知形状按普通矩形渲染，不会崩溃。
- 未实现：反向链接、大纲/侧栏目录、文档内行内链接、链接引用计数与目标搜索。

## 2026-10-31 底栏统一与去重

核对来源、当前清单、重复项处理与保留差异详见 `TOOLBAR_INVENTORY.md`（BlockSuite commit `5cb5cb6` 底栏 senior/quick tool 源码、AFFiNE 发布 API `v0.27.5-beta.2`、官方 frontend toolbar 配置）。

- 主按钮：线型与插画按钮统一 44×44dp 触控区、12dp 圆角、PrimarySoft 选中背景；线型图标 22dp、插画 32dp（`evidence/toolbar-unified-main.png`）。
- 参数条：新增 `CanvasParamSurface` / `CanvasParamWidthDot`，Frame、连接、形状、画笔、文本与多选工具条统一表面、chip、颜色点和粗细控件（`evidence/toolbar-frame-bar.png`、`toolbar-connector-bar.png`、`toolbar-shape-bar.png`、`toolbar-pen-bar.png`、`toolbar-text-bar.png`、`toolbar-multiselect.png`）。
- 去重：More 仅保留“选择操作/视图”；思维导图只保留五种结构；链接按钮直接打开选择器；内容菜单不再重复“文本框”；文本条“图片”移除、`T` 改为“正文”（`evidence/toolbar-more-menu-dedupe.png`、`toolbar-mindmap-menu.png`、`toolbar-content-menu.png`、`toolbar-link-direct.png`）。
- 保留差异：官方将参数收在工具按钮滑出菜单，本实现使用独立浮动条以适配平板点按与窄窗口；官方 Note 工具、Template 面板、Slide 预设直接创建未实现，保持 `PARTIAL`。

## 2026-10-31 底栏状态互斥与思维导图菜单

复检底栏按钮与浮层后确认并修复以下交互问题：

- 形状工具处于激活状态时点击“思维导图”，形状按钮与思维导图按钮会同时显示选中底色，形状参数条仍停留在菜单下方；内容与更多菜单也会与工具选中态叠加。
- 现在工具按钮与弹层互斥：打开文本、思维导图、内容、更多时先收起其他弹层，画布底层切换到选择工具，隐藏框选、连接、画笔/橡皮、形状和多选参数条，只保留被打开菜单按钮的选中底色。
- 菜单关闭后保持该菜单按钮为当前选项：连点同一个菜单按钮时第一次打开、第二次关闭，第二下不会再激活前一个形状/画笔工具，也不会跳到选择箭头；思维导图、内容、更多按钮都会保持高亮，直到选择其他工具或菜单选项。
- 选择思维导图布局后同样保持思维导图按钮为当前项，画布处于选择工具，可立即点选新节点。
- 选择框选、连接、画笔、橡皮、形状、手形/选择会清除菜单当前项并关闭其他弹层；文本按钮会清除思维导图/内容/更多的当前项；T/M 快捷键同样进入互斥状态。
- 思维导图菜单五项改用各自的布局预览图标（气泡/向右/向下/组织/大纲），不再共用同一条连线图标；实测“向右树”生成向右堆叠的分支，插入后撤销可恢复画布。
- 核对同时修正文档中的两处参数描述：绘制参数条不含连接终点，终点在选中连线后编辑；画笔参数为十色、六档粗细。

### 底栏功能实测（2026-10-31）

| 按钮/菜单 | 实测动作 | 结果 |
|---|---|---|
| 手形/选择 | 点击切换拖拽与选择 | 通过 |
| 框选 | 选 4:3 后在空白处拖拽创建 Frame | 通过，Frame 按比例创建，撤销恢复 |
| 连接 | 选直线、6px、红色后拖拽 | 通过，生成对应连线，撤销恢复 |
| 链接 | 打开“链接已有文档” | 通过，列出 191 篇文档，可关闭 |
| 文本 | 插入“正文” | 通过，画布新增文本对象，撤销移除 |
| 画笔 | 手写笔/手指绘制高亮笔迹 | 通过 |
| 橡皮 | 划过刚绘制的笔迹 | 通过，整笔删除 |
| 形状 | 选圆形、蓝色后拖拽 | 通过，生成蓝色椭圆，撤销移除 |
| 思维导图 | 气泡图/向右树/大纲 | 通过，结构正确，撤销恢复 |
| 内容与数据 | 插入笔记卡片 | 通过，画布新增笔记，撤销移除 |
| 更多 | 多选、网格吸附开关 | 通过，菜单显示“多选：开 ✓”“网格吸附：开 ✓” |
| 更多 | 全屏画布、Frame 导航、演示 Frame | 通过，可进入/退出，Frame 导航列出 2 个 Frame |
| 撤销/重做 | 绘制图形后点击撤销、重做 | 通过，图形移除后恢复 |

本轮未逐项重测：H1–H6/代码/引用/列表/待办插入、图片选择、数据库在文档中的呈现、其他卡片预设与模板、画笔普通模式与粗细、区域橡皮、其余四种形状、向下树/组织图、链接创建与跳转、思维导图节点编辑。这些路径沿用此前各轮模拟器验证与 239 项单元测试；本轮没有把它们标记为新验证。

证据：`evidence/toolbar-mindmap-overlap-before.png`、`toolbar-mindmap-exclusive-release.png`、`toolbar-shape-only.png`、`toolbar-text-exclusive.png`、`toolbar-content-exclusive.png`、`toolbar-mindmap-right-tree.png`、`toolbar-mindmap-restored.png`、`toolbar-final-state.png`、`toolbar-doubletap-persist-open.png`、`toolbar-doubletap-persist-closed.png`、`toolbar-doubletap-content.png`，以及 `toolbar-test-*.png` 功能实测截图。

## 2026-10-31 键盘快捷键与提示

核对来源：BlockSuite `5cb5cb6` 各工具按钮的 `data-shortcut`（Hand H、Select V、Frame F、Connector C、Link @、Pen P、Eraser E、Shape S、Note N）与 `tooltip-content-with-shortcut` 组件。

- 工具键与命令键映射见 `CanvasShortcuts.kt`；画布通过 `onPreviewKeyEvent` 处理，文本/卡片编辑或菜单打开时不拦截。
- 底部栏按键：V/H、F、C、@、T、P、E、S、M、Ctrl+Z、Ctrl+Shift+Z；参数条与上下文工具条以悬停/长按提示为主。
- 提示样式：深色圆角浮层 + 白色名称 + 等宽键帽；悬停 320ms 或长按显示，触摸长按不会触发按钮点击。
- 验证：模拟器已实测工具键、撤销/重做、全选、Delete、Esc 与长按提示；悬停事件无法注入，鼠标/手写笔悬停路径标记为未设备验证（PARTIAL）。
- 证据：`evidence/shortcut-*.png`、`evidence/tooltip-long-press.png`。

## 2026-10-31 窄窗口与无界边界

- `CanvasLayout.parameterBarMaxWidth` 按视口宽度限制浮动参数条的最大宽度（两侧各留 8dp，最小 240dp）；Frame、连线、形状、画笔/橡皮、多选五类参数条统一使用，内容超出时在条内横向滑动。
- 文本参数条沿用“参数区滚动 + 关闭固定”；画布对象工具条继续通过 `CanvasLayout.toolbarX` 在右边缘裁剪。
- 640dp（1280×1600）实测：画笔条可滑到“仅手写笔”（`evidence/narrow-param-pen-start.png`、`narrow-param-pen-scrolled.png`），连线条（`narrow-param-connector.png`）、形状条（`narrow-param-shape.png`）、文本条（`narrow-param-text.png`）均在视口内。
- 200% 字体（2560×1600）实测：画笔条与文本条完整（`evidence/font200-param-pen.png`、`font200-param-text.png`）。
- 窄窗口非参数区问题已修复：首页头部在 640dp 侧栏展开时改为两行（`evidence/narrow-home-header-two-row.png`），文档块类型条改用 `FlowRow` 自动换行（`evidence/narrow-document-block-toolbar-wrapped.png`）；宽屏保持单行，见 `wide-home-header-single-row.png`、`wide-document-block-toolbar-single-row.png`。

## 2026-10-31 设置弹窗与底栏留白

- 文本参数条：移除 `fillMaxWidth`，参数区改为 `weight(1f, fill = false)`；内容不足时“关闭”紧贴参数，内容超出时参数区滚动、关闭固定（`evidence/toolbar-content-width-wide.png`、`toolbar-content-width-narrow.png`）。
- 底部工具栏：Surface 按内容自适应宽度，Row 始终可横向滚动；移除内容尾部空白与 More 与撤销之间多余的 4dp 间隔，保留统一 2dp 间距。
- 同类留白检查：Frame/连线/形状/画笔/多选参数条、对象工具条、文档块条与演示浮条均为内容自适应；仅编辑顶栏和页面顶栏按设计占满宽度。
- 设置入口改为模态弹窗，不改变底栏参数条布局；截图见 `evidence/settings-modal-wide.png`、`settings-modal-narrow.png`。

## 2026-11-01 底栏视觉与交互修正（用户六项）

1. **文本工具条**：H1–H6、代码、引用、项目符号、编号、待办改为直接创建画布文本对象（标题按级别设置字号，其余带对应前缀），不再只写入在画布上不可见的文档块；“关闭”按钮移除，再次点文本工具或选择其他工具即可收起。证据：`toolbar-polish-text-h1.png`、`toolbar-polish-text-code.png`、`toolbar-polish-text-quote.png`、`toolbar-polish-text-bullets.png`、`toolbar-polish-text-numbered.png`、`toolbar-polish-text-todo.png`、`toolbar-polish-text-bar.png`。
2. **菜单间距**：思维导图、内容与数据、更多三个下拉菜单使用 `Modifier.offset(y = 24.dp)` 下移，菜单底部与工具栏的可见间距从约 28dp 统一到约 16dp，菜单不遮挡工具栏。证据：`toolbar-polish-more-gap.png`。
3. **缩放控件**：右下缩放条底部内边距由 104dp 改为 36dp，与底部工具栏水平对齐。证据：`toolbar-polish-base.png`。
4. **选中态与弹跳**：工具栏按钮、参数 chip 和粗细点的选中底色由浅蓝改为灰色 `LingyeColors.Subtle`；`ToolIconButton` 与 `IllustratedToolButton` 增加 0.94→1.14→1 的 spring 弹跳缩放，仅主工具栏三个竖点左侧的按钮传入 `bounce = true`，更多、撤销、重做和参数条不启用。
5. **文档/白板切换**：`CompactIconAction` 新增 `selectedBackground`，页头两个模式按钮的选中底色改为白色。
6. **Frame 比例浮层**：仅在宽视口（参数条可用宽度 > 420dp）时向左平移 110dp，使浮层左缘靠近框选按钮；垂直间距保持不变。

验证：debug 与 R8 release 均实测文本条各项、菜单间距、缩放对齐、页头切换、Frame 浮层位置；239 项单元测试通过。弹跳动画已接入并连续点击验证无裁切，未做逐帧截图。