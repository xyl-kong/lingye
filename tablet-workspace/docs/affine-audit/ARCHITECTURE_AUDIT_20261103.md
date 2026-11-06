# 技术架构审计与第一批优化（2026-11-03）

目标：系统性梳理模块、职责和依赖，降低冗余与不合理耦合，在不破坏现有功能的前提下优化资源、渲染与运行性能，并为后续迭代建立可持续的结构。

## 1. 审计范围与方法

- 统计 70 个 Kotlin 源文件、约 20,149 行，检查最大文件、Composable 职责、数据访问路径、重复工具函数、图片/页面/备份 IO、Canvas 绘制与命中路径。
- 以“调用关系 + Android 主线程约束 + 页面格式 25 数据安全”为判断标准。
- 本轮只做低风险、可编译、可回归的改造；高风险拆分列入后续清单，不一次性重写。

## 2. 当前结构

### 2.1 文件体量（前 8）

| 文件 | 行数 | 主要职责 |
|---|---:|---|
| `MainActivity.kt` | 7,455 | Workspace 状态/导航、文档页、画布输入/渲染、工具栏、菜单、对话框、存储编排 |
| `SettingsPage.kt` | 1,490 | 设置弹窗与各分类 |
| `ColorPicker.kt` | 1,180 | 华为风格取色板与颜色算法 |
| `AllDocumentsHome.kt` | 912 | 全部文档列表/卡片、排序、筛选 |
| `VisualDesign.kt` | 804 | 主题、颜色、图标路径、通用视觉组件 |
| `TextToolbarUi.kt` | 752 | 文本选区/块级工具条 |
| `DocPropertiesSidebar.kt` | 638 | 文档属性右侧栏 |
| `DocumentInfoCard.kt` | 423 | 文档信息面板（本轮新拆出） |

### 2.2 逻辑分层（实际）

- **模型/算法**：`Page`、`Card`、`Connector`、`CanvasTransform`、`ConnectorSnapping`、`SmartGuides`、`TextFormatting`、`MarkdownCodec`、`PageHistory` 等，基本独立于 Android UI。
- **数据访问**：`PageStore`（页面/图片/历史/备份/文件夹）、`DocPropertyStore`、`CustomColorStore`、`PageMetadataStore`，大量使用 `SharedPreferences` 与文件系统。
- **UI**：`MainActivity` 同时是 Workspace 容器、文档页、画布编辑器和导航控制器；其余文件多为独立页面/面板。
- **平台**：Android `Context`、`Uri`、`BitmapFactory`、`AtomicFile`、`SharedPreferences` 直接出现在 UI 层。

### 2.3 依赖方向

- 模型 → 无 UI 依赖（良好）。
- 数据 → 模型 + Android（基本合理）。
- UI → 数据/模型（直接调用 store/prefs，缺少中间层）。
- `MainActivity` 是最大集成点，几乎依赖所有模块；其他页面依赖模型和数据。

## 3. 发现的问题

### 3.1 结构性

1. **MainActivity 过大**：7,455 行，混合 Workspace 状态、页面导航、文档编辑、Canvas 输入/渲染、菜单、对话框和存储调用。单文件修改风险高，代码审查困难。
2. **缺少 Repository/UseCase 层**：Composable 直接调用 `store.save`、`SharedPreferences`、`BitmapFactory`，UI 与存储格式耦合；页面保存、日志/模板、文档属性没有统一入口。
3. **导航状态分散**：`allDocsHome`、`favoriteMode`、`trashMode`、`journalMode`、`templateMode`、`overview`、`settingsPage`、`canvas` 等布尔值组合决定视图，容易出现互相覆盖和遗漏清理。
4. **元数据分散**：页面格式 25、`ink_preferences`、每页 `doc_properties_<id>`、`page_metadata`、`canvas_motion` 各存一部分；备份 ZIP 不包含偏好中的文档属性、Journal/模板标记。
5. **CanvasEditor 仍是巨型 Composable**：输入、绘制、命中、工具条、弹窗和状态都在一起；后续功能继续叠加会扩大技术债。

### 3.2 重复实现

- 日期/时间格式化在 3 处独立实现（文档信息、文档列表、概览），格式不一致。
- 图片加载在每个 Composable 内用 `produceState` 各自解码，同一图片多实例重复解码。
- 工具条/图标/菜单行有大量重复的尺寸、圆角、颜色与 `Surface` 包装，缺少统一 UI token/组件。
- 页面行菜单与画廊行、属性行各自维护相似的行样式和点击语义。

### 3.3 性能与资源

- **已较好**：日常自动保存在 `Dispatchers.Default` 编码、`Dispatchers.IO` 写盘；图片导入在 IO；LazyColumn/LazyVerticalGrid 使用 key。
- **问题**：
  - 显式 `store.save(page)` 仍在多处 UI 回调主线程执行；大页面会阻塞输入。
  - 图片解码无内存缓存，页面切换/重组时重复 `BitmapFactory.decodeFile`。
  - Canvas 每帧解析全部连线、重新计算选区包围盒；命中测试线性遍历所有对象；文本排版在绘制/测量中重复。
  - 页面列表的 `visiblePages` 以 `pages.size`、`revision` 等为 key 重算，标题/图标变化依赖 revision，可能产生额外重组。
  - 图片文件与页面生命周期未绑定，删除页面/对象不会回收孤立图片。
- **数据安全**：Journal/模板标记不在页面文件内，也不在备份 ZIP；恢复后丢失。

### 3.4 可测试性

- 已有 371 项 JVM 测试，覆盖几何、变换、连接线、属性、导入、日期等纯逻辑，质量较好。
- UI 状态与存储调用嵌入 Composable，难以单元测试；没有截图/仪器化回归基线。

## 4. 本轮已完成的优化

| ID | 改动 | 效果 |
|---|---|---|
| AR-01 | 新增 `DateTimeFormats.kt`，统一文档信息、文档列表、概览、Journal 的日期格式 | 删除 3 处重复实现；新增 3 项测试 |
| AR-02 | `PageStore` 增加 `LruCache<String, Bitmap>`，预算为最大内存 1/8、限制 4–24 MB；`image()` 命中缓存 | 减少重复解码；页面切换/多实例图片复用 |
| AR-03 | 新增 `PageImage.kt` 统一 `rememberStoredImage` 的 IO 加载 | 图片加载路径集中，便于后续替换/监控 |
| AR-04 | 从 `MainActivity` 抽出 `DocumentInfoCard.kt`（文档信息、属性行、自定义值编辑、Journal/模板/显示设置） | MainActivity 减少约 400 行；职责清晰的文档信息模块 |
| AR-05 | 抽出 `PageListComponents.kt`（页面行操作菜单、页面标签栏） | 页面列表 UI 与 Workspace 主容器解耦 |
| AR-06 | 清理抽取后的重复导入/私有格式化函数 | 编译无警告性重复；模块边界更明确 |

指标：MainActivity 7,989 → 7,455 行；新增 3 个模块；70 个 Kotlin 文件；371 项测试 0 失败；Release 3,407,663 字节（资源未变，代码结构变化）。

## 4.1 第二批：页面元数据备份

- 新增 `PageMetadataSnapshot`/`PageMetadataSnapshotCodec`：统一文档属性 raw、Journal 日期、模板标记，版本化二进制、UUID/日期/属性校验、2 MB 单项与 20 MB 总量限制。
- `BackupArchive` 升为 `lingye-backup-2`，新增可选 `metadata.dat`；解码兼容 `lingye-backup-1`。
- `PageStore.backup` 收集每页元数据；`restore` 把原页面 ID 映射到新页面 ID，批量 commit 文档属性与 Journal/模板标记。
- 新增 3 项元数据 round-trip/拒绝测试，总计 374 项 JVM 测试 0 失败。
- 未在设备上执行完整备份恢复（会复制全部页面，风险高）；JVM 格式与 round-trip 已验证，设备级恢复仍为未验证项。
- 追加抽出 `TextBoxStyleUi.kt`（378 行：文本缩放/对齐/自动高度、填充/阴影/边框/圆角样式面板）；MainActivity 7,455 → 7,112 行。
- 追加 `PageMetadataRepository`：Workspace 不再直接访问 `DocPropertyStore`/`PageMetadataStore`，文档属性、Journal、模板与备份快照统一经由仓库；`PageStore.backup` 改为接收元数据快照，不再自行读取偏好。
- 追加抽出 `CanvasChrome.kt`：画布参数分隔线、参数表面、粗细点、参数 chip、颜色行、缩放控件；MainActivity 7,112 → 7,010 行。
- 第三批指标：74 个 Kotlin 文件、约 20,361 行；MainActivity 7,010 行（累计减少 979 行）；374 项测试 0 失败；Release 3,407,663 字节。
- 发布：`releases/lingye-v0.15-architecture-batch3-release-20261103.apk`，3,407,663 字节，SHA-256 `F1E7075D91910CF7520BDE1DA3128DD25A0E785680AC43749413DDD03403C7C9`。

## 4.2 第四批：文档块编辑器拆分

