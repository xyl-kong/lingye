# AFFiNE Android 平板功能清单

本清单是 `FEATURE_MATRIX.csv` 的可读总览。原子条目的交互、状态、依赖、验收条件和证据以 CSV 为准。

## 1. 范围和方法

本次范围不局限于当前项目已有页面，而以 AFFiNE Web/Android 的可观察产品能力为参照，覆盖：

- 工作区导航、组织、搜索、模板和页面属性；
- 文档块、富文本、嵌入、引用和文档历史；
- 无界画布对象、选择态、上下文工具、笔画、连接线、模板和性能；
- 数据库字段、视图、筛选、排序和记录交互；
- 同步、协作、版本、分享与 AI；
- Android 生命周期、系统返回、输入法、手写笔、旋转和全屏；
- 本地持久化、迁移、备份和故障隔离；
- 设置、语言、视觉图标和响应式布局。

功能拆分原则：

1. 一个条目只验证一个可单独失败的用户结果。
2. 同一对象类型的“可见 UI”和“数据成功保存”分开判断。
3. 主流程成功不能推出边界条件成功。
4. 共享块不能证明普通文档块有行内富文本。
5. 数据库表格不能替代文档静态表格。
6. 自动日期标题不能替代 AFFiNE Journals。
7. 数据库中的内嵌可编辑网格不能证明 AFFiNE `Table` block 已实现。

## 2. 数量汇总

共有 148 个原子功能：

| 一级模块 | 数量 |
|---|---:|
| 工作区 | 20 |
| 文档 | 24 |
| 无界画布 | 43 |
| 数据库 | 18 |
| 同步与协作 | 7 |
| Android | 11 |
| 数据可靠性 | 12 |
| 设置与视觉 | 13 |
| 合计 | 148 |

状态统计：

| 状态 | 数量 |
|---|---:|
| VERIFIED | 21 |
| PARTIAL | 84 |
| MISSING | 38 |
| BROKEN | 0 |
| UNVERIFIED | 5 |
| NOT_APPLICABLE | 0 |
| 合计 | 148 |

## 3. 当前技术栈与架构事实

- `app/build.gradle.kts`：Android app 插件、Kotlin Android、Compose；依赖只有 Compose BOM、Activity Compose、Material 3、UI tooling 和 JUnit。
- `MainActivity.kt:MainActivity`：唯一 Activity，`onStop()` 调用注册的同步保存函数。
- `MainActivity.kt:Workspace`：持有页面、选择、工作区设置、搜索、文件夹、模板、共享块、撤销历史和多个弹窗状态。
- `MainActivity.kt:BlocksEditor`：文档块 UI 和事件处理。
- `MainActivity.kt:DatabaseEditor`：数据库 UI。
- `MainActivity.kt:CanvasEditor`：无界画布绘制、手势、工具、对象工具条和模板 UI。
- `Page.kt:Page`：页面、文档块、图形、连接线、图片、笔迹、图层和模板的单一模型。
- `PageStore.kt:PageStore`：私有目录、`AtomicFile`、文件夹事务和备份读写。
- `BackupArchive.kt`：自定义 ZIP 格式 `lingye-backup-2`（兼容 `lingye-backup-1`），可选 `metadata.dat` 保存文档属性/Journal/模板标记。
- `PageHistory.kt`：内存字节快照 undo/redo。
- `LocalDatabase.kt`：独立 Base64 二进制数据库块，格式版本 4。
- `SharedBlocks.kt`：本机共享文字/图片/数据库和独立历史，格式版本 3。
- 页面格式：22；没有外置 schema 迁移工具。
- 未发现 ViewModel、SavedStateHandle、Room、WorkManager、网络客户端、同步队列、CRDT 或依赖注入容器。

代码尺寸事实：

- 27 个主源码文件，约 3,833 行。
- `MainActivity.kt` 仍是最大的 UI/业务混合文件。
- 当前实现新增页面/模式/工具/画布视口/对象选区恢复、持久页面历史、多文件夹、Tags 和 Favorites 元数据。
- 当前为 33 个测试文件、147 个 testcase。
- 没有 `androidTest`。

## 4. 已确认可用的基础能力

这些条目在矩阵中不是全部为 `VERIFIED`；部分仅达到 `PARTIAL`。

- 中英文切换在设置页，选择跨重启保存；英文设置页和画布页在 2560×1600 模拟器中未发生横向越界。
- 工作区概览从本地页面计算页面数、字符、对象数、图片、模板、存储和热力图。
- 文档/白板模式可切换，标题、正文和画布对象保存在同一 `Page`。
- 画布有方阵圆点背景，背景随 `scale`/`pan` 计算相位。
- 当前实测可选中矩形并看到附近上下文工具条；选中框由白色外描边、紫色主线和四角控制点组成。
- 连接线可分别设置圆点、箭头、三角、菱形或无端点，支持翻转方向和可编辑中点文字。
- More Tools 已使用“选择操作 / 基础图形 / 内容 / 视图”分类。
- 全屏隐藏侧栏、页头和系统栏，右上角保留退出按钮。
- 设置页有外观、编辑器、画布、数据、关于五个分类；图标路径声明来自 `@blocksuite/icons 2.2.17`。
- 页面使用 `AtomicFile`；损坏页面加载隔离、并行启动错误隔离、备份路径校验、共享块历史和页面历史有自动测试。
- 中英文切换、白板/视口/选区会话恢复、原位编辑 Back、紧凑屏工具栏、标签持久化和颜色连续性已在模拟器逐项验证。

## 5. 功能清单导航

### 5.1 工作区

`WS-001` 至 `WS-019`：

