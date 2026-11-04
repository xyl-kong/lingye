# 无界画布对象清单（阶段 A/B）

审计日期：2026-11-01  
范围：只做官方研究与当前实现扫描；未修改任何业务代码。

## 0. 参考版本锁定

| 项 | 值 | 证据 |
|---|---|---|
| AFFiNE 仓库 | `toeverything/AFFiNE`，canary | `deepseek/.tmp/affine-src/.git/HEAD` |
| AFFiNE commit | `8cef09e1dd62b47a3cbc9f352aa10b3a90c75d34` | `affine-src/.git/refs/heads/canary`、`.git/logs/HEAD` |
| AFFiNE package version | `0.27.5` | `affine-src/package.json:2-3` |
| BlockSuite commit（沿用既有审计） | `5cb5cb68471ca692f3c162258f0087cb22fcb82d` | `docs/affine-audit/TOOLBAR_INVENTORY.md:3-8`；本地 `blocksuite-reference` 仅有空工作树和 `.git`，无法重读源码 |
| 当前页面格式 | 25 | `app/src/main/java/com/example/tabletworkspace/Page.kt` |
| 当前单元测试 | 289 项，0 失败 | 2026-11-01 `testDebugUnitTest` 结果 |

**证据等级**（全文统一）：

- **SRC**：本次直接读取本地 AFFiNE 源码可证（附文件与行号）。
- **PRIOR**：既有审计文档的结论；本次无法重读其引用的 BlockSuite 源码，只能作为次级证据。
- **UNVERIFIED**：没有可用源码、官方产品截图或可执行浏览器；不得据此判定“与官方一致”。
- **CUR**：当前 Android 项目源码可证。

`docs.affine.pro` 在本环境返回 403 Cloudflare（既有审计 `U-REF-001`）；本次没有浏览器自动化，也没有可操作的 AFFiNE 官方 Android App。因此所有官方产品交互、工具栏外观、菜单顺序只能标为 PRIOR 或 UNVERIFIED。

## 1. 官方对象目录（0.27.5 本地可证部分）

官方 Edgeless 设置面板按以下对象分节（`affine-src/packages/frontend/core/src/desktop/dialogs/setting/general-setting/editor/edgeless/edgeless.tsx:17-24`）：

| 官方对象 | 官方模型/属性证据 | 官方设置属性（SRC） | 备注 |
|---|---|---|---|
| General（画布） | `general.tsx:11-33,81-86` | 主题 specified/dark/light/auto；滚轮缩放 | 不是对象，但决定默认外观 |
| Text（`affine:edgeless-text`） | `text.tsx`；`docs/text.json` | 颜色、字体族、字体样式、字重、对齐 | 官方存在独立 Edgeless Text 对象 |
| Note（`affine:note`） | `note.tsx`；`docs/note.json` | 背景、圆角 0/8/16/24/32、阴影、边框样式 solid/dash/none、边框 2–12 | 官方 Note 是块容器 |
| Shape（`affine:shape`，按 shapeName 分别保存） | `shape.tsx`；`docs/shape.json`、`docs/flow.json` | 5 种形状；General/Scribbled；填充、边框色/样式/宽度；文字色、字体族/样式/字重/字号 16–64、对齐 | 每种形状独立默认值 |
| Frame（`affine:frame`） | `frame.tsx`；`docs/frame.json` | 背景（Transparent + 填充色板） | 另有 F 工具比例菜单（PRIOR） |
| Connector（surface connector） | `connector.tsx`；`docs/connector.json` | rough/general；直线/折线/曲线；边框 solid/dash；宽度 2–12；颜色；起终点样式；标签字体/字号/颜色/对齐 | 官方设置覆盖起点和终点两套端点 |
| Brush（`brush`） | `pen.tsx`；`docs/pen.json` | 颜色、线宽 2–12 | 官方高亮笔参数来自 prior audit（PRIOR） |
| Mind Map（surface mindmap） | `mind-map.tsx`；`docs/mindmap.json` | 布局 LEFT/BALANCE/RIGHT；Style 1–4 | 具体枚举值映射 UNVERIFIED |
| Image（`affine:image`） | 本地设置面板没有 Image 分节；工具栏配置含 image/surface 配置 | 无官方设置源码可读 | 对象存在为 PRIOR/UNVERIFIED；属性应以官方产品截图补证 |
| Embed / Linked Doc / Synced Doc / Reference / iframe | `editor-config/toolbar/index.ts:503-1077` 有对应工具栏配置 | 无设置面板分节 | 官方对象类型存在；具体交互 UNVERIFIED |
| Link（链接卡片） | 既有审计记录；官方 Link 工具为直接动作（PRIOR） | 无本地设置源码 | 与 Embed 区分 |
| 其他 | 官方 `affine:note` 内部块、数据库、AI 等 | 不在对象设置面板范围内 | 本审计只记录对画布对象有影响的边界 |