- 新增 `BlocksEditor.kt`：从 `MainActivity` 整体移出块类型图标、`BlocksEditor`、块插入菜单、共享块编辑器、共享文本注解、数据库数字字段、`DatabaseEditor`，共 670 行。
- 抽出的顶层函数改为 `internal`，仍与 `Workspace`/`CanvasEditor` 同包，避免本轮引入迁移层；导入补齐后 `compileDebugKotlin` 通过。
- 文档块编辑、数据库编辑器、块菜单、共享块编辑器代码从 `MainActivity` 解耦，后续可独立演进为 `ui/document` 子模块。
- 第四批指标：75 个 Kotlin 文件；MainActivity 7,010 → 6,425 行（累计减少 1,585 行）；374 项 JVM 测试 0 失败。
- 发布：`releases/lingye-v0.15-architecture-batch4-release-20261103.apk`，3,407,663 字节，SHA-256 `3DCDC1BD878802D856AA2C1CA3511BC22E9337622043D66661F178AFFB5868DE`；模拟器已安装同一 APK。

## 4.3 第五批：CanvasEditor 外移与选择工具条拆分

- 新增 `CanvasEditor.kt`（3,697 行）：整个画布 Composable、`SystemBarsEffect`、`ConnectorRenderData`、`ConnectorMarkerRow`、`MindmapLayoutPreview`、`TextSpanVisualTransformation` 全部移出 `MainActivity`；`CanvasEditor` 改为 `internal`。
- 新增 `CanvasSelectionToolbars.kt`（383 行）：把页面链接、思维导图节点、Frame、图片、笔迹五类选中工具条拆成独立 Composable，显式接收页面、视图变换、尺寸与选择回调；工具条内部菜单状态仍各自持有。
- `Rect.toIntRectPx()` 改为 `internal`，供 `MainActivity` 与画布共用；删除未被引用的 `SelectionToolbarDivider`。
- `MainActivity` 6,425 → **2,652 行**；`CanvasEditor` 不再与工作区导航/页面列表同文件。77 个 Kotlin 文件、约 21,508 行（含重复导入头）。
- 冒烟验证（`ToolbarTest` 测试页）：Frame 工具条可选中、重命名、定位、打开 Frame 导航、进入演示并退出、复制/删除；笔迹工具条可改色、加减粗细、图层菜单、删除；思维导图节点工具条可加子节点、改布局、删除整棵树；页面链接工具条可打开/复制/删除链接文档。
- 图片工具条随批编译通过，但测试页没有可选的图片对象；未为验证而插入新图片文件，故该项未做交互冒烟，留待后续带图片对象时补测。
- 删除测试创建的 Frame/笔迹/思维导图/链接卡片后，`ToolbarTest` 页面文件仍为 2,173 字节、格式 25；200 页面文件、20 历史、1 图片、All Docs 192 未变。
- 发布：`releases/lingye-v0.15-architecture-batch5-release-20261103.apk`，3,407,663 字节，SHA-256 `11C61B7F6D8F5E1F2C98B2E6A3845041B46FC297531B12DC78B79E07581D14DF`；模拟器已安装同一 APK。

## 4.4 第六批：画布绘制、点击输入与底部工具条拆分

- 新增 `CanvasRendering.kt`（686 行）：`DrawScope.drawCanvasContent(...)` 承接画布全部绘制内容（网格、形状、文字排版、图片、连线、笔迹、选择框、吸附参考线、框选矩形、橡皮轨迹）；参数为纯值，不持有 Compose 状态。
- 新增 `CanvasInput.kt`（81 行）：`handleCanvasDoubleTap` 与 `handleCanvasTap` 承接点击/双击命中逻辑（链接文档、文字链接、卡片待办、多选切换），选择结果通过回调写回。
- 新增 `CanvasBottomBar.kt`（570 行）：底部工具条、六类参数条（文本、Frame、连线、形状、画笔/橡皮、多选）与内容画廊/更多菜单入口整体移出；`paramBarMaxWidth`、`frameBarShift` 的计算随之下沉。
- `CanvasEditor.kt` 3,697 → **2,885 行**；`MainActivity` 保持 2,652 行。80 个 Kotlin 文件、约 22,033 行（含重复导入头）。
- 冒烟验证：画布网格/形状/文字/连线/笔迹/选中手柄渲染正常；底部选择/Frame/文本/画笔/橡皮/形状工具切换与对应参数条正常；内容画廊可打开并显示官方模板；多选参数条（复制/对齐/组合/绑定连线/保存模板/删除）可显示；点击画布可选中菱形/矩形并形成多选。
- 笔迹/橡皮状态机、框选/缩放拖拽与键盘快捷键仍在 `CanvasEditor` 内，是下一批输入层拆分对象；图片工具条仍未交互验证（同第五批原因）。
- 数据：`ToolbarTest` 仍为 2,173 字节、格式 25；200 页面文件、20 历史、1 图片、All Docs 192 未变，未增删页面、未删除图片。
- 发布：`releases/lingye-v0.15-architecture-batch6-release-20261103.apk`，3,424,047 字节，SHA-256 `12EFCB2B1254A8B66C11A35DAD4D7D946236C8113DB087FBD922DD52B29F5BB9`。

## 4.5 第七批：手势状态机与画布对话框拆分

- 新增 `CanvasPointerInputs.kt`（508 行）：`Modifier.canvasGestureInput(...)` 承接演示缩放、笔迹/橡皮状态机、移动/选择拖拽、框选、缩放/旋转手柄、连线拖拽、形状/Frame/文本拖拽创建；状态通过 `MutableState` 适配器（`canvasStateOf`）与 `CanvasEditor` 共享，手势逻辑本身不再内联在 Composable 中。
- 新增 `CanvasDialogs.kt`（298 行）：`CanvasDialogHost` 承接思维导图改接/删除、链接已有文档、Frame 导航、Frame 重命名、模板管理/重命名/删除/保存、保存选区模板、图片裁剪九个对话框；状态同样经 `MutableState` 适配器共享。
- `CanvasEditor.kt` 2,885 → **2,508 行**；`MainActivity` 保持 2,652 行。82 个 Kotlin 文件、约 22,462 行（含重复导入头）。
- 冒烟验证：画笔可正常落笔、区域橡皮可擦除；框选可选中图形；拖拽图形后位置变化、撤销可恢复；链接已有文档与 Frame 导航对话框正常显示和关闭。
- 仍留在 `CanvasEditor` 的输入/宿主：长按快捷菜单位置、键盘快捷键、labelEditor、卡片原位编辑、模板/裁剪以外的局部弹窗；图片工具条仍未交互验证。
- 数据：`ToolbarTest` 页面因绘制擦除与拖拽撤销，文件从 2,173 字节变为 1,841 字节；文档标签页的标题、数据库等块内容与画布图形目视完整，格式 25；200 页面文件、20 历史、1 图片、All Docs 192 未变，未增删页面、未删除图片。
- 发布：`releases/lingye-v0.15-architecture-batch7-release-20261103.apk`，3,424,047 字节，SHA-256 `C7A1F346C04838CC51551265625843B65D5AE329B7DB8630A4F430664AF4EFD2`。

## 4.6 第八批：CanvasEditorState 与快捷键/原位编辑拆分

- 新增 `CanvasEditorState.kt`（72 行）：统一持有画布缩放/平移、选择、手势、菜单、对话框与裁剪状态；`CanvasEditor` 改为通过委托读写该状态，`CanvasPointerInputs`/`CanvasDialogs` 直接接收状态对象，删除 `canvasStateOf` 适配器。
- 新增 `CanvasShortcutHandler.kt`（153 行）：键盘快捷键判断从 Composable 移出，接收状态对象与 `CanvasShortcutActions`（停止/切换演示、复制、组合、层级、对齐、微移、撤销/重做、删除、缩放、全选、工具切换）。
- 新增 `CanvasInlineEditor.kt`（244 行）：卡片/文本/思维导图的原位编辑器从 `CanvasEditor` 移出；打开编辑时自动请求焦点并弹出输入法，文本编辑器补上“完成”按钮；Escape 在编辑框内只隐藏输入法（Android IME 行为），退出编辑使用“完成”按钮或点击画布。
- 修复用户反馈的键盘问题：文本框选中后按 Delete 现在会删除对象；双击进入原位编辑后输入、退格正常；编辑中按 Delete 编辑文字而不是删除对象；点击“完成”退出编辑，再按 Delete 删除对象。
- `CanvasEditor.kt` 2,508 → **2,301 行**；`MainActivity` 保持 2,652 行。85 个 Kotlin 文件。
- 冒烟验证：选中菱形/文本框按 Delete 删除、Ctrl+Z 恢复；双击文本框弹出键盘并可输入/退格；点击“完成”退出编辑后按 Delete 删除文本框；H/V 工具快捷键生效；Escape 仅隐藏输入法，不退出编辑（已在文档中注明）。
- 数据：`ToolbarTest` 在测试与撤销后为 1,841 字节、格式 25，标题/数据库等块与主要画布图形目视正常；测试中新增的文本框经历多次撤销后仍以不可选中形式显示，已作为观察项记录；200 页面文件、20 历史、1 图片、All Docs 192 未变，未增删页面。
- 发布：`releases/lingye-v0.15-architecture-batch8-release-20261103.apk`，3,424,047 字节，SHA-256 `BC41D6A0726A4A01554332A230BCD0308DF5C156DB9F188309A83E8F2086966C`。

