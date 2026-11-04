# 导入入口与页面更多菜单审计清单（2026-11-02）

用户要求：左侧栏增加导入按钮，导入弹窗按图 2 展示支持的文件格式；编辑区顶栏增加“更多”按钮，按图 3 复刻一级/二级浮窗的样式、尺寸与位置；图 3 中红色横线标注的选项（默认为文档模式、在新标签页打开、在客户端中打开）不实现。

## 1. 官方源码与截图证据

- 官方导入弹窗：`packages/frontend/core/src/desktop/dialogs/import/index.tsx`
  - `ImportType` 为 `markdown`、`markdownZip`、`notion`、`obsidian`、`bear`、`oneNote`、`snapshot`、`html`、`docx`。
  - 使用 `MarkdownTransformer`、`ZipTransformer`、`HtmlTransformer`、`DocxTransformer`；Notion、Obsidian、Bear 走 zip/目录导入。
  - 弹窗内含帮助图标、OneNote 禁用态、底部 Discord 提示。
- 官方导出菜单：`packages/frontend/core/src/components/page-list/operation-menu-items/export.tsx`
  - `ExportMenuItems` 包含 Export to HTML、Export to Markdown、Copy as Markdown、Export to Snapshot；图片中的二级浮窗即该子菜单。
- 图 1/图 3 为本次 UI 复刻基准；图 3 的 ⋮ 位于收藏、信息按钮右侧。

## 2. 实现清单

状态：✅ 已实现并验证；◐ 部分实现；⬜ 未实现。

| ID | 能力 | 当前实现 | 状态 |
|---|---|---|---|
| IM-01 | 左侧栏导入按钮 | 回收站下方新增“导入”行，下载托盘图标 `AppIcon.IMPORT` | ✅ |
| IM-02 | 导入弹窗样式 | 560dp 宽、24dp 圆角、9 个 56dp 格式行 + 帮助圆点 + 底部提示；OneNote 灰显禁用 | ✅ |
| IM-03 | Markdown (.md) | 复用既有 `markdownImporter` | ✅ |
| IM-04 | Markdown 压缩包 (.zip) | 读取 zip 内 `.md/.markdown/.txt`，每个文件导入为页面 | ✅ |
| IM-05 | HTML | 解析 `<title>`、标题/段落/列表，转换为 Markdown 后导入 | ✅ |
| IM-06 | Notion | 支持 Notion 导出的 HTML 或 HTML zip | ◐（仅 HTML 内容） |
| IM-07 | Obsidian 仓库 | 读取 zip 内 Markdown 文件并逐页导入 | ◐（不解析附件/链接） |
| IM-08 | Bear (.bear2bk) | 读取备份 zip 内 Markdown/文本 | ◐（不解析 Bear 专有元数据） |
| IM-09 | OneNote | 按图 2 置灰，点击不触发 | ✅（禁用） |
| IM-10 | Word (.docx) | 读取 `word/document.xml`，按段落提取文字 | ◐（保留纯文本，不保留样式/图片） |
| IM-11 | 快照 | 读取灵页快照 JSON 的 title/text/blocks；其他 JSON 回退提取 text 字段 | ◐（非完整 AFFiNE 快照语义） |
| IM-12 | 帮助圆点 | 每个格式行的 ? 打开说明弹窗 | ✅ |
| PM-01 | 顶栏更多按钮 | 信息按钮右侧新增 ⋮；属性栏图标仍保留在其右侧 | ✅ |
| PM-02 | 一级浮窗 | 264dp 宽、12dp 圆角、6dp 阴影，菜单项与图 3 一致；红色横线项不出现 | ✅ |
| PM-03 | 二级 Export 浮窗 | 248dp 独立面板位于一级面板左侧，与 Export 行同高对齐，间距 8dp（向左展开） | ✅ |
| PM-04 | 重命名 | 打开既有页面重命名弹窗 | ✅ |
| PM-05 | 加入收藏/取消收藏 | 复用 `togglePageFavorite` | ✅ |
| PM-06 | 查看信息 | 打开既有文档信息弹窗 | ✅ |
| PM-07 | 查看所有的 Frames | 切到画板并打开既有 Frame 导航弹窗 | ✅ |
| PM-08 | 查看历史版本 | 列出撤销栈中的历史状态，可恢复到指定版本 | ✅ |
| PM-09 | 复制 | `Page.duplicate` 复制全部内容并生成新 id，标题追加“副本” | ✅ |
| PM-10 | 导入 | 打开导入弹窗 | ✅ |
| PM-11 | Export 二级菜单 | 导出为 HTML / 导出为 Markdown / 复制为 Markdown / Export to Snapshot | ✅ |
| PM-12 | 移动到回收站 | 复用 `trashPage` | ✅ |
| PM-13 | 排除项 | 默认为文档模式、在新标签页打开、在客户端中打开均未加入 | ✅（排除） |

