# 设置专项改进清单（2026-10-31）

## 1. 官方核对

参考 AFFiNE 源码 commit `8cef09e1dd62b47a3cbc9f352aa10b3a90c75d34`（canary，2026-10-27），设置弹窗位于：

- `packages/frontend/core/src/desktop/dialogs/setting/index.tsx`、`layout.tsx`、`setting-sidebar/index.tsx`
- `.../general-setting/appearance/index.tsx`
- `.../general-setting/editor/general.tsx`、`page.tsx`、`edgeless/*.tsx`
- `.../general-setting/shortcuts/index.tsx`
- `.../workspace-setting/index.tsx`、`preference/`、`properties/`、`storage/`
- `packages/frontend/component/src/components/setting-components/setting-row.tsx`

官方结构为左侧“设置”标题 + 账户卡 + 两组导航（常规 / 工作区），右侧为设置内容，每个条目由名称、说明和右侧控件组成。

| 官方分组 | 官方条目（本机可见部分） | 本机处理 |
|---|---|---|
| 外观 | 主题（跟随系统/浅色/深色）、语言、图片抗锯齿、打开链接方式、侧边栏显示链接文档 | 保留语言、图片平滑、动效；主题仅浅色并说明；新增侧边栏链接文档 |
| 编辑器 | 字体（Sans/Serif/Mono）、正文字号、默认页面模式、日期标题、白板默认主题、滚轮缩放 | 新增正文字体；其余已有或不适配本机 |
| 键盘快捷键 | 工具与命令按键列表、快捷键设置 | 保留只读列表，补充分组与说明 |
| 数据/储存 | 备份、恢复、导入、导出、占用空间 | 已有，重排到“数据与储存” |
| 工作区 | 偏好设置（名称等）、属性、成员、集成、储存、嵌入、计费 | 新增“工作区”分组与名称/副标题；成员/集成/云储存/计费不适用于离线单机版 |
| 关于 | 版本、构建、开源信息 | 保留版本与格式信息 |

不移植：账号、通知、会议、计费、定价计划、成员、集成、云嵌入、实验性云功能。原因：本机离线版没有账号与服务端，放置无效入口会违反“未完成功能不得伪装可用”。

## 2. 当前差距

- 设置虽有弹窗形态，但条目样式不完全一致：部分行用 `SettingsToggleRow`，部分直接拼 Row；滑块、分段、颜色控件缺少统一标题/说明/帮助结构。
- 缺少逐项帮助：用户无法从界面了解“仅手写笔”“平滑图片渲染”“默认打开方式”等选项的具体影响。
- 可配置项偏少：无工作区名称/副标题、无正文字体、无网格显示/大小/默认吸附、无侧边栏链接文档。
- 工作区与个人数据没有独立分组；数据、关于与外观混在同一层导航。

## 3. 任务清单

### SET-01 统一设置弹窗结构与视觉令牌

- [x] 左侧导航分为“常规”（外观、编辑器、画布、键盘快捷键、数据与储存、关于）与“工作区”（工作区信息）两组，带分组标题。
- [x] 右侧内容统一为“标题 + 说明 + 分区 + 设置行”，设置行统一为：名称（Medium）、说明（12sp Muted）、可选问号帮助、右侧控件；滑块/分段等整行控件放在说明下方。
- [x] 统一间距（分区 20dp、行 12dp、控件高 32–36dp）与分隔线；保持白色卡片、浅色背景、圆角 24dp 的清爽风格。
- [x] 导航项保持 44dp 行高、选中白底浅阴影；工作区卡片使用应用启动图标。

### SET-02 逐项帮助（问号 + 浮窗）

- [x] 每个设置行名称后提供 20dp 圆形“?”按钮，点击弹出 260–300dp 浮窗，包含该项的作用、影响范围与建议值。
- [x] 浮窗使用与下拉菜单一致的白色圆角表面和阴影；再次点击或点击外部关闭；帮助文案中英双语。
- [x] 帮助覆盖：语言、动效、平滑图片、主题说明、正文字号、正文字体、默认打开方式、日期标题、画布背景、卡片/图形颜色、连接线默认值、画笔、网格、吸附、侧栏链接文档、数据备份/导入/导出、快捷键分组。

### SET-03 工作区名称与副标题

- [x] `WorkspaceSettings` 新增 `workspaceName`（默认“灵页”）与 `workspaceSubtitle`（默认“离线创意空间”），写入 SharedPreferences，带长度与空白规范化。
- [x] 新增“工作区”分类：名称、副标题输入框，实时生效并持久化；侧栏品牌区实时显示。
- [x] 提供帮助说明：名称仅影响本机显示，不改变页面内容。

### SET-04 正文字体

- [x] `WorkspaceSettings` 新增 `editorFontFamily`（`sans` / `serif` / `mono`），默认 `sans`。
- [x] 文档标题、正文、块内容与卡片文本使用对应 `FontFamily`；切换后立即生效并跨重启保持。
- [x] 设置行使用分段控件，并提供帮助说明（等宽适合代码与对齐，衬线适合长文阅读）。

### SET-05 画布网格与吸附

- [x] `WorkspaceSettings` 新增 `showGrid`（默认开）、`gridSize`（小 20 / 中 28 / 大 36，默认中）、`snapToGridDefault`（默认关）。
- [x] 画布网格按设置绘制/隐藏，间距按世界坐标 20/28/36 计算；缩放小于阈值时仍自动隐藏。
- [x] 画布吸附初始值取设置；顶部“更多 → 网格吸附”仍可临时切换。
- [x] 帮助说明网格只影响显示与吸附，不写入页面数据。

### SET-06 侧边栏链接的文档

- [x] `WorkspaceSettings` 新增 `showLinkedDocs`（默认开）。
- [x] 外观页提供开关；开启且有当前页面时，侧栏在导航区下方显示“链接的文档”列表，列出当前页面画布链接卡片指向且未删除的页面。
- [x] 点击列表项按链接打开逻辑进入目标页面；目标失效或列表为空时不显示该分组。
- [x] 帮助说明列表来自当前页面的链接卡片，不额外写入数据。

### SET-07 外观分组整理

- [x] 外观页重新分组：主题（说明仅浅色）、界面（语言、动效）、图片（平滑图片渲染）、侧边栏（显示链接的文档）。
- [x] 每个条目接入 SET-02 帮助；主题不放置不可用开关，保留说明。
- [x] 语言切换后帮助文案同步切换。

### SET-08 编辑器分组整理

- [x] 编辑器页分组：排版（正文字号、正文字体）、新页面（默认打开方式、日期标题）。
- [x] 正文字号滑块显示当前值；字体分段控件与字号联动应用到文档。
- [x] 帮助说明字号只影响文档编辑与阅读，不改变画布对象。

### SET-09 画布分组整理

- [x] 画布页分组：背景与卡片、图形、网格与吸附、连接线、画笔。
- [x] 网格开关、大小分段、默认吸附开关接入设置；现有颜色与默认值保持不变。
- [x] 帮助说明各默认值只影响新建对象。

### SET-10 快捷键页