## 4.7 第九批：不可选中文本框诊断与连线工具条拆分

- 诊断 batch8 的“撤销后文本框不可选中”：页面形状日志显示该文本不在 `page.shapes` 的任何坐标上，点击命中为 null；实际是原位编辑器的 `cardEdit` 在删除/撤销后仍指向已变化的索引，编辑器 Surface 以错误位置残留并被误认为画布文本。重启进程后该“文本”消失，确认不是页面数据。
- 修复：删除选中对象时清空 `cardEdit`；新增 `LaunchedEffect(page.shapes.size, cardEdit)`，当编辑目标索引失效或类型不可编辑时自动退出编辑，避免残留编辑器覆盖画布。
- 新增 `CanvasConnectorToolbar.kt`（329 行）：连线选中工具条与“连接线文字”对话框从 `CanvasEditor` 移出，`CanvasEditor` 内最后一个 `AlertDialog` 清零。
- `CanvasEditor.kt` 2,301 → **2,109 行**；`MainActivity` 保持 2,652 行；86 个 Kotlin 文件、约 22,814 行。
- 冒烟验证：选中连线显示工具条，“连接线文字”对话框可打开/取消；双击文本框编辑后点“完成”，按 Delete 删除文本对象，画布不残留编辑器；Ctrl+Z 可恢复文本。
- 数据：`ToolbarTest` 仍为 1,841 字节、格式 25；200 页面文件、20 历史、1 图片、All Docs 192 未变，未增删页面；无崩溃日志。
- 发布：`releases/lingye-v0.15-architecture-batch9-release-20261103.apk`，3,424,047 字节，SHA-256 `79370744FD4B269957381A21CD814F1051803125F07C1A61F97A6AA70D58CDA6`。

## 4.8 第十批：文本选区格式浮层拆分

- 核对画布交互：`CanvasEditor` 没有长按快捷菜单实现，右键/长按菜单只存在于文档块（`BlocksEditor`）与工具提示，因此本批不虚构画布长按菜单，改为拆分剩余最大的局部状态——文本选区格式浮层。
- 新增 `CanvasTextFormattingToolbar.kt`（606 行）：字号/对齐/加粗/代码/公式/斜体/链接/删除线/下划线/高亮/更多等格式浮层与全部子菜单移出 `CanvasEditor`；复制、删除等操作通过 `onSelectionChange` 回写，不再直接改选择状态。
- `CanvasEditor.kt` 2,109 → **1,654 行**；`MainActivity` 保持 2,652 行；87 个 Kotlin 文件、约 22,965 行。
- 冒烟验证：双击文本框进入编辑后 `Ctrl+A` 选中全部文字，格式浮层正常显示；点“完成”退出后文本对象保留；Ctrl+Z 可恢复格式变更；无崩溃。
- 卡片/形状工具条仍留在 `CanvasEditor`，下一批可继续拆分。
- 数据：`ToolbarTest` 仍为 1,841 字节、格式 25；200 页面文件、20 历史、1 图片、All Docs 192 未变，未增删页面。
- 发布：`releases/lingye-v0.15-architecture-batch10-release-20261103.apk`，3,424,047 字节，SHA-256 `2690875C925387149653EFEB62A3BC0322BB86EBD830032AECF379B8494C980C`。

## 4.9 第十一批：卡片/形状选择工具条拆分

- 新增 `CanvasCardShapeToolbars.kt`（449 行）：卡片选择工具条（原位编辑、勾选待办、保存模板、复制、缩放显示、背景色、图层/笔记样式/阴影/圆角、插入文档、锁定、删除）与形状工具条（类型切换、填充/边框色、边框样式、粗细、绘制连线、复制、图层、锁定、删除）移出 `CanvasEditor`。
- 选择回写统一走 `onSelect`，编辑入口走 `onEditCard`，保存模板与绘制连线分别走 `onSaveTemplate`/`onDrawConnector`；动画保留在调用点。
- `CanvasEditor.kt` 1,654 → **1,343 行**；`MainActivity` 保持 2,652 行；88 个 Kotlin 文件、约 23,103 行。
- 冒烟验证：新建矩形的工具条正常显示并可选中；从模板画廊“Cards”插入“笔记”卡片后，卡片工具条正常显示；测试后已删除矩形与卡片。
- 数据：`ToolbarTest` 仍为 1,841 字节、格式 25；页面文件 200 → **201**（新增一个被移入回收站的空白测试页，All Docs 仍为 192），历史 20、图片 1 未变。
- 发布：`releases/lingye-v0.15-architecture-batch11-release-20261103.apk`，3,424,047 字节，SHA-256 `6F5AD79B790CC5866EDAE6EF2131CAE76354D0D7550AAE99FCEDC7C1C5C49692`。

## 4.10 第十二批：文本对象工具条与相邻控件拆分

- 新增 `CanvasTextToolbar.kt`（412 行）：文本对象工具条（Display in Page、文本颜色、自动高度、缩放/Scale、定位、锁定、更多）与文本左右“相邻添加”按钮、锁定角标整体移出 `CanvasEditor`。
- `CanvasTextToolbar` 与 `CanvasTextAdjacentControls` 通过 `onSelect` 回写选择，绘制连线通过 `onDrawConnector(index)` 回调；文本锁定角标解锁通过 `onChanged` 写回。
- `CanvasEditor.kt` 1,343 → **1,069 行**；`MainActivity` 保持 2,652 行；89 个 Kotlin 文件、约 23,241 行。
- 冒烟验证：选中文本后工具条正常显示（Displayed in Page、颜色、Size、100%、定位、锁定、更多），左右“+”相邻添加按钮正常显示；无崩溃。
- 数据：`ToolbarTest` 仍为 1,841 字节、格式 25；页面文件 201（含回收站空白测试页）、历史 20、图片 1、All Docs 192 未变。
- 发布：`releases/lingye-v0.15-architecture-batch12-release-20261103.apk`，3,424,047 字节，SHA-256 `067732A211B277F2199B5B4DEF312056E12664BA24859635D5E2CF51797484F5`。

## 4.11 第十三批：内联编辑器交互修正与底部宿主拆分

- 按用户反馈与 AFFiNE 官方交互移除内联编辑器的“完成”按钮：编辑中不再出现任何常驻按钮，改为点击画布外部、`Esc` 或 `Ctrl+Enter` 退出；文本框编辑层不再叠加阴影，编辑期间隐藏对象选中外框与四角手柄（`CanvasRendering` 在 `i == cardEdit` 时跳过选中框绘制），避免编辑器与选中框双重描边。
- 新增 `CanvasObjectToolbarHost.kt`（222 行）：卡片、形状、页面链接、思维导图节点、Frame、文本、文本相邻添加按钮、图片、笔迹、连线九类对象工具条与文本选区格式浮层统一由同一个宿主渲染；`CanvasEditor` 只计算选中索引并回传回调。
- 新增 `CanvasPresentationBar.kt`（63 行）：Frame 演示模式底部控制条（上一页/下一页、适应/填充、退出）移出 `CanvasEditor`；同时修正原实现“下一页”按钮误用“‹”的问题，现为“›”。
- `CanvasEditor.kt` 1,069 → 928 行；`MainActivity` 2,652 行；91 个 Kotlin 文件。
- 排查“画布上无法选中的黄色矩形”：解码 `ToolbarTest` 页面确认它是页面中既有的 `rect` 形状（`0xfffdf5dc`，世界坐标 172.6,199.2,261.0×296.8），不是浮层残留；点击后能正常选中并弹出形状工具条。
- 冒烟验证：选中文本/形状时工具条与左右“+”正常；双击文本进入编辑时无“完成”按钮、无选中外框，可输入与退格，点击画布外部、`Esc`、`Ctrl+Enter` 均能退出并收起输入法；Frame 演示可进入，适应/填充切换与退出正常。
- 说明：模拟器上 Gboard 在个别状态下会先吞掉 `Esc`（只收起键盘），重新聚焦编辑框后 `Esc` 可正常退出；`Ctrl+Enter` 与点击外部在所有测试中稳定。
- 数据：`ToolbarTest` 仍为 1,841 字节、格式 25；201 页面文件、20 历史、1 图片、All Docs 192 未变；测试中创建并删除的 Frame 未留下内容，页面文件尺寸回到测试前。
- 发布：`releases/lingye-v0.15-architecture-batch13-release-20261103.apk`，3,424,047 字节，SHA-256 `38E87B07B1CC3358F553B347471F15E5A69EF98CB26A57DA7988D4937FC61734`。

## 4.12 第十四批：单选目标与画布命令抽取