## 3. 主要代码

- `MainActivity.kt`
  - 左侧栏新增 `SidebarNavigationRow(AppIcon.IMPORT, "导入", …)`。
  - 顶栏新增“更多”按钮与两级菜单；一级菜单 264dp，Export 二级面板 248dp 位于其左侧，使用 `onGloballyPositioned` 读取 Export 行位置，使二级面板与 Export 行对齐。
  - 新增 `documentImporter`、`htmlExporter`、`snapshotExporter` 启动器；导入结果通过 `addImportedPages` 写入页面存储。
  - 历史版本弹窗读取 `PageHistory` 的撤销快照。
- `ImportDialog.kt`：9 种格式的弹窗、禁用态、帮助说明。
- `DocumentImport.kt`：HTML/Word/zip/快照解析，HTML 与快照导出。
- `Page.kt`：新增 `duplicate(newTitle)`。
- `PageHistory.kt`：新增 `undoCount/undoVersion/undoTo`。
- `AffineIconPaths.kt`、`VisualDesign.kt`：新增 `AppIcon.IMPORT` 下载图标。

## 4. 验证

- 单元测试：`DocumentImportTest` 6 项（HTML、Markdown zip、docx、快照导入、快照往返、HTML 导出）、`PageDuplicateTest` 1 项、`PageHistoryPersistenceTest` 新增历史版本恢复 1 项；总计 339 项 JVM 测试 0 失败。
- 模拟器：
  - `imp-1.png`：左侧栏出现“导入”按钮，点击打开格式弹窗。
  - `imp-2.png`：9 种格式完整显示，OneNote 灰显，底部提示可见。
  - `imp-help.png`：点击 HTML 行 ? 打开说明。
  - `imp-picker.png`：点击 Markdown 行打开系统文件选择器并列出 Downloads 中的 `.md` 文件（未实际选择文件）。
  - `menu-2.png`：顶栏 ⋮ 一级菜单；红色横线项不存在。
  - `menu-5.png`、`menu-left.png`：Export 二级面板向左展开，独立圆角面板且与 Export 行对齐。
  - `hist-1.png`：历史版本弹窗列出撤销栈中的版本与“恢复”。
  - `frames-1.png`：查看所有 Frames 切到画板并打开 Frame 导航（该页无 Frame，显示提示）。
  - `export-picker.png`：导出为 Markdown 打开系统保存对话框，文件名为 `未命名页.md`。

## 5. 已知限制

1. 图片/媒体不随 Markdown、Obsidian、Bear、Word 或 HTML 导入；只导入文字内容。
2. Notion 仅解析 HTML/HTML zip；数据库、synced block 等专有结构不保留。
3. Word 仅提取段落文字，不保留样式、表格、图片。
4. 快照导入为灵页 JSON 或宽松的 text 字段提取，不等同于完整 AFFiNE snapshot 语义。
5. 历史版本来自撤销栈，没有时间戳；最多显示 30 条。
6. HTML 导出为简单语义 HTML，不含图片、画布对象和数据库完整内容。
7. 导入弹窗的格式图标使用应用现有图标近似官方品牌图标。
8. 本次只验证了选择器打开与未选择文件；未在设备上实际导入示例文件，避免向用户页面集合写入测试数据。

## 6. 数据状态

- 页面格式 25；193 个 `.page`、18 个历史文件、活动文档 193、回收站 0；本任务未创建/删除页面或历史。
- 发布：`releases/lingye-v0.15-import-menu-left-release-20261102.apk`，2,643,649 字节，SHA-256 `8544C08C09972331767F7F0FDF14613ABF53D02BD33425B5019D9F5CFBE47BBE`（二级 Export 面板向左展开）。