> 说明：AFFiNE 的 `editor-setting` schema 只在本仓库保留 AFFiNE 侧字段，真正的对象默认值在 BlockSuite `GeneralSettingSchema`（`affine-src/packages/frontend/core/src/modules/editor-setting/schema.ts:4`）；BlockSuite 源码不在本机可读，故“官方默认值”一律不作为 SRC 结论。

## 2. 当前对象目录（CUR）

当前没有统一的“对象类型”枚举；对象由 `Shape.type`、`Shape.cardKind`、`Block.type` 与独立列表共同表达（`Page.kt`）。

| 当前对象 | 存储位置 | 关键字段/判别 | 创建入口 | 选中类型 | 证据 |
|---|---|---|---|---|---|
| 自由文本 | `Page.shapes` | `Shape.type="text"`；`body`、`textColor`、`fontSize`、`textAlign` | `Page.addTextBox`；文本条 `addCanvasTextBlock` | `"shape"`（由 `selectedTextIndex` 派生） | `Page.kt:20-41,60`；`Cards.kt:48-101`；`MainActivity.kt:3449-3591` |
| 笔记卡片 | `Page.shapes` | `Shape.type="card"`；`cardKind=note/todo/project/meeting/idea`；`title/body`、颜色、圆角、边框、阴影 | `Page.addCard`；内容菜单 | `"shape"`（`selectedCardIndex`） | `Cards.kt:3-37`；`MainActivity.kt:3111-3178` |
| 几何形状 | `Page.shapes` | `type=rect/roundRect/ellipse/triangle/diamond` | `Page.addShape`；形状工具 | `"shape"`（`selectedShapeIndex`） | `Page.kt:103-105`；`MainActivity.kt:3179-3321` |
| Frame | `Page.shapes` | `type="frame"`；`title`、背景色、边框 | Frame 工具拖拽 | `"shape"`（`selectedFrameIndex`） | `MainActivity.kt:2808`；`FrameGeometry.kt` |
| 画布图片 | `Page.images` | `CanvasImage`：旋转 0–3、翻转、裁剪四边 | 内容菜单图片 | `"image"` | `Page.kt:45,134`；`CanvasImageEdit.kt:3-20` |
| 连接线 | `Page.connectors` | 端点坐标、绑定形状/端口、路径、宽度、颜色、起终 marker、标签、动画 | 连接工具；绑定连线 | `"connector"` | `Page.kt:43,106-133`；`MainActivity.kt:3708-3798` |
| 画笔笔迹 | `strokes` + `strokeStyles` + `strokeIds` | 点列、颜色、线宽 | 画笔；高亮笔 | `"stroke"` | `Page.kt:7,91-92,153` |
| 思维导图节点 | `Page.shapes` + `connectors` | `type="mindNode"`；`cardKind="mindmap:<layout>:<mapId>"` | 思维导图菜单；节点工具条 | `"shape"`（`selectedMindNodeIndex`） | `Mindmap.kt:13-28,85-120`；`MainActivity.kt:3355-3397` |
| 链接卡片 | `Page.shapes` | `type="pageLink"`；`cardKind="linked-doc"`；`body=目标页面 UUID` | 链接工具 → 页面选择器 | `"shape"`（`selectedPageLinkIndex`） | `PageLinks.kt:9-54`；`MainActivity.kt:3322-3354` |
| 页面卡 | `Page.cardX/cardY/title/text` | 页面级封面卡，不在 `shapes` | 页面创建 | 独立 `pageCardSelected` | `Page.kt:50-61`；`MainActivity.kt:3800` |
| 文档块 | `Page.blocks` | `Block.type` + `content` + `checked/bold/italic/underline` | 文档编辑器；文本条 | 文档块状态 | `Page.kt:8,93-102` |
| 数据库块 | `Page.blocks`（type=database） | Base64 `DatabaseCodec` v4 | 内容菜单数据库 | 文档块 | `LocalDatabase.kt:53-60` |
| 共享块引用 | `Page.blocks`（type=shared） | 引用 `SharedBlockCatalog` v3 | 共享块菜单 | 文档块 | `SharedBlocks.kt:56-72` |
| 卡片模板/画布模板 | `cardTemplates` / `templates.dat` | `CardTemplate`；画布模板 Base64 片段 | 保存模板 | 无画布选中 | `Cards.kt:112-116`；`CanvasTemplates.kt:12-24` |