- 新增 `CanvasSelectionTargets.kt`（40 行）：卡片、形状、Frame、链接卡片、思维导图节点、文本、图片、笔迹、连线九类“当前单选目标索引”集中计算，替换 `CanvasEditor` 中 9 段重复的 `selection?.takeIf … getOrNull(it)?.type == …` 判断，并删除未使用的本地 `isLocked`。
- 新增 `CanvasEditorCommands.kt`（162 行）：删除、复制、组合/取消组合、图层前后、对齐、微移、全选、以视口中心缩放、Frame 定位、演示开始/停止/翻页从可组合函数移出，成为持有 `CanvasEditorState` 的普通类，可独立测试；快捷键、工具条、底部栏与对话框共用同一实例。
- `CanvasEditorState` 新增 `viewportSizePx`、`frameRatio`、`lastNavigationTool`、`selectionRestored` 与演示（`presentationWasFocus/PrevTool/PrevScale/PrevPan/FitMode`）状态；`CanvasEditor` 不再用零散 `remember` 保存这些值。
- `CanvasEditor.kt` 928 → 815 行；`MainActivity` 保持 2,652 行；93 个 Kotlin 文件。
- 新增 `CanvasEditorCommandsTest`（7 项）：单选目标映射、多选/越界为空、删除后清理选区与 `cardEdit`、缩放保持视口中心、全选跳过锁定对象、演示开始/停止恢复工具与视口、Frame 定位选中。
- 冒烟验证：选中文本/形状工具条正常；Delete 删除所选、Ctrl+Z 恢复；Ctrl+A 全选显示多选工具条；缩放 51%→64%→51%；新建 Frame 选中、演示（2/2）进入、退出、删除两个测试 Frame 后页面文件回到 1,841 字节。
- 排查记录：此前“Frame 点不中”是画布处于“移动”工具时点击不选中（底部第一个按钮在移动/选择间切换，创建 Frame 后工具状态容易被连点切换），不是命中或绘制缺陷；切到“选择”后 Frame 边框/标题均可命中。
- 数据：`ToolbarTest` 仍为 1,841 字节、格式 25；201 页面文件、20 历史、1 图片、All Docs 192 未变；未增删页面。
- 发布：`releases/lingye-v0.15-architecture-batch14-release-20261103.apk`，3,424,047 字节，SHA-256 `FF59CBF3404C0EE0E9A1B7E5EFF95663178D9D60B484760A8359B49748289C13`。

## 4.13 第十五批（P0）：页面写队列与保存链路收口

- 新增 `PageRepository.kt`：页面与撤销历史的全部写入进入单线程队列（`lingye-page-writer`）。主线程只调用 `page.snapshot()`；编码与 `AtomicFile` 落盘都在写线程，主线程不再编码页面。
- 同步 API（`save`/`saveAll`/`saveHistory`/`deletePermanently`）等待自己的写入并重抛失败，保留原有“保存成功后再导航/修改结构”的语义；异步 API（`saveAsync`/`saveHistoryAsync`）供 400 ms 自动保存使用，失败经回调显示，不再吞错。
- `MainActivity` 中 21 处 `store.save(...)` 与 1 处永久删除全部改走仓库；`saveCurrent()` 保存页面与历史后等待落盘；自动保存只在写入成功的回调里推进 `savedRevision`，失败时保留未保存状态以便下次重试；`onStop` 追加 `flush(3 s)`；备份恢复前先 `flush`。
- 单队列保证提交顺序，较早的自动保存快照不会覆盖之后的重命名、复制、移入回收站或永久删除。
- 新增 `PageRepositoryTest`（8 项）：同步等待与失败重抛、页面→历史顺序、提交顺序、`flush` 等待、异步失败回调、删除排在挂起保存之后、`flush` 超时、历史编码。
- 测试：389 项 JVM 测试 0 失败。
- 设备冒烟：`ToolbarTest` 文本框输入 A、B，文件 1,841→1,843 字节且含“AB”；输入后立即切页，文件已落盘；`force-stop` 重启后文本保持；Ctrl+Z 两次恢复原文，文件回到 1,841 字节；无崩溃日志。
- 数据：201 页面文件、20 历史、1 图片、格式 25；未增删页面。
- 发布：`releases/lingye-v0.15-architecture-batch15-release-20261104.apk`，3,424,047 字节，SHA-256 `3665ED80AE930C8B4A0A08F545A527614AAB781E3849CA0A0414C803CAD26462`。

## 4.14 第十六批（P0）：页面加载与会话/工具偏好收口

- 新增 `WorkspaceRepository.kt`：页面加载 `load()`（委托 `loadWorkspaceBootstrap`）、工作区会话 `readSession()/writeSession()`、界面语言 `readLanguage()/writeLanguage()`、画布工具偏好 `readCanvasTools()` 与各项 `save…()`。`MainActivity` 不再直接读写这些 SharedPreferences 键。
- 偏好访问抽象为 `WorkspacePreferenceStore` 接口，生产实现 `SharedPreferencesWorkspacePreferences`，单元测试使用内存实现；工具偏好的默认值与钳制（高亮笔 10–30、画笔 1–12）移入仓库并保持原值。
- 修复会话恢复：现在会读回 `workspace_session_document_scroll_index`，此前只写不读，文档滚动位置索引重启后丢失。
- 仍保留：`preferences.readWorkspaceSettings/writeWorkspaceSettings` 直接使用 SharedPreferences（已有扩展封装键名）；页面元数据（文档属性、Journal/模板标记）仍由 `PageMetadataRepository` 单独管理，尚未与备份/恢复统一。
- 新增 `WorkspaceRepositoryTest`（6 项）：会话往返、默认值、语言回退与持久化、工具默认与越界钳制、工具写入回读、`load()` 委托。
- 测试：395 项 JVM 测试 0 失败。
- 设备冒烟：设置中切换 English → `language=en` 写盘 → `force-stop` 重启后仍为 English 并恢复设置页（会话）→ 切回简体中文；画笔“仅手写笔”切换使 `pen_only` false→true→false 即时写盘。
- 数据：201 页面文件、20 历史、1 图片、格式 25；未增删页面，无崩溃日志。
- 发布：`releases/lingye-v0.15-architecture-batch16-release-20261104.apk`，3,424,047 字节，SHA-256 `4943F1489C5ED11C5535A80CA187C85DD8D47BF1698DA6359FA2BFAB9D8291B6`。

## 4.15 第十七批（P0）：设置与元数据收口

- `WorkspaceSettings` 的编解码从 `SharedPreferences` 扩展改为 `WorkspacePreferenceStore` 扩展：读取走类型化 getter，写入使用新增的 `write {}` 编辑器一次提交 39 个键（键名、默认值与归一化规则不变）。`WorkspaceRepository.readSettings/writeSettings` 成为唯一入口，MainActivity 不再直接读写设置。
- 画布工具偏好（画笔/橡皮/高亮/墨色/粗细）的 7 个写入点全部改经 `WorkspaceRepository.save…`，不再出现 `preferences.edit()`。
- 备份恢复的元数据写入从 `PageStore.applyRestoredMetadata` 移到 `PageMetadataRepository.applyRestored`：文档属性仍写 `ink_preferences`，Journal/模板仍写 `page_metadata`，但 `PageStore.restore` 只接收 `(snapshot, original, imported) -> Unit` 回调，页面存储不再依赖元数据存储实现。
- 新增纯函数 `PageMetadataSnapshot.remapToImported`（旧页面 ID → 恢复后新 ID），并补 2 项测试：正常映射、未知页面的条目被丢弃。
- 测试：400 项 JVM 测试 0 失败。
- 设备冒烟：外观主题“深色/跟随系统”切换使 `theme_mode` 在 `dark`/`system` 间写入并同步界面；应用启动、页面列表与画布正常。
- 未验证：设备级完整备份导出→清空→恢复（需独立数据副本，维持 U-PROJ-022）。
- 数据：201 页面文件、20 历史、1 图片、格式 25；未增删页面，无崩溃日志。
- 发布：`releases/lingye-v0.15-architecture-batch17-release-20261104.apk`，3,424,047 字节，SHA-256 `42B9C5C0DB8B2F45B50E7F2A0DB8985CD8966F53E53D4304058E904664967374`。

## 4.16 第十八批（P1）：文本排版与连线采样缓存

- 新增 `TextLayoutCache.kt`：有界 LRU（400 项）缓存文字的换行结果、样式 run 与每段测宽；键包含正文、`textSpans`、像素宽度、字号、字体族、字重与斜体，编辑/缩放/改格式自动失效。静态画布不再每帧逐字符测量。
- 新增 `CanvasTypefaces.kt`：按字体族/字重/斜体缓存 `Typeface`，替换每帧每个 run 的 `Typeface.create`。
- 新增 `ConnectorSampleCache.kt`（LRU 256，键为 `Connector` 数据类）：命中测试与绘制共用采样结果；绘制时额外按连线缓存本帧渲染数据（描边与内容两层原各算一次），并用每帧一次构建的 `id → Shape` 索引替代每个连线的线性扫描。
- 修复用户报告的“文本框左右＋停在原位”：页面对象是原地修改，Compose 强跳过按引用比较，相邻按钮缺少可观察几何参数。现由 `CanvasObjectToolbarHost` 计算值类型 `AdjacentTextAnchor`（`geometryRevision` 变化时重算），拖动时随其他工具条隐藏、松手后出现在新位置；贴近画布边缘时隐藏该侧按钮而不是挤压到文本框上。
- 测试：新增 `TextLayoutCacheTest` 5 项、`ConnectorSampleCacheTest` 3 项；408 项 JVM 测试 0 失败。
- 设备冒烟：51%/80%/51% 缩放文字与连线渲染正常，连线可点选；拖动与方向键微移后相邻＋跟随；右边缘隐藏右侧＋。
- 未做帧时间前后对比，不声明数值收益；命中测试空间索引（网格/R-tree）尚未实现，当前命中仍为 O(对象数)，只在点击时执行。
- 数据：201 页面文件、20 历史、1 图片、格式 25；`ToolbarTest` 因手动测试与拖拽验证为 1,937 字节（文本已撤销恢复）。
- 发布：`releases/lingye-v0.15-architecture-batch18-release-20261104.apk`，3,424,047 字节，SHA-256 `D3E4ABD9ED1FEF4F63A7EC639158EE6E5180B3399FCF6702756EA38BEAF90846`。
- 追加核实：在链接文字上起手拖动会移动文本框（＋随动），点按链接才会打开外部页面；“拖拽误开链接”是点按链路造成的误判，无需修改。
- 追加采样（模拟器、`ToolbarTest`、移动工具 10 次横扫）：batch17 99 帧 / 59 卡顿（59.6%）、50 分位 46 ms；batch18 88 帧 / 52 卡顿（59.1%）、50 分位 44 ms。差异在噪声范围内，测试页对象少且模拟器渲染占主导，不作性能提升声明。

