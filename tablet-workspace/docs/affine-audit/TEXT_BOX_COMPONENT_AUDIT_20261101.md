# 无界画布文字框：官方实现溯源与组件级设计审计

日期：2026-11-01  
状态：**审计已完成；用户已确认按本报告实施，Ask AI 不添加。实施记录见 `TEXT_BOX_OFFICIAL_UI_20261101.md`。**  
范围：无界画布中被选中的文字框、其主工具条、所有二级/三级浮窗、文本编辑状态、文本选区状态、加号与选择框。

## 0. 结论摘要

1. 官方 AFFiNE 存在至少两套不同的文字对象模型和工具条：
   - `EdgelessTextBlockModel`（无界文字块）的块级工具条：Ask AI、Display in Page、Note Style、Size、Scale、Draw connector、Lock、More。
   - `TextElementModel`（surface text）的文本动作：字体、文字颜色、字重/斜体、字号、对齐（`gfx/text/src/toolbar/actions.ts`）。
   - 文本编辑并选中文字范围时，还会出现独立的 594×38 文本格式化工具条。
2. 当前项目只有一个 `Shape(type="text")`，把块级、元素级、文本选区级的能力混在同一工具条；这是最大的结构性问题，单纯改样式无法解决。
3. 当前实现与官方的主要差异不是“圆角大小”，而是：
   - 主工具条顺序和内容不同；
   - Note Style 面板宽度、位置、内容不同；
   - 缺少官方文本选区工具条、链接浮窗、高亮菜单、块类型菜单；
   - 官方 6 种阴影、6 档边框宽度、6 档圆角，当前分别只有 3、4、4 档；
   - 官方默认填充是主题自适应的 White，当前默认透明；
   - 官方面板在上方、按按钮居中；当前多数菜单在下方、按组件左对齐；
   - 官方编辑态无“完成”按钮，文本选区工具条在选中文字范围时出现；当前是带“完成”的行内编辑层。
4. 本轮在收到“先审计、后修改”的要求之前，已经写入并安装了一版未批准的改动。用户确认后已按本报告重做，Ask AI 不添加；最终状态见 `TEXT_BOX_OFFICIAL_UI_20261101.md`。
5. 无法确认的项已在第 8 节列出；没有把推断写成事实。

## 1. 访问方式与证据等级

### 1.1 实际应用

- 访问地址：`https://app.affine.pro/`
- 工作区：Demo Workspace（浏览器本地工作区）
- 访问时间：2026-11-01
- 操作方式：Chromium headless + CDP，真实鼠标/键盘事件；不是只看截图。
- 已实际操作：
  - 从 Page 切换到 Edgeless；
  - 用文本工具创建文字块并输入文字；
  - 选中文字块、选中文字范围、进入编辑；
  - 打开 Note Style、Scale、块类型、对齐、高亮、链接、More 等浮窗；
  - 执行 Ask AI、Display in Page、Draw connector、Lock、加号添加相邻文字。
- 主题：先测深色，再切到浅色复测 Note Style 面板与工具条。

限制：这是 **AFFiNE Web 应用**，不是 AFFiNE Android 官方 App；触摸、手写笔、IME、Android 分屏下的行为未在本报告中验证。

### 1.2 官方源码

本地 AFFiNE 源码快照：

- 版本：`@affine/monorepo` 0.27.5
- 分支：`canary`
- commit：`8cef09e1dd62b47a3cbc9f352aa10b3a90c75d34`
- 路径：`C:\Users\17482\Documents\ChatGPT\deepseek\.tmp\affine-src`

已阅读并保存到证据目录的源码：

- `blocksuite/affine/blocks/edgeless-text/src/edgeless-toolbar/config.ts`
- `blocksuite/affine/gfx/text/src/toolbar/actions.ts`
- `blocksuite/affine/gfx/text/src/toolbar/config.ts`
- `blocksuite/affine/blocks/note/src/components/edgeless-note-style-panel.ts`
- `blocksuite/affine/blocks/note/src/components/edgeless-note-shadow-menu.ts`

GitHub 原始文件基址：

```text
https://raw.githubusercontent.com/toeverything/AFFiNE/8cef09e1dd62b47a3cbc9f352aa10b3a90c75d34/<path>
```

**版本漂移警告**：实际 Web 应用的自定义颜色面板是“Colors / Custom”两个 tab；本地 0.27.5 源码的同一组件是“Custom color + Back”结构。因此当前部署版本不保证等于上述 commit。源码只作为算法、属性、组件关系的证据；具体像素以 LIVE DOM 为准。

### 1.3 用户提供的截图

用途：补充浅色主题外观和用户期望。  
已确认与 LIVE 浅色主题一致的：Note Style 面板、Scale 菜单、More 菜单、加号位置。  
已确认不一致的：用户截图中有一个 comment 气泡按钮；2026-11-01 的 LIVE 工具条没有该按钮。该差异按 UNVERIFIED 处理，见第 8 节。

### 1.4 当前项目实现

- 源码：`C:\Users\17482\Documents\ChatGPT\软件开发\tablet-workspace`
- 主要文件：`app/src/main/java/com/example/tabletworkspace/MainActivity.kt`
- 组件尺寸/菜单逻辑与当前安装的 Release 截图共同作为“当前实现”证据。
- 当前实现截图见 `evidence/final-text.png`、`evidence/final-style.png`、`evidence/v2-aa.png`、`evidence/v2-zoom.png`、`evidence/v2-align.png`、`evidence/tb-more.png`。

## 2. 官方文字框的结构与状态

### 2.1 块级工具条（元素被选中）

LIVE DOM 实测（浅色，1440×1000 视口；单位 CSS px）：

