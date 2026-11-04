# AFFiNE Android 平板差异分析

> 2026-10-30 补充核对：本文保留了多轮历史分析，部分表格与排序已落后于 0.15 的增量实现，不能单独作为当前状态。审计质量、界面对照及保守修订见 [补充核对](2026-10-30-followup-review.md)，逐项状态以 `FEATURE_MATRIX.csv` 为准。尤其多文件夹、Tags、Favorites、页面撤销持久化和连接线端点已有部分实现，Back 退出问题已有针对性修复证据；仍须按验收条件补齐边界。

## 1. 结论先行

当前灵页 0.15 是一个可运行、可持久化的本机文档/画布工作区，不是 AFFiNE 的功能等价实现。

基于 148 个原子功能：[FEATURE_MATRIX.csv](FEATURE_MATRIX.csv) 当前为：

- `VERIFIED` 21
- `PARTIAL` 84
- `MISSING` 38
- `BROKEN` 0
- `UNVERIFIED` 5

这不是完成率分数。`PARTIAL` 和 `UNVERIFIED` 不能合并成“已完成”；仅 UI、字段或空事件也不能升级为 `VERIFIED`。

最重要的事实差异是编辑内核：

- AFFiNE Android 是 Capacitor/WebView，内容能力由 BlockSuite/Yjs 驱动。
- 灵页是原生 Compose，自已实现 `Page`、`Block`、数据库、画布和文件格式。
- 因此 AFFiNE 的块树、同步文档、行内属性、Frame、Mind Map、关系数据、CRDT、协作权限等能力不能从相似 UI 自动推导。

## 2. 官方行为、当前实现、差异、影响和建议

### 2.1 工作区组织

| 官方行为 | 当前实现 | 差异 | 影响 | 建议 |
|---|---|---|---|---|
| Docs、Collections、Tags 顶层导航；页面信息面板可管理 Tags | Docs 级页面列表、单值 `Page.folder`；没有 Collections/Tags | 信息组织维度只有单层文件夹 | 页面变多后无法组织、筛选和检索关系 | 先新增稳定页面属性模型，再实现 Tags/Collections；不要继续给页面列表堆按钮 |
| 一个文档可属于多个文件夹 | `Page.folder: String` | 关系被压成单值 | 改数据结构才能满足；直接改名或加 UI 会迁移失败 | 版本化迁移到 `folderIds` 或通用属性表，并保留旧格式读取 |
| Favorites、Trash、Import、Template、Search 形成独立工作区能力 | 有搜索、回收站、本机备份、有限模板 | 缺收藏、标准导入、完整模板中心、字典/大纲 | 导航数量和页面规模提升后效率低 | 先做统一命令/导航服务，再接页面属性 |
| Journal、Bi-Directional Links | 只有日期自动标题；无反向链接 | 名称接近，语义完全不同 | 不能视为 Journals 或关系图已实现 | 建立日期索引和引用索引后再做 UI |

### 2.2 文档 Block 模型

| 官方行为 | 当前实现 | 差异 | 影响 | 建议 |
|---|---|---|---|---|
| BlockSuite 有 paragraph、heading、list、todo、quote、code、latex、table、image、attachment、bookmark、embed、callout、divider、database、linked-doc 等模块 | `Page.blocks` 是扁平 `Block(type, content, checked, bold, italic, underline)`；已有单层列表、引用、代码和分隔线 | 没有块树、子块、行内节点、代码语言、schema 和通用属性 | 只增加 `when(type)` 无法获得官方行为，且会持续扩张单体文件 | 先做编辑器架构决策：评估原生富文本数据模型，或隔离 BlockSuite Web 子模块并定义 Android 桥 |
| 文本支持行内选区样式、链接、mention、reference、comment、footnote | 普通块只有整块三个布尔样式；共享块有 span | 富文本能力碎片化，普通文档仍是简化表单 | 用户核心写作体验低于 AFFiNE | 统一行内 rich-text 模型，先替换普通段落/标题，不允许两条格式实现长期并存 |
| Slash menu、拖拽手柄、块多选、块级快捷键 | `/` 菜单可按中英文过滤并触控创建 8 种块；扁平块支持长按手柄拖拽、落点提示和撤销；底部“更多块”仍保留 | 尚无斜杠菜单键盘导航、边缘自动滚动、嵌套拖拽和多块选择 | 长文档与嵌套内容效率仍低于 BlockSuite | 先补菜单键盘导航与拖拽边缘滚动；P2 块树和多块移动 |
| 文档静态 Table | 数据库 table 视图不是文档 Table | 用户无法插入简单静态表格 | 内容表达缺口，数据库被误用 | 新增独立 `table` block，不复用 `LocalDatabase` |

