# 文本选区工具条与多级浮窗复刻清单（2026-11-01）

## 1. 参考与证据

- 实际产品：`https://app.affine.pro/`，Demo Workspace，页面模式（Page），浅色主题。
- 视口：1440×1000 CSS px；浏览器：Microsoft Edge（Playwright Core 驱动）。
- 操作：在 “Getting Started” 标题末尾输入测试文字，按 `Shift+Home` 选中，等待选区工具条出现；逐一打开文字样式、对齐、高亮、更多、链接浮条。
- 证据目录：`evidence/affine-inline-20261101/`
  - `live-04a-selection.png`：选区工具条整体。
  - `live-menu-text-style2.png` / `popup-text-style2.json`：文字样式菜单。
  - `geometry-align.json`、`geometry-highlight.json`、`geometry-more.json`：三个菜单的几何与行位置。
  - `live-highlight-open.png`：高亮菜单色板。
  - `link-popover.json`：链接输入浮条。
  - `toolbar-full.html`、`toolbar-children.json`、`icons.json`：工具条 DOM、按钮尺寸与 SVG 路径。
  - `highlight-css.json`：高亮前景/背景色变量。

## 2. 官方工具条实测

容器（`editor-toolbar`）：

| 项目 | 值 |
|---|---|
| 外框 | 594×38（含 1px 边框；内容高 36） |
| 背景 / 边框 / 圆角 | `#FFFFFF` / `1px #E6E6E6` / `8px` |
| 阴影 | `0 1px 6px rgba(0,0,0,.16), 0 8px 14px rgba(0,0,0,.08)` |
| 内边距 / 间距 | `0 6px` / `8px` |
| 位置 | 文字选区上方；水平方向以选区中心对齐；`data-placement="top"` |

组件顺序与尺寸（从左到右）：

| 顺序 | 组件 | 尺寸 | aria / 说明 |
|---|---|---:|---|
| 1 | Ask AI | 76×36 | 用户此前确认不添加 |
| 2 | 分隔 | 4×36 | 1px 视觉线 |
| 3 | 文字样式 | 40×36 | “T” + 16px 下箭头；打开块类型菜单 |
| 4 | 分隔 | 4×36 | |
| 5 | 对齐 | 40×36 | 左对齐图标 + 16px 下箭头；打开三行对齐菜单 |
| 6 | 分隔 | 4×36 | |
| 7 | 粗体 | 24×36 | Bold |
| 8 | 代码 | 24×36 | Code |
| 9 | 行内公式 | 24×36 | Inline Equation |
| 10 | 斜体 | 24×36 | Italic |
| 11 | 链接 | 24×36 | Link；打开输入浮条 |
| 12 | 删除线 | 24×36 | Strikethrough |
| 13 | 下划线 | 24×36 | Underline |
| 14 | 分隔 | 4×36 | |
| 15 | 高亮 | 40×36 | 高亮笔图标 + 16px 下箭头 |
| 16 | 分隔 | 4×36 | |
| 17 | 创建链接文档 | 24×36 | Create Linked Doc |
| 18 | 分隔 | 4×36 | |
| 19 | 更多 | 24×36 | “⋯” |

图标统一 20×20，SVG `viewBox="0 0 24 24"`，路径已保存到 `icons.json` 并转为 `InlineToolbarIcons.kt`。

用户提供的截图在“创建链接文档”和“更多”之间还有一个评论气泡；2026-11-01 的 LIVE 页面模式工具条没有该按钮。按截图保留评论入口，并在实现说明中标记该差异。

## 3. 多级浮窗实测

所有菜单都从触发按钮**向右下**展开（页面模式），容器与工具条同色、同边框、同阴影、同 8px 圆角，容器内边距 8px；菜单行高 30px、左右内边距 8px、图标 20px、图标与文字间距 8px、行圆角 4px。

| 浮窗 | 容器尺寸 | 位置 | 行数/结构 |
|---|---:|---|---|
| 文字样式 | 198×408 | 工具条下方；顶部间距 5px；水平中心对齐“文字样式”按钮 | 13 行：Text、Heading 1–6、Bulleted List、Numbered List、To-do List、Code Block、Equation、Quote |
| 对齐 | 198×108 | 同上，中心对齐对齐按钮 | 3 行：Align left / center / right |
| 高亮 | 198×602 | 同上，中心对齐高亮按钮 | 标题 Color + 9 行（Default Color、Red、Orange、Yellow、Green、Teal、Blue、Purple、Grey）；标题 Background + 9 行（Default Background + 同色） |
| 更多 | 198×176 | 同上，中心对齐更多按钮 | Copy、Copy as Markdown、Copy link to block、Duplicate、间距 8px、Delete |
| 链接 | 350×48 | 选区下方（实测 top=322，选区底=320）；水平以选区中心对齐 | 输入框 280×30、圆角 4、1px 蓝色边框、占位 “Paste or type a link”；右侧 28×30 确认按钮（勾） |