| 顺序 | 组件 | 宽度×高度 | 说明 |
|---|---|---:|---|
| 1 | Ask AI | 74.33×36 | 蓝色星标 + 文本；打开 AI 动作菜单 |
| 2 | 分隔 | 4×36 | 1px 视觉线 |
| 3 | Display in Page | 133.83×36 | 文档图标 + 文本；切换 Page Mode 显示 |
| 4 | 分隔 | 4×36 | |
| 5 | Note Style | 24×36 | 调色板图标；打开 Note Style 面板 |
| 6 | 分隔 | 4×36 | |
| 7 | Size | 24×36 | 图标；LIVE tooltip 为 "Customized height"；未观察到弹窗 |
| 8 | 分隔 | 4×36 | |
| 9 | Scale | 65×36 | 显示 `100%`；打开 50/100/200/自定义 |
| 10 | 分隔 | 4×36 | |
| 11 | Draw connector | 24×36 | 进入连接线创建模式 |
| 12 | 分隔 | 4×36 | |
| 13 | Lock | 24×36 | 锁定后工具条消失，元素显示解锁徽标 |
| 14 | 分隔 | 4×36 | |
| 15 | More | 24×36 | 13 项元素级菜单 |

容器：`38px` 外高（`36px` 内容 + 1px 边框），`padding: 0 6px`，`gap: 8px`，`border-radius: 8px`。

- 浅色：背景 `#FFFFFF`，边框 `#E6E6E6`，阴影 `0 1px 6px rgba(0,0,0,.16), 0 8px 14px rgba(0,0,0,.08)`，文字 `#77757D`，字体 14px。
- 深色：背景 `#252525`，边框 `#414141`，阴影 `0 1px 6px rgba(0,0,0,.72), 0 8px 14px rgba(0,0,0,.32)`，文字 `#A8A8A0`。
- 图标容器 20×20；图标按钮宽 24、高 36；文本按钮的标签从图标右侧开始，14px/400。
- 工具条整体宽度会随按钮文本和主题略有变化：浅色约 533.16，深色约 547.16。

LIVE 操作结果：

- **Ask AI**：打开动作浮层，含 Fix spelling、Fix grammar、Explain selection、EDIT TEXT 分组（Translate to、Change tone to、Improve writing、Make it longer、Make it shorter、Continue writing）、GENERATE FROM TEXT 和输入框。
- **Display in Page**：切换为 "Displayed in Page"；出现 toast "Note displayed in Page Mode" / "Content added to your page. Find it in the TOC for quick navigation."，提供 Undo、View in Toc。再次点击可移除。
- **Size**：点击只出现 tooltip "Customized height"，未出现弹窗；没有把它当作字号菜单。
- **Scale**：打开 50/100/200/自定义菜单；200% 会同时放大文字和元素边界。
- **Draw connector**：关闭选区工具条、进入底部连接线工具；从元素右侧拉出连接线。
- **Lock**：工具条消失，元素显示 "Click to unlock" 徽标；点击徽标解锁。
- **More**：见 2.5。

### 2.2 文本选区工具条（编辑态 + 选中文字范围）

LIVE DOM 实测：

| 顺序 | 组件 | 宽度×高度 |
|---|---|---:|
| 1 | Ask AI | 76×36 |
| 2 | 分隔 | 4×36 |
| 3 | Text style（Aa） | 40×36 |
| 4 | 分隔 | 4×36 |
| 5 | Alignment | 40×36 |
| 6 | 分隔 | 4×36 |
| 7 | Bold | 24×36 |
| 8 | Code | 24×36 |
| 9 | Inline Equation | 24×36 |
| 10 | Italic | 24×36 |
| 11 | Link | 24×36 |
| 12 | Strikethrough | 24×36 |
| 13 | Underline | 24×36 |
| 14 | 分隔 | 4×36 |
| 15 | Highlight | 40×36 |
| 16 | 分隔 | 4×36 |
| 17 | Create Linked Doc | 24×36 |
| 18 | 分隔 | 4×36 |
| 19 | More | 24×36 |

容器：`594×38`（内容高 36 + 边框），与块级工具条相同的 8px 圆角、6px 水平内边距、8px gap、20px 图标。  
出现条件：**双击进入编辑后，选中一段文字**；普通元素选中时不出现。

LIVE 子浮窗：

- **Text style（Aa）**：不是字体族菜单，而是块类型菜单：Text、Heading 1–6、Bulleted List、Numbered List、To-do List、Code Block、Equation、Quote。行高约 30px，左侧图标 + 文本。
- **Alignment**：Alignment left / center / right，三行，每行 30px，左侧 20px 对齐图标；菜单约 195px 宽，出现在按钮上方。
- **Highlight**：约 212px 宽，分组 "Color" 与 "Background"；Color 含 Default Color、Red、Orange、Yellow、Green、Teal、Blue、Purple、Grey；Background 含 Default Background 及同组颜色；可滚动，行高约 30px。
- **Link**：浮动输入条约 334×40，占位文字 "Paste or type a link"，右侧确认按钮；不是菜单。
- **More**：Copy、Copy as Image、Copy as Markdown、Duplicate、Delete，共 5 项，行高约 30px。
- **Bold / Italic / Underline / Strikethrough / Code / Inline Equation**：直接作用于当前文字选区。
- **Create Linked Doc**：独立动作按钮；未完成展开行为验证（见第 8 节）。

### 2.3 Note Style 面板（块级）

LIVE DOM 实测：

