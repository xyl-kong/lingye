# 数据安全索引（2026-11-03 更新）

本页集中记录当前页面格式、备份、迁移与回滚凭据。详细字段见 `PAGE_FORMAT_25.md`，D1/D2 实施见 `STAGE_D_IMPLEMENTATION.md` 与 `STAGE_D2_IMPLEMENTATION.md`。

## 1. 当前状态

| 项 | 值 |
|---|---|
| 应用版本 | 0.15（R8 Release 已安装于模拟器） |
| 页面格式 | 25；200 个 `.page` 文件首 4 字节均为 `00 00 00 19` |
| 活动文档 | 192（All Docs 显示；回收站含本轮 4 个“未命名页 副本”、1 个“ToolbarTest 副本”及此前测试页，均可恢复，未永久删除） |
| 历史文件 | 20 |
| 图片 | `files/images` 有 1 个测试图片（`b3904ba3…img`，被测试页 `ToolbarTest` 引用）；62 个页面含 `.img` 引用，早期事故丢失的图片仍无法恢复 |
| 最新发布 | `releases/lingye-v0.15-architecture-batch26-release-20261104.apk`；3,440,431 字节，SHA-256 `D08414DBF2C14D4004AF82D3D92061493ECBEFAC60CCF7428AA59A3B6812443B`（2026-11-04 构建；通用菜单行组件迁移；模拟器已安装同一 APK） |

## 2. 备份凭据

| 备份 | 内容 | 大小 | SHA-256 |
|---|---|---|---|
| `evidence/format24-backup.tar` | v24 实施前原始数据（195 页/13 历史） | 487,424 | `9D02F2D3EC04BBFAE416E2CF56D6A6CF19CD35F885ED88F61D3A945258C70194` |
| `evidence/pre-format25-20261101.tar` | 格式 25 实施前再次导出（195 页/13 历史） | 487,424 | `F68C378E0BC9D9B24334EC864F8EA742E0698AF6D3E0BF57E6EAED17108161D1` |
| `evidence/post-format25-20261101.tar` | 迁移后（196 页/14 历史） | 515,584 | `4C838ADDB6FD3C3FB3887A42164124F4222A1B0712D8D70C21543F09487AC174` |
| `evidence/pre-d2-20261101.tar` | D2 前（196 页/14 历史，与上一项一致） | 515,584 | `4C838ADDB6FD3C3FB3887A42164124F4222A1B0712D8D70C21543F09487AC174` |
| `evidence/post-d2-20261101.tar` | D2 后（197 页/15 历史，含回收站测试页） | 520,192 | `0C9DF73E1C50A3EC5A2AFF281980519DE6A8D5E23A04F37CF0441B1B53014C82` |
| `evidence/post-repair-20261101.tar` | 误回收站修复后（202 页/20 历史，191 活动） | 2,578,944 | `D1EBA70E444E7486B540670214DE379022E8045A70C0E2E82C5F3ACE56474BF9` |
| `evidence/pre-trash-ui-20261101.tar` | 回收站多选/清空改动安装前（203 页/23 历史，192 活动） | 2,623,488 | `AAB434D002685073BD2A8EE48C70BD5ACF2A57261C502602D6743B6B207FDE0B` |
| `evidence/post-incident-20261101.tar` | 12:29 批量文件丢失后现场（仅 4 页/2 历史） | 71,680 | `6B5C2E49CD60A88EBC6F759473D76C138C76FF918181CDF84C61D3040203E6C4` |

所有备份由 root 在应用停止后打包 `files` 与 `shared_prefs`；未覆盖任何既有备份。

## 3. 迁移记录

- 格式 24 → 25：启动时 `PageStore.migratePagesToFormat25()` 逐页检测版本，v1–v24 解码后以 v25 原子重写；195 个原页面成功，失败 0。
- 历史文件：`PageHistory` 将 v24 快照规范化为 v25；撤销/重做在迁移后仍可用。
- 迁移后 197 个页面全部为 v25；未残留 `.bak`/`.tmp`。
- 历史格式回归：`PageMigrationMatrixTest` 覆盖设备出现过的 13 个版本（v4/v5/v6/v8/v11–v16/v22–v24）+ v26 拒绝 + 截断/损坏拒绝；测试使用 `LegacyPageFixtures` 生成的最小页面，不嵌入用户页面内容。真实 195 页迁移有设备证据。

### 3.1 误回收站事故与修复

- 事故：安装调试构建后，活动文档从 191 降到 22。诊断组件解码 202 个页面，报告 180 个处于回收站。
- 对照 `post-d2-20261101.tar`：其中 169 个页面在事故前为活动且原本不在回收站，属于误置。
- 修复：仅对这 169 个页面解码、清除 `trashed`、按格式 25 重新编码并原子覆盖；未修改标题、正文、块、画布对象、文件夹、标签或历史文件。
- 结果：活动文档恢复到 191；5 个测试/性能基准页仍留在回收站；页面总数 202、历史 20 不变。
- 副作用：修复页被重新编码，mtime 变为修复时刻，信息面板的创建/更新时间可能显示“今天”；原创建时刻无法从现有元数据恢复。
- 根因：未找到自动批量回收站代码路径，最可能是界面误触“批量移入回收站”；最终根因仍未确认。
- 修复后备份：`evidence/post-repair-20261101.tar`（见第 2 节）。

### 3.2 12:29 批量文件丢失与恢复

- 事故：2026-11-01 12:29 左右，设备 `files/pages` 从 203 个 `.page` 降到 4 个，`files/history` 从 23 个降到 2 个；应用仍在运行，界面在事故前后分别显示 192 与 191 个活动文档。
- 现场：剩余 4 页为 `00db827e…`、`010431b2…`、`0143b08e…`（未命名）与 `05bbd691…`（AlphaRenamed），mtime 均为 12:29；`shared_prefs` 未丢失。现场已打包为 `evidence/post-incident-20261101.tar`。
- 恢复：12:31 停止应用，将原 `files` 目录改名为 `files.incident-20261101` 保留，再用 `pre-trash-ui-20261101.tar` 解出 `files`，`chown u0_a211:u0_a211` 并 `restorecon`；未覆盖 `shared_prefs`。
- 结果：页面恢复到 203 个（格式 25 全部核对）、历史 23 个、活动文档 192；界面核对见 `evidence/after-restore.png`、`evidence/final-1.png`。
- 根因：未确认。事故前刚执行过“回收站多选 → 永久删除 1 个新建测试页”和一次恢复尝试；更早的 191→22 误回收站事故发生在没有永久删除代码的版本，两次都未能从代码中找到可复现的批量删除路径。日志中未出现 FATAL 崩溃。
- 处置：`清空回收站` 的确认窗口与单页永久删除路径已实现并核对，但**未再执行清空全部**；后续真实数据上的批量删除测试必须先在独立备份副本上进行。

### 3.3 页面行菜单功能验证（2026-11-02）

- 为验证“复制 / 添加链接文档 / 在新标签页打开”，新建空页 `未命名页面T51Blank`（`92af1c6b-6b76-4767-90c7-a18788d99fb1.page`），并复制测试页得到 `未命名页面 副本`（`70b72353-5703-49b4-a8ba-90441e6fae8c.page`）。
- 页面总数 193 → 195，活动文档 193 → 195，回收站 0；未删除任何既有页面。
- 链接文档验证时，侧栏菜单对应页面为 `28f74e84-df50-4f09-9ccf-b145a2a9423b`，曾两次插入链接卡片，均随后通过画布选中并删除；该页最终 292 字节，不含 `pageLink` token，原有黄色矩形保留。添加/删除产生 2 个新历史文件（18 → 20）。
- 上述两个测试页在 Export 浮窗修复验证完成后已移入回收站，活动文档恢复到 193、回收站 2；未执行永久删除或清空回收站，页面文件仍为 195 个。
- `未命名页面T51Blank` 在复现过程中误入一个空数据库块和三张模板卡片；已通过文档块菜单删除数据库、白板全选删除卡片，随后移入回收站。该页为测试页，不涉及用户页面。

