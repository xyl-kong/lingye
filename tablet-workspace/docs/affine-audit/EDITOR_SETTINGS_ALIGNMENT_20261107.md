# 设置 → 编辑器：官方对齐审计（2026-11-07）

## 0. 审计范围与依据

- 本应用版本：batch38（提交 `538f8fd`）后的工作区。
- 官方参照：AFFiNE canary 源码，提交 `ef401b20cb2a5960278a8e7ab2c0e96d8f759aeb`（2026-11-06），本地克隆 `C:\Users\17482\AppData\Local\Temp\affine-src`。
- 官方关键文件：
  - `packages/frontend/core/src/modules/editor-setting/schema.ts`（字段、默认值、取值）
  - `packages/frontend/core/src/desktop/dialogs/setting/general-setting/editor/general.tsx`（排版、新文档、图标、行号）
  - `packages/frontend/core/src/desktop/dialogs/setting/general-setting/editor/page.tsx`（页面宽度、页面信息、双向链接）
  - `packages/frontend/core/src/modules/editor-setting/utils/date-title.ts`（日期标题与重名后缀）
  - `packages/frontend/core/src/modules/editor-setting/__tests__/date-title.spec.ts`
  - `packages/frontend/core/src/blocksuite/block-suite-page-list/utils.tsx`（新建文档标题解析）
  - `packages/frontend/core/src/desktop/pages/workspace/detail-page/page-detail-editor.tsx`（`pageWidth ?? fullWidthLayout`）
- 本应用关键文件：
  - `WorkspacePreferences.kt`（`WorkspaceSettings`、读写、归一化）
  - `SettingsPage.kt`（`EditorSettings`，约 439–569 行）
  - `MainActivity.kt`（新建页面 647/1011/1382/2000 行；文档宽度 2047 行；图标行 2052 行；双向链接面板 2133 行）
  - `DocumentInfoCard.kt`（页面信息里的页面宽度）
  - `data/metadata/PageMetadata.kt` / `PageMetadataRepository.kt`

原则：**功能行为以官方为准，视觉与交互以现有设计系统为准。** 不引入官方蓝色控件，不重排左导航/右内容结构。

---

## 1. 现有实现结构（审计结论）

| 层 | 现状 |
| --- | --- |
| 页面结构 | `SettingsPage.kt` 的 `EditorSettings`；左导航 `SettingsNavItem`，右侧内容；顶部 `SettingsHeading` + 文档/白板 `SegmentedChoice`。 |
| 选项分组 | 文档：排版 / 页面 / 新页面；白板：画布主题及默认对象样式。 |
| 状态 | 单一 `WorkspaceSettings` 数据类，`SettingsPage` 通过 `onSettingsChange` 回写。 |
| 持久化 | `WorkspacePreferenceStore.readWorkspaceSettings` / `writeWorkspaceSettings` 逐键读写 SharedPreferences；`normalized()` 负责范围与枚举校验。 |
| 即时生效 | 字号、字体、行号、页面宽度、显示页面信息直接参与当前文档渲染。 |
| 仅影响新建 | 默认打开方式、自动日期标题；白板默认颜色/字号/连线等只用于新对象。 |
| 与编辑器关系 | `MainActivity` 把字号/字体/行号传给 `BlocksEditor`；页面宽度决定编辑区 `widthIn(max = 860.dp)` 或 `fillMaxWidth()`；`displayDocInfo` 控制顶栏信息入口。 |
| 页面信息 | `DocumentInfoCard` 内再次提供“页面宽度”，当前直接改写全局 `documentFullWidth`。 |
| 元数据 | 每页 Journal 日期、模板标记、单页宽度覆盖存于 `page_metadata`；文档属性存于 `ink_preferences`；均随备份元数据导出。 |

### 1.1 现状分类

- **已完整实现（A）**：字号（12–24，默认 16）、字体（无衬线/衬线/等宽）、默认打开方式（文档/白板）、自动日期标题开关、显示页面信息、代码块行号（开关生效，但默认值不符）、画布主题与默认对象样式。
- **仅 UI 无行为（B）**：本次审计未发现“点了完全没反应”的编辑器选项。
- **行为不正确 / 语义偏差（C）**：
  1. 页面信息里的“页面宽度”修改全局默认值，而官方是单页覆盖（`pageWidth ?? fullWidthLayout`）。
  2. 日期格式是项目自定义的 `iso/slash/monthDay/chinese`，默认 `iso`；官方为 `DD-MM-YYYY / MM-DD-YYYY / YYYY-MM-DD / journal`，默认 `DD-MM-YYYY`。
  3. 自动日期标题不做重名处理；官方追加 `(2)`、`(3)`。
  4. 代码块行号默认 `false`；官方默认 `true`。
  5. 编辑器页副标题“修改只影响之后新建的内容”不准确：字号、字体、行号、宽度、页面信息会立即影响当前文档。