高亮色值（浅色主题）：

- 前景：Red `#C62222`、Orange `#D34F0B`、Yellow `#B67C04`、Green `#149343`、Teal `#0782A0`、Blue `#2159D3`、Purple `#842ED3`、Grey `#7A7A7A`；Default 使用正文色。
- 背景：Red `#FED5D5`、Orange `#FEDFBB`、Yellow `#FEF3A1`、Green `#E1FAB1`、Teal `#ADF8E9`、Blue `#CCE2FE`、Purple `#EDDDFF`、Grey `#EAECEF`；Default 透明。

## 4. 当前项目差异

- 现有选区工具条已复刻组件顺序（无 Ask AI），高度 36dp、按钮 24dp；但：
  - 文字样式按钮显示 “Aa”，官方为 “T” + 下箭头。
  - 图标多为自绘/文字，官方为 SVG；缺评论。
  - 高亮菜单、链接浮条、评论浮窗不存在。
  - 文字样式/对齐菜单位置用固定偏移，未按触发按钮居中；展开方向与间距未按 LIVE。
  - Code、Inline Equation、Link、Strikethrough、Highlight、Create Linked Doc 全部禁用。
  - 更多菜单含 “复制为图片”，官方没有；缺 “Copy link to block”。
- `TextSpan` 只支持 BOLD/ITALIC/UNDERLINE；格式 25 的 span 只存 start/end/flags。

## 5. 任务清单与状态（2026-11-01 实施后）

### P0 UI 复刻

- [x] 工具条几何：内容高 36、外框 38、圆角 8、边框 1px、水平内边距 6、间距 8。
- [x] 组件顺序：文字样式(T)、对齐、B、Code、公式、I、Link、S、U、高亮、创建链接文档、评论、更多。
- [x] 全部图标换成 `InlineToolbarIcons` 的官方 SVG 路径；文字样式/对齐/高亮使用 16px 下箭头。
- [x] 文字样式菜单：198×408、13 行、每行 30、图标 20、行距 0，工具条下方、按按钮中心对齐。
- [x] 对齐菜单：198×108、3 行，同上位置规则。
- [x] 高亮菜单：Color/Background 两组、18 行、色值按第 3 节；高度随内容。
- [x] 更多菜单：Copy / Copy as Markdown / Copy link to block / Duplicate / 8px 间距 / Delete。
- [x] 链接输入浮条：350×48、工具栏下方（gap 41dp）、280×30 输入框、右侧确认按钮。
- [x] 评论入口：按用户截图加入；本地评论浮窗（查看/添加/删除）已在 T23 实现，仅本地存储、无云端同步。
- [x] 工具条与浮窗使用 `LingyeColors` 主题 token（背景、边框、阴影、文字）。

### P1 功能

- [x] B / I / U：沿用现有 TextSpan flags。
- [x] 删除线：新增 `STRIKE` flag，编辑态与画布渲染。
- [x] 行内代码：新增 `CODE` flag，等宽字体与浅色底。
- [x] 高亮：前景色/背景色写入 flags 的 4 bit 颜色字段，编辑态与画布渲染。
- [x] 文字样式：Text / H1–H6 / Code Block 作用于整段；列表/公式/引用无块模型，保持禁用。
- [x] 对齐：左/中/右。
- [x] 链接：输入、确认、编辑态蓝色下划线、画布渲染；URL 存 `cardKind` 的 `link:` token；重启/覆盖安装后保持。
- [x] 行内公式：`TextInlineMath` 子集渲染（希腊字母/运算符/上下标/分数/根号），输入浮条+TeX 源存 `math:` token，渲染为衬线斜体。
- [x] 创建链接文档：弹出文档列表，把选区链接到所选页面的 `lingye://page/<id>`（不是官方的新建 linked doc 块）。
- [x] 评论：本地评论 `comment:` token、评论浮窗添加/删除、浅黄底标记；无云端同步。
- [x] 更多：Copy、Copy as Markdown、Copy link to block、Duplicate、Delete。

### P2 验证

- [x] 299 项 JVM 测试 0 失败（新增 TextSpan/TextLinks/TextComments/TextEquations/公式渲染测试）。
- [x] Release 安装到模拟器；在 ToolbarTest 测试页选中文字，截图对照 LIVE。
- [x] 打开文字样式、对齐、高亮、更多、链接输入浮条并截图；行高、方向、间距核对。
- [x] 页面格式仍 25；未改用户页面；测试页为新建的 `ToolbarTest`。
- [x] 链接点击：点击画布中的链接文字成功打开 Chrome（外链）；页面链接走 `lingye://page/<id>`。
- [x] 选中后点击 B/Code 等格式按钮后工具条保持（2026-11-02 精确触摸回归：B 加粗可见、删除线可见、U 生效，按钮显示激活态，工具条全程保持展开；证据 `fmt-1.png`、`fmt-4.png`、`fmt-5.png`、`fmt-6.png`、`fmt-7.png`）。
- [x] 公式/评论浮窗在模拟器中的稳定截图与逐项交互（2026-11-02：公式浮窗 `eq-0/1.png`、`x^2+1` 渲染 `eq-2.png`；评论浮窗 `cm-2.png`、添加 Review 后列表与浅黄底 `cm-3/5/6.png`、删除后 `cm-7.png`）。