- 文档/白板模式、会话恢复、新建、标题、搜索；
- 文件夹、多文件夹、移动；
- Collections、Tags、Favorites（Tags/Favorites 已有基础本机实现）；
- 回收站、恢复、永久删除；
- 本机备份、Markdown 导入/导出；
- 概览、模板、Journals、大纲与双向链接。

主要源码：`MainActivity.kt`、`WorkspaceCatalog.kt`、`PageStore.kt`、`TemplateCatalog.kt`、`WorkspaceOverview.kt`。

### 5.2 文档

`DOC-001` 至 `DOC-023`：

- 标题、段落、标题块、行内富文本；
- 列表、待办、代码、公式、静态表格；
- 图片、附件、书签、嵌入、Callout、分隔线；
- 斜杠菜单、拖拽手柄、多块选择；
- undo/redo、剪贴板；
- 块引用/同步文档、行内链接/提及、评论/脚注；
- 文档 Info 属性。

关键差异：当前 `Block.type` 是可扩展字符串，但 UI 只针对 `paragraph`、`heading`、`todo`、`image`、`database`、`shared` 实现；普通块没有行内 span 数据。

### 5.3 无界画布

`CV-001` 至 `CV-043`：

- 视口、点阵、单选、框选/多选、分组/Frame；
- Note、页面卡、Text、图形库；
- 画笔、高亮笔、橡皮、stylus-only、压力/掌托；
- Connector 的创建、端口、路径、端点、方向、标签；
- 图层、锁定、对象上下文工具、More Tools；
- 复制、复制为格式、插入页面、对齐、吸附、缩放、旋转；
- 模板、Fit、全屏、Note 显示模式、Slicer；
- AI、大画布渲染、手势取消。

当前有真实内容的部分远超 UI 外壳，但对象类型与 BlockSuite 仍不对等。`shape` 不是任意 Block，`note/card` 也不是 BlockSuite note 子树。

### 5.4 数据库

`DB-001` 至 `DB-018`：

- 创建、table、board、list、calendar；
- 5 种字段；
- 字段 CRUD/排序；
- 记录 CRUD/手动排序；
- sort、filter、view config；
- board 拖拽、calendar 编辑、record detail、group-by；
- relation/linked doc；
- database search 完整性和格式导入/导出。

当前数据库模型是 `LocalDatabase(name, rows, fields, viewMode, sortField, descending, calendarField)`，编码进单个 `Block.content`。它适合轻量离线原型，但不是 BlockSuite data source。

### 5.5 同步与协作

`SYNC-001` 至 `SYNC-007`：

- 本机离线存储；
- 账号/工作区；
- 云同步；
- 实时协作；
- 版本历史；
- 分享和权限；
- AI/外部连接器。

Manifest 无 `INTERNET` 权限，因此除本机离线外，当前不能发出网络请求。README 有明确告知“云同步与多人协作尚未启用”，这是诚实的现状说明，不构成已实现。

### 5.6 Android

`AND-001` 至 `AND-011`：

- 启动加载、onStop、页面/模式/工具恢复；
- 系统返回；
- 旋转/分屏；
- 中文 IME；
- 触控和手写笔；
- 全屏系统栏；
- 硬件键盘；
- 无障碍语义。

已实际确认的失败：`AND-004`，在画布原位编辑状态按系统 Back 会离开应用。源码中未发现 `BackHandler`、`onBackPressedDispatcher` 或显式导航栈。

### 5.7 数据可靠性

`REL-001` 至 `REL-012`：

- 原子保存、文件夹事务；
- 坏页和独立数据源隔离；
- 备份安全和缺失素材检测；
- 共享块持久历史；
- 页面 undo 持久化（已补齐正常重启路径）；
- schema 迁移；
- CRDT；
- 自动保存主线程成本和大数据压力。

当前测试强项是序列化、边界拒绝和部分恢复；弱项是 Android 进程/文件故障注入、主线程编码成本、跨版本 fixtures 和大数据压力。

### 5.8 设置与视觉

`UI-001` 至 `UI-012`：

- 语言、布局稳定、设置分类和紧凑屏工具栏；
- 动效、图片平滑；
- 编辑器、画布、画笔默认值；
- 本机数据统计；
- 深色/文档主题；
- 标准图标；
- 响应式工具栏；
- 页面级属性。

中英文切换和设置页五个分类已实际验证。图标来源是标准 BlockSuite 路径，不是临时自绘线框。工具栏已在 2560×1600 和 1280×800 物理像素下通过，但更窄窗口和大字号仍可能溢出，因此 `UI-011` 为 `PARTIAL`。

## 6. 文件索引

- 配置：`app/build.gradle.kts`、`app/src/main/AndroidManifest.xml`
- 页面和对象模型：`Page.kt`
- 文档编辑器：`MainActivity.kt:BlocksEditor`
- 数据库：`LocalDatabase.kt`、`MainActivity.kt:DatabaseEditor`
- 画布：`CanvasSelection.kt`、`CanvasGroups.kt`、`CanvasTransform.kt`、`ConnectorGeometry.kt`、`MainActivity.kt:CanvasEditor`
- 笔迹：`Ink.kt`
- 卡片和模板：`Cards.kt`、`CanvasTemplates.kt`、`TemplateCatalog.kt`
- 共享内容：`SharedBlocks.kt`
- 工作区：`WorkspaceBootstrap.kt`、`WorkspaceCatalog.kt`、`WorkspaceOverview.kt`、`WorkspaceInsights.kt`
- 设置和视觉：`SettingsPage.kt`、`WorkspacePreferences.kt`、`VisualDesign.kt`、`AffineIconPaths.kt`
- 存储与恢复：`PageStore.kt`、`BackupArchive.kt`、`Reliability.kt`
- 测试：`app/src/test/java/com/example/tabletworkspace/`