## 4.17 第十九批（P1）：卡片/笔记相邻“＋”与命中索引

- `CanvasTextAdjacentControls` 扩展为卡片与文本共用：选中卡片（笔记）同样显示左右“＋”，点击复制出相邻空卡片/文本框并进入编辑；描述按类型显示“添加笔记/添加文本”；沿用 `AdjacentTextAnchor`（值类型）驱动定位，拖动隐藏、边缘隐藏该侧。
- `Page.hitTest` 每次点击构建一次 `id → CanvasSelection` 索引，替代 `findStableObject` 的逐层线性扫描，复杂度 O(n²) → O(n)；图层倒序、顶层优先、连线延后命中与 Frame 边框语义保持不变。
- 新增 `CanvasHitTestIndexTest` 5 项（顶层优先、空白、Frame 边框/内部、连线路径、删除后重排）；413 项 JVM 测试 0 失败。
- 设备冒烟：内容库插入“笔记”卡片→选中后左右“＋”显示→点右侧“＋”生成相邻空卡片并弹出输入法→删除两张测试卡片后页面回到 1,937 字节；矩形点选正常。
- 仍待完成：命中空间索引（点击级 O(n) 已足够，网格/R-tree 未做）、`selectionBounds` 缓存、图片资源治理、UI 组件统一、页面列表状态优化。
- 数据：201 页面文件、20 历史、1 图片、格式 25；未增删页面，无崩溃日志。
- 发布：`releases/lingye-v0.15-architecture-batch19-release-20261104.apk`，3,424,047 字节，SHA-256 `BA18544DB20D0B720B1F1ED4483AC6FD21ED13AC30FDEFE9CFF8BDC0E5E07E6F`。

## 4.18 第二十批（P1）：图片资源治理与格式扩展

- 新增 `ImageGovernance.kt`：汇总页面画布图片、文档图片块、工作区模板画布与共享块（含撤销/重做）引用；`containsBytes` 在页面历史原始字节中查找图片名，撤销不会恢复出缺图页面。
- `PageStore` 增加 `imageFileNames/orphanImages/imageBytes/deleteImages`；设置 → 储存显示未使用图片数量与体积，确认后清理；存在无法读取页面时拒绝执行，仅删除 `files/images` 内符合命名且无引用的文件。
- 图片选择器改为 `OpenDocument` 且 MIME 为 `image/*` + `application/octet-stream`，MIME 未识别的图片也能选择；校验失败提示支持的格式。
- 新增 `PlatformImageDecoder.kt`：`BitmapFactory` 失败时在 API 28+ 回退 `ImageDecoder`（软件位图），提高 HEIF/HEIC 与较新 AVIF 的解码可靠性。
- 格式：JPEG/PNG/WebP/GIF/BMP 稳定；HEIF 自 Android 8（设备相关）；AVIF 自 Android 14 起保证；动画仅首帧；SVG/TIFF/PSD 无平台解码器。
- 新增 `ImageGovernanceTest` 4 项；417 项 JVM 测试 0 失败。
- 设备冒烟：合成 221 KB 孤儿图被统计并确认清理，真实引用图保留；OpenDocument 选取 PNG 导入成功、撤销/重做渲染正常；页面回到 1,937 字节。
- 数据：201 页面文件、20 历史、2 图片（1 个测试导入图因重做历史引用而保留）、格式 25；无崩溃日志。
- 发布：`releases/lingye-v0.15-architecture-batch20-release-20261104.apk`，3,440,431 字节，SHA-256 `D3236F42813F39109E7F156069ECB4F73FF7490C8399F533C7CB0B3C26A2CE3D`。

## 4.19 第二十一批（P1）：图片分桶采样与缓存统计

- 新增 `ImageSampleBuckets`（256/512/1024/1600）：画布按图片世界尺寸 × 缩放 × density 选择解码档位；`PageStore.image(name, maxDimension)` 以 `name@bucket` 为缓存键，静态画面复用位图，只有缩放跨档才重新解码；文档图片、共享图片与裁剪对话框保持 1600 档。
- 删除图片时清理该名称的全部分档缓存；设置 → 储存新增“图片缓存”行（条目 · 已用/上限 · 命中率）。
- 新增 `ImageSampleBucketsTest` 5 项；422 项 JVM 测试 0 失败。
- 设备冒烟：画布图片在 51%/100% 渲染正常；缓存行显示 `1 · 250 KB / 24.0 MB · 0%`，证明小图使用 256 档而非 1600 档。
- 数据：撤销测试图片后页面回到 1,937 字节；201 页面文件、20 历史、2 图片、格式 25，无崩溃。
- 发布：`releases/lingye-v0.15-architecture-batch21-release-20261104.apk`，3,440,431 字节，SHA-256 `C621D5C5149E6A3334FFDD542784AFA52C4EE5DB90371A1A856544D42780D973`。

## 4.20 第二十二批（P1）：UI 菜单组件统一与“?”居中

- 新增 `CanvasMenuComponents.kt`：`layerMenuEntries` 集中四类对象的图层排序文案；`LayerMenuItems` 支持带/不带图标两种样式；`MenuSectionLabel`、`MenuCheckItem` 统一卡片菜单的分组与勾选行。
- 迁移卡片、形状、图片、笔迹与文本“更多”五处菜单，消除重复文案与行为；视觉保持原样（卡片菜单带图标，其余不带）。
- 新增 `HelpGlyph.kt`：以原生 Paint 和字体度量把“?”画在圆形的几何中心，替换设置页帮助点与导入对话框的圆形帮助图标，修复用户反馈的未居中问题。
- 422 项 JVM 测试 0 失败（本批为 UI 调整，无新增测试）。
- 设备冒烟：放大截图确认“?”居中；形状图层菜单和文本“更多”菜单显示与操作正常。
- 数据：201 页面文件、20 历史、2 图片、格式 25；未增删页面，无崩溃。
- 发布：`releases/lingye-v0.15-architecture-batch22-release-20261104.apk`，3,440,431 字节，SHA-256 `1327E5A8360EF9C56A31D6EE70B51D71AFDBD513809AFFD854430DC1A2B4D6FF`。

## 4.21 第二十三批（P1）：侧栏页面行不可变模型与稳定 key

- 新增 `PageRowModel`（id、标题、标签、收藏、回收站、文件夹、图标、画布图标）与 `PageRowModel.of`；`SidebarPageItem`、`PageRowActions` 改为接收模型，不再直接读取可变的 `Page` 字段。
- 列表 key 从 `page:${id}:${icon}:${title}` 改为 `page:${id}`（文件夹树 `folder-page:$name:${id}`）；重命名/换图标只让该行重组，不再重挂载 item 并丢失滚动/动画状态。
- `PageGlyph` 增加 `(icon, canvasIcon)` 重载，旧的 `(page)` 版本保留并委托。
- 新增 `PageRowModelTest` 4 项；426 项 JVM 测试 0 失败。
- 设备冒烟：普通侧栏列表、选中高亮、点击打开、回收站行（图标与恢复按钮）均正常。
- 未覆盖：首页“全部文档”的列表/卡片行仍使用可变 `Page`，其 key 仍包含标题；后续可复用同一模型。
- 数据：201 页面文件、20 历史、2 图片、格式 25；未增删页面，无崩溃。
- 发布：`releases/lingye-v0.15-architecture-batch23-release-20261104.apk`，3,440,431 字节，SHA-256 `D0EE618F7FF3DB8F5ADAB574FE1617E2309A37A2AAC874ABFBA4276F6449C7D3`。

## 4.22 第二十四批（P1）：首页列表/卡片不可变模型