证据：

- 官方模块清单见 `REF-BLOCKSUITE-GIT` 的 `packages/affine/all/package.json`。
- `MainActivity.kt:BlocksEditor` 只为少数类型分支渲染。
- `Page.kt:Block` 没有 span、children、props 或 inline attribute。
- `SharedBlocks.kt:SharedSpan` 只覆盖共享块。

### 2.3 页面与无界画布

| 官方行为 | 当前实现 | 差异 | 影响 | 建议 |
|---|---|---|---|---|
| BlockSuite surface、Frame、Group、Mind Map、Note、Text、Shape、Connector、Brush、Image、Attachment、Bookmark、Embed、Linked Doc | 自研 shape/image/stroke/connector 列表 + page card | 有核心视觉原型，但没有等价对象模型和关系树 | 选中工具栏、复制、图层、样式和迁移会按对象类型分叉 | 建立统一画布节点接口/属性表，再补 Frame/Mind Map/Linked Doc |
| 对象上下文工具按类型组合 | 图形、卡片、文本、图片、笔迹、连线分别硬编码 | 动作集合不一致，官方动作缺失 | 用户在不同对象间需要学习不同规则 | 用声明式 registry 管理对象能力、排序、启用条件和收纳 |
| 2 个起止端点各有 5 种样式，可翻转方向，连接线可加文字 | 已有独立 `startMarker`/`endMarker`、三种基础样式、翻转和中点标签；仍缺 diamond/triangle 等完整样式 | 基础流程表达已补齐，完整官方样式矩阵仍不足 | 高级图表的端点语义仍有差异 | 补剩余端点图形和标签行内样式 |
| Note 可原位编辑、切换 Page/Edgeless 可见性、Slicer、尺寸 | 卡片可原位编辑；没有 displayMode/Slicer | 编辑形态改善，但画布内容与文档块仍非同一模型 | 图文混排与文档大纲不一致 | 先决定 Note 是否绑定 Block 子树，再实现显示模式 |
| AFFiNE Issue #14333 仍反映大画布性能问题 | 有视口裁剪、图片缓存和 10 页性能基线 | 没有 100/1000 对象、长时间手写、接近容量上限证据 | 无法声称复杂画布性能达标 | 在功能扩张前建立大型画布基准和回归阈值 |

已实际确认：

- 点阵背景、单矩形选中态、形状上下文工具条、More Tools 分类、全屏均能在模拟器显示。
- 选中框有明确视觉反馈，不属于“没有选中设计”。
- 该验证只覆盖一个矩形和当前窗口；不能推出所有对象类型或旋转/分组状态都正确。

主要缺口：

- Frame section。
- Mind Map。
- 图形内文字。
- 自由旋转。
- 高亮笔。
- 压力/倾斜与掌托策略。
- Linked Doc / Embed / Attachment / Bookmark 画布对象。
- Copy as Image / Markdown / block link。
- Note 的 Page/Edgeless 可见性和 Slicer。
- 响应式工具栏在窄分屏/大字号下的可用性。

### 2.4 数据库

| 官方行为 | 当前实现 | 差异 | 影响 | 建议 |
|---|---|---|---|---|
| 多字段类型、关系、人员、标签、公式、多级筛选/排序、记录详情、视图配置 | 5 种字段；简单 substring filter；单字段排序；table/board/list/calendar | 只能承担轻量数据库 | 复杂项目管理和关系数据不可用 | 先扩展 schema/字段注册表，再逐个实现类型 |
| Board 拖拽、Calendar 直接编辑 | 看板移动用“下一列”按钮；日历只读 | 与 AFFiNE 核心交互差异大 | 平板触控工作流低效 | 先补 Drag/Drop 命中、取消和键盘替代，再接状态写回 |
| 关联文档标题参与数据库搜索 | 无关联文档字段 | 无关系索引 | 搜索会漏掉外部关系 | 与 Tags/Collections 共用关系索引 |
| 单条 malformed cell 不应拖垮搜索 | 当前 codec 严格校验，理论上阻止异常值 | 当前更“严格”，但没有坏数据隔离和搜索降级测试 | codec 外异常仍可能中断列表 | 对每行/每字段做解析隔离并记录诊断 |

数据库的主要风险不是 UI 数量，而是 `LocalDatabase` 整个对象编码到一个 `Block.content` 字符串。每次字段编辑会重新编码整个数据库，数据量变大后可能使保存和 UI 更新变慢。

### 2.5 同步、协作、版本与 AI