- **官方有、当前缺失（D）**：`displayAddIconOption`（无图标文档是否显示“添加图标”）、`displayBiDirectionalLink`（文档底部双向链接面板）、官方四种日期格式与重名后缀、自定义字体（Custom，第二轮已补齐）、Ask 新文档模式。
- **项目特有、保留（E）**：黑白灰视觉体系、`SegmentedChoice`/`DateFormatChoice`/`SettingsToggleRow` 等组件、左侧设置导航、中文语境下的说明文案、侧栏“显示链接的文档”（`showLinkedDocs`，与官方双向链接不是同一功能）、页面信息中的 Journal/模板入口。
- **不适用 / 受限（F）**：`newDocDefaultMode = ask`（无 AI）、AI 开关、拼写检查、Linux 中键粘贴、`openDocMode`（官方当前设置弹窗未暴露，且 Android 单窗口无 tab/new-view 语义）；自定义字体只枚举本机已安装字体，不下载或打包字体。

---

## 2. 功能差异表

| # | 官方项 | 官方行为 / 默认值 | 当前实现 | 差异类型 | 处理决定 | 证据 |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | 正文字体 `fontFamily` | Sans / Serif / Mono / Custom，默认 Sans | Sans / Serif / Mono，默认 Sans，立即生效 | A + 部分 D | 保留三项；Custom 标记为暂不支持，不放假选项 | `schema.ts`；`general.tsx` |
| 2 | 正文字号 `fontSize` | 12–24，默认 16 | 12–24 滑杆，默认 16，立即生效 | A | 保留；修正说明为“立即应用于文档” | `schema.ts`；`SettingsPage.kt:463` |
| 3 | 新文档默认模式 `newDocDefaultMode` | page / edgeless / ask，默认 page | 文档 / 白板，默认文档；根目录与文件夹新建使用该值，快捷入口显式指定模式 | A（ask 为 F） | 保留两项；Ask 不实现 | `schema.ts`；`MainActivity.kt` 各创建入口 |
| 4 | 日期自动标题 `autoTitleNewDocWithCurrentDate` | 默认 false；只命名空白新文档 | 默认 false；新建页面直接用日期标题 | A（命名时机略宽） | 保留开关；新建页面即命名符合本项目创建流程 | `schema.ts`；`general.tsx` |
| 5 | 日期格式 `newDocDateTitleFormat` | DD-MM-YYYY（默认）/ MM-DD-YYYY / YYYY-MM-DD / journal（本地化） | iso（默认）/ slash / monthDay / chinese | C + D | 改为官方四项；旧值迁移：iso/slash→YYYY-MM-DD，monthDay→DD-MM-YYYY，chinese→journal | `schema.ts`；`date-title.ts`；`WorkspacePreferences.kt:96` |
| 6 | 日期标题唯一化 | 已存在则追加 `(2)`、`(3)`… | 不处理重名 | C + D | 新建时读取现有标题并追加后缀 | `date-title.ts:19-41`；测试 `date-title.spec.ts` |
| 7 | 显示添加图标选项 `displayAddIconOption` | 默认 true；控制无图标文档是否显示添加入口 | 顶栏始终显示“添加图标/更改图标” | D | 新增字段、设置开关、顶栏条件渲染 | `schema.ts`；`general.tsx:526-534`；`MainActivity.kt:2052` |
| 8 | 页面宽度 `fullWidthLayout` | 设置中是默认值 false；每页可用 `pageWidth` 覆盖 | 设置中是全局值；页面信息修改它也改全局 | C | 设置项改称“默认页面宽度”；页面信息改为单页覆盖，未覆盖时回落到全局 | `schema.ts`；`page.tsx`；`page-detail-editor.tsx` |
| 9 | 显示页面信息 `displayDocInfo` | 默认 true；控制文档信息入口 | 默认 true；控制顶栏信息按钮 | A | 保留，补充准确说明 | `schema.ts`；`page.tsx`；`MainActivity.kt:1779` |
| 10 | 显示双向链接 `displayBiDirectionalLink` | 默认 true；文档内显示双向链接 | 始终显示 `DocumentLinksPanel` | D | 新增字段、设置开关、面板条件渲染；不与 `showLinkedDocs` 混用 | `schema.ts`；`page.tsx:92-105`；`MainActivity.kt:2133` |
| 11 | 代码块行号 `codeBlockLineNumbers` | 默认 true | 开关生效，默认 false | C | 默认改为 true；已有用户显式关闭的 false 保留 | `schema.ts`（`codeBlockLineNumbers: true`）；`SettingsPage.kt:515` |
| 12 | 白板默认主题 `edgelessDefaultTheme` | specified / dark / light / auto，默认 specified | auto / light / dark，默认 light | 部分 C | 本轮不改默认值，避免已有画布观感突变；记录为后续项（specified 需页面级主题模型） | `schema.ts`；`edgeless/*.tsx` |
| 13 | 打开文档方式 `openDocMode` | active / new-view / new-tab / center-peek，默认 active | 单窗口单页切换 | F | 不实现、不放假控件；Android 端不适用 | `schema.ts`；官方设置弹窗未暴露 |
| 14 | 中键粘贴 `enableMiddleClickPaste` | 默认 false，仅 Linux | 无 | F | 不适用 | `schema.ts`；`general.tsx` |
| 15 | AI / 拼写检查 | 官方有独立开关 | 无 | F | 不实现；不伪造入口 | `general.tsx` |