### 3.4 模板画廊验证（2026-11-03）

- 模板插入测试在既有测试页 `ToolbarTest`（`ba488d2c-8519-4801-88e8-05f6739a5d92.page`）上完成；插入第一个官方箭头后删除，页面恢复为 1,513 字节，测试产生的孤立 `.img` 文件已删除，`files/images` 仍为空。
- 未增删页面；活动文档 193、回收站 2、页面文件 195、历史 20、格式 25 均未变。

### 3.5 任意缩放验证（2026-11-03）

- 在测试页 `ToolbarTest` 上对文本框做了仅水平、仅垂直两次角手柄拖拽，随后用应用内撤销恢复原尺寸。
- 该测试页文件当前 1,838 字节（包含此前测试留下的数据库/文本/连线及撤销历史），格式 25；未增删页面，活动文档 193、回收站 2、页面文件 195、历史 20、图片目录 0 均未变。

### 3.6 模板画廊直达验证（2026-11-03）

- 在测试页 `ToolbarTest` 点击内容按钮一次打开模板画廊；点击图片图标打开系统选择器后取消，未导入图片。
- 点击数据库图标添加数据库后立即用应用内撤销恢复；页面文件回到 1,838 字节。
- 活动文档 193、回收站 2、页面文件 195、历史 20、图片目录 0、格式 25 均未变。

### 3.7 边中点与工具条收起验证（2026-11-03）

- 在测试页 `ToolbarTest` 上用左右边中点、上下边中点分别缩放矩形/菱形，并拖动文本框验证工具条在拖动中隐藏、松手后恢复；随后多次撤销。
- 该测试页当前 2,015 字节（测试页历史内容）；未增删页面，活动文档 193、回收站 2、页面文件 195、历史 20、图片目录 0、格式 25 均未变。

### 3.8 Journal 与模板验证（2026-11-03）

- 在测试页 `ToolbarTest` 上验证 Journal 勾选进入 Journals、日期 chip/清除、模板勾选，以及 Templates 列表的“使用模板”与“新建模板”入口；测试结束已清除 ToolbarTest 的 Journal/模板标记，并把生成的 `ToolbarTest 副本` 移入回收站。
- 期间发现此前测试误触创建了 4 个“未命名页 副本”，已全部移入回收站，未永久删除。
- 当前 All Docs 显示 192 个活动文档，页面文件 200、历史 20、`files/images` 1 个 ToolbarTest 引用图片；全部页面格式 25。

### 3.9 架构第一批验证（2026-11-03）

- 代码结构改动：`DateTimeFormats.kt`、`PageImage.kt`、`DocumentInfoCard.kt`、`PageListComponents.kt`；`PageStore` 增加 Bitmap LRU 缓存。
- 未改变页面格式/页面数据；Release 安装后冒烟：全部文档、信息面板、白板图片正常；371 项 JVM 测试 0 失败。
- 冷启动 `am start -W`：TotalTime 1,686 ms（模拟器当前基线，未采集同条件改造前基线，不作提升声明）。
- 页面文件 200、历史 20、图片 1、All Docs 192、格式 25；未增删页面。

### 3.10 元数据备份（2026-11-03）

- 备份格式 `lingye-backup-2` 新增可选 `metadata.dat`，包含每页文档属性 raw、Journal 日期、模板标记；旧 `lingye-backup-1` 仍可读取，元数据为 null。
- 恢复时按原页面→新页面映射，批量 commit 属性与标记；格式/round-trip 由 3 项 JVM 测试覆盖。
- **未在设备上执行完整备份+恢复**（会复制全部页面）；设备级恢复仍未验证，不做“已验证恢复”声明。
- 页面格式 25、页面数据未变；All Docs 192、页面文件 200、历史 20、图片 1。
- 第二批同时抽出 `TextBoxStyleUi.kt`（378 行），MainActivity 7,112 行；白板文本样式面板冒烟通过；374 项 JVM 测试 0 失败。
- 第三批新增 `PageMetadataRepository` 收口文档属性/Journal/模板访问，`PageStore.backup` 改为接收元数据快照；抽出 `CanvasChrome.kt`；MainActivity 7,010 行；模板勾选/取消与白板画布冒烟通过。

### 3.11 文档块编辑器拆分（2026-11-03）

- 新增 `BlocksEditor.kt`（670 行），从 `MainActivity` 移出 `BlocksEditor`、块插入菜单、共享块/共享文本编辑器、数据库编辑器与数字字段；`MainActivity` 7,010 → 6,425 行，75 个 Kotlin 文件。
- 374 项 JVM 测试 0 失败；Release `batch4` 已安装并冒烟：文档块输入、回车新增空块、退格清理、数据库编辑器各区域、白板画布与 8 个选中手柄正常。
- 画布原位编辑（双击文本）在 batch3 与 batch4 上表现一致（均未自动聚焦/弹出输入），未发现本批回归；该行为列为既有观察项，未在本批修改。
- `ToolbarTest` 测试页因块编辑测试从 2,015 字节变为 2,173 字节，正文内容未改变；页面数 200、历史 20、图片 1、All Docs 192、格式 25 均未变，未增删页面。

### 3.12 CanvasEditor 外移与选择工具条拆分（2026-11-03）

- 新增 `CanvasEditor.kt`（3,697 行），把画布 Composable 及 `SystemBarsEffect`、`ConnectorRenderData`、`ConnectorMarkerRow`、`MindmapLayoutPreview`、`TextSpanVisualTransformation` 移出 `MainActivity`；`MainActivity` 6,425 → 2,652 行。
- 新增 `CanvasSelectionToolbars.kt`（383 行），拆出页面链接、思维导图节点、Frame、图片、笔迹五类选择工具条；`Rect.toIntRectPx()` 改为 `internal`，删除未引用的 `SelectionToolbarDivider`。
- 374 项 JVM 测试 0 失败；Release `batch5` 已安装，冒烟 Frame/笔迹/思维导图/页面链接四类工具条全部正常；图片工具条仅编译通过，测试页无图片对象，未做交互验证。
- 在 `ToolbarTest` 上创建并删除 Frame、笔迹、思维导图、链接卡片后，页面文件仍为 2,173 字节、格式 25（`00 00 00 19`）；页面数 200、历史 20、图片 1、All Docs 192 均未变，未增删页面、未删除任何图片文件。

### 3.13 画布绘制/输入与底部工具条拆分（2026-11-03）

- 新增 `CanvasRendering.kt`（686 行）承接全部画布绘制；新增 `CanvasInput.kt`（81 行）承接点击/双击命中；新增 `CanvasBottomBar.kt`（570 行）承接底部工具条、六类参数条与画廊/更多菜单入口。
- `CanvasEditor.kt` 3,697 → 2,885 行；`MainActivity` 保持 2,652 行；80 个 Kotlin 文件。
- 374 项 JVM 测试 0 失败；Release `batch6` 已安装，冒烟网格/形状/文字/连线/笔迹渲染、工具切换、Frame/文本/画笔/橡皮/形状参数条、内容画廊、多选参数条与点击选择均正常。
- 笔迹/橡皮状态机、框选/缩放拖拽与快捷键仍在 `CanvasEditor`，未做行为改动；图片工具条仍未交互验证。
- 数据：`ToolbarTest` 仍为 2,173 字节、格式 25；200 页面文件、20 历史、1 图片、All Docs 192 未变，未增删页面、未删除图片。