- [x] 保留只读快捷键列表，按“工具 / 命令 / 对象操作”分组，键帽统一右对齐。
- [x] 每组提供帮助说明；页面底部说明暂不支持自定义。
- [x] 核对 `CanvasShortcuts.kt` 与界面文案一致。

### SET-11 数据与关于整理

- [x] 数据与储存页统一为“本机内容 / 备份与恢复 / 格式导入导出”三组，全部条目接入帮助。
- [x] 关于页保留版本、页面格式、数据位置、云同步未启用说明；帮助解释“页面格式 22”和备份范围。
- [x] 不新增云同步、成员、集成、计费入口。

### SET-12 验收

- [x] 单元测试覆盖设置默认值、规范化、偏好读写（新增 `WorkspaceSettingsTest` 或等价测试）。
- [x] 构建 `testDebugUnitTest assembleRelease` 通过。
- [x] 模拟器验证：2560×1600 与 640dp 下弹窗、导航、帮助浮窗、字体/网格/侧栏开关生效并跨重启保持；关闭弹窗不改变底层视图。
- [x] 更新 `WORK_CHECKLIST.md`、`FEATURE_MATRIX.csv`、`TOOLBAR_PARITY.md`，保存证据截图并发布 APK。

## 4. 状态

- SET-01 至 SET-12 已实施并在 2560×1600 与 640dp 模拟器验证；221 项单元测试 0 失败。
- 证据：`evidence/settings-overhaul-wide.png`、`settings-overhaul-narrow.png`、`settings-help-popover.png`、`settings-workspace.png`、`settings-editor.png`、`settings-canvas-grid.png`、`settings-shortcuts-groups.png`、`sidebar-linked-docs.png`、`canvas-grid-off.png`、`canvas-grid-on.png`、`editor-serif-title.png`。
- 发布 APK：`releases/lingye-v0.15-settings-pro-release-20261031.apk`（SHA-256 `E35B737BECD1C63E5FBEE99660DFB35E25AD72F7263A649914BFF164643978B4`）。
- 已知不移植项：深色主题、云通知、会议、计费、成员、集成、云嵌入；在设置中不出现无效入口，外观页保留说明。

## 5. 第二轮：按官方设置布局审核校正（2026-10-31）

审核对象：用户提供的 AFFiNE 设置截图（2048×1220）与源码 `packages/frontend/core/src/desktop/dialogs/setting`。本轮将设置弹窗的布局、字号、分组、控件对齐和页脚重新按官方重做。

### 5.1 审核对照

| 审核项 | 官方表现 | 本轮前 | 校正结果 |
|---|---|---|---|
| 弹窗外壳 | 左侧浅灰导航、右侧纯白内容、无内层卡片 | 顶部标题 + 左右两张圆角卡片、右侧带阴影 | 改为左侧 #F7F7F9 通栏、右侧纯白通栏，取消内层卡片与阴影 |
| 标题 | “设置”位于左栏顶部 | 标题横跨左右两栏 | 标题移入左栏顶部 |
| 账户/工作区卡 | 头像、名称、Free 徽标、邮箱 | 应用启动图标、名称、副标题 | 保留启动图标与名称，增加“本机”徽标，副标题替代邮箱 |
| 导航分组 | 常规：外观、编辑器、键盘快捷键、通知、计费、计划、实验性、关于；工作区：偏好设置、属性、成员、集成、储存、嵌入 | 常规含画布/数据，工作区含工作区信息 | 仅保留本机可用项：常规（外观、编辑器、键盘快捷键、关于灵页）、工作区（偏好设置、储存）；画布并入编辑器；云/账号项不显示 |
| 页面标题 | “外观设置”+说明 | “外观”+说明 | 改为“外观设置”/“编辑器设置”等，说明与官方语气一致 |
| 设置行 | 名称/说明在左，控件右对齐，分区横线 | 混用左右行与整行控件 | 统一 `SettingsControlRow`（240dp/320dp 控件区），窄内容自动上下堆叠 |
| 主题 | 跟随系统/浅色/深色三选一 | 仅“浅色”标签 | 显示三项，跟随系统与深色禁用并附帮助说明，浅色选中 |
| 语言 | 下拉框 | 分段按钮 | 改为下拉框（简体中文/English） |
| 链接 | 打开 AFFiNE 链接方式 | 无 | 新增“打开页面链接”返回行为（返回来源/直接打开），非外部链接 |
| 图片/侧栏 | 平滑图片渲染；侧栏显示链接文档 | 已有 | 保留并纳入新分区 |
| 页脚 | GitHub 加星/反馈 | 无 | 增加“灵页 · 离线优先的文档与白板工作区”页脚；不放置外部链接 |

### 5.2 功能改进清单

#### SET2-01 弹窗外壳

- [x] 左栏使用 #F7F7F9 通栏背景，右栏纯白，无内层卡片与阴影。
- [x] 关闭按钮位于右栏右上角，保留“关闭设置”语义。
- [x] 弹窗保持 94%×90%，圆角 24dp。

#### SET2-02 标题与工作区卡

- [x] “设置”移入左栏顶部，字号 26sp。
- [x] 工作区卡显示启动图标、工作区名称、副标题与“本机”徽标。
- [x] 名称/副标题来自设置并可实时修改。

#### SET2-03 导航分组

- [x] 常规：外观、编辑器、键盘快捷键、关于灵页。
- [x] 工作区：偏好设置、储存。
- [x] 取消独立“画布”导航，画布默认值并入编辑器的“白板”标签页。
- [x] 导航选中态改为浅灰底，不再使用白色浮起卡片。

#### SET2-04 页面标题

- [x] 外观设置、编辑器设置、键盘快捷键、关于灵页、偏好设置、储存均使用“标题 + 一句说明”。
- [x] 字号、行距与官方截图一致（标题 25sp，说明 13sp muted）。

#### SET2-05 设置行

- [x] 统一名称/说明左对齐，控件右对齐；主题控件 320dp，语言/链接 240dp。
- [x] 内容宽度小于 460dp 时控件自动换到说明下方，避免 640dp 挤压。
- [x] 分区之间使用横线，分区标题为 12sp 半粗 muted。

#### SET2-06 外观页

- [x] 主题配色方案三选一，跟随系统/深色禁用并在帮助中说明原因。
- [x] 显示语言改为下拉框，切换后界面立即更新。
- [x] 动效、平滑图片渲染、侧栏链接文档保留并接入帮助。
- [x] 新增“链接”分区与“打开页面链接”返回行为设置。

#### SET2-07 编辑器页

- [x] 新增“文档/白板”标签切换，对应官方 Page/Edgeless 结构。
- [x] 文档：正文字号、正文字体、默认打开方式、日期标题。
- [x] 白板：背景与卡片、图形、网格与吸附、连线、画笔默认值。

#### SET2-08 偏好设置页

- [x] 工作区名称与副标题输入框。
- [x] 存储位置、云同步、数据上传信息。
- [x] 帮助说明名称仅影响本机显示。

#### SET2-09 储存页

- [x] 本机内容、备份与恢复、Markdown 导入导出。
- [x] 数据/备份/页面格式帮助说明。
- [x] 不增加云同步、成员、集成、计费入口。

#### SET2-10 帮助