- 新增 `DocumentRowModel` 与 `documentRowModel(page, modifiedTime)`：首页列表行、卡片网格、收藏按钮与选择图标统一读取模型（标题、摘要、图标、画布标记、收藏、文件夹、修改时间）。
- 模型在 `visible + modifiedTimes + contentRevision` 变化时重建；列表/网格沿用页面 ID key，重命名、收藏、换图标只重组对应条目。
- `SelectionIndicator` 改为接收图标/画布标记；`FavoriteAction` 改为接收收藏状态与回调，不再持有可变 `Page`。
- 新增 `DocumentRowModelTest` 4 项；430 项 JVM 测试 0 失败。
- 设备冒烟：列表与卡片显示正常；卡片星标收藏/取消立即切换且只更新该卡片。
- 数据：201 页面文件、20 历史、2 图片、格式 25；收藏冒烟使对应页面文件重写（修改时间为当天），未增删页面，无崩溃。
- 发布：`releases/lingye-v0.15-architecture-batch24-release-20261104.apk`，3,440,431 字节，SHA-256 `0BA3998F1181E5281292FFF9E732055098A02D414A0A2C86BE7D94468C19342E`。

## 4.23 第二十五批（P1）：标签页模型与搜索索引

- `PageTabStrip` 改用 `List<PageRowModel>`，标签标题来自不可变模型，选择/关闭回调按 id 找回页面。
- 新增 `PageSearchIndex` 与 `filterPagesIndexed`：索引缓存页面标题、正文、块、卡片与共享块的小写文本，按页面 revision（及共享块数量）失效；快速搜索连续输入复用索引，空查询不建索引。
- 新增 `PageSearchIndexTest` 4 项（多字段匹配、缓存重建条件、与 `filterPages` 结果一致、空查询不建索引）；434 项 JVM 测试 0 失败。
- 设备冒烟：搜索标题与正文词过滤正确；标签页显示/关闭正常。
- 数据：201 页面文件、20 历史、2 图片、格式 25；未增删页面，无崩溃。
- 发布：`releases/lingye-v0.15-architecture-batch25-release-20261104.apk`，3,440,431 字节，SHA-256 `05E8C9FA3F2AD49C7A6B6FE2FB54AEF1781BDFFC1CE24B79C33C1F161E01A05E`。

## 4.24 第二十六批（P1）：通用菜单行组件迁移

- 新增 `MenuRow`：统一 18dp 图标或自定义前导（B/I/U 字形）、尾部提示、禁用/危险/勾选状态与文字颜色；块菜单分组标题统一使用 `MenuSectionLabel`。
- 迁移文档块操作菜单、块类型菜单与侧栏页面行操作菜单，页面菜单图标由 19dp 统一为 18dp。
- 434 项 JVM 测试 0 失败（UI 调整，无新增测试）。
- 设备冒烟：块菜单（禁用上移/下移、B/I/U、红色删除块）与页面菜单显示和点击正常。
- 数据：201 页面文件、20 历史、2 图片、格式 25；未增删页面，无崩溃。
- 发布：`releases/lingye-v0.15-architecture-batch26-release-20261104.apk`，3,440,431 字节，SHA-256 `D08414DBF2C14D4004AF82D3D92061493ECBEFAC60CCF7428AA59A3B6812443B`。

## 4.25 第二十七批（P1）：剩余菜单行统一与 P2 计划

- `MenuRow` 新增可选 `subtitle`，支持“标题 + 说明”两行菜单项。
- 迁移 `MainActivity` 的导出、根新建、页面更多、回收站、文件夹新增、文件夹选项六组菜单；`AllDocumentsHome` 的排序、新建（带副标题）、左滑移动文件夹菜单；`BlocksEditor` 的斜杠插入与块插入菜单；文档属性显示/归属菜单；设置页选项下拉。Export 行因尾部“›”与子菜单位置计算保留原实现。
- 迁移后禁用项统一使用次要色，危险项统一 Coral，勾选态统一“✓ + Primary”；文件夹新增/选项菜单的禁用能力仍以 `enabled = false` 明示。
- 新增 `docs/affine-audit/P2_MODULE_PLAN.md`：建议的 Gradle 模块边界、依赖方向、S0–S6 拆分顺序、每步验收清单与数据安全护栏。
- 冒烟误判记录：新建文件夹后侧栏看似不显示，实为 `LazyListState` 恢复的滚动偏移遮住了顶部文件夹行；上滚即可见，未修改侧栏逻辑，也未保留任何试探性刷新代码。
- UI 调整无新增测试；434 项 JVM 测试 0 失败。
- 数据：201 页面文件、20 历史、2 图片、`ToolbarTest` 1,937 字节、格式 25；测试文件夹已删除，未增删页面，无崩溃。
- 发布：`releases/lingye-v0.15-architecture-batch27-release-20261104.apk`，3,440,431 字节，SHA-256 `5FD3A2E21092186C3DBEE4EFD15AE007C6FC9C35E4F7C69FF922CDEE74BCB496`。

## 4.26 第二十八批（P2 S0 试点）：包边界与死代码清理

- 新建 `com.example.tabletworkspace.core` 包，迁入四个无 UI 依赖的叶文件：`DateTimeFormats.kt`（日期/语言格式化）、`TextLayoutCache.kt`（文字排版 LRU）、`ConnectorSampleCache.kt`（连线采样 LRU）、`PageSearchIndex.kt`（搜索索引与 `filterPagesIndexed`）。这些文件只依赖模型值类型、`AppLanguage/tr` 与 JDK 集合，不访问 `Context`、文件或 Compose。
- 新建 `com.example.tabletworkspace.platform.image` 包，迁入 `PlatformImageDecoder.kt`（含 `ImageSampleBuckets`）；Android 解码细节与画布/设置 UI 分离，调用方通过显式 import 使用。
- 更新 `MainActivity`、`CanvasRendering`、`CanvasSelection`、`AllDocumentsHome`、`DocumentInfoCard`、`JournalTemplates`、`WorkspaceOverview`、`PageStore`、`CanvasEditor`、`SettingsPage` 及 5 个测试文件的 import；包名保持 `com.example.tabletworkspace`，未改变 `applicationId`、文件路径、页面格式或备份格式。
- 删除 `BlocksEditor.kt` 中 74 行 `if (false)` 死菜单（与现用的 `BlockActions` 重复），文件由 646 行降至 572 行；行为不变。
- 这一步只建立**包边界**，没有新增 Gradle 模块；`P2_MODULE_PLAN.md` 的 S1 起才拆模块。
- 434 项 JVM 测试 0 失败（无新增测试；既有日期、缓存、搜索与图片分档测试继续覆盖搬移后的类）。
- 设备冒烟：安装后启动、全部文档列表、打开 `ToolbarTest`、文档信息区与数据库块编辑区显示正常；画布图片解码仅编译级迁移，未插入新图片做交互验证。
- 数据：201 页面文件、20 历史、2 图片、All Docs 192、`ToolbarTest` 1,937 字节、格式 25；未增删页面，无崩溃。
- 发布：`releases/lingye-v0.15-architecture-batch28-release-20261104.apk`，3,440,431 字节，SHA-256 `822A1744F2C0A7F1D76E28A2CB1D3B098FEF4B7A48DDFED50BD771ED550A5080`。

## 4.27 第二十九批（P2 S0 续）：UI 模型迁入 `ui.model`

- 新增 `com.example.tabletworkspace.ui.model` 包：`PageRowModel`（含 `of(page)`）从 `PageListComponents.kt` 抽出，`DocumentRowModel` 与 `documentRowModel(page, modifiedTime)` 从 `AllDocumentsHome.kt` 抽出；`Page.previewText()` 一并移入 `DocumentRowModel.kt` 并改为 `internal`。
- 更新 `MainActivity`、`PageListComponents`、`AllDocumentsHome`、`PageRowModelTest`、`DocumentRowModelTest` 的 import；`PageListComponents.kt` 清除 29 行，`AllDocumentsHome.kt` 清除 22 行，新增两个模型文件各约 30 行。
- UI 行/卡片只依赖不可变模型，模型层不依赖 Compose；这是 S0 中“模型与列表 UI 分离”的第二步，仍未拆 Gradle 模块。
- 434 项 JVM 测试 0 失败（`PageRowModelTest` 4 项、`DocumentRowModelTest` 4 项继续覆盖搬移后的模型）。
- 设备冒烟：全部文档列表与卡片网格切换正常，标题、摘要、日期、收藏与画布标记显示正确；返回列表视图。
- 数据：201 页面文件、20 历史、2 图片、All Docs 192、`ToolbarTest` 1,937 字节、格式 25；未增删页面，无崩溃。
- 发布：`releases/lingye-v0.15-architecture-batch29-release-20261104.apk`，3,440,431 字节，SHA-256 `05EC5E07AECC925BADB501E50A00C6B77FA7BE20C0783232681DFF2DBEACBA8C`。

## 4.28 第三十批（P2 S0 续）：元数据层迁入 `data.metadata`