- 内容区：`276×234.72px`；外层含 8px 内容 padding，视觉外框约 292px 宽。
- 位置：面板在工具条 **上方**，水平中心与 Note Style 按钮中心对齐；面板底边 y=409.72，工具条顶边 y=425，间距约 15.3px。
- 结构：纵向 4 个 section，section 间距 8px；section 标题高 22px、14px/400、secondary 色。
- Fill color：9 个 24×24 色块 + 1 个 24×24 自定义彩虹按钮；色块间距 4px；当前色有 active 属性。调色板包含 Red/Orange/Yellow/Green/Blue/Purple/Magenta/White/Transparent；颜色随主题解析（深色下 White 解析为 #252525）。
- Shadow：6 个选项，项均约 40.7–44.7×46.7–50.7；内部图标 32×38 或 36×42；类型为 None、Box、Sticker、Paper、Float、Film；每项有 tooltip，选中项 1px 品牌色边框。
- Border：左侧 6 档宽度（DOM 点值 2、4、6、8、10、12），右侧 3 个线型按钮（solid/dashed/none），每个 24×24；整行 24px 高。
- Corner Radius：滑块宽 168px，点值 0、4、8、16、24、32；分隔线 4px；输入框 88×24，radius 4，边框 0.5px；输入值上限 32。
- 自定义颜色：点击彩虹按钮后在同一浮窗内切换为 Custom 面板；LIVE 有 Colors / Custom 两个 tab、Light/Dark 预览、饱和度/明度渐变、色相滑条、棋盘透明度滑条、hex 输入、opacity 输入。源码 0.27.5 同期实现是 Back + "Custom color"，与 LIVE 不一致。

### 2.4 Scale 下拉

LIVE DOM 实测：

- 菜单内容区：`68×127px`；在 Scale 按钮上方，x=1200.66，y=283–410；按钮 y=431–455，间距约 21px；水平中心与按钮基本一致。
- 3 个 preset：50%、100%、200%，每项 `68×30`，padding 4px 8px，radius 4px，14px/400；选中项背景 `rgba(255,255,255,.1)`（浅色主题下仍为半透明）。
- 底部自定义输入：`68×25`，radius 8px，背景 `#3B3B3B`（深色）/对应浅色变量，边框 1px，字体约 13.33px；没有独立“应用”按钮。
- 200% 实际会放大文字和元素 bounds；不是只放大画布视图。

### 2.5 元素 More 菜单

LIVE DOM 实测：

- 每个菜单项 `176×30px`；容器约 192–208px 宽；项 padding 4px 8px，radius 4px，14px/400；分组间距 8px。
- 顺序与分组：
  1. Frame section、Group section
  2. Bring to Front、Bring Forward、Send Backward、Send to Back
  3. Copy、Copy as Image、Copy as Markdown、Copy link to block、Duplicate
  4. Turn into linked doc
  5. Delete（delete 样式）
- 菜单位置：默认向按钮上方展开；超出视口时整体被钳制到视口内。本机 1440×1000 下菜单从顶部 y≈13 开始，至 y≈451，右侧与视口右缘留约 13px。

### 2.6 加号、选择框、编辑态

- 加号：元素左右两侧各一个约 20×20 的半透明白色圆角方块，垂直居中；hover 时变蓝。点击左侧加号会在左边创建新的文字块并进入编辑；右侧对称。加号在元素被选中时出现。
- 选择框：蓝色 1–2px 边框；四角白色圆形手柄；左上角外有 2×3 灰色拖拽点阵；无左右连接端口。
- 编辑态：双击进入行内编辑，**没有“完成”按钮**；块级工具条消失；选中文字范围后才出现文本选区工具条。
- 未选中：仅显示文字/填充，无边框、工具条、加号。

### 2.7 官方文本动作源码

`gfx/text/src/toolbar/actions.ts` 定义了 surface text 的动作：

- `a.font`：`Aa` 字体族面板，作用于 `fontFamily`、`fontWeight`、`fontStyle`。
- `b.text-color`：`edgeless-color-picker-button`，label "Text color"，hollow circle，主题色板。
- `c.font-style`：字重 + 斜体面板（Light/Regular/Semibold、Normal/Italic）。
- `d.font-size`：`affine-size-dropdown-menu`，仅在 `type !== 'edgeless-text'` 时出现；选项 16/24/32/40/64/128。
- `e.alignment`：左/中/右对齐，带 `TextAlignLeft/Center/RightIcon`。

`edgeless-text/src/edgeless-toolbar/config.ts` 把这个动作集绑定到 `EdgelessTextBlockModel`，type 为 `edgeless-text`，因此 font-size 动作被排除。  
**注意**：LIVE 中我选中的无界文字块，块级工具条没有显示 Aa/文字颜色/对齐；这些动作出现在编辑态的文本选区工具条中。文本选区工具条的 Aa 菜单实际是块类型菜单，而不是 0.27.5 源码中的字体族面板。该差异说明 LIVE 与本地源码版本/模型绑定不一致，不能把源码的字体族面板直接当成当前块级 UI。

## 3. 官方组件数据（LIVE 实测汇总）

| 项目 | 数值 |
|---|---|
| 块级工具条 | 高度 36 + 1px 边框；padding 0 6；gap 8；radius 8；图标按钮 24×36；图标 20×20；文字 14px |
| 文本选区工具条 | 594×38；按钮 24×36；两个文本菜单按钮 40×36；Ask AI 76×36；分隔 4×36 |
| Note Style 内容区 | 276×234.72；section gap 8；标题 22 高，14px/400 |
| Fill 色板 | 9×24×24 + custom 24×24；gap 4 |
| Shadow | 6 项；约 40–45×47–51；icon 32×38/36×42 |
| Border | 6 档宽度（2–12）；3 个线型；按钮 24×24 |
| Corner Radius | 6 点（0/4/8/16/24/32）；slider 168×24；input 88×24；上限 32 |
| Scale 菜单 | 68×127；项 68×30；输入 68×25；上方展开 |
| More 菜单 | 项 176×30；容器约 192–208 宽；分组间隔 8 |
| 加号 | 约 20×20；左右各一；hover 蓝 |
| 编辑态“完成” | 不存在 |