---

## 3. UI 保护清单（不得改动）

### 3.1 布局与结构

- 设置页保持“左导航 + 右内容”两栏；编辑器页顶部仍是标题、说明、文档/白板分段切换。
- 文档页分组顺序保持：排版 → 页面 → 新页面；新增开关放入对应分组，不新开分区。
- 行内控件保持右侧对齐；滑杆 + 数值仍为同一行；说明文字仍在标题下方。
- 不引入官方设置页的蓝色主按钮、蓝色开关或官方卡片布局。

### 3.2 设计令牌

- 背景/表面：`LingyeColors.Background`、`Surface`、`SurfaceHigh`。
- 文本：`Primary`、`Secondary`、`Muted`。
- 分段控件：`SegmentSelected`、`SegmentBorder`；圆角 18dp（外框）/15dp（选项），内边距 3dp。
- 导航选中：`NavSelected`；侧栏选中：`Subtle`。
- 字体层级与现有 `SettingsHeading`、`SettingsGroup`、说明文字字号保持一致。

### 3.3 组件复用

- 开关：`SettingsToggleRow`。
- 右侧分段：`SegmentedChoice`。
- 多行/滑杆：`SettingsStackRow`、`CompactSlider`。
- 日期格式：`DateFormatChoice`（FlowRow，仍用现有黑白选中态）。
- 分组与分隔：`SettingsGroup`、`SettingsDivider`。
- 不新增通用组件，不为单个选项造新视觉。

### 3.4 交互

- 开关点击整行可切换（沿用 `SettingsToggleRow` 行为）。
- 分段控件选中态即时切换，不新增动画体系。
- 设置修改后仍通过 `updateWorkspaceSettings` 统一持久化，不绕过 `normalized()`。
- 页面信息中的宽度切换只影响当前页，不改变设置页的默认值。

---

## 4. 本轮实施清单

1. `WorkspaceSettings` 新增 `displayAddIcon: Boolean = true`、`displayBiDirectionalLink: Boolean = true`；读写键 `display_add_icon_option`、`display_bi_directional_link`。
2. `codeBlockLineNumbers` 默认改为 `true`。
3. 日期格式改为官方四项，默认 `dayMonthYear`；读取时迁移旧值；`formatDateTitle` 支持本地化 journal 样式。
4. `newPageTitle` 增加现有标题集合参数，重复时追加 `(2)`、`(3)`；四个新建入口传入 `pages.map { it.title }`。
5. 设置页新增两个开关：
   - 页面组：“显示双向链接”（默认开）。
   - 新页面组：“显示添加图标选项”（默认开）。