**关键结构事实**：

- 一个 `Shape` 同时承载文本、卡片、几何、Frame、思维导图节点和链接卡；`cardKind` 是第二判别维度（CUR，`Page.kt:20-41`，`Cards.kt:32-33`，`PageLinks.kt:9-10`，`Mindmap.kt:13-28`）。
- 图片、连接线、笔迹是独立列表，不共享 `Shape` 的边框/锁定/字号字段（CUR，`Page.kt:43-45,63`）。
- 官方按对象类型分别保存默认值；当前只有全局 `workspaceSettings`（note/shape/connector/brush 各一组默认值），没有按形状类型或按对象细分的默认值（CUR，`WorkspacePreferences.kt:36-62`）。

## 3. 官方 → 当前对象映射

| 官方对象 | 当前对应 | 映射质量 | 主要差异 |
|---|---|---|---|
| Text | `Shape(type=text)` | PARTIAL | 当前有颜色/字号/对齐，没有字体族/字体样式/字重；没有文本范围富文本/选区工具栏 |
| Note | `Shape(type=card, cardKind=note)` | PARTIAL | 当前是扁平卡片，不是官方 Note 块容器；没有子块/Note 显示模式；背景/圆角/阴影/边框已有 |
| Shape | `Shape(rect/roundRect/ellipse/triangle/diamond)` | PARTIAL | 5 种形状与填充/边框已有；缺 shape style General/Scribbled、按类型独立默认值、文字字体族/样式/字重、旋转 |
| Frame | `Shape(type=frame)` | PARTIAL | 背景、成员几何、导航、演示已有；缺嵌套 Frame、删除联动、官方完整属性 |
| Image | `CanvasImage` | PARTIAL | 裁剪/旋转/翻转已有；缺替换、边框/圆角、锁定、官方 Image 属性/工具栏语义 |
| Connector | `Connector` | PARTIAL | 路径/宽度/颜色/端点/标签/动画已有；缺官方 rough 样式、起点样式 UI、标签字体族/样式/字重/字号、连接线图层菜单 |
| Brush | `strokes + strokeStyles` | PARTIAL | 画笔/高亮、颜色/线宽已有；缺压感、平滑/预测、透明色、局部擦除 |
| Mind Map | `mindNode` shapes + connectors | PARTIAL | 5 种结构布局；官方为 LEFT/BALANCE/RIGHT + 4 styles；缺主题、拖拽改接（当前有对话框改接） |
| Embed | 无 | MISSING | 当前没有 Embed / linked-doc / synced-doc / reference / iframe 对象 |
| Link card | `pageLink` shape | PARTIAL | 创建/打开/失效/反向链接已有；缺官方 Link 工具的完整嵌入/书签语义 |
| Note/Frame 之外的对象属性模型 | 文档块 | 不同模型 | 数据库/共享块只存在于文档块，不是官方画布对象 |