当前 Manifest 没有 `INTERNET` 权限；没有网络层、账号、权限、同步队列、版本存储或 CRDT。

矩阵仍把 AFFiNE 的以下能力列为 `MISSING`：

- 登录与工作区；
- 云同步；
- 实时协作和远端选区；
- 持久版本历史；
- 分享与角色权限；
- AI 与外部连接器。

“当前没有实现”不等于“本项目约定不需要”。如果产品确实只做离线版，应通过用户确认把相应条目改为 `NOT_APPLICABLE`，并同步修改验收范围；不能在审计里自行缩小范围。

### 2.6 Android 生命周期与输入

#### 已修复：系统 Back

原失败步骤：

1. 打开白板。
2. 激活页面卡片原位编辑。
3. 按 Android 系统 Back。
4. 应用曾退到桌面，而不是先关闭编辑/键盘或返回上一编辑层。

修复后的源码事实：

- `MainActivity` 增加顶层 `BackHandler`，`CanvasEditor` 增加画布级返回优先级。
- `BackNavigationPolicy` 按 overlay → 原位编辑 → 全屏 → 设置/概览 → 选择/工具 → 画布 → 应用退出处理。

验收：

- 模拟器中原位编辑时第一次 Back 收键盘，第二次 Back 清除编辑态，应用保持前台。
- 当前状态从 `BROKEN` 提升为 `VERIFIED`；仍建议补 instrumentation 测试覆盖复杂弹窗组合。

#### 会话恢复

修复后强停并重启：

- 语言保持。
- 页面内容仍在。
- 页面 ID、Document/Whiteboard/Overview/Settings 视图、当前工具、画布平移偏移和稳定 ID 选区会恢复。
- 滚动位置、旋转和 Don't keep activities 尚未系统验证。

状态由 `MISSING` 提升为 `PARTIAL`。

#### 输入法、手写笔、旋转/分屏

- AFFiNE Android 有专用 `AffineImeBridge` / `AffineInputConnection`，处理 WebView 组合输入和删除回放。
- 灵页依赖 Compose 标准 TextField，没有专项 IME 桥。
- 灵页的 stylus-only 通过 `PointerType` 区分指针；无压力或掌托模型。
- 当前没有 rotate/split screen/Don't keep activities 自动样本。

### 2.7 数据可靠性

已验证的强项：

- `AtomicFile` 页面写入。
- 单页解码失败不隐藏其他页面（`REL-003`）。
- 页面、文件夹、模板、共享块独立加载失败（`REL-004`）。
- ZIP 路径、重复条目、容量和图片缺失拒绝（`REL-005`、`REL-006`）。
- 共享块历史持久化（`REL-007`）。

仍有风险：

- `PageHistory` 纯内存，页面 undo 跨切页/重启丢失（`REL-008`）。
- 自研格式版本 20 没有统一迁移 API、历史 fixtures 和降级策略（`REL-009`）。
- 编辑提交路径先在组合线程调用 `page.encode()`，之后才在 IO 写盘（`REL-011`）。
- 没有 CRDT 或离线并发（`REL-010`）。
- 没有大数据/长时间故障注入基线（`REL-012`）。

影响：

- 大页面可能卡顿。
- 桌面/平板在高频编辑时更易丢交互。
- 格式膨胀后每个后续功能都要改人工条件分支。

## 3. 重大架构问题

### A. 原生 Compose 自研编辑器 versus BlockSuite

这是最高层风险，不只是代码结构问题。

- 文档块、画布、数据库在 AFFiNE 中共享 BlockSuite store/Yjs 和块模型。
- 灵页将它们聚合进一个可变 `Page` 类，UI 直接修改属性，数据库另用 Base64 嵌套。
- 继续按按钮补功能会扩大 `when(type)`、对象 ID 和历史规则的组合数。

建议在下一阶段先做一个“编辑内核决策”：

1. 原生路线：定义稳定 Block/Inline/Property schema，约束迁移和 UI registry。
2. Web/BlockSuite 路线：评估在原生外壳内嵌入 BlockSuite WebView 和桥接，满足离线/手写/备份。
3. 混合路线：文档使用 Web/BlockSuite，画布保留原生；必须明确数据同步边界。

在没有决定前，不应继续大规模扩展块类型。

### B. `MainActivity.kt` 单体耦合

- 约 1,463 行，UI、导航、保存、菜单、手势、模板、数据库编辑、错误状态和对话框均在少数 composable 中。
- 缺少 ViewModel、导航状态和对象 action registry。
- 一个状态变化容易触发整页重组，测试也难定位。

建议拆分：