### 3.14 手势状态机与画布对话框拆分（2026-11-03）

- 新增 `CanvasPointerInputs.kt`（508 行）：演示缩放、笔迹/橡皮状态机、移动/选择拖拽、框选、缩放/旋转手柄、连线拖拽、形状/Frame/文本拖拽创建。
- 新增 `CanvasDialogs.kt`（298 行）：思维导图改接/删除、链接已有文档、Frame 导航/重命名、模板管理/重命名/删除/保存、保存选区模板、图片裁剪九个对话框。
- 状态通过 `canvasStateOf`（getter/setter 适配的 `MutableState`）共享，未改变各状态变量的存储方式；后续应替换为正式的 `CanvasEditorState`。
- `CanvasEditor.kt` 2,885 → 2,508 行；82 个 Kotlin 文件、约 22,462 行。
- 374 项 JVM 测试 0 失败；Release `batch7` 已安装，冒烟画笔落笔、区域橡皮擦除、框选、拖拽后撤销、链接文档与 Frame 导航对话框均正常。
- 数据：`ToolbarTest` 因绘制擦除与拖拽撤销从 2,173 字节变为 1,841 字节；文档块与画布图形目视完整，格式 25；200 页面文件、20 历史、1 图片、All Docs 192 未变，未增删页面、未删除图片。

### 3.15 CanvasEditorState、快捷键与原位编辑（2026-11-03）

- 新增 `CanvasEditorState.kt`（72 行）作为画布状态持有者，删除 `canvasStateOf` 适配器；新增 `CanvasShortcutHandler.kt`（153 行）与 `CanvasInlineEditor.kt`（244 行）。
- 修复键盘行为：文本框选中时 Delete 可删除对象；双击进入原位编辑后自动聚焦并弹出输入法，输入/退格正常；编辑中 Delete 编辑文字；点击“完成”退出编辑；退出后 Delete 删除对象。Escape 在编辑框内仅隐藏输入法，不退出编辑。
- `CanvasEditor.kt` 2,508 → 2,301 行；`MainActivity` 2,652 行；85 个 Kotlin 文件。
- 374 项 JVM 测试 0 失败；Release `batch8` 已安装，冒烟菱形/文本框删除与 Ctrl+Z、原位编辑输入/退格、完成按钮均通过；Escape 只隐藏输入法。
- 数据：`ToolbarTest` 在测试与撤销后为 1,841 字节、格式 25（`00 00 00 19`）；200 页面文件、20 历史、1 图片、All Docs 192 未变，未增删页面。测试中新增的文本框多次撤销后仍以不可选中形式显示，后在 batch9 确认为残留原位编辑器，不是页面数据。
- 说明：Escape 在编辑框内由字段级 `onPreviewKeyEvent` 处理；Canvas 级快捷键在编辑框失焦后恢复。

### 3.16 不可选中文本框诊断与连线工具条拆分（2026-11-03）

- 诊断 batch8 的“撤销后文本框不可选中”：形状日志显示该文本不在任何页面形状坐标上，命中为 null；实际是 `cardEdit` 在删除/撤销后仍指向已变化索引，原位编辑器 Surface 以错误位置残留。重启进程后消失，确认不是页面数据损坏。
- 修复：删除选中对象时清空 `cardEdit`；`LaunchedEffect(page.shapes.size, cardEdit)` 在编辑目标失效时自动退出；不再残留编辑器覆盖画布。
- 新增 `CanvasConnectorToolbar.kt`（329 行），连线工具条与“连接线文字”对话框移出 `CanvasEditor`，`CanvasEditor` 内已无 `AlertDialog`。
- `CanvasEditor.kt` 2,301 → 2,109 行；`MainActivity` 2,652 行；86 个 Kotlin 文件、约 22,814 行。
- 374 项 JVM 测试 0 失败；Release `batch9` 已安装，冒烟连线工具条、文字对话框、文本编辑→完成→Delete 且无残留、Ctrl+Z 恢复正常。
- 数据：`ToolbarTest` 仍为 1,841 字节、格式 25；200 页面文件、20 历史、1 图片、All Docs 192 未变，未增删页面。

### 3.17 文本选区格式浮层拆分（2026-11-03）

- 核对确认画布没有长按快捷菜单实现（长按/右键菜单只在文档块与工具提示中），本批改为拆分剩余最大的局部状态：文本选区格式浮层。
- 新增 `CanvasTextFormattingToolbar.kt`（606 行），字号/对齐/加粗/代码/公式/斜体/链接/删除线/下划线/高亮/更多及子菜单移出 `CanvasEditor`；复制/删除通过 `onSelectionChange` 回写。
- `CanvasEditor.kt` 2,109 → 1,654 行；`MainActivity` 2,652 行；87 个 Kotlin 文件、约 22,965 行。
- 374 项 JVM 测试 0 失败；Release `batch10` 已安装，冒烟编辑文本→Ctrl+A→格式浮层显示、完成退出、Ctrl+Z 均正常，无崩溃。
- 数据：`ToolbarTest` 仍为 1,841 字节、格式 25；200 页面文件、20 历史、1 图片、All Docs 192 未变，未增删页面。
- 未拆分：卡片/形状选择工具条仍在 `CanvasEditor`。

### 3.18 卡片/形状选择工具条拆分（2026-11-03）

- 新增 `CanvasCardShapeToolbars.kt`（449 行），卡片与形状选择工具条从 `CanvasEditor` 移出；选择通过 `onSelect` 回写，编辑/保存模板/绘制连线分别通过 `onEditCard`/`onSaveTemplate`/`onDrawConnector`。
- `CanvasEditor.kt` 1,654 → 1,343 行；`MainActivity` 2,652 行；88 个 Kotlin 文件、约 23,103 行。
- 374 项 JVM 测试 0 失败；Release `batch11` 已安装，冒烟新建矩形工具条、模板画廊插入“笔记”卡片后工具条均正常；测试对象已删除。
- 数据：`ToolbarTest` 仍为 1,841 字节、格式 25；页面文件 200 → 201（新增一个被移入回收站的空白测试页，All Docs 仍为 192），历史 20、图片 1 未变，未永久删除页面。

### 3.19 文本对象工具条与相邻控件拆分（2026-11-03）

- 新增 `CanvasTextToolbar.kt`（412 行），文本对象工具条、左右“相邻添加”按钮与锁定角标移出 `CanvasEditor`；选择/编辑/连线/解锁通过回调写回。
- `CanvasEditor.kt` 1,343 → 1,069 行；`MainActivity` 2,652 行；89 个 Kotlin 文件、约 23,241 行。
- 374 项 JVM 测试 0 失败；Release `batch12` 已安装，冒烟选中文本后工具条与左右“+”按钮正常显示，无崩溃。
- 数据：`ToolbarTest` 仍为 1,841 字节、格式 25；页面文件 201、历史 20、图片 1、All Docs 192 未变。

### 3.20 内联编辑交互与底部宿主拆分（2026-11-03）