## 4. 当前项目实现（SRC / 截图）

主要代码：

- `ContextualToolbar.kt:38-56`：`ContextualToolbarTokens.margin=12dp`、`gap=8dp`、`radius=17dp`、`shadow=13dp`；`CanvasUiTokens.compactIconSize=34dp`。
- `MainActivity.kt:4362` 起：文字元素被选中时的主工具条。
- `MainActivity.kt:2850` 起：`TextBoxStylePanel`。
- `MainActivity.kt:3973`：`selectionFrame`（2026-11-02 修正后行号）。
- `MainActivity.kt:3018`：`TextAdjacentPlus`；调用点 `5526-5531`（2026-11-02 修正后行号）。
- `Cards.kt:48-65`：`addTextBox` 默认 `color=0x00ffffff`（透明填充）、`borderStyle="none"`、`cornerRadius` 使用默认 12。
- `MainActivity.kt:2635-2665`：`textScalePercent` / `applyTextScale`，用 `Shape.cardKind` 存 `scale:xxx`（以及后加的 `valign:xxx`）。
- `ColorPicker.kt:249` 起：`ColorEditorDialog`（自定义颜色，模态 Dialog）。

当前主工具条内容：

```text
✦ 文本 | Aa | 100% | 填充色块 | 对齐 | 锁定 | 更多
```

当前实现的关键数值：

| 项目 | 当前实现 |
|---|---|
| 工具条圆角 | 17dp |
| 工具条阴影 | 13dp elevation |
| 图标按钮 | 34dp 圆形/方形容器，图标 16–20dp |
| 标签 | 13sp；Aa 14sp |
| 主工具条内容高度 | 约 44dp（34dp 按钮 + 上下内边距） |
| Note Style 面板 | 440dp 宽；最多 600dp 高；包含“填充颜色/文字颜色/阴影/边框/圆角” |
| Fill | 透明 + 10 个经典色 + 自定义加号；默认透明 |
| Shadow | 无 / 柔和 / 贴纸 3 项 |
| Border | 0/1/2/4 四档 + solid/dashed/none |
| Radius | 0/8/16/24 四个 chip + 0..64 数字输入 |
| Scale 菜单 | 214dp 宽；下方展开；50/100/200 + 输入框 + “应用”按钮 |
| Aa 菜单 | 320dp 宽；字体 Sans/Serif/Mono、字重 400/500/600/700、斜体 |
| Alignment 菜单 | 220dp 宽；水平左/中/右 + 垂直顶/中/底 |
| More 菜单 | 最小 280dp；图层 4 + 复制文本 + 复制为 Markdown + 创建副本 + 删除 |
| 选中框 | 1.5dp 主色描边 + 6dp 白底主色描边的四角手柄（无白色底层/光晕）；文字框圆角 4dp；左上 3×2 拖拽点；无连接端口 |
| 加号 | 24dp 透明点击区内的 20dp 裸加号（无卡片底色），左右各一、与文字框间距 16dp；点击创建同尺寸相邻文字框并进入编辑 |
| 编辑态 | 行内 BasicTextField 覆盖层（无“完成”按钮）；主工具条仍显示；选中文字后出现文本选区工具条 |
| 主题 | 多数颜色直接使用 `LingyeColors`/int 色值；没有官方主题解析层 |

当前实现截图：

- `evidence/final-text.png`：主工具条 + 选中框 + 左右加号。
- `evidence/final-style.png`：当前 Note Style 面板（浅色）。
- `evidence/v2-aa.png`：当前 Aa 菜单。
- `evidence/v2-zoom.png`：当前 Scale 菜单。
- `evidence/v2-align.png`：当前水平/垂直对齐菜单。
- `evidence/tb-more.png`：当前 More 菜单。
- `evidence/final-plus.png`：加号创建相邻文字。
- `evidence/final-text-restored.png`：编辑结束后的状态。

## 5. 组件对照表

级别：P0 = 结构/信息表达错误，必须先决策；P1 = 明显不一致，影响体验；P2 = 细节/主题/无障碍。