- [x] 保留问号帮助浮窗，触控区扩大到 30dp，浮窗 280dp。
- [x] 帮助覆盖新增主题、语言、链接、字体、网格、侧栏与储存项。

#### SET2-11 页脚

- [x] 右栏底部固定“灵页 · 离线优先的文档与白板工作区”，不添加外部链接。

#### SET2-12 验收与交付

- [x] 221 项单元测试 0 失败；`assembleRelease` 通过。
- [x] 2560×1600 与 640dp 模拟器验证：外观、编辑器文档/白板、偏好设置、储存、帮助浮窗与响应式堆叠。
- [x] 证据：`settings-official-wide.png`、`settings-official-editor.png`、`settings-official-editor-whiteboard.png`、`settings-official-preferences.png`、`settings-official-storage.png`、`settings-official-narrow.png`、`settings-official-english.png`。
- [x] 发布 APK：`releases/lingye-v0.15-settings-official-release-20261031.apk`（SHA-256 `07CB6CB6DDBF94DD32CF10F68E46AA2A9F65598651B50208B7178531E767F1A0`）。

### 5.3 保留差异

- 深色主题未实现：主题控件保留三项，跟随系统/深色禁用，帮助说明原因。
- 不含账号、通知、会议、计费、定价计划、成员、集成、云嵌入与实验性云功能：离线单机版无对应服务，不显示无效入口。
- “打开页面链接”是本机页面链接返回行为，不等同官方的外部 AFFiNE 链接打开方式。
- 页脚为产品说明，不含 GitHub 外链。
## 6. 第三轮：编辑器与快捷键板块核对（2026-10-31）

核对对象：AFFiNE `8cef09e` 的 `general-setting/editor`（`general.tsx`、`page.tsx`、`edgeless/*`）与 `general-setting/shortcuts/index.tsx`，参考官方设置截图与快捷键文档。

### 6.1 官方编辑器设置与实现对照

| 官方分组 | 官方条目 | 本机实现 | 状态 |
|---|---|---|---|
| General | 正文字体 Sans / Serif / Mono（桌面另有自定义字体） | 无衬线/衬线/等宽，应用到文档标题、正文、文本块 | 已实现；自定义系统字体不适用平板 |
| General | 正文字号 12–24 | 12–24 滑块与当前值 | 已实现 |
| General | 新建文档默认模式 Page / Edgeless / Ask | 文档 / 白板；创建入口本身就是文档/白板二选一 | 未提供 Ask；入口已显式选择，不伪装 |
| General | 自动日期标题 + 日期格式 | 开关 + 4 种格式（2026-10-31 / 2026/10/31 / 10-31 / 10月31日） | 已实现 |
| General | 显示添加图标选项 | 本机页面无图标模型 | 不适用 |
| General | 代码块行号 | 开关 + 代码块左侧行号 | 已实现 |
| General | AI、拼写检查、中键粘贴 | 无云 AI、无桌面拼写检查/中键 | 不适用/不移植 |
| Page | 默认页面宽度 标准 / 全宽 | 标准（最大 860dp 居中）/ 全宽 | 已实现 |
| Page | 显示页面信息 | 顶栏信息按钮可隐藏 | 已实现；页面选项菜单仍可查看 |
| Page | 显示双向链接 | 本机无反向链接模型 | 未实现/不适用 |
| Edgeless | 默认主题 specified/dark/light/auto | 仅浅色画布 | 未实现（深色未接入） |
| Edgeless | 滚轮缩放 | 平板以双指缩放为主，未接滚轮 | 未实现/不适用 |
| Edgeless | 连接线默认值 | 路径、线宽、颜色、终点样式 | 部分实现；粗糙笔触、虚线未实现 |
| Edgeless | Frame 默认背景 | 6 种背景色 | 已实现 |
| Edgeless | 画笔/形状/网格 | 画笔粗细/颜色/仅手写笔、形状填充色、网格与吸附 | 部分实现；Note/文本/思维导图主题等未覆盖 |

### 6.2 官方快捷键与实现对照