- 按用户反馈移除内联编辑器“完成”按钮，改为点击画布外部、`Esc`、`Ctrl+Enter` 退出；编辑时隐藏选中外框和四角手柄，文本框编辑层不再叠加阴影。
- 新增 `CanvasObjectToolbarHost.kt`（222 行）与 `CanvasPresentationBar.kt`（63 行）；`CanvasEditor.kt` 1,069 → 928 行，91 个 Kotlin 文件。
- 排查确认 `ToolbarTest` 中的黄色矩形是既有 `rect` 形状（不是浮层残留），点击可正常选中。
- 冒烟：文本/形状工具条、相邻添加按钮、内联编辑三种退出方式、Frame 演示条（含修正的“›”）均正常。
- 数据：`ToolbarTest` 仍为 1,841 字节、格式 25；201 页面文件、20 历史、1 图片、All Docs 192 未变；测试中创建并删除的 Frame 未留下内容，未增删页面。
- 374 项 JVM 测试 0 失败；Release `batch13` 已安装并冒烟。

### 3.21 单选目标与画布命令抽取（2026-11-03）

- 新增 `CanvasSelectionTargets.kt`（40 行）：九类对象“当前单选目标索引”集中计算；新增 `CanvasEditorCommands.kt`（162 行）：删除/复制/组合/层级/对齐/微移/全选/缩放/Frame 定位/演示控制移出可组合函数，改为持有 `CanvasEditorState` 的命令对象。
- `CanvasEditorState` 新增视口尺寸、Frame 比例、导航工具、选区恢复与演示状态；`CanvasEditor.kt` 928 → 815 行，93 个 Kotlin 文件。
- 新增 `CanvasEditorCommandsTest` 7 项（目标映射、多选/越界、删除清理、缩放中心、全选跳过锁定、演示开始/停止恢复、Frame 定位）；总计 381 项 JVM 测试 0 失败。
- 冒烟：文本/形状工具条、Delete 删除并 Ctrl+Z 恢复、Ctrl+A 全选、缩放 51%→64%→51%、Frame 新建/选中/演示/退出/删除均正常；页面文件回到 1,841 字节。
- 数据：201 页面文件、20 历史、1 图片、All Docs 192、格式 25；未增删页面，测试 Frame 已删除。
- Release `batch14` 已安装并冒烟。

### 3.22 P0 页面写队列（2026-11-04）

- 新增 `PageRepository.kt`：所有页面/撤销历史写入进入单线程队列（线程名 `lingye-page-writer`）。主线程只调用 `page.snapshot()`，编码与 `AtomicFile` 落盘都在写线程；同步 API 等待自己的写入并重抛失败（保留原有“保存后再导航”的语义），自动保存使用异步 API，失败通过回调显示错误。
- MainActivity 中 21 处 `store.save(...)` 与 1 处 `store.deletePermanently(...)` 全部改走仓库；`saveCurrent()` 保存页面与历史后等待落盘；自动保存在 400 ms 后入队，只有写成功回调才推进 `savedRevision`，写失败不会被误标为已保存；`onStop` 额外 `flush(3 s)`；备份恢复前先 `flush`。
- 同一队列保证提交顺序：较早的自动保存快照不会覆盖之后的重命名/复制/删除。
- 新增 `PageRepositoryTest` 8 项：同步等待与失败重抛、页面→历史顺序、提交顺序、flush 等待、异步失败回调、删除排序、flush 超时、历史编码。
- 389 项 JVM 测试 0 失败。
- 设备冒烟：在 `ToolbarTest` 文本框输入 A、B，自动保存使页面文件 1,841→1,843 字节且含“AB”；输入后立即切到全部文档，文件已落盘；`am force-stop` 重启后重新打开页面文本仍为“AB”；随后 Ctrl+Z 两次恢复原文，文件回到 1,841 字节。
- 数据：201 页面文件、20 历史、1 图片、格式 25；未增删页面，未删除图片。
- Release `batch15` 已安装并冒烟。

### 3.23 P0 页面加载与会话偏好收口（2026-11-04）

- 新增 `WorkspaceRepository.kt`：`load()` 负责工作区启动加载（页面、文件夹、模板、共享块、历史）；`readSession()/writeSession()` 接管工作区会话；`readLanguage()/writeLanguage()`、`readCanvasTools()` 与 `savePenOnly/saveEraserArea/saveBrushKind/saveHighlighter*/saveInk*` 接管画布工具偏好。Composable 不再直接读写这些 SharedPreferences 键。
- 偏好访问抽象为 `WorkspacePreferenceStore` 接口，生产环境用 `SharedPreferencesWorkspacePreferences`，测试用内存实现。
- 修复：会话恢复现在会读回 `workspace_session_document_scroll_index`（此前只写不读，文档滚动位置索引重启后丢失）。
- `MainActivity` 中剩余的 `preferences.readWorkspaceSettings/writeWorkspaceSettings` 仍直接使用 SharedPreferences（这两个扩展已封装键名，留待下一批迁移）。
- 新增 `WorkspaceRepositoryTest` 6 项：会话往返、默认值、语言回退与持久化、工具默认与越界钳制、工具写入回读、`load()` 委托。
- 395 项 JVM 测试 0 失败。
- 设备冒烟：设置中切换界面语言 English → 立即生效且 `ink_preferences` 写入 `language=en` → `force-stop` 重启后仍为 English 并直接恢复到设置页（会话恢复）→ 切回简体中文并写回 `zh`；画笔参数条切换“仅手写笔”，`pen_only` 由 false→true→false，均即时写盘。
- 数据：201 页面文件、20 历史、1 图片、`ToolbarTest` 1,841 字节、格式 25；未增删页面，无崩溃日志。
- Release `batch16` 已安装并冒烟。

### 3.24 P0 设置与元数据收口（2026-11-04）

- `WorkspaceSettings` 的读取与写入改为 `WorkspacePreferenceStore` 扩展：读取走类型化 getter，写入经一次 `write {}` 编辑器批量提交；`WorkspaceRepository.readSettings/writeSettings` 成为 Composable 的唯一入口，MainActivity 不再直接调用 `SharedPreferences` 的设置读写。
- 恢复备份的元数据从 `PageStore` 移到 `PageMetadataRepository.applyRestored`：文档属性（`ink_preferences`）、Journal 日期与模板标记（`page_metadata`）仍按原键写入，但 `PageStore.restore` 只接收元数据回调，不再直接操作元数据存储。
- 新增纯函数 `PageMetadataSnapshot.remapToImported` 完成“原始页面 ID → 恢复后新 ID”映射，并用两项测试覆盖（正常映射、未知页面条目丢弃）。
- 设置写回仍保存全部 39 个键（与旧实现一致），未改变任何键名或默认值。
- 400 项 JVM 测试 0 失败。
- 设备冒烟：设置 → 外观切换“深色”，`ink_preferences` 的 `theme_mode` 由 `system` 写为 `dark`；切回“跟随系统”后写回 `system`，界面同步变化。
- **设备级备份恢复仍未执行**：本次只验证了写入链路与映射单元测试；完整导出→清空→恢复需要独立数据副本，维持 U-PROJ-022 的未验证状态。
- 数据：201 页面文件、20 历史、1 图片、`ToolbarTest` 1,841 字节、格式 25；未增删页面，无崩溃日志。
- Release `batch17` 已安装并冒烟。

### 3.25 P1 第一批：文本排版/连线采样缓存与相邻按钮修复（2026-11-04）