6. 编辑器页副标题与相关说明改为“排版/页面宽度立即生效，新页面选项只影响之后新建的内容”。
7. 页面宽度：
   - 设置页文案改为“默认页面宽度”，说明未单独设置的页面使用该值。
   - `PageMetadataStore` 新增 `pageWidth:<id>`（`standard`/`full`），`PageMetadataRepository` 暴露读写。
   - `MainActivity` 计算 `pageWidthOverride ?: workspaceSettings.documentFullWidth`，文档区与 `DocumentInfoCard` 共用。
   - 页面信息切换写入单页元数据并触发重组，不改全局设置。
   - 单页宽度写入备份 `metadata.dat`（V2 编码，兼容读取旧 V1）；恢复后按新页面 ID 还原。
8. 顶栏“添加图标/更改图标”行仅在 `displayAddIcon` 为 true 时显示；文档底部 `DocumentLinksPanel` 仅在 `displayBiDirectionalLink` 为 true 时显示。
9. 更新测试：默认值、日期格式与迁移、重名后缀、页面宽度元数据读写。
10. 构建、安装、模拟器回归；未验证项单独列出。

---

## 5. 验收场景

| 场景 | 预期 |
| --- | --- |
| 设置 → 编辑器首屏 | 分组、顺序、黑白样式不变；说明文字准确 |
| 字号 12/16/24 | 当前文档立即变化；重开应用保持 |
| 字体三项 | 当前文档立即变化；重开保持 |
| 代码块行号默认 | 新安装默认开启；手动关闭后重开保持关闭 |
| 默认打开方式 | 仅影响之后从根目录/文件夹新建的页面；快捷入口仍按入口语义 |
| 自动日期标题 + 四种格式 | 标题分别为 `07-11-2026`、`11-07-2026`、`2026-11-07`、本地化日记样式 |
| 同一天连续新建 | `07-11-2026`、`07-11-2026(2)`、`07-11-2026(3)` |
| 显示添加图标关闭 | 无图标页面顶栏不再出现“添加图标”；已有图标仍显示 |
| 显示双向链接关闭 | 文档底部面板消失；重新打开仍关闭 |
| 页面宽度（页面信息） | 只改当前页；其他页不受影响；设置页默认值不变；重开保持 |
| 页面宽度（设置页） | 未单独覆盖的页面跟随默认值变化 |
| 旧版本偏好 | `iso/slash/monthDay/chinese` 迁移到官方四项，不崩溃 |

### 5.1 实施结果（2026-11-07）

已实际执行：

- 442 项 JVM 单元测试通过，0 失败。
- Release APK：3,440,443 字节，SHA-256 `EE394B2355985BC4DEA65F66C028B58BFBE24ABEFE6F8F6BD6E614A6DD5234B0`。
- 模拟器（Android 15 平板，保留原 192 页工作区）：
  - 编辑器设置页结构与黑白样式未变；新增两个开关与四个日期格式可见。
  - 关闭“显示添加图标选项”后，无图标页面顶栏不再显示添加入口；恢复后重新显示。
  - 关闭“显示双向链接”后，页面底部 `Bi-Directional Links` 面板消失；恢复后重新出现。
  - 页面信息中把当前页切为“全宽”后，仅该页变宽；切换到另一页仍为“标准”；强制停止并重启后该页保持全宽；设置页“默认页面宽度”仍为“标准”。
  - 自动日期标题选择 `DD-MM-YYYY` 后，新建页面标题为 `07-11-2026`，再次新建为 `07-11-2026(2)`。
  - 测试新建的两页已移入回收站，未永久删除；测试结束已把两个开关、自动命名和当前页宽度恢复原值。
- 旧偏好迁移：模拟器原有 `dateTitleFormat=iso`，设置页显示并选中 `2026-10-31`（YYYY-MM-DD），未回落到默认值。

未实际验证 / 已知限制：

- 单页宽度已进入备份元数据；已在隔离模拟器完成“设置全宽 → 导出 → 清除 → 恢复”验证，恢复后该页仍为全宽。
- 代码块行号已在隔离清洁安装 AVD 验证默认开启；显式关闭后重启仍为关闭，重新开启后已有代码块立即恢复行号。
- 白板默认主题仍为 `light`，未对齐官方 `specified`；本轮未改，避免已有画布观感突变。
- Ask 模式、AI、拼写检查、中键粘贴、`openDocMode` 未实现；自定义字体已在第二轮实现。

---

## 6. 明确不做的部分

- 不实现 Ask 模式、AI、拼写检查、中键粘贴、`openDocMode`。
- 不把官方 React/vanilla-extract 组件或蓝色主题搬进本项目。
- 不重构设置页导航与整体布局。
- 不改变页面文件格式 25。
- 不改变备份 ZIP 外壳格式；页面元数据升级为 V2，同时保留 V1 读取。