## 4. 公共对象原语

| 原语 | 当前实现 | 证据 | 缺口 |
|---|---|---|---|
| 稳定 ID | shape/image/connector UUID；stroke 使用并行 `strokeIds` | `Page.kt:20-45,63,156` | 文档块无 ID；模板/页面卡无稳定对象 ID |
| 分组 | `Shape.group`、`CanvasImage.group`；仅 shape+image 可分组 | `CanvasGroups.kt:16-31` | connector/stroke 不能分组；Frame 不是 group |
| 锁定 | 仅 `Shape.locked`；只拦截拖动/缩放和全选 | `Page.kt:37,163`；`MainActivity.kt:2389-2390,2787` | 图片/连线/笔迹无锁定；方向键、对齐、组合、删除未完整拦截 |
| 图层 | `layerIds` 持久化，`orderedLayerIds()` 归一 | `Page.kt:65,157,204` | 对象工具条并非所有类型都有图层菜单（连接线没有） |
| Frame 成员 | 几何完全包含；不存父子关系 | `FrameGeometry.kt:69-109` | 嵌套、删除联动、拖出即时失效未实现 |
| 撤销/重做 | 整页字节快照，750ms 合并，100 步/16MB | `PageHistory.kt:31-36,92-97` | 非命令级，无对象级合并策略；大页面成本高 |
| 持久化 | 页面格式 25，追加字段 + 版本门 | `Page.kt` | 启动时自动迁移；v14–v16 无解码门（历史跳号） |

## 5. 对象持久化字段

页面格式 25 的编码顺序见 `Page.kt`，解码门支持 1–25。对象相关字段：

- Shape：`type,x,y,w,h,color,id,group,title,body,borderColor,borderStyle,borderWidth,cornerRadius,textColor,locked,cardKind,fontSize,textAlign,shadowStyle`。
- Connector：端点、颜色、宽度、绑定形状、端口、动画、路径、起终 marker、标签、id。
- Image：名称、位置、尺寸、id、group、旋转、翻转、裁剪。
- Stroke：点列、样式、稳定 id。
- 文档块：type/content/checked/bold/italic/underline；image/database/shared 内容在 block 中。

## 6. 结论摘要

- 当前是“少量通用 Shape 派生 + 独立列表”的对象系统，不是官方按 flavour 建模的对象系统；在 Text、Note、Shape、Connector、Frame、Mindmap 上已有真实业务和持久化，不能当作纯 UI 外壳。
- 最大对象级缺口：Text 字体族/样式/字重与范围格式、Note 块容器语义、Shape 旋转/按类型默认值/Scribbled、Image 锁定/替换/边框、Connector rough/起点 UI/标签字体、Brush 压感/平滑、Frame 嵌套/删除联动、Mindmap 官方布局/主题、Embed 全部缺失。
- 官方对象工具栏/交互的精确外观受 `docs.affine.pro` 403 与无浏览器自动化限制；本审计只对本地能读到的 AFFiNE 前端设置做 SRC 结论，其余标记 PRIOR/UNVERIFIED。

## 7. 证据索引

- 官方：`deepseek/.tmp/affine-src/packages/frontend/core/src/desktop/dialogs/setting/general-setting/editor/edgeless/*`；`.../blocksuite/view-extensions/editor-config/toolbar/index.ts`。
- 当前模型：`Page.kt`、`Cards.kt`、`Mindmap.kt`、`PageLinks.kt`、`SharedBlocks.kt`、`LocalDatabase.kt`、`CanvasImageEdit.kt`、`PageHistory.kt`。
- 既有审计：`docs/affine-audit/TOOLBAR_INVENTORY.md`、`TOOLBAR_PARITY.md`、`REFERENCE_BASELINE.md`、`FEATURE_MATRIX.csv`、`UNVERIFIED_ITEMS.md`。