- 新增 `TextLayoutCache.kt`（有界 LRU，400 项）：键为正文、字符格式区间、像素宽度、字号、字体族、字重与斜体；缓存换行结果、按格式切分的 run 与每段测宽。画布渲染不再每帧对每个文字对象逐字符测量。
- 新增 `CanvasTypefaces.kt`：按字体族/字重/斜体缓存 `Typeface`，替代每帧每个 run 的 `Typeface.create`。
- 新增 `ConnectorSampleCache.kt`（LRU 256，键为 Connector 数据类）：连线采样点缓存；渲染内同时按连线索引缓存本帧的 `ConnectorRenderData`（描边层与内容层原本各算一次），并用每帧一次性构建的形状 ID 索引替代每个连线的线性扫描；命中测试也复用采样缓存。
- 修复用户反馈：文本框左右“＋”在对象移动/缩放后停在原位。根因是页面对象原地修改、Compose 强跳过按引用比较，相邻按钮没有可观察的几何参数；现改为传入值类型 `AdjacentTextAnchor`（由 `geometryRevision` 驱动重算），拖动时随其他工具条隐藏、松手后出现在新位置；靠近画布边缘时隐藏该侧按钮，而不是压到文本框上。
- 新增 `TextLayoutCacheTest` 5 项、`ConnectorSampleCacheTest` 3 项；408 项 JVM 测试 0 失败。
- 设备冒烟：51% / 80% / 51% 缩放下文本换行与链接颜色正常，曲线连线渲染与点选正常；拖动文本后左右“＋”跟随到新位置，方向键微移同样跟随；靠右边缘时右侧“＋”隐藏。
- 未采集到可区分的性能差异：模拟器上 `ToolbarTest`（对象较少）用移动工具横扫 10 次，batch17 为 99 帧/59 卡顿（59.6%）、50 分位 46 ms，batch18 为 88 帧/52 卡顿（59.1%）、50 分位 44 ms；差异在噪声内，不作提升声明。
- 链接文本交互核实：点按链接文字打开外部页面，从链接文字起手拖拽则移动文本框且“＋”随动；此前的“拖拽误开链接”是点按链路造成的误判，无需修改。
- 数据：`ToolbarTest` 因用户手动测试与本次拖拽验证变为 1,937 字节（文本内容已用撤销恢复原样）；201 页面文件、20 历史、1 图片、格式 25，未增删页面，无崩溃日志。
- Release `batch18` 已安装并冒烟。

### 3.26 P1 第二批：卡片/笔记相邻“＋”与命中索引（2026-11-04）

- `CanvasTextAdjacentControls` 扩展为卡片与文本共用：选中卡片（笔记）后同样显示左右“＋”，点击复制出相邻的空卡片/文本框并进入原位编辑；描述文案按对象类型显示“添加笔记/添加文本”；拖动时隐藏、边缘时隐藏该侧、几何变化由 `AdjacentTextAnchor` 驱动。
- `Page.hitTest` 每次点击先构建一次 `id → CanvasSelection` 索引（笔迹/形状/图片/连线），替代逐层 `findStableObject` 的 `indexOf` 扫描，复杂度从 O(n²) 降为 O(n)；顺序仍按图层倒序，顶层优先、连线延后命中、Frame 仅边框可点。
- 新增 `CanvasHitTestIndexTest` 5 项：顶层优先、空白返回空、Frame 边框/内部、连线路径、删除后索引重排。
- 413 项 JVM 测试 0 失败。
- 设备冒烟：内容库插入“笔记”卡片后选中，左右“＋”正常显示；点右侧“＋”生成相邻空卡片并弹出输入法；删除两张测试卡片后 `ToolbarTest` 回到 1,937 字节；矩形点选与工具条正常。
- 仍待完成：命中空间索引（当前仍是每次点击 O(n)）、`selectionBounds` 缓存、图片资源治理、UI 组件统一、页面列表状态优化。
- 数据：201 页面文件、20 历史、1 图片、格式 25；未增删页面，无崩溃日志。
- Release `batch19` 已安装并冒烟。

### 3.27 P1 第三批：图片资源治理与格式扩展（2026-11-04）

- 新增 `ImageGovernance.kt`：`ImageReferences.referenced()` 汇总页面画布图片、文档图片块、工作区模板画布与共享块（含撤销/重做历史）引用的图片名；`containsBytes` 用于在页面历史文件的原始字节中查找图片名，避免撤销恢复出缺图页面。
- `PageStore` 新增 `imageFileNames()`、`orphanImages()`、`imageBytes()`、`deleteImages()`。设置 → 储存新增“未使用图片”数量与体积，以及带确认对话框的“清理未使用图片”；有无法读取的页面时拒绝清理，只删除 `files/images` 下符合命名规则且无任何引用的文件。
- 图片导入：画布与共享块的图片选择器从 `GetContent` 改为 `OpenDocument`，MIME 过滤为 `image/*` + `application/octet-stream`，使 MIME 未识别的图片文件也能被选择；校验失败时提示支持的格式清单。
- 新增 `PlatformImageDecoder.kt`：优先 `BitmapFactory`，API 28+ 失败时回退 `ImageDecoder`（强制软件位图），提升 HEIF/HEIC 与较新系统上 AVIF 的解码可靠性。
- 格式支持：JPEG、PNG、WebP、GIF、BMP 由平台稳定支持；HEIF/HEIC 自 Android 8.0（API 26）起、依设备解码器而定；AVIF 自 Android 14（API 34）起为强制能力，更早设备可能可用；动画图片只保留首帧；SVG、TIFF、PSD 等无平台解码器，未支持。
- 新增 `ImageGovernanceTest` 4 项；417 项 JVM 测试 0 失败。
- 设备冒烟：在 `files/images` 放入 221 KB 合成孤儿图，设置显示“1 · 221 KB”，确认后删除，真实引用图 `b3904ba3…img` 保留；随后显示“无”。通过系统选择器导入 PNG 成功并在画布显示（撤销/重做后渲染正常），撤销后页面回到 1,937 字节。
- 数据：201 页面文件、20 历史、2 图片（新增 1 个测试导入图，被页面重做历史引用，因此保留）、格式 25；未增删页面，无崩溃日志。
- Release `batch20` 已安装并冒烟。

### 3.28 P1 第四批：图片分桶采样与缓存统计（2026-11-04）

- `ImageSampleBuckets`：按目标显示像素向上取整到 256/512/1024/1600 四档；画布按 `max(图片宽, 高) × 缩放 × density` 选择档位，缓存键为 `name@bucket`，缩放跨档才重新解码，静态画面复用同一档位位图。
- `PageStore.image(name, maxDimension = 1600)` 默认行为不变（文档图片、裁剪对话框仍使用 1600）；删除图片时清理该名称的所有分档缓存。
- 设置 → 储存新增“图片缓存”：条目数 · 已用/上限 · 命中率（`LruCache.hitCount/missCount`）。
- 新增 `ImageSampleBucketsTest` 5 项（分档、上限、非法值、命中率、档位递增）；422 项 JVM 测试 0 失败。
- 设备冒烟：画布图片在 51% 与 100% 下渲染正常，缓存统计显示 `1 · 250 KB / 24.0 MB · 0%`（小图按 256 档解码，未按 1600 档占用内存）。
- 数据：撤销测试图片后 `ToolbarTest` 回到 1,937 字节；201 页面文件、20 历史、2 图片（1 个测试导入图仍被重做历史引用）、格式 25，无崩溃日志。
- Release `batch21` 已安装并冒烟。

### 3.29 P1 第五批：UI 菜单组件统一与“?”居中（2026-11-04）

