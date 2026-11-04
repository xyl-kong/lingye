# P2 模块化拆分计划（2026-11-04）

本文是 P2「Gradle 多模块化 / ViewModel / DI」的执行草案。目标是先建立**可验证的包边界**，再逐步拆成 Gradle 模块；每一步都必须保持页面格式 25、文件路径、备份格式与现有交互不变。

## 1. 现状

- 单模块 `:app`，源码全部位于 `com.example.tabletworkspace` 包。
- 107 个 Kotlin 文件，约 23,470 行；`MainActivity.kt` 仍是最大的单文件（2,656 行）。
- 已有仓储层：`PageRepository`（单线程写队列）、`WorkspaceRepository`（会话/语言/工具偏好/设置）、`PageMetadataRepository`（文档属性、Journal、模板标记）。
- 已有纯逻辑与缓存模块：`PageCodec`/`PageHistory`、`ImageGovernance`、`PlatformImageDecoder`、`TextLayoutCache`、`ConnectorSampleCache`、`PageSearchIndex`、`PageRowModel`/`DocumentRowModel`。
- 测试：434 项 JVM 单元测试，0 失败；Release 启用 R8，通过模拟器冒烟。
- 依赖方向仍靠包内约定，UI 文件可直接访问 `PageStore` 与 `SharedPreferences` 扩展；没有编译期边界。

## 2. 目标模块

```
:app                     MainActivity、导航宿主、依赖容器、Application
:core:model              纯 Kotlin：Page/Block/Shape/CanvasText/Connector/
                         Selection/Transform/Snapping、PageCodec、FolderCatalog、
                         日期与文本纯函数（不依赖 Android/Compose）
:core:ui                 设计 token（LingyeColors）、AppIcon/WorkspaceIcon、
                         MenuRow/MenuSectionLabel/HelpGlyph、通用按钮与 chip
:data:pages              PageStore、PageRepository、PageHistory、图片存储与治理、
                         BackupArchive（依赖 Android Context/AtomicFile）
:data:workspace          WorkspaceRepository、WorkspacePreferenceStore、
                         WorkspaceSettings、PageMetadataRepository
:feature:canvas          CanvasEditor 及渲染/输入/工具条/对话框/缓存
:feature:document        BlocksEditor、文档信息面板、Markdown 导入导出 UI
:feature:home            AllDocumentsHome、侧栏页面列表、标签页、搜索
:feature:settings        SettingsPage、取色器、储存与备份 UI
```

包名保持 `com.example.tabletworkspace`（`applicationId` 不变），模块只改变源码集与依赖关系；不重命名现有类，避免无意义的全仓 diff。

## 3. 依赖规则

```
:app  ──►  :feature:*  ──►  :core:ui
  │            │                │
  │            └──►  :data:*  ──┘
  │                     │
  └─────────────────────┴──►  :core:model
```

- `:core:model` 不依赖 Android、Compose、`Context`；可在纯 JVM 测试中运行。
- `:core:ui` 只依赖 Compose 与 `:core:model` 的值类型，不访问存储。
- `:data:*` 依赖 `:core:model` 与 Android，不依赖任何 `:feature:*`。
- `:feature:*` 通过构造参数/回调接收数据与意图，不直接 `new PageStore(...)`；由 `:app` 的依赖容器注入。
- `:app` 是唯一知道全部模块的层；`MainActivity` 只做导航、生命周期与容器装配。

## 4. 迁移顺序

每一步单独成批，独立编译、测试、Release 构建与模拟器冒烟；任一步失败可停在上一步，不跨步回滚。

### S0：包内分层（不新增 Gradle 模块，风险低）

- 在 `com.example.tabletworkspace` 下建立 `model/`、`data/`、`ui/`、`feature/canvas`、`feature/document`、`feature/home`、`feature/settings` 子包，先移动**叶子文件**（无 UI 依赖的纯逻辑）。
- 用 `internal` + 显式 import 暴露边界；IDE 的「依赖分析」应显示 `model` 不被 `data`/`ui` 反向引用。
- 验收：编译、434 项测试、Release 冒烟与迁移前一致；不修改任何持久化格式。
- **进展（2026-11-04）**：batch28 建立 `core/` 与 `platform/image/`；batch29 建立 `ui/model/`；batch30 建立 `data/metadata/`；batch31 建立 `data/pages/`（`PageRepository`、`PageHistory`）；batch32 建立 `data/images/`（`PageImageStore`、`ImageGovernance`）；batch33 把 `PageStore` 整体迁入 `data/pages`；batch34 建立 `data/backup/`（`BackupStore`），`PageStore` 的备份与 SAF 方法改为委托。页面/备份格式未变。下一批可把 `core` 中的纯模型进一步抽为独立源码集，或开始评估 `:core:model` 的 Gradle 模块化（S1）。

### S1：抽出 `:core:model`（风险中低）

- 移动 `Page`/`Block`/`Shape`/`CanvasText`/`Connector`/`Selection`/`PageCodec`/`FolderCatalog`/几何与吸附纯函数，以及对应单元测试。
- `PageCodec` 的 `DataInputStream`/`DataOutputStream` 只依赖 JDK；`Rect` 等 Compose 类型如被模型直接引用，先替换为自有的 `WorldRect` 值类型，或在 `:core:ui` 提供 `Rect` 扩展，避免把 Compose 拖进 model。
- 验收：`:core:model:test` 全绿；页面读写的 round-trip 字节与迁移前一致（对 `ToolbarTest` 与一份固定样本做逐字节比对）。

### S2：抽出 `:core:ui`（风险低）