| ID | 组件/状态 | 官方证据（LIVE/SRC） | 当前实现 | 差异与风险 | 级别 |
|---|---|---|---|---|---|
| A1 | 文字对象模型 | SRC：`EdgelessTextBlockModel` 与 `TextElementModel` 两套工具条；LIVE 块级与文本选区级工具条分离 | 单一 `Shape("text")` | 把块、元素、文本选区能力混在一个对象；无法同时表达官方两种工具条 | P0 |
| A2 | 块级工具条项 | Ask AI, Display in Page, Note Style, Size, Scale, Draw connector, Lock, More | 文本, Aa, Scale, Fill, Align, Lock, More | 缺 Ask AI/Display/Size/Draw connector；多出 Aa/Align；顺序、信息层级不同 | P0 |
| A3 | 文本选区工具条 | 编辑 + 选中文字后出现 594×38 工具条，含 Aa/对齐/B/Code/Equation/I/Link/Strike/Underline/Highlight/Create Linked Doc/More | 无独立工具条；编辑层内只有 B/I/U chip | 官方核心文本格式化路径缺失；选中文字后没有链接/高亮/代码/公式/删除线/下划线/关联文档 | P0 |
| B1 | 工具条几何 | 36px 高 + 1px 边框；padding 0 6；gap 8；radius 8；图标 20；按钮 24×36 | 约 44dp 高；radius 17dp；shadow 13dp；按钮 34dp | 当前明显更厚、更圆、更松；与官方“扁平紧凑”不符 | P1 |
| B2 | 工具条位置 | 元素上方，左对齐后按视口右缘钳制；style/scale/align 浮窗在上方居中 | 元素上方；浮窗多在下方向下展开 | 主工具条位置接近；子浮窗方向/锚点错误 | P1 |
| B3 | 文本标签 | Ask AI / Display in Page 为 14px/400；文本按钮宽 74.33/133.83 | 仅“文本”静态标签；无 Ask AI/Display 文本按钮 | 信息表达不同；用户看不到对象能力和页面模式 | P0 |
| C1 | Note Style 面板尺寸 | 276×234.72 内容区，外框约 292；8px section gap | 440dp 宽，最高 600dp | 当前约宽 1.5 倍；信息密度低、遮挡画布 | P1 |
| C2 | Note Style 面板位置 | 在工具条上方，中心对齐 Note Style 按钮，底边距工具条约 15px | 在按钮下方，DropdownMenu 默认锚点 | 位置/方向相反；容易遮挡文字 | P1 |
| C3 | Fill 色板 | 9 个 24px 主题色 + 自定义；White 主题自适应；默认 White | 透明 + 10 个固定经典色 + 自定义；默认透明 | 默认外观不同；缺少主题自适应；颜色数量/排列不同 | P1 |
| C4 | 文本颜色 | 官方 Note Style 无“文字颜色”；surface text 用独立的 hollow circle Text color；块级 LIVE 未显示 | 当前把 Text color 放进 Note Style 面板 | 概念混淆：填充色与文字颜色混在同一面板 | P1 |
| C5 | Shadow | 6 种：None/Box/Sticker/Paper/Float/Film，各自 CSS 阴影 | 3 种：无/柔和/贴纸；预览为自绘卡片 | 缺 3 种；阴影语义和视觉不等价 | P1 |
| C6 | Border | 6 档宽度 2–12；3 线型；按钮 24×24 | 4 档 0/1/2/4；3 线型；按钮 38×30 | 档位和尺寸不同；当前“0 宽度”承担 none，官方由 none 线型承担 | P1 |
| C7 | Corner Radius | 6 点 0/4/8/16/24/32；滑块 168×24；输入 88×24，上限 32 | 4 chip 0/8/16/24；输入上限 64 | 档位/范围/控件形态不同 | P1 |
| C8 | 自定义颜色 | 同一浮窗内 Colors/Custom tab；Light/Dark、渐变、色相、透明度、hex/opacity | 模态 `ColorEditorDialog`，RGB/HSV、保存色卡 | 结构/交互不同；模态遮罩打断画布操作 | P1 |
| D1 | Scale | 68×127，按钮上方；项 68×30；输入 68×25；无 Apply；200% 同时放大文字和 bounds | 214dp，按钮下方；项 48dp；有“应用”；通过 cardKind 存 scale，放大 fontSize/width/height | 菜单几何/位置不同；cardKind 编码是项目私有约定；官方 Scale 的持久化字段尚未在源码中确认 | P1 |
| D2 | Aa | 文本选区工具条的 Aa 打开块类型（Text/H1–H6/列表/To-do/Code/Equation/Quote） | 字体族/字重/斜体面板 | 语义完全不同；当前缺少官方块类型转换 | P0 |
| D3 | Alignment | 文本选区工具条的 Alignment：左/中/右，195px 宽，项 30px，图标 20 | 元素工具条的对齐；水平 + 垂直；黑色选中 chip；220dp | 入口层级错误；官方无垂直对齐证据；当前垂直对齐是推断实现 | P1 |
| E1 | Highlight | Color / Background 两组色板，约 212px 宽，可滚动 | 无 | 缺少高亮/背景文字功能 | P0 |
| E2 | Link | 334×40 浮条，输入 "Paste or type a link"，确认按钮 | 无文本链接浮窗 | 缺少链接输入交互 | P0 |
| E3 | Create Linked Doc | 文本选区工具条按钮 | 无 | 缺少关联文档动作 | P1 |
| E4 | Element More | 13 项，176×30，frame/group/layer/copy/image/markdown/link/duplicate/linked doc/delete | 8 项，280dp，48dp 行，缺 frame/group/copy image/copy link/turn into linked doc | 功能动作缺失；当前把部分动作塞进“更多”但标签/分组不同 | P1 |
| E5 | Text More | Copy/Copy as Image/Copy as Markdown/Duplicate/Delete，5 项 | 无文本选区 More | 缺少文本选区级动作 | P1 |
| F1 | 编辑态 | 无“完成”；块工具条隐藏；选中文字后出现文本选区工具条 | 行内编辑覆盖层 + “完成”按钮；主工具条仍在；B/I/U chip 在框内 | 编辑/完成路径不同；官方的选区格式化路径缺失 | P0 |
| F2 | 选择框 | 蓝色边框、白色圆手柄、左上 2×3 拖拽点、无连接端口 | 1.5dp 主色边框 + 6dp 白底主色描边手柄、左上点阵、无端口；已去掉 5dp 白色底层 | 2026-11-02 按用户反馈修正：边框变细、去掉白色光晕、文字框圆角 4dp | P2 |
| F3 | 加号 | 约 20×20 半透明白块，hover 变蓝；点击创建相邻文字 | 24dp 点击区 + 20dp 裸加号，无卡片底；点击创建相邻文字并编辑 | 2026-11-02 按用户反馈去掉卡片底并按官方尺寸重做；触摸屏无 hover 变色 | P2 |
| F4 | Lock | 工具条消失；元素显示解锁徽标 | 工具条保留，锁图标选中；无解锁徽标 | 交互状态不同 | P1 |
| F5 | Display in Page | 切换 Page Mode；toast + Undo/View in Toc | 无 | 缺页面模式显示和撤销入口 | P1 |
| F6 | Ask AI | 动作菜单 + 输入框 + 文本编辑操作 | 无 | 离线项目无法直接等价；不能伪 UI | 决策 |
| F7 | Draw connector | 进入连接线工具 | 无 | 缺连接线入口 | P1 |
| G1 | 图标系统 | `@blocksuite/icons`：20px；字体/文字颜色/对齐/B/Code/Equation/I/Link/Strike/Underline/Highlight/Linked Doc/More | 自定义 `AppIcon`；16–20dp；缺 Code/Equation/Strike/Highlight/Linked Doc/Comment | 图标语义/风格不同；部分用文字/手绘 | P1 |
| G2 | 主题适配 | CSS 变量/主题解析；fill White 在深色变 #252525；工具栏/面板双主题 token | 固定 int 色 + LingyeColors；填充默认透明 | 深色/浅色语义不一致；White/Transparent 行为不同 | P1 |
| G3 | 无障碍/提示 | aria-label、tooltip（如 Note Style、Bold、Click to unlock） | 部分 contentDescription；无 tooltip/aria 体系 | TalkBack/鼠标悬停信息不完整 | P2 |