- 新增 `CanvasMenuComponents.kt`：`layerMenuEntries` 统一四类对象的图层排序文案；`LayerMenuItems` 供下拉菜单使用（卡片带图标、形状/图片/笔迹不带图标，保持原视觉）；`MenuSectionLabel` 与 `MenuCheckItem` 统一卡片菜单的分组标题与勾选行。
- 迁移点：卡片菜单、形状菜单、图片菜单、笔迹菜单、文本“更多”菜单；行为保持不变。
- 新增 `HelpGlyph.kt`：用原生 Paint 按字体度量（ascent/descent 中点）在圆内绘制“?”，替换设置页帮助圆点与导入对话框帮助圈；修复用户反馈的“圆圈内图形未水平/垂直居中”。
- 无新增单元测试（纯 UI）；422 项 JVM 测试 0 失败。
- 设备冒烟：放大截图确认“?”在圆内居中；形状图层菜单与文本“更多”菜单（含图层排序、复制、删除）显示正常。
- 数据：`ToolbarTest` 1,937 字节、201 页面文件、20 历史、2 图片、格式 25；未增删页面，无崩溃日志。
- Release `batch22` 已安装并冒烟。

### 3.30 P1 第六批：侧栏页面行不可变模型与稳定 key（2026-11-04）

- 新增 `PageRowModel`（id、标题、标签、收藏、回收站、文件夹、图标、是否画布图标）与 `PageRowModel.of(page)`；`SidebarPageItem`、`PageRowActions` 改为只读该模型，不再直接读可变的 `Page` 字段。
- 列表 key 从 `page:${id}:${icon}:${title}` 改为 `page:${id}`（文件夹树为 `folder-page:$name:${id}`），标题/图标变化不再重挂载整行，而是由模型值变化触发该行重组。
- `PageGlyph` 增加按图标/画布标记的重载，原有按 `Page` 的版本保留并委托。
- 新增 `PageRowModelTest` 4 项（字段快照、画布图标判定、重命名产生新值、未变化模型相等）；426 项 JVM 测试 0 失败。
- 设备冒烟：侧栏普通页面列表、选中高亮、点击打开、回收站行（回收站图标 + 恢复按钮）均正常；打开页面后数据未变。
- 数据：201 页面文件、20 历史、2 图片、`ToolbarTest` 1,937 字节、格式 25；未增删页面，无崩溃日志。
- Release `batch23` 已安装并冒烟。

### 3.31 P1 第七批：首页列表/卡片不可变模型（2026-11-04）

- 新增 `DocumentRowModel`（id、标题、摘要、图标、画布标记、收藏、文件夹、修改时间）与 `documentRowModel(page, modifiedTime)`；首页列表行、卡片网格、收藏按钮与选择图标改为读取该模型。
- 列表与网格沿用页面 ID 作为 key；模型在 `visible + modifiedTimes + contentRevision` 变化时重建，重命名、收藏、换图标只重组对应条目。
- `SelectionIndicator` 改为接收图标/画布标记；`FavoriteAction` 改为接收收藏状态与回调，不再持有可变 `Page`。
- 新增 `DocumentRowModelTest` 4 项（字段映射、摘要回退与截断、画布图标、重命名产生新值）；430 项 JVM 测试 0 失败。
- 设备冒烟：列表与卡片网格显示标题/摘要/日期/收藏正常；点击卡片星标收藏立即变为实心、再次点击恢复空心（卡片局部更新）；数据页面未增删。
- 数据：201 页面文件、20 历史、2 图片、`ToolbarTest` 1,937 字节、格式 25；冒烟中被收藏/取消的页面文件被重写，修改时间变为当天；无崩溃日志。
- Release `batch24` 已安装并冒烟。

### 3.32 P1 第八批：标签页模型与搜索索引（2026-11-04）

- `PageTabStrip` 改为接收 `List<PageRowModel>`，标签标题来自模型；选择/关闭回调按 id 解析回页面，行为不变。
- 新增 `PageSearchIndex`：按页面 revision（及共享块数量）缓存每页标题、正文、块内容、卡片标题/正文与共享块内容的小写文本；同一 revision 下连续输入复用索引，仅在页面修改后重建。新增 `filterPagesIndexed`，筛选语义与 `filterPages` 一致。
- 快速搜索在有查询时走索引；空查询保持原路径，不做索引构建。
- 新增 `PageSearchIndexTest` 4 项（多字段匹配、按 revision/共享块重建、与 `filterPages` 结果一致、空查询不建索引）；434 项 JVM 测试 0 失败。
- 设备冒烟：快速搜索 “Toolbar” 与正文词 “SecondBlock” 过滤正确；标签页显示/关闭正常（关闭唯一标签回到全部文档）。
- 数据：201 页面文件、20 历史、2 图片、`ToolbarTest` 1,937 字节、格式 25；未增删页面，无崩溃日志。
- Release `batch25` 已安装并冒烟。

### 3.33 P1 第九批：通用菜单行组件迁移（2026-11-04）

- 新增 `MenuRow`（标签、点击、18dp 图标或自定义前导、尾部提示、禁用/危险/勾选态）于 `CanvasMenuComponents.kt`；禁用态统一使用次要文字色，危险态文字与图标为 Coral，勾选态为 Primary 并加“✓”。
- 迁移文档块操作菜单（更改类型、顺序、文字样式、共享同步、删除等）、块类型菜单与页面行操作菜单（重命名、信息、链接、复制、新标签页、收藏、移出文件夹、回收站）。
- 旧的 19dp 页面菜单图标统一为 18dp；块菜单分组标题改用 `MenuSectionLabel`。
- 无新增单元测试（UI 调整）；434 项 JVM 测试 0 失败。
- 设备冒烟：块操作菜单（含禁用的上移/下移、B/I/U、红色删除块）与侧栏页面菜单显示、点击正常。
- 数据：201 页面文件、20 历史、2 图片、`ToolbarTest` 1,937 字节、格式 25；未增删页面，无崩溃日志。
- Release `batch26` 已安装并冒烟。

### 3.34 P1 第十批：剩余菜单行统一与 P2 模块化计划（2026-11-04）

- `MenuRow` 增加可选 `subtitle`（第二行说明，11sp 次要色），用于首页“新建文档/新的无界页面”两项；原手写 `DropdownMenuItem` 的标题+副标题结构被替换。
- 本批迁移的菜单：`MainActivity` 导出菜单、根新建菜单、页面更多菜单、回收站菜单、文件夹新增菜单（4 行，禁用项用 `enabled=false`）、文件夹选项菜单（重命名/添加文档/删除，删除为危险态）；`AllDocumentsHome` 排序（勾选态）、新建菜单（带副标题）、左滑“移动到文件夹”（勾选态）；`BlocksEditor` 斜杠插入菜单与块插入菜单；文档属性显示设置与文件夹归属；设置页语言/选项下拉。
- 页面更多菜单中的 Export 行保留原实现（需要尾部“›”和 `onGloballyPositioned` 定位子菜单）。
- 冒烟过程中曾误判“新建文件夹后侧栏不显示”：实际是侧栏 `LazyListState` 恢复了上次的滚动偏移，文件夹行在视口上方；向上滚动即出现。未保留任何猜测性的刷新代码，侧栏逻辑与数据无改动。
- 新增 `docs/affine-audit/P2_MODULE_PLAN.md`：包边界、`:core:model`/`:core:ui`/`:data:*`/`:feature:*`/`:app` 拆分顺序、依赖规则、每步验收与风险护栏。
- 无新增单元测试（UI 调整）；434 项 JVM 测试 0 失败。
- 设备冒烟：排序菜单（勾选“最近修改”）、新建菜单（图标+副标题）、侧栏页面菜单、文件夹新增菜单（仅“新建文档”可用）、文件夹选项菜单（分隔线与红色删除）、设置语言下拉（“✓ 简体中文”）显示与点击正常；临时文件夹 `smoke27` 已通过菜单删除，侧栏回到 0 个文件夹。
- 数据：201 页面文件、20 历史、2 图片、All Docs 192、格式 25；未增删页面，未删除图片，无崩溃日志。
- Release `batch27` 已安装并冒烟；APK 3,440,431 字节，SHA-256 `5FD3A2E21092186C3DBEE4EFD15AE007C6FC9C35E4F7C69FF922CDEE74BCB496`。
- 未验证：设备级完整备份恢复（维持 U-PROJ-022）；README 其余功能未逐一回归。