---

## 7. 第二轮：遗留修复与自定义字体（2026-11-07）

### 7.1 单页宽度备份

- `PageMetadataSnapshot` 新增 `pageWidths`；编码升级为 `MAGIC_V2`（`0x4C504D32`），每个页面记录追加 1 字节宽度标记：`0` 未设置、`1` 标准、`2` 全宽。
- `decode` 同时接受 `MAGIC_V1`（`0x4C504D31`），旧备份解码后 `pageWidths` 为空，页面回落到工作区默认宽度。
- 备份快照包含宽度；恢复按旧页面 ID → 新页面 ID 重映射后写入 `page_metadata`。
- 单元测试覆盖：V2 往返、V1 解码、缺失宽度、非法宽度标记、Archive 往返、重映射与未知页面丢弃。
- 模拟器验证：隔离 AVD 中把 `Code test` 设为全宽 → 导出备份（`metadata.dat` 末字节为 `2`）→ `pm clear` → 恢复 → 页面仍为全宽；另用人工构造的 V1 备份恢复，页面回落为标准宽度且无报错。

### 7.2 自定义字体

- 设置数据新增 `customFontFamily`，`editorFontFamily` 增加 `custom`；键为 `custom_font_family`。
- `SystemFontCatalog` 只读取本机 `/system/fonts` 与 Android 10+ 的 `SystemFonts`，不下载、不打包字体；按文件名清理显示名，保留实际文件路径用于创建 Typeface。
- 选择器保持现有黑白分段控件，增加“自定义”第四项；自定义行复用设置页的 Surface/描边/圆角样式，提供搜索、字体列表（按各自字体预览）、手动字体名称、刷新和清除。
- 作用范围：文档标题、页面正文、正文文本块、共享文字块、文档内文本对象；代码块始终等宽，工具栏、侧边栏、设置页和系统界面不改变。
- 不可用字体：`isCustomFontAvailable` 不在目录中时显示“当前不可用，已回退为无衬线”，编辑器仍使用 Sans 渲染，不会出现空白文字。
- 验证：DancingScript 在英文标题和正文上生效；中文页面回退系统字体；切换到不存在的名称后文字正常显示且显示回退提示；强制停止重启后自定义字体恢复；白板与新工具栏未受影响。
- 已知限制：Android 没有公开的统一字体族枚举；选择器以字体文件名作为稳定标识，部分旧系统只能手动输入族名；字体文件被系统移除后按不可用处理。

### 7.3 白板默认主题分析（未改代码）

官方 `edgelessDefaultTheme` 的取值含义（`doc-create-middleware.ts`）：

| 官方值 | 新建白板时的行为 |
| --- | --- |
| `specified` | 读取新建时的应用主题，快照为 `light` 或 `dark` |
| `dark` / `light` | 固定深色/浅色 |
| `auto` | 写入 `system`，由白板跟随系统主题 |

本应用当前的 `canvasTheme` 是工作区级即时值（`auto/light/dark`，默认 `light`），会立即影响所有已有白板；页面信息里的“无界配色方案”也修改这个全局值，而官方是每个文档的 `edgelessColorTheme`。

结论：本轮不替换默认值。若直接改为 `specified`，在深色界面下新建白板会从浅色变深色，改变现有观感；若改为每页主题，还需要扩展页面元数据与备份，属于新的行为模型。保留 `light` 作为显式默认，不新增迁移；后续若要实现，应参照单页宽度增加 `edgelessTheme` 页面级覆盖与备份字段，并把设置页文案改为“默认白板配色”。

### 7.4 第二轮测试结果

- 447 项 JVM 单元测试通过，0 失败。
- Release APK：3,456,827 字节，SHA-256 `04ADDE2C45E15C98918EF4AA23DD8F0808D5651659D37437E0C37D5D6353C43E`。
- 隔离清洁安装 AVD：行号默认开启；导入含代码围栏的 Markdown 后显示 `1/2` 行号；关闭开关立即消失；重启后仍关闭；重新开启立即恢复。
- 隔离 AVD 备份：V2 备份恢复保留全宽；V1 备份恢复回落标准宽度。
- 原验证 AVD（未清除数据）：自定义字体英文标题/正文生效，DancingScript → 不存在字体 → 回退无衬线；测试结束恢复无衬线并清除自定义名称，页面数量与内容保持。