## 6. 功能逻辑与交互差异

### 6.1 官方选中元素的操作链

```text
单击文字块 → 块级工具条（Ask AI / Display / Note Style / Size / Scale / Connector / Lock / More）
双击文字块 → 进入行内编辑（块级工具条消失）
编辑中选中文字 → 594×38 文本选区工具条（Aa 块类型 / 对齐 / B / Code / Equation / I / Link / Strike / U / Highlight / Linked Doc / More）
```

### 6.2 当前实现的操作链

```text
单击文字框 → 元素工具条（文本 / Aa / Scale / Fill / Align / Lock / More）
双击 → 行内编辑覆盖层 + “完成”按钮；元素工具条仍显示
编辑中选中文字 → 框内 B/I/U chip
```

差异：

- 当前没有“块级”和“文本选区级”两层工具条，只有一层。
- 当前“Aa”被解释为字体族/字重/斜体；官方块级的 Aa（选区态）是块类型。
- 当前“Fill”是填充样式；官方“Fill color”在 Note Style 面板，文字颜色是独立动作。
- 当前“Align”包含垂直对齐；官方 LIVE 只观察到水平对齐（文本选区级）。
- 当前“Scale”用 `cardKind` 存比例；官方 Scale 是模型属性，且菜单没有 Apply 按钮。
- 当前“More”是元素级混合动作；官方元素 More 13 项、文本选区 More 5 项。

### 6.3 二级/三级浮窗清单

| 层级 | 官方 | 当前 |
|---|---|---|
| 二级 | Note Style 面板、Scale 菜单、Alignment 菜单、Element More、Text More、Highlight、Link、Ask AI | Aa 面板、Scale 菜单、Fill/Style 面板、Alignment 面板、More 菜单 |
| 三级 | Note Style 内 Custom color；Ask AI 内 Translate to / Change tone to 子菜单；链接/高亮内部搜索或选项（部分未展开） | 自定义颜色 `ColorEditorDialog`（模态）；无其他三级 |
| 未验证 | Create Linked Doc 选择器、Ask AI 子菜单完整流程、Highlight 搜索 | 无 |

## 7. 应复用的官方设计规律与 Android 适配决策

### 7.1 直接复用（不应自创）

1. **两层工具条**：元素级与文本选区级分离；编辑态隐藏元素级工具条。
2. **36px 内容高度、24×36 图标按钮、20px 图标、8px gap、6px 横向内边距、8px 圆角**。
3. **Note Style 内容宽 276px、8px section 间距、22px section 标题、14px/400 标题字**。
4. **浮窗在上方、以触发按钮中心对齐、按视口钳制**；不是默认向下。
5. **Fill 9 色 + 自定义；Shadow 6 种；Border 6 档宽度 + 3 线型；Radius 6 点 + 数值输入**。
6. **默认填充为 White（主题解析），默认阴影 Box**；不是透明。
7. **编辑态无“完成”按钮**；点击外部/选区结束完成编辑。
8. **元素 More 13 项、文本选区 More 5 项**；分组和标签按官方。
9. **加号约 20px，hover 变蓝**；选择框无连接端口。
10. **图标语义按官方**：Note Style（调色板）、Scale、Draw connector、Create Linked Doc、Highlight 等，不用自绘替代。

### 7.2 Android 平板适配决策（需要确认）

- 官方 36px 是 CSS px；Android 平板触摸目标是否直接 36dp，还是保留 36dp 视觉 + 44dp 热区？
- 官方 276px 面板在 1280dp 平板上是否仍为 276dp，还是按窗口宽度缩放？
- 官方字体 Inter；Android 是否引入 Inter 资源，还是保持系统 Sans？
- 官方 hover 状态（加号、按钮）在触摸屏上是否改为选中/长按反馈？
- 官方 AI/评论/协作依赖云端；本离线应用是否提供等价入口，还是明确禁用/隐藏？

## 8. 无法确认 / 限制