### 3.35 P2 S0 试点：包边界与死代码清理（2026-11-04）

- 新增包 `com.example.tabletworkspace.core`：`DateTimeFormats`、`TextLayoutCache`、`ConnectorSampleCache`、`PageSearchIndex`；新增包 `com.example.tabletworkspace.platform.image`：`PlatformImageDecoder` 与 `ImageSampleBuckets`。
- 仅移动源码与 import：`applicationId`、包名、`filesDir` 文件名、`folders.dat`、`page_metadata`、`ink_preferences` 键名、`images/` 目录、页面格式 25 与备份格式全部未改；没有数据迁移。
- 删除 `BlocksEditor.kt` 的 `if (false)` 死菜单（74 行），不涉及任何保存/加载路径。
- 434 项 JVM 测试 0 失败；日期格式、文本排版缓存、连线采样、搜索索引与图片分档测试继续覆盖搬移后的类。
- 设备冒烟：启动、全部文档、打开 `ToolbarTest`、文档信息与数据库块显示正常；未插入新图片，画布图片解码未做交互验证。
- 数据：201 页面文件、20 历史、2 图片、All Docs 192、`ToolbarTest` 1,937 字节、格式 25；未增删页面，无崩溃。
- Release `batch28` 已安装并冒烟；APK 3,440,431 字节，SHA-256 `822A1744F2C0A7F1D76E28A2CB1D3B098FEF4B7A48DDFED50BD771ED550A5080`。
- 未验证：设备级完整备份恢复（维持 U-PROJ-022）；包搬移后未重跑性能基线。

### 3.36 P2 S0 续：UI 模型迁入 `ui.model`（2026-11-04）

- 新增 `com.example.tabletworkspace.ui.model`：`PageRowModel`、`DocumentRowModel`、`documentRowModel` 与 `Page.previewText()` 从列表/首页文件抽出；只改变源码组织与 import，不触碰存储。
- 434 项 JVM 测试 0 失败；行模型与首页模型测试继续覆盖搬移后的代码。
- 设备冒烟：全部文档列表/卡片切换、标题、摘要、日期、收藏与画布标记显示正常。
- 数据：201 页面文件、20 历史、2 图片、All Docs 192、`ToolbarTest` 1,937 字节、格式 25；未增删页面，无崩溃。
- Release `batch29` 已安装并冒烟；APK 3,440,431 字节，SHA-256 `05EC5E07AECC925BADB501E50A00C6B77FA7BE20C0783232681DFF2DBEACBA8C`。
- 未验证：设备级完整备份恢复（维持 U-PROJ-022）。

### 3.37 P2 S0 续：元数据层迁入 `data.metadata`（2026-11-04）

- 新增 `com.example.tabletworkspace.data.metadata`：`PageMetadataStore`、`PageMetadataRepository`、`PageMetadataSnapshot`/`Codec` 与 `remapToImported` 从根包迁入；仅改变源码组织与 import。
- 存储键名 `page_metadata`/`ink_preferences`、`metadata.dat` 备份格式、页面格式 25、数据目录均未变；没有数据迁移。
- 434 项 JVM 测试 0 失败（元数据 round-trip、旧备份读取、ID 重映射继续覆盖）。
- 设备冒烟：打开 `ToolbarTest` 后文档信息区的 Journal/模板复选与创建/更新时间显示正常。
- 数据：201 页面文件、20 历史、2 图片、All Docs 192、`ToolbarTest` 1,937 字节、格式 25；未增删页面，无崩溃。
- Release `batch30` 已安装并冒烟；APK 3,440,431 字节，SHA-256 `B0A9A82DE2BE689407F3B1111211AB309DD5EF340F5F6229F0201AD23F475039`。
- 未验证：设备级完整备份恢复（维持 U-PROJ-022）。

### 3.38 P2 S0 续：页面仓库与历史迁入 `data.pages`（2026-11-04）

- 新增 `com.example.tabletworkspace.data.pages`：`PageRepository`（单线程写队列）与 `PageHistory` 从根包迁入；仅改变源码组织与 import。
- 写队列语义、历史编码/上限、`AtomicFile` 路径、页面格式 25、数据目录与备份格式均未变；没有数据迁移。
- 更新 MainActivity、PageStore、WorkspaceBootstrap 与 8 个测试文件的 import。
- 434 项 JVM 测试 0 失败（仓库队列与历史持久化测试继续覆盖）。
- 设备冒烟：启动、打开 `ToolbarTest` 后返回全部文档；未触发保存，`ToolbarTest` 修改时间保持 07:02。
- 数据：201 页面文件、20 历史、2 图片、All Docs 192、`ToolbarTest` 1,937 字节、格式 25；未增删页面，无崩溃。
- Release `batch31` 已安装并冒烟；APK 3,440,431 字节，SHA-256 `9026B0710FBFFD54572017FAE005532131E5626CD10354A402D5AC0365F48000`。
- 未验证：设备级完整备份恢复（维持 U-PROJ-022）。

### 3.39 P2 S0 续：图片存储从 PageStore 拆出（2026-11-04）

- 新增 `data.images.PageImageStore`：图片导入/校验、位图 LRU 缓存、缓存统计、孤儿扫描与删除；`ImageGovernance`（含 `containsBytes`）一并迁入。
- `PageStore` 的图片 API 改为委托，公开方法签名不变；页文件、历史、文件夹/模板/共享目录、备份与 URI IO 仍在 `PageStore`。
- 图片目录 `files/images`、孤儿扫描（含历史文件字节检查）、删除逻辑与 20 MB 限制均未变；没有数据迁移。
- 构建副本 `project-fix` 改为镜像同步，清除移动后残留的旧根包源码，避免旧类与新类同时打进 APK。
- 434 项 JVM 测试 0 失败。
- 设备冒烟：启动、打开 `ToolbarTest` 并切换白板，文本/形状/连线渲染正常；当前视口无图片对象，图片解码未做交互验证。
- 数据：201 页面文件、20 历史、2 图片、All Docs 192、`ToolbarTest` 1,937 字节、格式 25；未增删页面，无崩溃。
- Release `batch32` 已安装并冒烟；APK 3,440,431 字节，SHA-256 `852560D52E94D57EB9287E042E92D523FAA8D52DB44AB4E931600AF26DDC64A6`。
- 未验证：设备级完整备份恢复（维持 U-PROJ-022）；画布图片解码。

### 3.40 P2 S0 续：PageStore 迁入 data.pages（2026-11-04）

- `PageStore` 迁入 `com.example.tabletworkspace.data.pages`，与 `PageRepository`/`PageHistory` 同包；补齐页面模型、加载辅助函数、目录编解码器与新包类型的 import。
- 更新 MainActivity、WorkspaceBootstrap、WorkspaceRepository、CanvasEditor、BlocksEditor、CanvasBottomBar、CanvasDialogs、PageImage 的 import。
- 页面/历史文件路径、`AtomicFile` 用法、文件夹/模板/共享目录、备份 ZIP、URI IO、写队列与页面格式 25 均未变；没有数据迁移。
- 434 项 JVM 测试 0 失败。
- 设备冒烟：启动 → 打开 `ToolbarTest` → 返回全部文档；无崩溃日志。
- 数据：201 页面文件、20 历史、2 图片、All Docs 192、`ToolbarTest` 1,937 字节、格式 25；未增删页面。
- Release `batch33` 已安装并冒烟；APK 3,440,431 字节，SHA-256 `8DAD1406DAAEEFA56486D2E071489676B15A166E3D3BC2D16EA173EC0C0069DA`。
- 未验证：设备级完整备份恢复（维持 U-PROJ-022）。