- 新增 `com.example.tabletworkspace.data.metadata` 包：`PageMetadataStore`（Journal/模板偏好）、`PageMetadataRepository`（文档属性、Journal、模板与备份快照的统一入口）、`PageMetadataSnapshot`/`PageMetadataSnapshotCodec` 与 `remapToImported`。
- 更新 `PageStore`（备份快照参数）、`BackupArchive`（`metadata.dat` 编解码）、`MainActivity`（仓库构造）与 `PageMetadataSnapshotTest` 的 import；偏好键名 `page_metadata`/`ink_preferences` 与备份格式未变。
- 元数据层现在不依赖任何 UI 文件；`PageStore.restore` 仍只接收 `(snapshot, original, imported) -> Unit` 回调，保持原有解耦。
- 434 项 JVM 测试 0 失败（元数据 round-trip、旧备份兼容与 ID 重映射测试继续覆盖）。
- 设备冒烟：启动、全部文档、打开 `ToolbarTest`，文档信息区的 Journal/模板复选、创建/更新时间与数据库块显示正常。
- 数据：201 页面文件、20 历史、2 图片、All Docs 192、`ToolbarTest` 1,937 字节、格式 25；未增删页面，无崩溃。
- 发布：`releases/lingye-v0.15-architecture-batch30-release-20261104.apk`，3,440,431 字节，SHA-256 `B0A9A82DE2BE689407F3B1111211AB309DD5EF340F5F6229F0201AD23F475039`。

## 4.29 第三十一批（P2 S0 续）：页面仓库与历史迁入 `data.pages`

- 新增 `com.example.tabletworkspace.data.pages` 包：`PageRepository`（单线程写队列）与 `PageHistory`（撤销/重做快照与持久化）从根包迁入；`PageHistory` 显式引用 `Page` 与 `pageFormatVersion`。
- 更新 `MainActivity`（仓库与历史缓存）、`PageStore`（历史读写）、`WorkspaceBootstrap`（历史映射）以及 8 个测试文件的 import；写队列语义、历史编码、`AtomicFile` 路径与页面格式 25 未变。
- 页面保存/历史层现在与 UI 文件分离；`PageStore` 仍留在根包，作为下一批的独立评估对象（它同时被 UI、备份与图片治理引用，需要先拆分引用面）。
- 434 项 JVM 测试 0 失败（`PageRepositoryTest`、`PageHistoryPersistenceTest` 与各画布/历史测试继续覆盖）。
- 设备冒烟：安装后启动、打开 `ToolbarTest` 再返回全部文档；页面未变更，`ToolbarTest` 修改时间保持 07:02，未触发保存。
- 数据：201 页面文件、20 历史、2 图片、All Docs 192、`ToolbarTest` 1,937 字节、格式 25；未增删页面，无崩溃。
- 发布：`releases/lingye-v0.15-architecture-batch31-release-20261104.apk`，3,440,431 字节，SHA-256 `9026B0710FBFFD54572017FAE005532131E5626CD10354A402D5AC0365F48000`。

## 4.30 第三十二批（P2 S0 续）：图片存储从 `PageStore` 拆出

- 新增 `com.example.tabletworkspace.data.images.PageImageStore`：接管 `files/images` 目录、导入校验、位图 LRU 缓存、缓存统计、孤儿图片扫描（含页面历史字节扫描）与删除；构造时接收 history 目录用于历史引用保护。
- `ImageGovernance.kt`（`OrphanImages`、`ImageCacheStats`、`ImageReferences`、`containsBytes`）一并迁入 `data.images`。
- `PageStore` 的 8 个图片 API 改为委托 `PageImageStore`（import/importBytes、image、cacheStats、fileNames、orphan、bytes、delete）；页文件、历史、文件夹/模板/共享目录、备份与 URI IO 仍留在 `PageStore`，公开 API 不变。
- `PageStore.kt` 由约 281 行降至约 205 行，图片职责与页面职责分离，为下一批整体迁入 `data.pages` 做准备。
- 构建副本清理：早期批次把源码移动到新包后，构建副本 `project-fix` 仍保留旧的根包文件（`PageHistory`、`ImageGovernance` 等）；本批把构建副本改为镜像同步，避免旧类与新类同时存在于 APK。
- 434 项 JVM 测试 0 失败（图片治理、分档与页面/历史测试继续覆盖）。
- 设备冒烟：启动、打开 `ToolbarTest` 并切换到白板，文本、形状与连线渲染正常；测试页当前视口没有图片对象，图片解码路径本批未做交互验证（与 batch28 相同的未验证项）。
- 数据：201 页面文件、20 历史、2 图片、All Docs 192、`ToolbarTest` 1,937 字节、格式 25；未增删页面，无崩溃。
- 发布：`releases/lingye-v0.15-architecture-batch32-release-20261104.apk`，3,440,431 字节，SHA-256 `852560D52E94D57EB9287E042E92D523FAA8D52DB44AB4E931600AF26DDC64A6`。

## 4.31 第三十三批（P2 S0 续）：`PageStore` 整体迁入 `data.pages`

- `PageStore` 从根包迁入 `com.example.tabletworkspace.data.pages`，与 `PageRepository`、`PageHistory` 同包；补齐 `Page`、`PageLoadResult`、`pageFileNames`、`loadPageEntries`、`pageFormatVersion`、`canvasPage`、`imageNames`、`remapImages`/`remapSharedReferences`/`remapTemplateImages`、目录编解码器与 `data.images`/`data.metadata` 类型 import。
- 更新 `MainActivity`、`WorkspaceBootstrap`、`WorkspaceRepository`、`CanvasEditor`、`BlocksEditor`、`CanvasBottomBar`、`CanvasDialogs`、`PageImage` 的 import；`PageRepository` 删除同包自引用。
- 页面文件、历史、文件夹/模板/共享目录、备份 ZIP 与 URI 读写仍在 `PageStore` 内，下一步可把备份与 URI IO 拆成 `BackupStore`；本次未改任何路径、格式或写队列语义。
- 434 项 JVM 测试 0 失败（页面加载、备份 round-trip、历史与仓库测试继续覆盖）。
- 设备冒烟：安装后启动、打开 `ToolbarTest`、返回全部文档；无崩溃日志。首次冒烟时 `Back` 退到桌面，确认是点击未命中页面行导致，重新定位行后打开/返回均正常。
- 数据：201 页面文件、20 历史、2 图片、All Docs 192、`ToolbarTest` 1,937 字节、格式 25；未增删页面。
- 发布：`releases/lingye-v0.15-architecture-batch33-release-20261104.apk`，3,440,431 字节，SHA-256 `8DAD1406DAAEEFA56486D2E071489676B15A166E3D3BC2D16EA173EC0C0069DA`。

## 4.32 第三十四批（P2 S0 续）：备份与 SAF 读写拆入 `data.backup`

- 新增 `com.example.tabletworkspace.data.backup.BackupStore`：备份 ZIP 组装（页面/模板/共享块图片引用收集与 90 MB 上限）、恢复合并（图片写入、文件夹去重、共享块与模板 ID 重映射）、`metadata.dat` 回调，以及 `writeBackup`/`readBackup`/`writeText`/`readText` 四个 SAF URI 读写。
- `PageStore` 新增 `appContext`、`pagesDirectory`、`imagesDirectory` 三个 internal 访问器，并把 `backup`/`restore`/`writeBackup`/`readBackup`/`writeText`/`readText` 委托给惰性创建的 `BackupStore`；`RestoreResult` 仍保留在 `PageStore`，公开 API 与调用点不变。
- `PageStore` 由 207 行降至 170 行，`BackupStore` 84 行；`PageStore` 现在主要保留页面/历史文件、文件夹/模板/共享目录与迁移逻辑。
- 备份格式、图片校验、文件夹合并规则、共享块/模板重映射与 100 MB 读取限制均保持原实现，没有数据迁移。
- 434 项 JVM 测试 0 失败（备份 round-trip、旧备份兼容与恢复 ID 映射测试继续覆盖）。
- 设备冒烟：安装后启动、打开 `ToolbarTest`、返回全部文档；无崩溃。备份导出/恢复的设备级流程仍未执行（U-PROJ-022）。
- 数据：201 页面文件、20 历史、2 图片、All Docs 192、`ToolbarTest` 1,937 字节、格式 25；未增删页面。
- 发布：`releases/lingye-v0.15-architecture-batch34-release-20261104.apk`，3,440,431 字节，SHA-256 `E47897F7826A0E5C5977F2790856C8B67D083169FA03BBF09797AA0C232F8CD9`。

## 4.33 第三十五批：导入/导出能力与说明完善

- 导入对话框去掉 Obsidian 与 Bear 名称中的“实验性”标注；帮助文案补充目录、附件、wiki 链接与标签不会保留。
- 设置 → 储存新增“导入与导出能力”分区：逐项说明完整备份 ZIP、Markdown、HTML、快照保留的内容与限制（20 MB 单文件、100 MB / 300 条 ZIP、媒体不导入）。
- HTML 导出新增文档图片内嵌：按图片魔数识别 PNG/JPEG/GIF/WebP/BMP/AVIF 并写成 `data:` URI；找不到图片时保留 `[Image] 文件名` 占位。
- Markdown 导入新增图片块识别：`![](...)` 只在本地图片存在时恢复为图片块，否则保留为文本，避免生成缺图块；Markdown 导出/导入在同一设备上可往返已存在的图片引用。
- 完整备份恢复新增页面链接重映射：按备份页面 ID → 新页面 ID 重写画布 `pageLink` 卡片的 `body`；指向备份外页面的链接保持不变。新增 `PageLinksTest` 用例。
- 新增 5 项测试（Markdown 图片往返/缺图、HTML data URI/占位、页面链接重映射）；439 项 JVM 测试 0 失败。
- 设备冒烟：设置 → 储存显示新的能力说明；导入对话框显示“Obsidian 仓库”“Bear (.bear2bk)”且无“实验性”；其余界面正常。
- 页面格式 25、备份格式 `lingye-backup-2` 与存储路径未变。
- 发布：`releases/lingye-v0.15-architecture-batch35-release-20261106.apk`，3,440,443 字节，SHA-256 `65A7351DD68ECB5F506A926E9961848FAF1CA444A1907BFD031A4DC509DF24D8`。