1. 实际访问的是 **AFFiNE Web Demo**，不是 Android 官方 App；触摸、手写笔、IME、分屏下的工具条位置和键盘避让未验证。
2. 实际部署版本未暴露；本地源码为 0.27.5 canary commit `8cef09e`。LIVE 的自定义颜色 tab 与本地源码结构不同，说明存在版本漂移；不能把源码的每个样式直接当成 LIVE。
3. 用户截图中的 **comment 气泡按钮** 在 2026-11-01 LIVE 工具条中不存在；可能是版本差异或 feature flag，未确认，不应据此新增伪 UI。
4. 用户截图中的“垂直对齐图标”在 LIVE 中 aria 为 **Size**，tooltip 为 "Customized height"，没有观察到弹窗；不能确认它是垂直对齐。
5. 官方 `gfx/text` 源码有字体族/文字颜色/字重/字号/对齐动作，但 LIVE 块级工具条未显示；这些动作在文本选区工具条中以块类型/对齐/格式按钮出现，具体模型绑定仍需按版本确认。
6. Create Linked Doc 点击后的完整选择器、Ask AI 的 Translate/Tone 子菜单、Highlight 的搜索/自定义行为未完整展开。
7. 官方颜色值/阴影 CSS 已取得；浅色主题下部分色块的精确 RGB、选中环宽度、图标 SVG 资源未逐项提取。
8. 当前项目只有 Web/Compose 截图和代码测量，没有 Android 官方 App 的 dp 对照；不能把 CSS px 直接断言为 dp。
9. 当前项目的 `cardKind` 存储 `scale`/`valign` 是项目私有扩展；官方是否有对应属性、如何持久化，未确认。

## 9. 审计前已改动项（已按确认重做）

在收到“先提交审计报告、等待确认”之前，本轮已经改动并安装过一版预览。以下为当时已改动的范围；用户确认后已按本报告重做，Ask AI 不添加，最终状态见 `TEXT_BOX_OFFICIAL_UI_20261101.md`：

1. 创建者徽标“本”文字居中（`MainActivity.kt` 信息面板）。
2. 文档空块移除手形拖拽图标，加号在 76dp 槽内居中（`MainActivity.kt:1978-2025`）。
3. 文档信息/链接区左对齐与黑色胶囊分段（前一阶段）。
4. 文字框主工具条改为“文本 / Aa / Scale / Fill / Align / Lock / More”。
5. 新增 `TextBoxStylePanel`（440dp，Fill/Text color/Shadow/Border/Radius）。
6. 新增 Aa 字体族/字重/斜体面板、Scale 百分比菜单、水平+垂直对齐面板。
7. 新增左右加号创建相邻文字框；选择框隐藏连接端口并加拖拽点。
8. 新增 `cardKind=scale:...;[valign:...]` 私有元数据；新增文字填充/阴影/边框/圆角绘制。
9. 编辑层“完成”按钮改为 Surface 样式，编辑层使用文字颜色/填充。

当前最终 APK 见 `TEXT_BOX_OFFICIAL_UI_20261101.md` 第 4 节。

## 10. 建议的实施顺序（待确认）

### P0：先定结构，不碰细节

1. 写 ADR：当前 `Shape("text")` 对应官方哪一种模型；是否需要拆分 `textBlock` / `textElement`，以及页面格式变更/迁移方案。
2. 重做主工具条为官方元素级顺序与几何；没有实现的 Ask AI/Display/Comment 是否隐藏、禁用或做本地等价，需先决定。
3. 实现文本选区工具条（594×38）和编辑态/选区态切换；这是官方文字体验的核心。
4. 将 Note Style 面板改为官方内容与语义：默认 White、9 色、6 阴影、6 档边框、6 点圆角、上方居中；移除“文字颜色”混入。

### P1：二级/三级浮窗

5. Scale 菜单改为 68×127、上方、无 Apply。
6. 元素 More 13 项、文本 More 5 项，按官方顺序/分组；未支持动作明确禁用或隐藏，不伪造。
7. Alignment、Highlight、Link、Create Linked Doc、加号按官方结构和状态重做。
8. Lock、Display in Page、Draw connector 的行为对齐。

### P2：视觉 token/无障碍

9. 图标资源、主题解析、aria/tooltip、触摸热区、深浅色对照。

## 11. 证据索引

### 11.1 LIVE 截图

- `evidence/affine-live-20261101/initial.png`：Demo Workspace 初始文档。
- `affine-edgeless.png`、`affine-texttool.png`、`affine-text-created.png`：进入 Edgeless、文字工具、创建文字。
- `affine-text-selected4.png`：块级选中工具条（深色）。
- `affine-light-selected.png`：块级选中工具条（浅色）。
- `affine-style-panel.png`、`affine-light-style.png`：Note Style 面板深/浅色。
- `affine-zoom-menu.png`：Scale 菜单。
- `affine-more-menu2.png`：元素 More 菜单。
- `affine-askai.png`：Ask AI 浮层。
- `affine-display-in-page.png`：Display in Page toggle + toast。
- `affine-connector.png`：Draw connector 状态。
- `affine-locked.png`：Lock 状态与解锁徽标。
- `affine-plus.png`、`affine-plus-undo.png`：加号创建相邻文字和 hover/撤销后状态。
- `affine-text-editing.png`：编辑态（无“完成”）。
- `affine-text-selection-toolbar.png`：文本选区工具条。
- `affine-text-aa.png`：Aa 块类型菜单。
- `affine-text-align2.png`：Alignment 菜单。
- `affine-highlight3.png`：Highlight Color/Background 菜单。
- `affine-text-more.png`：文本选区 More 菜单。
- `affine-text-link2.png`：Link 输入浮条。
- `affine-custom-color.png`：Custom color 三级面板。

### 11.2 官方源码

本地证据副本：

- `evidence/affine-source-0.27.5/edgeless-text-toolbar-config.ts`
- `evidence/affine-source-0.27.5/gfx-text-toolbar-actions.ts`
- `evidence/affine-source-0.27.5/gfx-text-toolbar-config.ts`
- `evidence/affine-source-0.27.5/edgeless-note-style-panel.ts`
- `evidence/affine-source-0.27.5/edgeless-note-shadow-menu.ts`

GitHub（commit `8cef09e1dd62b47a3cbc9f352aa10b3a90c75d34`）：