- 移动 `LingyeColors`、`AppIcon`、`WorkspaceIcon`、`MenuRow`、`MenuSectionLabel`、`MenuCheckItem`、`HelpGlyph`、`CanvasChrome` 中的通用表面/chip。
- 图标资源继续留在 `:app` 的 `res/drawable`，`WorkspaceIcon` 用资源 ID 或 `ImageVector` 适配；若采用资源 ID，需在 `:core:ui` 建立 `IconRes` 映射，避免 `R` 类跨模块引用。
- 验收：菜单、设置页与画布工具条视觉不变（截图对比 batch27）。

### S3：抽出 `:data:*`（风险中高）

- 先拆 `:data:pages`：`PageStore`、`PageRepository`、`PageHistory`、`PlatformImageDecoder`、`ImageGovernance`、`BackupArchive`。
- 再拆 `:data:workspace`：`WorkspaceRepository`、`WorkspacePreferenceStore`、`WorkspaceSettings`、`PageMetadataRepository`、`WorkspaceBootstrap`。
- 约束：`filesDir` 下文件名、`AtomicFile` 用法、`images/` 目录与 `folders.dat`/`page_metadata`/`ink_preferences` 键名全部不变；不做数据迁移。
- `PageRepository` 的单线程执行器继续由 `:app` 生命周期管理（`onStop` flush），不改为模块级全局单例。
- 验收：安装后打开现有 201 个页面文件、20 个历史、2 张图片均正常；写入队列测试与备份 round-trip 测试全绿。

### S4：抽出 `:feature:*`（风险高，按功能逐个）

建议顺序（依赖少 → 多）：

1. `:feature:settings`（设置、取色器、储存/备份 UI）。
2. `:feature:home`（全部文档、侧栏行、标签页、搜索展示）。
3. `:feature:document`（块编辑器、文档信息、Markdown UI）。
4. `:feature:canvas`（最大的画布子系统）。

每个 feature 只接收不可变模型与回调；禁止在 feature 内直接读写文件或偏好。先以 `feature` 内部 `remember` 状态运行，确认无回归后再考虑 ViewModel。

验收：对应界面冒烟清单（打开/编辑/撤销/切换）与原版一致；跨 feature 导航仍由 `:app` 的 `selected`/`allDocsHome` 等状态驱动。

### S5：收口 `:app`（风险中）

- `MainActivity` 只保留：窗口/系统栏、导航状态、依赖容器、`saveCurrent`/自动保存生命周期、顶层快捷键分发。
- 建立手写 `AppContainer`（`PageStore`、`PageRepository`、`WorkspaceRepository`、`PageMetadataRepository`），通过 `CompositionLocal` 或参数注入；不引入 Hilt，直到模块边界稳定。
- 将 `MainActivity` 的 2,656 行目标降到 800 行以内，并为其补最少的状态测试（保存/导航/会话恢复）。

### S6（可选）：ViewModel 与 Hilt

- 仅为确有生命周期需求的界面引入 `ViewModel`：Workspace（页面列表/筛选）、Document（块编辑/历史）、Canvas（选择/工具/视口）。
- ViewModel 只持有 UI 状态与用例调用，不持有 `Context`；IO 仍经 `:data:*` 的挂起/队列 API。
- Hilt 放在最后；若手写容器已足够清晰，可不引入。

## 5. 风险与护栏

| 风险 | 影响 | 措施 |
|---|---|---|
| 页面格式/文件路径变化 | 数据不可读 | 格式保持 25；编解码逐字节 round-trip；不在拆模块时改存储键名 |
| `R` 类/资源跨模块 | 编译失败或图标丢失 | 资源留在 `:app` 或用 `IconRes` 映射；每个 feature 的 `consumer-rules.pro` 单独审查 |
| R8/ProGuard 规则遗漏 | Release 运行崩溃 | 每步构建 `assembleRelease` 并在模拟器启动、冒烟；保留现有 `proguard-rules.pro` 并复制到需要的模块 |
| 构建时间上升 | 迭代变慢 | 拆模块后启用 Gradle 配置缓存；先做包边界，确认收益再拆 Gradle 模块 |
| Compose 强跳过/状态提升回归 | 交互异常 | 每步冒烟：文本＋跟随、拖动、撤销、搜索、文件夹树、设置切换 |
| 测试迁移遗漏 | 覆盖率下降 | 每个模块保留自己的 `src/test`；总测试数不得低于 434 |
| 数据安全 | 用户数据风险 | 不删除页面/图片/备份；冒烟只用 `ToolbarTest` 与临时空文件夹，结束即删除；不运行 `connectedDebugAndroidTest` |

## 6. 每步验收清单

1. `testDebugUnitTest`：0 失败，测试数不低于上一步。
2. `assembleRelease`：R8 通过，APK 可安装。
3. 模拟器冒烟：启动 → 全部文档 → 打开 `ToolbarTest` → 文档/白板切换 → 选择/拖动/撤销 → 设置切换 → 返回。
4. 数据核对：页面文件数、历史数、图片数、`ToolbarTest` 大小与格式 25。
5. 文档更新：`ARCHITECTURE_AUDIT`、`DATA_SAFETY_INDEX`、`TASKS`、`WORK_CHECKLIST`、`README` 各追加一条，并记录未验证项。

## 7. 明确不做

- 不在拆模块的同一批升级 Kotlin/Compose/AGP 版本。
- 不改变 `applicationId`、包名、数据目录、备份格式或页面格式。
- 不把 `PageStore` 变成全局单例，也不在模块初始化时扫描用户数据。
- 不为拆分而拆分：若某文件只有 1–2 个调用点且无独立测试价值，留在原模块，不强行建空模块。