### 3.41 P2 S0 续：备份与 SAF 读写拆入 `data.backup`（2026-11-04）

- 新增 `data.backup.BackupStore`：备份 ZIP 组装、恢复合并、元数据回调与四个 SAF URI 读写方法从 `PageStore` 迁入。
- `PageStore` 的 `backup`/`restore`/`writeBackup`/`readBackup`/`writeText`/`readText` 改为委托，公开签名与 `RestoreResult` 类型不变；新增 `appContext`/`pagesDirectory`/`imagesDirectory` 三个 internal 访问器。
- 备份格式、图片 20 MB 单项/90 MB 总量限制、文件夹合并、共享块/模板重映射与 100 MB 读取上限均未变；没有数据迁移。
- 434 项 JVM 测试 0 失败（备份 round-trip、旧备份兼容、ID 重映射继续覆盖）。
- 设备冒烟：启动 → 打开 `ToolbarTest` → 返回全部文档；无崩溃。
- 数据：201 页面文件、20 历史、2 图片、All Docs 192、`ToolbarTest` 1,937 字节、格式 25；未增删页面。
- Release `batch34` 已安装并冒烟；APK 3,440,431 字节，SHA-256 `E47897F7826A0E5C5977F2790856C8B67D083169FA03BBF09797AA0C232F8CD9`。
- **设备级完整备份导出→清空→恢复仍未执行**（U-PROJ-022）；本批只验证编译、单元测试与启动/页面打开。

### 3.42 导入/导出能力与说明完善（2026-11-06）

- 完整备份恢复新增页面链接重映射：备份页面 ID → 新页面 ID 重写画布 `pageLink` 卡片；指向备份外页面的链接保持原样。写入顺序为“复制页面 → 重映射链接 → 落盘”。
- HTML 导出把文档图片内嵌为 `data:` URI（PNG/JPEG/GIF/WebP/BMP/AVIF 按魔数识别）；缺失图片仍输出 `[Image] 文件名`。
- Markdown 导入只在本地图片存在时把 `![](...)` 恢复为图片块，否则保留为文本；避免缺图块。
- 设置 → 储存新增“导入与导出能力”说明：完整 ZIP 保留页面/图片/目录/模板/共享块/元数据但不含撤销历史与应用设置；Markdown/HTML/快照只保留文本层级内容；单文件 20 MB、ZIP 100 MB / 300 条、ZIP 媒体不导入。
- Obsidian 与 Bear 名称移除“实验性”；帮助文案明确目录、附件、wiki 链接、标签不保留。
- 新增 5 项测试，439 项 JVM 测试 0 失败。
- 设备冒烟：设置储存说明与导入对话框显示正常。
- 数据：201 页面文件、20 历史、2 图片、All Docs 192、`ToolbarTest` 1,937 字节、格式 25；未增删页面，无崩溃。
- Release `batch35` 已安装并冒烟；APK 3,440,443 字节，SHA-256 `65A7351DD68ECB5F506A926E9961848FAF1CA444A1907BFD031A4DC509DF24D8`。
- 未验证：完整 201 页工作区/图片/模板/共享块/页面链接的整包恢复；页面链接重映射仅由单元测试覆盖。

### 3.43 设备级备份恢复与 APK 安装校验（2026-11-07）

- 在独立隔离 AVD（复制验证用 AVD 后 `-wipe-data`，不触碰用户验证工作区）完成小规模全流程：新建 1 页文档并输入 `RESTORE_TEST_BODY` → 设置 → 储存 → 导出备份，得到 `lingye-backup.zip`（722 B，`format.txt=lingye-backup-2`，含 `pages/0.page`）→ `pm clear com.example.tabletworkspace` → 重新启动确认 0 文档 → 恢复该 ZIP → 首页显示 1 文档，打开后标题与正文 `RESTORE_TEST_BODY` 均保留。
- 未覆盖：完整 201 页工作区、图片、模板、共享块、页面链接与页面元数据的整包恢复；页面链接重映射目前只有单元测试证据。
- APK 安装校验：`aapt2 dump badging` 显示 `minSdkVersion 26`、`targetSdkVersion 36`、label“灵页”；`apksigner verify` 通过（APK Signature Scheme v2，debug 证书）；在 Android 15 x86_64 模拟器安装成功。APK 不含原生库，ABI 不构成限制。
- 安装提示：开发版使用 debug 签名，Android/Play Protect 可能提示“未知来源/未经 Play 保护”，需允许安装；若平板上已安装不同签名的旧版本，覆盖安装会失败，只能卸载（会删除本地数据）后重装。
- 清理：测试 AVD 与测试截图/备份文件已在验证后删除；原验证 AVD 未修改。

### 3.44 复选框对齐、Journal 行为与侧栏选中态（2026-11-07）

- 复选框对勾改为 Canvas 几何绘制，消除字体度量导致的偏移；Journal/模板共用。
- 信息面板点击 Journal 改为直接创建或打开当天日记，不再切换 `journalMode`、不再跳到 Journals 列表；已有当天日记时只打开不重复创建。日期选择器修改日期不触发导航。
- 侧栏选中背景改为：未选中行组合时立即透明，选中行 150ms 淡入；避免旧行淡出与新行淡入重叠。
- 设备验证：隔离 AVD 首次创建/重复打开、验证 AVD 已有日记打开、从 Journal 返回 `AlphaRenamed`、640dp 与 200% 字体对勾、10× 慢放侧栏切换；侧栏列表位置不变。测试 AVD 与截图已删除，原验证工作区仍 192 文档。
- 439 项 JVM 测试 0 失败；页面格式 25、数据目录与备份格式未变。
- Release `batch36` 已安装并冒烟；APK 3,440,443 字节，SHA-256 `2125919C77EFAC2A840F0CAEC2AEE7A010C96F2B8AD7BA80A48BA79CFA6AE730`。

1. **先备份当前数据**，即使当前数据不可读；不要直接覆盖非空 `pages/history`。
2. 在 PC 上解包 tar，核对路径与 SHA；只把页面/历史/偏好复制回应用目录。
3. 回滚到 v25：优先使用 `post-repair-20261101.tar`（202 页/191 活动基线）或 `pre-trash-ui-20261101.tar`（203 页/192 活动，当前恢复来源）；需要事故前状态时使用 `post-d2-20261101.tar`；回滚到 v24：使用 `format24-backup` 并安装 v24 构建。
4. 恢复后核对页面数、首 4 字节版本、AlphaRenamed 内容、活动文档数和撤销历史。
5. 图片缺失是历史事实：恢复不会凭空生成 `files/images`；引用缺失图片的页面会显示“图片不可用”。

## 5. 禁止事项

- 不删除/覆盖用户页面、图片、备份、构建产物和测试数据。
- 不用 `connectedDebugAndroidTest` 在没有完整备份的情况下运行。
- 不在真实页面上做批量移入回收站测试；回收站测试只用一次性测试页，并在操作前确认选中数量。
- 不把 v25 页面改写成 v24；不静默覆盖不兼容文件。
- 不把 `PARTIAL`/`UNVERIFIED` 汇总成“已完成”。




