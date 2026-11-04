# 文档属性右侧栏审计与实现清单（2026-11-02）

用户要求：把页面标题栏“更多（⋮）”中的选项做成 AFFiNE 官网文档信息面板那样的右侧属性栏；核对官网信息、列清单并完成；不接入智能体/AI 对话选项。

## 1. 官方源码证据

- 仓库：`toeverything/AFFiNE`，commit `8cef09e1dd62b47a3cbc9f352aa10b3a90c75d34`。
- 本地副本：`C:\Users\17482\Documents\ChatGPT\deepseek\.tmp\affine-src\packages\frontend\core\src\components\`
  - `properties/sidebar/index.tsx`：属性列表区 + 可折叠容器；添加属性区枚举全部类型，唯一类型已存在时显示 “Added”，否则显示 “+”；名称冲突用 `generateUniqueNameInSequence` 生成。
  - `properties/manager/index.tsx`：属性行为“图标 + 名称 + 可见性文案 + ⋯”，点击行或 ⋯ 打开编辑菜单；行支持拖动排序（`canDrag`/`onDrop`，按上/下边缘插入）。
  - `properties/menu/edit-doc-property.tsx`：编辑菜单包含图标选择、属性名输入（`renameable === false` 时只读）、类型行、三个可见性选项（始终显示/空时隐藏/始终隐藏，当前项 selected）、删除（先弹确认框）。
  - `workspace-property-types/index.ts`：类型顺序为 `tags, text, number, checkbox, date, createdBy, updatedBy, updatedAt, createdAt, docPrimaryMode, journal, edgelessTheme, pageWidth, template`；`tags` 声明 `uniqueId`，`tags/createdAt/updatedAt` 不可重命名。
- 可见性取值：`always-show`、`hide-when-empty`、`always-hide`。
- 图 1（官方信息面板）显示的默认属性共 9 项，顺序：标签、文档模式、Journal、模板、创建时间、更新时间、创建者、无界配色方案、页面宽度。

## 2. 官方能力清单与实现状态

状态：✅ 已实现并验证；◐ 部分实现；⬜ 未实现（明确记录，不冒充完成）。

| ID | 能力 | 官方 | 当前实现 | 状态 |
|---|---|---|---|---|
| DP-01 | 右侧信息栏入口 | 标题栏信息按钮打开右侧面板 | 标题栏右侧改为侧栏图标开关（`AppIcon.SIDEBAR`，选中态高亮），点击开/关；仅替换这一处入口图标 | ✅ |
| DP-02 | 面板头部 | 文档标题 + 关闭按钮 | 320dp 宽面板，顶部标题 + `关闭属性栏` 按钮 | ✅ |
| DP-03 | 属性列表 | 默认 9 项、官方顺序 | 9 个内置属性按官方顺序渲染 | ✅ |
| DP-04 | 属性行 | 图标、名称、可见性文案、⋯ | 图标 + 名称 + `始终显示/空时隐藏/始终隐藏` + ⋯ | ✅ |
| DP-05 | 可见性菜单 | 三选项，当前项打勾 | 三选项，当前项 ✓，切换后行文案立即更新 | ✅ |
| DP-06 | 删除属性 | 菜单删除，带确认框 | 菜单删除，先弹确认框（取消保留、确认移除） | ✅ |
| DP-07 | 添加更多属性 | 枚举全部 14 类；唯一类型显示 “Added”，其余 “+” | 14 类全部列出；`标签` 存在时显示 `已添加`，其余显示 + | ✅ |
| DP-08 | 名称唯一 | 冲突时生成顺序后缀 | `uniqueName` 生成 “文本 2 / 文本 3” | ✅ |
| DP-09 | 每页持久化 | 工作区属性服务 | 每页 `SharedPreferences` 键 `doc_properties_<pageId>`，Base64 + DataOutputStream；关闭重开、强停重启均验证 | ✅ |
| DP-10 | 拖动排序 | 行可拖动，按上/下边缘插入 | 每行左侧 28dp 六点手柄：拖动时行浮起并显示阴影，目标行上/下边缘画紫色插入线；松手重排并持久化 | ✅ |
| DP-11 | 属性重命名 | ⋯ 菜单内输入框（部分类型只读） | 菜单内输入框 + ✓ 提交；`tags/createdAt/updatedAt` 按官方设为只读 | ✅ |
| DP-12 | 属性图标选择 | ⋯ 菜单内图标选择器 | 菜单内 16 个图标网格，选中项高亮并立即保存 | ✅ |
| DP-13 | 菜单类型行 | 显示“创建属性 → 图标 + 类型名” | 菜单底部显示 `类型 · 文本/数字/...` | ✅ |
| DP-14 | 删除确认 | 确认框说明影响页面数 | 确认框显示属性名与不可撤销说明 | ✅ |
| DP-15 | 自定义属性值编辑 | 文档“工作区属性”区可编辑文本/数字/复选框/日期值 | 文档信息区按可见性渲染自定义属性并就地编辑；空值/隐藏项在“还有 N 个属性”展开后编辑 | ✅ |
| DP-16 | 原 ⋮ 页面操作 | —（本应用自有） | 面板底部“页面操作”：重命名、更改图标、编辑标签、查看概览、多选、撤销、重做、移动到文件夹、导出 Markdown、移入回收站 | ✅ |
| DP-17 | 智能体/AI 对话 | 官方信息面板另有 AI 入口 | 按用户要求排除 | ✅（排除） |
| DP-18 | 页面格式 | — | 页面格式仍为 25；本功能只写 `SharedPreferences`，不改页面二进制 | ✅ |

## 3. 实现位置

- `app/src/main/java/com/example/tabletworkspace/DocProperties.kt`
  - `DocPropertyInfo` 增加 `icon` 与 `value` 字段；`DocPropertyType` 增加 `renameable`。
  - `DocPropertyCatalog`：14 类型、9 默认项、可见性文案、唯一名称、值类型（文本/数字/复选框/日期/只读）、是否自定义。
  - `DocPropertyCatalog.reorder`：把拖动项插入目标项前/后并重排 0..n-1 的 order，供侧栏拖放使用并有单元测试。
  - `DocPropertyStore`：每页序列化、校验、100 项/100 字/4000 值上限；新格式带 `LPP2` 魔数，旧五字段记录仍能解码。
- `app/src/main/java/com/example/tabletworkspace/DocPropertiesSidebar.kt`
  - 320dp 右侧 `Surface`，三个可折叠区：`属性`、`添加更多属性`、`页面操作`。
  - 属性行 ⋯ 菜单：可编辑名称（不可重命名类型只读）、16 个图标网格、类型行、三个可见性选项、删除（带确认框）。
  - 属性行左侧 28dp 六点拖动手柄：`detectDragGestures` 累计位移，比较各行窗口中心确定目标行与插入边；拖动行 `offset + zIndex + shadow` 浮起，目标边缘画 2dp 主题色插入线；松手调用 `DocPropertyCatalog.reorder` 保存新顺序。
  - 添加行：唯一类型 `已添加`，其余 `+`；点击追加定义并保存。
  - 页面操作行：文件夹子菜单、危险操作用珊瑚色。
- `app/src/main/java/com/example/tabletworkspace/MainActivity.kt`
  - 状态：`pageSidebarOpen`、`pageSidebarRevision`、`sidebarProperties`（按当前页 id 与 revision 读取）。
  - 标题栏右侧改为 `AppIcon.SIDEBAR` 属性栏开关（仅这一处入口图标变更）；移除原 `pageHeaderMenu` 与 `moveMenu` 两个下拉。
  - 侧栏作为顶层 `Row` 的第三个子项，仅在打开且有当前页、且不在全部文档/设置/概览时显示。
  - 回调复用既有页面操作：重命名、图标、标签、概览、多选、撤销/重做、移动文件夹、导出、回收站。
  - `DocumentInfoCard` 接入属性列表：按可见性渲染自定义属性，文本/数字/日期就地输入、复选框可勾选；空值与 `始终隐藏` 项在“还有 N 个属性”展开后仍可填写；“添加属性”行直接打开右侧属性栏。

## 4. 验证

- 单元测试：`DocPropertiesTest` 12 项（默认顺序、编解码往返、旧五字段兼容、未知可见性归一化、重名后缀、目录完整性、值类型与可重命名、自定义判定、拖动排序前后/中间/未知目标）；全量 331 项 JVM 测试 0 失败。
- 模拟器（2560×1600，density 320）：
  - `ps-1.png`：打开属性栏，9 个默认属性与官方顺序一致；“添加更多属性”显示 `标签 已添加` 和 `文本/数字/复选框/日期/创建者/最后编辑者/更新时间… +`。
  - `ps-2.png`：⋯ 菜单三个可见性选项与 `删除属性`，当前项 ✓。
  - `ps-3.png`：标签改为 `空时隐藏`；点 `数字 +` 后列表末尾新增 `数字` 行（始终显示）。
  - `ps-4.png`：滚动到 `页面操作`，重命名/更改图标/编辑标签等行正常渲染。
  - `ps-5.png`：关闭再打开、强制停止并重启应用后，`标签 = 空时隐藏` 与新增 `数字` 行均保留。
  - `t39-menu.png`：⋯ 菜单含名称输入、16 图标网格、类型行、可见性与删除。
  - `t39-icon-changed.png`：`数字` 重命名为 `Score` 并把图标改为星形，侧栏与文档信息区同时更新。
  - `t39-del-confirm.png`：删除确认框；`取消` 后属性保留，确认后移除。
  - `t39-value-filled.png`：数字属性填入 `42`，文档信息区就地显示。
  - `t39-final.png`：重启后文本 `hello`、复选框“已勾选”、日期 `2026-11-02` 全部保留。
  - `t39-hide6.png`：空值且设为 `空时隐藏` 的属性从主列表消失、计数变为“还有 7 个属性”，展开后可在隐藏区填写；填入值后自动回到主列表，计数恢复“还有 6 个属性”。
  - `t39-clean.png`、`t39-clean2.png`：验证数据清理后恢复 9 个默认属性，标签恢复 `始终显示`。
  - `drag-1.png`：每个属性行左侧出现 28dp 六点拖动手柄。
  - `drag-2.png`：拖动「标签」时行浮起并带阴影，目标「Journal」下边缘出现紫色插入线。
  - `drag-3.xml`：松手后顺序变为「文档模式 → Journal → 标签 → 模板…」，与拖放目标一致。
  - `drag-restart.xml`：强停重启后新顺序仍然保留。
  - `drag-up.xml`、`drag-final28.png`：向上拖回首位成功，顺序恢复默认 9 项。
  - `icon-sidebar.png`：标题栏属性入口显示为图 2 的侧栏图标；其他位置的 ⋮ 图标不变。
  - `ov-2.png`：工作区概览左上角新增返回箭头；`ov-back.png` 显示点击后回到原页面。
- 侧栏开关：点击 `关闭属性栏` 后侧栏节点消失；再次点击标题栏属性按钮后重新出现并回到顶部。
- 构建：`gradle testDebugUnitTest assembleRelease` 成功；安装包与构建产物 SHA-256 一致（`DD915F48…`）。

## 5. 已知限制

1. 拖动过程中不自动滚动；属性很多时需要先滚动到目标位置再拖。拖动位置按拖动开始时的行中心计算。
2. 删除属性只移除该页的显示定义，不删除任何页面内容。
3. 属性定义与值存储为每页独立的 `SharedPreferences` 附属数据，不进入页面格式 25；换设备/仅拷贝 `.page` 文件时属性不会跟随。
4. 图标使用应用现有图标集或自绘字形近似官方图标，未逐项对齐官方 SVG。
5. 日期为 `YYYY-MM-DD` 文本输入，未接入官方日期选择器；数字仅接受数字、小数点和负号。
6. 重命名允许同名，未强制去重（新增属性仍自动去重）；官方编辑菜单也未在输入时强制唯一。

## 6. 数据状态

- 页面格式 25；设备当前 193 个 `.page` 文件、18 个历史文件；活动文档 193、回收站 0。本任务只改测试页 `未命名页` 的属性附属数据和验证截图，验证后已删除测试属性、恢复默认顺序并保持 9 项默认属性。
- 与上一份索引记录的 204 页/24 历史相比，减少的 11 页经备份比对全部为此前留在回收站的性能基准/一次性测试页（`Benchmark Canvas`、`Benchmark Document`、数据库测试页等），活动文档数未变（193）；本任务未删除任何页面或历史。
- 发布：`releases/lingye-v0.15-sidebar-icon-overview-back-release-20261102.apk`，2,610,881 字节，SHA-256 `DD915F48B4CD7AF9CA7104A9F14E99B6B02B87371EE1AEB3FC0C96765783BCFE`。