## 5. 目标架构

建议按职责收敛为以下包/模块（当前先保持单模块，后续可迁移）：

```
core/model        Page/Shape/Connector/Selection/Transform/Snapping（纯 Kotlin）
core/ui           LingyeColors、AppIcon、WorkspaceIcon、Surface tokens、通用行/按钮
data/page         PageCodec、PageStore、PageHistory、PageImageStore
data/metadata     文档属性、Journal/模板标记、自定义颜色、会话
domain            页面库、Journal、模板、导入导出、备份恢复用例
ui/workspace      Workspace 容器、导航、侧栏、全部文档
ui/document       文档页、信息面板、块编辑
ui/canvas         画布输入、渲染、工具条、选择/变换/连线
ui/settings       设置、取色器
platform          Context/IO/调度/生命周期适配
```

依赖规则：
- `core/model` 不依赖 Android/Compose。
- `data` 依赖 model，不依赖 UI。
- `domain` 依赖 data/model，不依赖 Compose。
- `ui` 依赖 domain/model，不直接访问文件/偏好。
- `platform` 只被 data/ui 注入，不被 model 引用。

## 6. 后续优化清单（按优先级）

### P0（下一批，风险中高，需截图/回归）

1. **拆分 CanvasEditor**：输入手势、渲染绘制、工具条/参数条、选择与变换状态、弹窗宿主分离；先建立行为回归清单。文档侧 `BlocksEditor` 已在第四批移出；第五批已把画布整体移入 `CanvasEditor.kt` 并拆出五类选择工具条；第六批拆出绘制（`CanvasRendering.kt`）、点击输入（`CanvasInput.kt`）与底部工具条（`CanvasBottomBar.kt`）；第七批拆出手势状态机（`CanvasPointerInputs.kt`）与九个画布对话框（`CanvasDialogs.kt`）；第八批引入 `CanvasEditorState`（替换 `canvasStateOf` 适配器），拆出快捷键（`CanvasShortcutHandler.kt`）与卡片/文本原位编辑（`CanvasInlineEditor.kt`）；第九批拆出连线工具条与“连接线文字”对话框（`CanvasConnectorToolbar.kt`），`CanvasEditor` 内已无 `AlertDialog`。剩余长按快捷菜单定位、工具条内联菜单与少量局部状态可继续拆分。第十批拆出文本选区格式浮层（`CanvasTextFormattingToolbar.kt`）；画布没有长按快捷菜单实现；第十一批至第十四批继续拆出卡片/形状工具条（`CanvasCardShapeToolbars.kt`）、文本工具条与相邻控件（`CanvasTextToolbar.kt`）、对象工具条宿主（`CanvasObjectToolbarHost.kt`）、演示条（`CanvasPresentationBar.kt`）、单选目标（`CanvasSelectionTargets.kt`）与画布命令（`CanvasEditorCommands.kt`），`CanvasEditor` 降至 815 行。
2. **引入 `WorkspaceRepository`/`PageRepository`**：把 `store.save`、页面查询、偏好读写从 Composable 移出；UI 只发意图、读不可变状态。**进展（batch15–17）**：页面/历史写入收口到 `PageRepository` 单线程队列；`WorkspaceRepository` 接管启动加载、工作区会话、界面语言、画布工具偏好与 `WorkspaceSettings`；MainActivity 已无直接 `SharedPreferences` 读写。剩余：页面列表筛选仍由 Workspace 内纯函数 `filterPages` 计算（已是纯函数），无存储耦合。
3. **统一页面元数据仓库**：文档属性、Journal/模板标记合并；扩展备份 ZIP 并在恢复时写回；补迁移测试。**进展（batch17）**：恢复写回已收口到 `PageMetadataRepository.applyRestored`，ID 重映射有 2 项测试；备份 ZIP 自 batch3 起携带 `metadata.dat`。剩余：设备级导出→恢复仍未执行（U-PROJ-022）。
4. **显式保存异步化**：快照 + 单线程写队列 + 错误状态；导航前等待/排队，避免主线程编码。**进展（batch15）**：已实现；同步保存等待队列，自动保存成功回调后才推进 `savedRevision`，`onStop` 与恢复前 `flush`。

### P1（性能与一致性）

5. **Canvas 每帧缓存**：缓存 resolvedConnector、selectionBounds、文本排版结果；按对象/几何版本失效；命中测试加入空间索引（网格/R-tree）。**进展（batch18/19）**：文本排版/测宽、Typeface、连线采样、本帧连线渲染与形状索引已完成；`hitTest` 由 O(n²) 线性扫描改为每次点击构建一次 `id → CanvasSelection` 索引（O(n)）。`selectionBounds` 缓存与网格/R-tree 空间索引仍未实现。
6. **图片资源治理**：按视口/显示尺寸采样；孤立图片 GC（页面删除/对象删除后）；缓存命中率与内存统计。**进展（batch20/21）**：三项已完成。孤立图片为“确认式清理”（含页面撤销历史引用保护，不做后台自动删除）；显示尺寸采用 256/512/1024/1600 分桶近似；设置 → 储存显示缓存条目、占用与命中率。
7. **UI 组件统一**：`CompactIconAction`、菜单行、参数 chip、Surface token 收入 `ui/components`，减少重复尺寸/颜色。**进展（batch22/26/27）**：画布图层菜单、通用 `MenuRow`（块菜单、块类型菜单、页面行菜单）已统一；batch27 补齐副标题支持并迁移 MainActivity 导出/根新建/页面/回收站/文件夹菜单、首页排序/新建/移动文件夹、块插入菜单、文档属性与设置选项。剩余：`CompactIconAction`、参数 chip、局部自定义 Surface 尚未统一。
8. **页面列表状态优化**：使用不可变 UI model + 稳定 key；减少 `revision` 驱动的全列表重算，标题/图标局部更新。**进展（batch23–25）**：侧栏行（`PageRowModel`）、首页列表/卡片（`DocumentRowModel`）与标签页均改为不可变模型 + 页面 ID key；快速搜索改用按 revision 失效的 `PageSearchIndex`。该 P1 项已完成。
9. **P2 模块化**：拆分计划已写入 `docs/affine-audit/P2_MODULE_PLAN.md`（包边界、`:core:model`/`:core:ui`/`:data:*`/`:feature:*`/`:app`、S0–S6 与验收护栏）。**进展（batch28）**：S0 试点已建立 `core` 与 `platform.image` 两个源码包并完成 import 收口；尚未新增 Gradle 模块。

### P2（长期结构）

9. **Gradle 多模块化**：`core`、`data`、`domain`、`ui`、`app`；先建立包边界再拆模块。
10. **依赖注入/ViewModel**：手动 service container 或 Hilt；Workspace/Document/Canvas 各自 ViewModel；生命周期感知的 IO。
11. **UI 回归测试**：关键流程截图基线（打开文档、选择、缩放、Journal、模板、导入导出）；仪器化触摸测试。

## 7. 本轮验证与数据

- 371 项 JVM 测试 0 失败。
- Release 已安装，冷启动 `am start -W`：TotalTime 1,686 ms（模拟器，当前基线；此前未采集同条件基线，不作提升声明）。
- 冒烟验证：全部文档日期显示、文档信息面板（Journal/模板/日期/分段控件）、白板画布与测试图片加载正常。
- 数据：200 个页面文件、20 历史、1 个测试图片、All Docs 192 个活动文档、格式 25；未增删页面；回收站保留测试副本，可恢复。
- 发布：`releases/lingye-v0.15-architecture-batch1-release-20261103.apk`，3,407,663 字节，SHA-256 `53F35F6736AEB3D32BFE0B6946A27F9C02E7AA02CF728502CC7240649ADDC03B`。
- 第四批追加验证（2026-11-03）：374 项 JVM 测试 0 失败；Release 安装后冒烟打开 `ToolbarTest`，文档块输入/回车新增空块/退格清理、数据库编辑器（标题、筛选、排序、视图、字段、新增记录）、白板画布与选中框 8 个手柄均正常；画布原位编辑（双击文本）在 batch3 与 batch4 表现一致，未发现本批回归。
- 数据核对：200 个页面文件、20 历史、1 图片、All Docs 192、格式 25（`00 00 00 19`）；未增删页面。`ToolbarTest` 测试页因输入/退格从 2,015 字节变为 2,173 字节，正文内容未改变。

## 8. 约束与说明

- 本轮没有修改页面格式 25，没有迁移页面数据，没有删除/覆盖用户页面。
- 图片缓存只缓存已解码 Bitmap，不改变原图文件；缓存超限由 LruCache 自动淘汰。
- MainActivity 仍然较大；CanvasEditor 拆分和 Repository 层是下一批 P0，不在本轮一次性重写，以控制回归风险。
- 任何后续优化都必须先跑 JVM 测试、安装 Release、在模拟器冒烟，并记录未验证项。