| 官方分组 | 本机状态 |
|---|---|
| General（Esc、Ctrl+K 搜索、Ctrl+N 新建、Ctrl+/ 侧栏、前进/后退等） | 画布 Esc 已实现；应用级 Ctrl+K / Ctrl+N / Ctrl+/ 未实现 |
| Page（撤销/重做、粗体/斜体/下划线、标题、列表、斜杠菜单等） | 文档编辑快捷键未实现，使用块菜单/斜杠命令；画布撤销/重做已实现 |
| Edgeless（工具键、缩放、删除、复制、编组、层级、对齐、空格平移） | 工具键、缩放、删除、撤销/重做、全选已实现；本轮新增 Ctrl+D 复制、Ctrl+G 编组、Ctrl+Shift+G 取消编组、Ctrl+] 置顶、Ctrl+[ 置底、Ctrl+Shift+L/R/T/B 对齐；空格平移未实现 |
| Markdown 语法 | 导入/导出支持标题、列表、待办、引用、代码等子集；未提供语法参考面板 |

### 6.3 改进清单

#### SET3-01 核对与清单

- [x] 读取官方 editor/general/page/edgeless 与 shortcuts 源码，形成上表。
- [x] 将不适用项（AI、拼写检查、中键、页面图标、双向链接、深色主题、滚轮缩放、Ask 模式）明确列出，不放入无效 UI。

#### SET3-02 编辑器 General

- [x] 正文字号范围扩展为 12–24。
- [x] 自动日期标题增加日期格式下拉，仅开启时显示。
- [x] 新增代码块行号开关，并在代码块左侧渲染行号。
- [x] 字体、字号、日期格式、行号开关写入 SharedPreferences 并跨重启保持。

#### SET3-03 编辑器 Page

- [x] 新增页面宽度标准/全宽设置；标准宽度最大 860dp 居中。
- [x] 新增显示页面信息开关，控制顶栏信息按钮。
- [x] 页面选项菜单中的信息入口不受影响。

#### SET3-04 编辑器 Edgeless

- [x] 新增新 Frame 背景色（白/浅灰/浅蓝/浅绿/浅黄/浅紫），创建 Frame 时使用。
- [x] 新增连接线默认线宽（1/2/4/6）设置，新连线使用。
- [x] 保留连接线路径、颜色、终点、画笔、网格等已有默认值。

#### SET3-05 快捷键页内容

- [x] 工具、命令、对象操作三组保留。
- [x] 新增“选择与编组”组：Ctrl+D、Ctrl+G、Ctrl+Shift+G、Ctrl+]、Ctrl+[、Ctrl+Shift+L/R/T/B。
- [x] 页脚说明文档编辑官方快捷键与自定义绑定尚未实现。

#### SET3-06 快捷键实现

- [x] `handleCanvasShortcut` 支持 Ctrl+D 复制、Ctrl+G/Shift+G 编组/取消编组、Ctrl+]/[ 置顶/置底、Ctrl+Shift+L/R/T/B 四向对齐。
- [x] 增补 `[`、`]`、D/G/B/R 的硬件键盘映射。

#### SET3-07 设备验证

- [x] 标准宽度文档在 2560×1600 下居中，最大宽度生效（`editor-standard-width.png`）。
- [x] 切换全宽后内容铺满可用宽度（`editor-full-width.png`）。
- [x] 关闭“显示页面信息”后顶栏信息按钮消失，重新开启后恢复。
- [x] 代码块行号在 `line1/line2` 两行代码下显示 1、2 并随内容更新（`editor-code-line-numbers.png`）。
- [x] 日期格式下拉展示 4 种格式（`settings-editor-date-format.png`）。
- [x] 快捷键页显示新增分组（`settings-shortcuts-expanded.png`）。

#### SET3-08 测试与恢复

- [x] `WorkspaceSettingsTest` 增加日期格式与默认值断言；`WorkspacePreferencesTest` 同步字号上限 24。
- [x] 共 222 项单元测试，0 失败；`assembleRelease` 通过。
- [x] 测试后恢复：页面宽度标准、显示页面信息开启、代码块行号关闭、日期自动命名关闭、测试代码块删除。

#### SET3-09 保留差异

- [ ] 新建文档 Ask 模式、页面图标、双向链接、深色主题、滚轮缩放未实现。
- [ ] 连接线粗糙笔触/虚线、Note/文本/思维导图主题默认值未实现。
- [ ] 应用级 Ctrl+K / Ctrl+N / Ctrl+/ 与文档编辑粗体/斜体/标题快捷键未实现。
- [ ] 快捷键自定义未实现。

#### SET3-10 交付

- [x] 发布 APK：`releases/lingye-v0.15-editor-shortcuts-release-20261031.apk`（SHA-256 `D9ED051F000A2BA81395448BD19DFBB72EC514641157B80BE080135C1AABDDCF`）。
- [x] 证据：`settings-editor-general.png`、`settings-editor-page.png`、`settings-editor-date-format.png`、`editor-standard-width.png`、`editor-full-width.png`、`editor-code-line-numbers.png`、`settings-shortcuts-expanded.png`、`settings-editor-whiteboard-defaults.png`。
## 7. 第四轮：编辑器适用化组件与视觉重设计（2026-10-31）

### 7.1 官方编辑器项的适用化映射

| 官方项 | 处理 |
|---|---|
| 白板默认主题 specified/dark/light/auto | 适配为“画布主题：浅色/深色”，深色使用深色底与浅色网格，真实生效 |
| 连接线 rough/scribbled 风格 | 适配为“手绘连线”开关，绘制时对连线添加轻微抖动，不修改数据 |
| 文本默认样式（颜色/字号/对齐） | 适配为“默认文字颜色 / 默认字号 / 默认对齐”，新文本框使用 |
| 页面宽度、显示页面信息、代码块行号、日期格式、字号范围 | 已实现（见第 6 节） |
| AI 设置 | 本机无模型与服务，未提供无效入口 |
| 拼写检查 | Android 输入法负责，本机无独立拼写引擎，不提供伪开关 |
| 中键粘贴 | 触控平板无中键，文本字段沿用系统长按粘贴 |
| 页面图标 | 页面模型暂无图标字段，未提供无法保存的入口 |
| 双向链接 | 无反向链接索引，不提供伪面板 |
| 新建文档 Ask 模式 | 创建入口本身已是文档/白板二选一，不重复询问 |
| 滚轮缩放 | 平板以双指缩放为主，未验证滚轮设备，不提供伪设置 |

### 7.2 视觉与交互重设计清单

#### SET4-01 主题控件

- [x] 外观页移除“跟随系统/深色”禁用分段，改为单一“浅色”状态块，避免不可用选项破坏观感。
- [x] 帮助浮窗说明深色模式未开放的原因。

#### SET4-02 颜色选择器

- [x] `ColorChoiceRow` 从“整块颜色胶囊 + 深色文字”改为白色卡片 + 14dp 色点 + 11sp 深色标签。
- [x] 选中态使用 2dp 主色边框；所有颜色（含墨黑、深灰）标签清晰可读。
- [x] 六色 Frame 背景在 640dp 下仍可辨认。

#### SET4-03 日期格式

- [x] 日期格式从下拉菜单改为内联芯片（4 个格式），不再覆盖相邻设置行。
- [x] 芯片使用浅灰底与主色选中态，窄宽度自动换行。

#### SET4-04 文案与留白

- [x] 缩短编辑器页行内说明（页面宽度、页面信息、代码行号、字号、字体、默认视图、日期命名）。
- [x] 详细解释移入“?”帮助浮窗。
- [x] 右栏内容结束内边距从 56dp 增至 76dp，避免内容滚到关闭按钮下方。

#### SET4-05 代码块行号

- [x] 为代码块设置统一行高（1.2×字号），使行号与代码行基线对齐。
- [x] 行号使用等宽字体与弱化颜色，不抢占正文。

#### SET4-06 画布主题

- [x] 编辑器 > 白板新增“画布主题 浅色/深色”，深色使用 `#1C1D22` 画布与浅色网格。
- [x] 深色主题不改变已有对象颜色，页面数据与导出不受影响。

#### SET4-07 手绘连线

- [x] 连线组新增“手绘连线”开关，绘制路径加入固定相位抖动。
- [x] 仅影响绘制外观，路径数据与命中检测不变。

#### SET4-08 文本默认样式

- [x] 新增默认文字颜色、字号（12–32）、对齐（左/中/右）。
- [x] `addTextBox` 使用设置值创建新文本框；已有文本不受影响。

#### SET4-09 验证

- [x] 外观页主题状态块、画布主题分段、深色画布、日期芯片、颜色卡片、文本默认样式均在 2560×1600 实测。
- [x] 223 项单元测试 0 失败；`assembleRelease` 通过。
- [x] 测试后画布恢复浅色、日期命名关闭、代码行号关闭；未修改用户内容。

#### SET4-10 交付与保留差异

- [x] 证据：`settings-theme-light-only.png`、`settings-canvas-theme.png`、`editor-dark-canvas.png`、`settings-date-format-chips.png`、`settings-color-choices.png`、`settings-text-defaults.png`。
- [x] 发布 APK：`releases/lingye-v0.15-editor-polish-release-20261031.apk`（SHA-256 `A8D616C5D50AF8AF05D1E90BED930CAF63581A4CA91C0168A90D7A55DFE375F7`）。
- [ ] 页面图标、双向链接、Ask 模式、滚轮缩放、AI/拼写检查/中键粘贴未实现或不适用，已在 7.1 说明。
## 8. 第五轮：全局色圈、Photoshop 式取色、笔记样式与按钮缩放（2026-10-31）

### 8.1 全局颜色系统

- 新建 `ColorPicker.kt`：
  - 经典色圈：红/橙/黄/绿/浅蓝/紫/品红/灰/黑/白 10 色，选中显示主色圆环。
  - 自定义色卡：最多 12 个，写入 `ink_preferences` 的 `custom_colors`，设置页与画布工具条共用。
  - 自定义颜色编辑器：大色块 + Hex + RGB/HSV 折叠区（默认展开 RGB，HSV 展开为 Photoshop 式饱和度/明度色域与色相条），滑块显示实时数值；支持保存到色卡与清空色卡。
  - RGB/HSV 转换与色卡去重逻辑提供单元测试。
- 接入范围：设置页全部颜色行（画布背景/卡色/Frame/图形/连线/文字/笔/笔记背景）、画布形状/连线/画笔参数条、图形填充与边框、Frame 背景、文本颜色、笔迹颜色菜单。颜色比较忽略 alpha，高亮笔绘制时再叠加 30% 透明度。

### 8.2 笔记默认样式（官方 edgeless Note 设置）

核对 `editor/edgeless/note.tsx`：背景色、圆角 None/Small 8/Medium 16/Large 24/Huge 32、阴影 None/Box/Sticker、边框 Solid/Dash/None、厚度 2–12（步进 2）。

- 编辑器 > 白板新增“笔记”分组，含样式预览、背景（全局色圈+自定义）、圆角五档、阴影三档、边框样式与厚度滑块。
- 新笔记使用设置的背景色、圆角、边框样式与厚度；阴影按“无/卡片/贴纸”在画布渲染层统一生效。
- 预览卡实时反映当前背景、圆角与阴影，便于确认。

### 8.3 按钮缩放

- 官方“滚轮缩放”改为“按钮缩放”：编辑器 > 白板提供开关，默认开启。
- 画布右下角显示 `−  百分比  +` 胶囊控件；点击百分比复位视图，缩放按钮按 1.25/0.8 步进；演示模式下隐藏。

### 8.4 构建修复

非混淆 release 在大 `CanvasEditor` 方法上触发 ART `VerifyError`（register 类型不匹配）。解决办法：release 启用 R8（`isMinifyEnabled = true`）并加入 `proguard-rules.pro`（`-dontwarn javax.annotation.**`、`org.slf4j.**`）。R8 release 已在模拟器验证启动、设置、画布、取色、Lottie 文件夹图标与缩放控件正常；debug 包不受影响。

### 8.5 验收清单

- [x] 经典色圈与自定义色卡在设置和画布共用。
- [x] 自定义取色器 RGB/HSV 折叠区、Hex、预览、保存/清空色卡。
- [x] 设置页全部颜色入口使用统一色圈与取色器。
- [x] 画布形状/连线/画笔参数条与对象颜色菜单使用统一色圈。
- [x] 笔记背景/圆角/阴影/边框样式/厚度设置与预览。
- [x] 新笔记使用设置的背景色、圆角、边框；阴影渲染生效。
- [x] 按钮缩放开关与画布右下角缩放控件，百分比复位与 +/− 步进。
- [x] 228 项单元测试 0 失败；R8 release 构建与安装验证通过。
- [x] 证据：`settings-color-circles.png`、`color-picker-rgb.png`、`color-picker-hsv.png`、`canvas-color-menu-circles.png`、`settings-note-style.png`、`canvas-zoom-buttons.png`、`settings-note-and-zoom.png`。
- [x] 发布 APK：`releases/lingye-v0.15-color-note-release-20261031.apk`（SHA-256 `13D08D6350865D19E136ED2D68D9F0A405A5073A60353784B9EA8E461A88D058`）。

### 8.6 保留差异

- 自定义色卡跨重启持久化由 SharedPreferences 写入/读取实现；本轮未单独做重启复检（后续可补）。
- 笔记阴影为全局渲染设置，未写入单个笔记对象；官方为每个 Note 存储样式。
- 取色器在参数条内打开为独立对话框；HSV 已提供 2D 饱和度/明度色域与色相条，RGB 采用滑块数值，按 Photoshop 取色面板的分组方式收纳。

## 9. 紧凑控件与笔记预览（2026-10-31）

### 9.1 滑块

`VisualDesign.kt` 的 `CompactSlider` 统一为 26dp 高、3dp 轨道、8dp 端点内缩与 10dp 实心主色圆点，取消 Material 的白色外环和 48dp 最小高度；保留点按定位、拖动、`steps` 步进吸附、禁用透明度与 `progressBarRangeInfo`/`setProgress` 无障碍语义。已替换：

- 编辑器字号、默认文字字号、画笔粗细、笔记边框厚度。
- 取色器 RGB 三通道滑块。
- 图片裁剪左/右/上/下四个滑块。

全局源码中已无 Material `Slider` 调用。

### 9.2 分段选择（按用户提供的官方截图）

- 控件不再 `weight(1f)` 等分整行，改为内容自适应宽度，放在设置行右侧（`SettingsControlRow` 的 240–330dp 控件区）。
- 外层为 `#F4F4F6` 胶囊（3dp 内边距、10dp 圆角、2dp 项间距），选中项为白色 8dp 圆角片（1dp 阴影），选中文字使用深色 SemiBold，未选中为灰色 Medium；不再使用主色文字。
- 已覆盖：笔记圆角（五档）、阴影、边框样式、正文字体、页面宽度、默认打开方式、画布主题、网格大小、连线路径、默认终点、默认线宽、文本对齐；主题行仅保留受支持的“浅色”，不伪造禁用项。
- 小于 460dp 时随 `SettingsControlRow` 堆叠到说明文字下方，胶囊保持内容宽度并左对齐；640dp 下五档圆角完整显示。

### 9.3 其他紧凑按钮

- 主工具按钮 44→40dp，图标 22→20dp，带文字宽度 66→60dp；插画工具按钮 44→40dp，插画 32→28dp。
- 圆形小操作 38→34dp，图标 18→16dp；`IconTextAction` 从 Material TextButton 改为 30dp 高紧凑按钮（16dp 图标）。
- 画布参数 chip 34→30dp，粗细选择点 32→28dp，参数面板圆角 18→14dp、阴影 12→8dp，工具分隔线 27→22dp。
- 设置导航项、下拉框、动作按钮、经典色圈（26→24dp，点击区 30dp）与“+”按钮同步收紧。

### 9.4 笔记预览

- 在“笔记”分组顶部增加“笔记预览”小标题，预览卡补充示例标题“新建笔记”、说明文字与待办行。
- 预览实时反映背景色、圆角、阴影（无/卡片/贴纸）、边框样式（实线/虚线/无）与厚度；虚线使用 `PathEffect.dashPathEffect` 绘制。

### 9.5 验收

- [x] 滑块全局替换并实测设置页、取色器；点按/拖动与步进正常。
- [x] 分段控件在 2560×1600 右对齐、在 640dp 堆叠且五档圆角不裁切。
- [x] 主工具栏、参数条、设置页按钮与色圈紧凑化后无裁切。
- [x] 笔记预览标题、示例内容与背景/圆角/阴影/边框实时预览。
- [x] 228 项单元测试 0 失败；R8 release 安装运行。
- [x] 证据：`settings-compact-segmented.png`、`settings-compact-note-preview.png`、`color-picker-compact-slider.png`、`canvas-compact-toolbar.png`、`settings-compact-narrow.png`。
- [x] 发布 APK：`releases/lingye-v0.15-compact-ui-release-20261031.apk`（SHA-256 `4F4D27BFF099B72747F7497AF7E6468A11B9698DBDAFBE3DDA1E982E5A0DEAC5`）。

## 10. 设置弹窗收紧与开关重设计（2026-10-31）

### 10.1 删除无效的配色方案行

外观页的“主题 / 配色方案 / 浅色”只是单个只读状态块，不提供可切换操作，已连同底部浅色主题说明一起删除；原“主题”分组改为“语言”，仅保留“显示语言”。深色模式仍未实现，不再用无效控件占位。

### 10.2 弹窗边距与内部适配

- 弹窗宽高由 94%×90% 改为 **92%×92% 等比缩小**（宽高使用相同比例，不再单独拉伸某一方向）；圆角 18dp、阴影 16dp，四边留白一致。
- 设置弹窗只改变弹窗自身大小，应用主页面、画布和文字大小保持原样，不调整全局显示比例。
- 侧栏 280→248dp（窄屏 192→176dp）；侧栏内边距 16/18→10/10dp（窄屏 8/10dp）。
- 内容留白：宽屏左/右 48/76→28/44dp，窄屏 24/32→18/46dp；顶部 28→12dp、底部 12→8dp；页脚 16/12→12/6dp。
- 关闭按钮 36→32dp、外边距 14→8dp，增加白色圆形底、1dp 边框与 4dp 阴影；右栏末端预留 44–46dp，640dp 下不再与语言下拉框或开关重叠。

### 10.3 紧凑开关（全局）

新增 `VisualDesign.kt:CompactSwitch`：

- 38×22dp 胶囊轨道，16dp 白色圆形滑块，160ms 颜色过渡与弹簧位移动画。
- 开：黑色 `#0B0B0B` 轨道＋白色滑块；关：`#E9E9EC` 轨道＋1dp `#D5D5DA` 描边＋白色滑块（0.5dp `#B8B8C0` 描边），关闭态在浅色界面上清晰可见。
- 设置页 `SettingsToggleRow` 整行 `toggleable`（`Role.Switch`），点击标题、说明或开关均切换；“?”帮助按钮仍独立打开浮窗。
- 取色器“保存到自定义色卡”同样改为整行可点按 + `CompactSwitch`。
- 修复此前未选中状态使用浅色 `outline` 作为滑块颜色、在 `#F0F1F7` 轨道上几乎不可见的问题；源码中已无 Material `Switch`。

### 10.4 白色卡片式分段切换（全局）

`SegmentedChoice` 按用户最终指定的结构重做：

- 纯白卡片：34dp 高、18dp 圆角、1dp `#E4E4E4` 细描边、5dp 柔和阴影；卡片外框尺寸保持不变。
- 选中项：黑色 `#0B0B0B` 圆角框出现在文字下方，上下仅留 2dp 白边（原先 3dp），文字变为白色 SemiBold；未选中文字保持黑色 `#1A1A1A`。
- 不再绘制任何白色箭头，也不使用黑色底轨或右侧黑色分组；底部始终是完整纯白卡片。
- 日期格式单选 chip 同步为纯白卡片＋黑色选中框；首页列表/卡片视图切换同步为纯白卡片＋黑色选中图标。
- 已覆盖设置页全部 `SegmentedChoice`；640dp 下五档“无/小/中/大/超大”完整显示，无裁切。

### 10.5 验收

- [x] 外观页不再显示配色方案行。
- [x] 设置弹窗按相同宽高比例整体缩小，应用页面保持原样，宽屏与 640dp 均无控件重叠。
- [x] 开关开/关状态清晰，可点击整行或开关切换。
- [x] 分段切换统一为纯白卡片＋文字下方黑色圆角框＋白色选中文字/黑色未选中文字，无白色箭头。
- [x] 228 项单元测试 0 失败；R8 release 安装运行。
- [x] 证据：`settings-white-card-wide.png`、`settings-white-card-narrow.png`、`home-white-card-toggle.png`、`settings-dialog-92.png`、`settings-switch-black-white.png`。
- [x] 发布 APK：`releases/lingye-v0.15-white-card-release-20261031.apk`（SHA-256 `71BC90672E6C6D84A5F5283EF9D4495678835B32B4938AD69963011B0DFDB585`）。

## 11. 极窄窗口与分屏导航（2026-10-31）

### 11.1 触发条件与结构

设置弹窗内容宽度小于 520dp 时（按 92% 弹窗计算，约对应 565dp 以下的窗口），由两栏切换为单栏：

- 列表页：`设置` 标题、右上关闭按钮、工作区卡，以及“常规/工作区”两组分类；导航行高在单栏下增至 44dp。
- 详情页：返回箭头、`设置` 标题、右上关闭按钮；下方显示原分区内容与固定页脚。
- 系统返回：详情页先回列表并保留当前分类，列表页再触发弹窗关闭（`BackHandler`）。
- 宽屏（弹窗内容 ≥520dp）保持原两栏布局，不改变既有视觉、间距与关闭按钮位置。

### 11.2 实现与复用

- 抽出 `SettingsWorkspaceHeader`、`SettingsNavGroups`、`SettingsBackButton`、`SettingsCloseButton`，两栏与单栏共用同一工作区卡、导航、标题与关闭实现。
- 分区内容通过单一 `categoryContent` lambda 渲染，避免窄屏维护第二套设置页。
- 返回箭头用 Canvas 绘制 2dp 圆角折线，与现有线型图标风格一致；关闭按钮保持 32dp 白底圆形。

### 11.3 验收

- [x] 360dp（720×1600）：设置列表、外观、编辑器、键盘快捷键、储存详情均无折行溢出或控件裁切。
- [x] 360dp 首页、文档编辑器正常；白板底栏可横向滚动到末端并显示右侧圆角。
- [x] 540dp（1080×1600）：弹窗内容 497dp，自动进入单栏，详情页控件堆叠正常。
- [x] 640dp（1280×1600）与 2560×1600：保持两栏，关闭按钮与右栏内容不重叠。
- [x] 真实分屏（2560×1600，Chrome + 灵页各 640dp）：首页与设置正常。
- [x] 系统返回先回列表再关闭弹窗；分类选中态在返回列表后保持。
- [x] 228 项单元测试 0 失败；R8 release 安装运行并保留原数据。
- [x] 证据：`settings-compact-nav-360.png`、`settings-compact-appearance-360.png`、`settings-compact-editor-360.png`、`settings-compact-shortcuts-360.png`、`settings-compact-storage-360.png`、`settings-compact-detail-540.png`、`settings-split-640.png`、`settings-two-column-640-regression.png`、`settings-two-column-wide-regression.png`、`narrow-home-360.png`、`narrow-editor-360.png`、`narrow-whiteboard-360.png`、`narrow-whiteboard-toolbar-end-360.png`。
- [x] 发布 APK：`releases/lingye-v0.15-narrow-settings-release-20261031.apk`（SHA-256 `50481C8C1F6F46DD87CC8D3826EF60A1C6C81293AD5ECA92984204AD702C397D`）。

## 12. 分段切换滑动动画（2026-10-31）

### 12.1 实现

- `SegmentedChoice` 由“选中项自身绘制黑底”改为“白色卡片 + 独立黑色指示器”：
  - 每个选项用 `onGloballyPositioned` 记录 `boundsInParent`；指示器按选项 bounds 内缩 3dp / 2dp 绘制，尺寸与旧版黑色胶囊完全一致。
  - 切换时用两个 `Animatable` 并行滑动 `left` 与 `width`，时长 220ms、`FastOutSlowInEasing`；选项文字宽度变化时动画目标随重新测量更新。
  - 首帧在测量完成后直接使用目标位置绘制，避免出现一帧无选中框；文字颜色按指示器中心是否覆盖选项切换白色/黑色，避免滑动过程中白字白底。
- 首页列表/卡片视图切换新增 `AnimatedViewModeToggle`：34dp 黑色指示器在 2dp 内边距的白色卡片内滑动，图标颜色随覆盖切换；静态外观与旧版一致。
- 日期格式 chip 使用 `FlowRow` 换行，跨行滑动语义不适用，保持静态选中。
- 未改变设置项、持久化或布局断点。

### 12.2 验收

- [x] 设置页“文档/白板”切换：10× 慢放下捕捉到黑框位于两个选项之间、文字部分变白的中间帧；终点与选中态一致。
- [x] 首页列表/卡片切换：10× 慢放下捕捉到黑色指示器位于两个图标之间的中间帧；终点正确。
- [x] 静态外观与旧版一致：黑色胶囊尺寸、2dp 白边、15dp 圆角、卡片 34dp 高未变。
- [x] 360dp 下设置分段与首页切换无裁切；640dp、2560×1600 与分屏布局不受影响（布局未改）。
- [x] 系统动画时长恢复 1×；228 项单元测试 0 失败；R8 release 安装运行。
- [x] 证据：`settings-segment-animation-mid.png`、`settings-segment-animation-end.png`、`home-view-toggle-animation-mid.png`、`home-view-toggle-animation-end.png`、`settings-segment-360-animation.png`。
- [x] 发布 APK：`releases/lingye-v0.15-segment-animation-release-20261031.apk`（SHA-256 `DE3D76571907233947FC75D81F1A1EDDBFCB031C0B6450C6E8AD490E466D907F`）。

## 13. 深色界面主题（2026-10-31）

### 13.1 主题设置与持久化

- 外观页恢复“主题”分组，提供“跟随系统 / 浅色 / 深色”三项，使用同一 `SegmentedChoice` 与滑动选中框；默认“跟随系统”。
- `WorkspaceSettings.themeMode` 写入 SharedPreferences 键 `theme_mode`；非法值归一化为 `system`。
- `MainActivity` 启动时先按保存值/系统 `uiMode` 解析主题，再进入 Compose；`Workspace` 监听系统深色变化并更新全局主题状态，避免冷启动闪白。

### 13.2 颜色令牌

- `LingyeColors` 由固定颜色改为动态令牌，按 `dark` 状态返回浅色或深色值：Ink、Muted、Primary、PrimarySoft、Mint、Coral、Canvas、Line、Background、Surface、SurfaceHigh、Sidebar、SettingsSidebar、Subtle、SubtleAlt、Chip、ModeSwitchBg、ButtonSoft、ControlSelected、SliderTrack、GridDot、NavSelected、SegmentSelected、OnSegmentSelected、SegmentInk、SegmentBorder、Switch*、Tooltip、Error/ Success/ Danger、SurfaceBlue、SurfaceSoft、PreviewBg、LoadingGradient* 等。
- `LingyeTheme` 在 `lightColorScheme` / `darkColorScheme` 之间切换，并同步系统栏图标明暗（`isAppearanceLightStatusBars` / `isAppearanceLightNavigationBars`）。

### 13.3 覆盖范围

- 设置弹窗：侧栏、内容、分段切换、开关、下拉框、帮助浮层、页脚、关闭按钮、取色器对话框。
- 首页：侧栏、列表/卡片、排序/新建、视图切换、空状态插画、选择框、滑动操作、多选提示。
- 文档：标题/正文、块工具条、页面菜单、提示横幅。
- 白板：工具栏、参数条、浮动对象工具条、缩放控件、全屏按钮、Frame/链接卡片与 Frame 标题对比度。
- 其他：概览页、加载页、AlertDialog、数据库看板/列表、图片裁剪预览。

### 13.4 保留差异与验收

- 画布内容底色仍由“编辑器 → 白板 → 画布主题”单独控制，未新增“跟随界面”选项；深色界面下浅色画布仍可用。
- 笔记/卡片等用户内容保留其创建时颜色；深色界面不反转用户内容颜色。
- [x] 浅色主题与改动前一致；深色在宽屏与 360dp 下首页、文档、白板（浅色/深色画布）、设置、取色器、概览、卡片与加载页可读。
- [x] “跟随系统”在系统深色/浅色间双向切换；手动深色强停重启保持；R8 release 安装运行。
- [x] 229 项单元测试 0 失败（新增主题归一化测试）。
- [x] 证据：`dark-theme-home-wide.png`、`dark-theme-settings-wide.png`、`dark-theme-editor-settings.png`、`dark-theme-whiteboard-settings.png`、`dark-theme-canvas-light-bg.png`、`dark-theme-canvas-dark-bg.png`、`dark-theme-overview.png`、`dark-theme-cards.png`、`dark-theme-color-picker.png`、`dark-theme-storage.png`、`dark-theme-settings-360.png`、`dark-theme-appearance-360.png`、`dark-theme-system-dark.png`、`dark-theme-system-light.png`、`dark-theme-loading.png`、`light-theme-home-wide.png`、`light-theme-canvas.png`。
- [x] 发布 APK：`releases/lingye-v0.15-dark-theme-release-20261031.apk`（SHA-256 `207232B3EFB6D45124C3C5FBBAB1CEBA2226AD63C8EF59E9BBE7B63A2236762D`）。

## 14. 应用级与文档编辑快捷键（2026-10-31）

### 14.1 应用快捷键

- Ctrl+K：显示侧栏、退出画布专注模式并聚焦“快速搜索”；紧凑窗口会打开侧栏抽屉。
- Ctrl+N：保存当前页后新建文档并打开；Ctrl+Shift+N：新建白板并打开。
- Ctrl+,：打开设置弹窗。
- 事件由 `MainActivity.onKeyDown` 交给 `Workspace` 注册的处理器，因此首页、文档、白板都能使用；设置弹窗打开时只忽略新建/搜索，避免覆盖弹窗内容。macOS 外接键盘的 Command 键等价于 Ctrl。
- 应用快捷键为固定映射，自定义键位仍未提供。

### 14.2 文档编辑快捷键

文本块获得焦点时：

| 按键 | 行为 |
|---|---|
| Ctrl+B | 切换当前块粗体 |
| Ctrl+I | 切换当前块斜体 |
| Ctrl+U | 切换当前块下划线 |
| Ctrl+Alt+1–6 | 转为 H1–H6 |
| Ctrl+Alt+0 | 转回正文 |

- 仅对可格式化/可转换的块生效（代码块、分隔线等不响应）；格式写入块数据并进入页面撤销历史。
- 为使标题转换后继续输入，`BlocksEditor` 的块 key 由“索引 + 类型”改为仅索引；标题、正文转换时文本框不再重建，焦点保持。
- 纯映射集中在 `WorkspaceShortcuts`，并有单元测试覆盖应用/文档键位与标题级别。

### 14.3 设置页与验收

- 快捷键页新增“应用”分组（Ctrl+K/N/Shift+N/,）和“文档编辑”分组（Ctrl+B/I/U、Ctrl+Alt+1–6/0），保留原有白板工具、命令、对象操作与选择编组分组。
- 页面底部说明仍注明“快捷键为固定映射，自定义绑定尚未提供”。
- [x] 模拟器实测 Ctrl+K 聚焦搜索并过滤、Ctrl+N 新建文档、Ctrl+Shift+N 新建白板、Ctrl+, 打开设置。
- [x] 在 AlphaRenamed 段落块实测 Ctrl+B/I/U、Ctrl+Alt+1 与 Ctrl+Alt+0，转换后焦点保持；测试后块格式恢复。
- [x] R8 release 实测 Ctrl+K 与 Ctrl+,；232 项单元测试 0 失败。
- [x] 测试创建的两个空白“未命名页面”（11:53）已按用户指示移入回收站，文档数回到 191；回收站现有 4 项（含原有 2 项）。
- [x] 证据：`app-shortcut-search-focused.png`、`app-shortcut-search-filter.png`、`app-shortcut-settings.png`、`app-shortcut-new-document.png`、`app-shortcut-new-whiteboard.png`、`doc-shortcut-bold.png`、`doc-shortcut-italic.png`、`doc-shortcut-underline.png`、`doc-shortcut-heading1.png`、`doc-shortcut-paragraph.png`、`settings-shortcuts-app-document.png`。
- [x] 发布 APK：`releases/lingye-v0.15-shortcuts-release-20261031.apk`（SHA-256 `7DAEF1AA2C5D4FF2EEF6803E976C4A7E53CBE7A90EA90F37DB6056D249FD5239`）。

## 15. 页面图标与双向链接（2026-10-31）

### 15.1 页面图标与格式 23

- 新增 `Page.icon`（emoji 短字符串）与 `PageIcons`（24 个候选、合法性校验）。
- 页面格式 22 → 23：v22 字段不变，文件末尾追加图标字符串；读取兼容 1..23，旧页面图标为空并在下次保存时升级。
- 入口：编辑页顶栏图标（有图标时显示）或页面菜单“更改图标”；选择器支持 24 个 emoji 与“移除图标”。
- 展示：侧栏页面行、首页列表/卡片、编辑页顶栏、页面信息弹窗；无图标时按页面内容显示文档/白板线型图标。
- 侧栏 LazyColumn 项目 key 纳入图标与标题，保证选择/移除后立即刷新。
- 迁移、回滚、备份证明与 v22 兼容测试见 `PAGE_FORMAT_23.md`。

### 15.2 反向链接

- `PageLinks.backlinks(target, pages)`：返回未回收且包含指向当前页链接卡片的来源页。
- 侧栏在“链接的文档”后显示“反向链接”分组，行内显示来源页图标与标题，点击走现有链接返回逻辑。
- 页面信息弹窗增加“反向链接 · N”，列出前 6 个来源，可直接跳转。
- 反向链接随页面列表与 revision 实时计算；删除链接后分组立即消失。

### 15.3 验收

- [x] v22 页面在格式 23 构建中正常读取；设置 🚀 后顶栏、侧栏、首页即时显示，强停重启保持；移除后即时恢复；页面文件首 4 字节为 23。
- [x] AlphaRenamed → AlphaPage 测试链接显示“链接的文档”；目标页显示“反向链接 / AlphaRenamed”，点击返回来源；测试链接已通过撤销移除。
- [x] v22 数据备份：195 页、12 个历史文件，tar SHA-256 `985D6F4AEA331D0E1BAEB50DD22BED94E30A49B8EA1DD04C6022975268BE0B0D`，保存于 `evidence/format22-backup.tar`。
- [x] 236 项单元测试 0 失败；R8 release 安装并读取 v23 数据。
- [x] 证据：`page-icon-picker.png`、`page-icon-header.png`、`page-icon-list-sidebar.png`、`page-icon-removed.png`、`backlinks-source-sidebar.png`、`backlinks-target-sidebar.png`、`backlinks-jump.png`、`page-format23-home.png`、`page-format23-about.png`。
- [x] 发布 APK：`releases/lingye-v0.15-page-icons-backlinks-release-20261031.apk`（SHA-256 `C4064B8BD7312189EA6AF3F10A04503DE21C36DE0601901066B8E2662D04EF9E`）。

## 16. 单笔记阴影与圆角（2026-10-31）

### 16.1 单笔记样式

- `Shape` 新增 `shadowStyle`（`none` / `box` / `sticker`），页面格式 23 → 24；旧页面缺省为 `box`，与默认全局设置一致。
- 圆角继续使用已有的 `Shape.cornerRadius`；新建笔记写入当前全局 `noteCornerRadius`，不再依赖渲染时读取全局。
- `Page.addCard` 新增 `shadowStyle` 参数，调用方传入当前全局 `noteShadow`；背景、圆角、边框样式和厚度同样在创建时写入新笔记。
- 选中笔记工具条的“图层与更多”菜单内新增“笔记样式”分组，接在四个图层操作之后：
  - 阴影：无阴影 / 卡片阴影 / 贴纸阴影，当前项带 ✓。
  - 圆角：无 / 小 / 中 / 大 / 超大（0/8/16/24/32），当前项带 ✓。
- 修改直接写入该 Shape 并调用 `changed()`，进入页面历史；之后修改全局设置不会影响已有笔记。

### 16.2 设置页说明

- “圆角”说明改为“新建笔记的圆角”，帮助补充“选中已有笔记后，可在其工具条的‘图层与更多’中单独调整圆角”。
- “阴影样式”说明改为“新建笔记的阴影样式”，帮助从“对画布上的笔记统一生效”改为“只影响新笔记；选中已有笔记后，可在其工具条的‘图层与更多’中单独调整阴影”。
- 关于页页面格式改为 24。

### 16.3 迁移与验收

- [x] 安装格式 24 前导出 v23 数据 tar：195 页、13 个历史文件，480,768 字节，SHA-256 `C40F362740A1D42263DEA2E45D9DE6D8076AFD7003C88A6D3AE5698DD261B7F4`，保存于 `evidence/format23-backup.tar`。
- [x] 模拟器：新建笔记默认卡片阴影/中圆角；改为贴纸阴影＋超大圆角后强制重启保持；页面文件首 4 字节为 24；测试笔记已删除，画布恢复。
- [x] 239 项单元测试 0 失败；R8 release 安装后设置页、白板与“笔记样式”分组正常，测试笔记已删除。
- [x] 证据：`note-style-card-toolbar.png`、`note-style-menu-merged.png`、`note-style-sticker-huge-final.png`、`note-style-persisted-final.png`、`note-style-menu-persisted.png`、`note-style-restored-final.png`、`note-style-settings-help-corner.png`、`note-style-settings-help-shadow.png`、`page-format24-about.png`。
- [x] 发布 APK：`releases/lingye-v0.15-note-styles-release-20261031.apk`（SHA-256 `52DB296C07FB0B09C1B97180E24A5258C4BF2741B26D687A05097F0C6DE7E398`）。