## 6. 实施记录

- 源码：`TextToolbarUi.kt`、`InlineToolbarIcons.kt`、`TextFormatting.kt`、`TextLinks.kt`、`TextComments.kt`、`TextEquations.kt`、`TextInlineMath.kt`、`TextLinkHitTest.kt`、`MainActivity.kt`。
- `TextSpan` 新增 `STRIKE`、`CODE`、前景/背景各 4 bit 颜色、`LINK`、`MATH`、`COMMENT`；`ALL` 扩展后兼容已有 flags；未改页面格式。
- `cardKind` token：`link:`、`comment:`、`math:`（均为 start:end:base64url）；`TextExtras.tokens` 在缩放/垂直对齐/自动高度重写时保留全部扩展 token，文本编辑时三种 adjust 同步范围。
- 链接点击：`TextLinkHitTest` 按渲染的换行/对齐规则近似命中，非编辑态点击打开外链或 `lingye://page/<id>`。
- 菜单与工具条使用自建 `Popup`，以触发按钮 `anchorBounds` 居中；高度不足时钳制。打开菜单/浮窗前隐藏 IME，避免浮窗落在键盘后。
- 证据：`evidence/affine-inline-20261101/menu-13.png`（选区工具条）、`menu-16.png`（对齐）、`menu-18.png`（高亮）、`menu-20.png`（更多）、`menu-21.png`（链接浮条）、`menu-23.png`（链接生效）、`rich-11.png`（点击链接打开 Chrome）。

## 7. 限制与决策

- CSS px 直接映射 dp；触摸目标沿用 36dp 高。
- Ask AI 不添加（用户此前明确要求）。
- 评论按钮按用户截图添加；2026-11-01 LIVE 无该按钮；评论仅本地，不声明云端同步。
- 公式为 TeX→Unicode 子集，不是 KaTeX 全量排版；复杂公式会降级为可读文本。
- 列表/引用在自由文本框中没有块模型，保持禁用；文字样式菜单仍完整显示官方 13 项。
- 选中文字后点击格式按钮的焦点保持已完成精确触摸回归（B/删除线/U 与激活态、工具条保持）；公式/评论浮窗已取得稳定截图。仍未覆盖：真机、1440×1000 以上窗口的菜单钳制、系统剪贴板等行为。
- 源码恢复事故：PowerShell 5.1 以 ANSI 读取造成中文字符损坏；用 GBK 反转 + 发布 APK 的 DEX 字符串补齐；299 项测试通过，个别文案无法逐字核对，见 `UNVERIFIED_ITEMS.md`。

## 8. 2026-11-02 回归记录

- 设备/构建：模拟器 2560×1600、density 320；安装包 2,578,113 字节，SHA-256 `EF360A2631F4DD438C4CDD9D95F682BB79B91CC3AB4C65A7C1B0A9038BA94C8F`，与发布文件 `releases/lingye-v0.15-selection-frame-padding-release-20261102.apk` 逐字节一致。
- 选区回归：在 ToolbarTest 页对普通文本全选后逐项点击工具条；工具条在每次点击后保持展开，B 灰底激活且文字可见加粗；删除线灰底激活且文字出现贯穿线；U 灰底激活（该文本本身是链接，已有下划线）。
- 浮动回归：点击 TeX 按钮打开 350×48 输入浮条，输入 `x^2+1` 确认后选区替换为 `x²+1` 衬线斜体；点击评论按钮打开评论浮窗，添加 “Review” 后选区显示浅黄底并在浮窗列表中列出，删除后黄底与列表项同时消失。
- 触摸方式：格式按钮用 `adb shell input tap` 精确命中 uiautomator 报告的按钮 bounds；双击进入编辑用 UiAutomator 1.0 `audit.TouchTest` 注入两次点击（间隔 90–120ms）。
- 已知测试副作用：ToolbarTest 页右侧测试文本保留加粗/下划线/删除线格式；测试中一次误触把选中文本替换为 “u”，已用应用内撤销恢复原文。该页为测试页，非用户文档。
- 注：本节安装包 `EF360A26…` 已被 2026-11-02 11:02 的 `63EDA62E…`（缩放时文字消失修复，见 T26）取代；随后 11:29 的 `FBD0FAE0…`（缩放控件调整，见 T27）再次取代，二者均未改格式按钮逻辑，但未在新构建上重复完整格式回归。