- WorkspaceShell / Navigator
- DocumentEditor + BlockRegistry
- CanvasSurface + ObjectToolbarRegistry
- DatabaseEditor + FieldTypeRegistry
- PersistenceCoordinator + BackgroundSaveQueue
- SettingsViewModel

### C. 页面格式版本化

`Page.decode` 现在通过 `if (version >= N)` 顺序读取，新增字段必须精确维持历史读取分支。数据库和共享块又有独立版本。

建议：

- 建立页面、数据库、共享块三条迁移测试 fixture 链。
- 引入 `DocumentSchemaVersion` 和显式迁移函数。
- 弃用字段先读后写，不直接删除。
- CI 每次校验全部历史样本。

### D. 保存/历史不是事务一致

- 页面历史内存化。
- 文件夹操作有 journal，但页面、模板和历史之间没有统一事务。
- 自动保存只在 IO 写盘阶段切线程。
- 备份恢复在部分目录写失败时有补偿，但缺少完整故障注入。

建议先做 persistence 边界与 write queue，再扩展功能。

### E. UI 响应式与无障碍

- 当前主要按 2560×1600 横屏平板验证。
- 底部工具栏宽度、弹窗、上下文工具条、顶栏和设置页没有全部按窗口类别测试。
- Canvas 是直接绘制层，TalkBack 不能按对象语义浏览。

建议启用 Window Size Class，定义 Compact/Medium/Expanded 三套布局；画布对象至少暴露可选择、可操作语义。

## 4. 严重性排序

### P0

1. `AND-004` 系统 Back 直接退出。
2. `AND-003` 进程死亡后没有页面/模式/工具恢复。
3. `REL-008` 页面 undo 不持久。
4. `REL-011` 大页面序列化仍在主线程。
5. `REL-009` 多格式版本迁移缺少统一 fixtures。
6. `REL-012` 大数据与长时间性能未知。
7. `UI-011` 工具栏在窄分屏/大字号下可能溢出。

### P1

1. 文档行内富文本、Slash Menu、块拖拽/多选。
2. Frame、Mind Map、Linked Doc、图形内文字。
3. Connector 起止端点完整矩阵和标签。
4. 页面多文件夹、Tags 和搜索索引。
5. 数据库自定义分组、类型、记录详情、拖拽。
6. TalkBack 与 IME/手写笔真实设备验收。

### P2

1. Collections、Favorites、Journals、永久删除。
2. 标准 Markdown/HTML/PDF 导入导出。
3. 更多 Shape/Block、Callout、Code、LaTeX、Attachment。
4. 复制为图片/Markdown/链接和高级对齐。
5. 页面级主题、页宽、自定义属性。

### P3/P4

1. 云同步、账号、版本历史。
2. 实时协作、分享/权限、评论。
3. AI、外部连接器、自托管。

## 5. 已确认、推断和未知

### 已确认事实

- 当前构建、147 项单测通过（最终以 XTest XML 为准）。
- 中英文切换、持久化、设置页、点阵画布、全屏、矩形选择与上下文工具条在模拟器可操作。
- 系统 Back 在原位编辑中不再退出应用。
- Whiteboard 模式和当前工具在强停重启后恢复。
- 页面 undo 历史在强停重启后仍可撤销，连续输入按一次编辑撤销。
- 已有 Tags、Favorites 和多文件夹基础本机实现，并随页面保存。
- 当前仍没有 Collections、Journals、Frame、Mind Map、Linked Doc、CRDT、同步、版本历史或 AI 实现。
- AFFiNE 实际产品可同时属于多个文件夹并管理 Tags，文档 Info 有 Journal/Template/Page width/Edgeless theme 等属性。

### 有源码依据的推断

- 会话状态目前由 SharedPreferences 恢复，未使用 SavedStateHandle；更复杂 UI 状态仍需统一容器。
- 自动保存已在主线程生成隔离快照，并把最终 `page.encode()`、历史编码和写盘移到后台；显式保存/onStop 仍可能同步编码，大数据阈值尚未测。
- 单值 `folder` 和没有关系字段使多文件夹与双向链接无法通过简单 UI 补齐。
- `MainActivity.kt` 的集中状态会随块/对象类型增加而放大重组和回归风险。

### 未知

- AFFiNE 文档站在 2026-10-30 被 Cloudflare 阻挡的页面详情。
- 真实 Android App 的全部设备专属菜单和输入法行为。
- 三星/联想/小米手写笔的压感、掌托、延迟。
- 窄分屏、折叠屏、屏幕旋转和系统大字号。
- 大画布、长文档、接近备份上限时的性能。
- 当前项目 Git 提交基线。