- `https://github.com/toeverything/AFFiNE/blob/8cef09e1dd62b47a3cbc9f352aa10b3a90c75d34/blocksuite/affine/blocks/edgeless-text/src/edgeless-toolbar/config.ts#L9-L19`
- `https://github.com/toeverything/AFFiNE/blob/8cef09e1dd62b47a3cbc9f352aa10b3a90c75d34/blocksuite/affine/gfx/text/src/toolbar/actions.ts#L119-L388`
- `https://github.com/toeverything/AFFiNE/blob/8cef09e1dd62b47a3cbc9f352aa10b3a90c75d34/blocksuite/affine/gfx/text/src/toolbar/config.ts#L13-L56`
- `https://github.com/toeverything/AFFiNE/blob/8cef09e1dd62b47a3cbc9f352aa10b3a90c75d34/blocksuite/affine/blocks/note/src/components/edgeless-note-style-panel.ts#L49-L124`
- `https://github.com/toeverything/AFFiNE/blob/8cef09e1dd62b47a3cbc9f352aa10b3a90c75d34/blocksuite/affine/blocks/note/src/components/edgeless-note-style-panel.ts#L292-L363`
- `https://github.com/toeverything/AFFiNE/blob/8cef09e1dd62b47a3cbc9f352aa10b3a90c75d34/blocksuite/affine/blocks/note/src/components/edgeless-note-shadow-menu.ts#L18-L106`

### 11.3 当前实现

- `evidence/final-text.png`、`final-style.png`、`v2-aa.png`、`v2-zoom.png`、`v2-align.png`、`tb-more.png`、`final-plus.png`、`final-text-restored.png`
- 源码行号：`ContextualToolbar.kt:38-56`、`MainActivity.kt:2635-2665`、`2850-2930`、`3617-3645`、`4362-4692`、`4694-4725`、`Cards.kt:48-65`。

## 12. 2026-11-02 选区框、加号与文本内边距修正

用户指出：官方选中框与当前实现有差距；官方左右加号没有底部卡片，而当前实现有；同时要求修正文本与边框之间的默认距离。以下为修正后的实现与证据。

### 12.1 选区框

- `selectionFrame` 改为单层 1.5dp 主色描边，去掉原 2dp 蓝线下的 5dp 白色底层；四角为 6dp 白色实心圆 + 1.6dp 主色描边。
- 文字框圆角 4dp，普通对象 8dp；左上保留 3×2 拖拽点；无连接端口。
- 证据：`evidence/affine-inline-20261101/selbox-1.png`（细边框、无白色光晕、裸加号、文字内边距）。

### 12.2 加号

- `TextAdjacentPlus` 去掉白色卡片 Surface，改为 24dp 透明点击区内的 20dp 自绘加号（1.6dp 圆头线），颜色 `LingyeColors.Muted`。
- 左右加号与文字框的水平间距由 10dp 调整为 16dp，垂直居中。
- 证据：`selbox-1.png` 中左右两侧均为裸加号，无圆角卡片底。

### 12.3 文本与边框的默认距离

- 内边距是文档坐标中的 24dp：画布渲染按 `24.dp.toPx() * scale` 绘制，缩放时与文字框一起等比缩小；编辑覆盖层使用 `24.dp * scale`，字段最小高度同步按 `scale` 计算。
- 自动高度拟合 `autoFitTextHeight` 以世界坐标 `width-48dp` 计算行数并加 48dp 高度（最小 88dp），与缩放无关。
- 链接命中测试 `TextLinkHitTest` 改为世界坐标：内边距 24dp、字号按 dp 计算，不再乘 density，避免缩放/密度耦合。
- 证据：`selbox-1.png`（文字不再贴边）；`selbox-4.png`（单击已选中文字进入编辑）；`zoom-10-crop.png`（31% 下编辑覆盖层内边距按比例缩小）。

### 12.4 验证与发布

- 设备：模拟器 2560×1600、density 320；安装包与发布文件逐字节一致。
- 299 项 JVM 测试 0 失败；页面格式仍 25；页面数 204、历史 24、活动文档 193 未变。
- 发布：`releases/lingye-v0.15-selection-frame-padding-release-20261102.apk`（2,578,113 字节，SHA-256 `EF360A2631F4DD438C4CDD9D95F682BB79B91CC3AB4C65A7C1B0A9038BA94C8F`）。
- 未覆盖：真机上不同字体/字号下的内边距逐像素对照；深色主题下的描边对比度。

### 12.5 缩小画布后文字消失（2026-11-02 修复）

- 现象：画布缩到 33% 左右时文字框内文字完全不可见，33% 与 51% 截图见用户提供的 `images/19.png`、`images/20.png`。
- 根因：12.3 首版把内边距写成屏幕像素常数 `24.dp.toPx()`；缩小时 `innerHeight = height*scale*density - 2*48px` 变成 1px 或负数，`clipRect` 把整段文字裁掉，同时文字起点落在裁剪区之外。
- 修复：渲染内边距改为 `24.dp.toPx() * scale`；编辑覆盖层内边距与最小高度按 `scale` 缩放；`TextLinkHitTest` 改为世界坐标（24dp 内边距、字号按 dp），链接命中不再受缩放/密度影响。
- 验证：模拟器 41%、33%、31%、25% 缩放下两个文字框均正常显示（`zoom-2/3/4.png`、`zoom-12.png`）；31% 下编辑覆盖层文字与内边距成比例（`zoom-10-crop.png`）；31% 下点击链接文字打开 Chrome（`zoom-20.png`）。
- 测试：新增 `TextLinkHitTestTest` 5 项，总计 304 项 JVM 测试 0 失败。
- 发布：`releases/lingye-v0.15-zoom-text-padding-release-20261102.apk`（2,578,113 字节，SHA-256 `63EDA62E2B8ED9EC1C35987CB63DE35CB750E69D77851E0AF8DDABAAECC88CB8`）。
