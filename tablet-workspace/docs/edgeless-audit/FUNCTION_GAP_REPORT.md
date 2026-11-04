# 功能差异报告（阶段 A/B）

审计日期：2026-11-01。只报告差异，不修改代码。

## 0. 结论摘要

- 当前对象系统不是空壳：文本、笔记卡片、五种形状、图片、连接线、画笔/高亮/橡皮、Frame、思维导图、链接卡片、页面卡、文档块都有真实数据与持久化。
- 与 AFFiNE 0.27.5 的主要差距不是“有没有按钮”，而是 **对象模型粒度**（Note/Shape 文本/Embed）、**属性完整度**（字体、旋转、标签样式、主题）、**统一上下文工具栏/浮窗架构**，以及 **官方行为证据不足**。
- 官方 BlockSuite 源码不在本机、`docs.affine.pro` 403、无浏览器/官方 Android App；因此“官方精确按钮、默认值、交互”大量为 PRIOR/UNVERIFIED，不能据此验收“高保真复刻”。

## 1. 对象完成情况

| 对象 | 已有真实能力 | 主要缺口 | 综合判断 |
|---|---|---|---|
| Text | 创建/编辑/颜色/字号/对齐/复制/图层/锁定/删除/撤销/持久化 | 字体族/样式/字重、范围格式、自动换行、Copy as Image/Markdown | PARTIAL |
| Note | 五种预设/编辑/背景/圆角/阴影/边框/待办/模板/插入文档/复制/图层/锁定 | Note 块容器、子块、显示模式、完整色板、插入文档丢失格式 | PARTIAL |
| Shape | 5 种形状/填充/边框色/样式/宽度/复制/图层/锁定/删除/持久化 | 形状内文字、字体、旋转、Scribbled、按类型默认值、圆角 UI | PARTIAL |
| Image | 插入/移动/缩放/裁剪/旋转/翻转/复制/图层/删除/持久化 | 锁定、替换、边框/圆角/说明、剪贴板 | PARTIAL |
| Connector | 创建/端口/路径/宽度/颜色/起终 marker/标签/翻转/动画/删除 | rough/dash、标签字体、完整色板、图层菜单、官方端点全集 | PARTIAL |
| Brush | 画笔/高亮/颜色/线宽/仅手写笔/整笔/区域擦除/撤销/持久化 | 压感、平滑/预测、局部擦除、锁定/分组/缩放 | PARTIAL |
| Frame | 创建/比例/选择/重命名/背景/成员移动复制/导航/演示 | 嵌套、删除成员联动、锁定、官方完整属性 | PARTIAL |
| Mind Map | 5 种结构/节点/增删改/布局/对话框改接/删除子树/持久化 | 官方 3 布局+4 主题、拖拽改接、整图选择 | PARTIAL |
| Link Card | 创建/打开/重命名跟随/失效/反向链接/复制/删除 | 官方 Link 其他语义（嵌入/书签） | PARTIAL |
| Embed | 无 | 全部 | MISSING |
| Page Card | 选中/编辑标题正文/移动/关闭 | 属性、样式、复制 | PARTIAL |
| 文档块（数据库/共享/图片） | 文档视图内完整 | 不是画布对象；无法作为对象工具条目标 | 边界 |

## 2. 缺失对象能力（按优先级）

| ID | 对象 | 缺失能力 | 官方证据 | 当前证据 | 影响 |
|---|---|---|---|---|---|
| GAP-01 | Text | 字体族/字体样式/字重 | `text.tsx:96-139` SRC | `Shape` 无字段 | 无法复刻官方文本样式 |
| GAP-02 | Text/Note | 文本范围选择与选区格式 | PRIOR（BlockSuite 富文本） | 无 range 模型 | 选区工具栏、粗斜下划线无法实现 |
| GAP-03 | Note | Note 块容器/子块/显示模式 | 官方 `affine:note` PRIOR | 扁平 `title/body` | Note 与官方语义不同 |
| GAP-04 | Shape | 形状内文字编辑与完整文字样式 | `shape.tsx:269-347` SRC | rect 等无 `cardEdit` | 无法在形状中写字 |
| GAP-05 | Shape | 旋转 | PRIOR | 无 `rotation` 字段 | 官方对象操作缺失 |
| GAP-06 | Shape | General/Scribbled、按类型默认值 | `shape.tsx:81-104,96-104` SRC | 仅全局填充色 | 默认值/风格不一致 |
| GAP-07 | Image | 锁定、替换、边框/圆角/说明 | UNVERIFIED | `CanvasImage` 无 locked | 对象属性不完整 |
| GAP-08 | Connector | rough/dash、标签字体/字号/颜色/对齐 | `connector.tsx:63-159,243-353` SRC | 无字段/无 UI | 官方连线样式缺失 |
| GAP-09 | Connector | 完整色板/图层菜单 | PRIOR | 选中后仅蓝/灰；无图层 | 属性编辑不完整 |
| GAP-10 | Brush | 压感/倾斜、平滑/预测、局部擦除 | `docs/pen.json` pressure；PRIOR/UNVERIFIED | 无 pressure/无平滑 | 手写体验差距 |
| GAP-11 | Frame | 嵌套、删除成员联动、锁定 | PRIOR/UNVERIFIED | 几何成员、无嵌套 | Frame 语义不完整 |
| GAP-12 | Mind Map | 官方 LEFT/BALANCE/RIGHT、Style 1–4、拖拽改接 | `mind-map.tsx:20-94`；`mindmap-preview.ts` SRC；拖拽 PRIOR | 5 结构、无主题、对话框改接 | 官方布局/主题缺失 |
| GAP-13 | Embed | Embed/Linked Doc/Synced Doc/Reference/iframe | `toolbar/index.ts:503-1077` SRC 配置 | 无 | 官方对象类型缺失 |
| GAP-14 | 全部 | Copy as Image/Markdown/link | `toolbar/index.ts:277-413` SRC | 无 | 官方 More 菜单能力缺失 |
| GAP-15 | Text/Shape/Note | 自动换行/最大宽度/缩放策略 | `docs/text.json` PRIOR | 固定 width/height，8 行裁切 | 文本布局不完整 |

## 3. 缺失/无效的工具栏按钮与菜单

### 3.1 当前已存在的空实现/禁用项（CUR）

| 位置 | 控件 | 状态 |
|---|---|---|
| 3820-3821 | 文本条“链接/附件” | 禁用，`onClick={}`；官方文本条是否有此二项 UNVERIFIED |
| 4107-4111 / 4126-4130 | 更多菜单“选择操作/视图”分组标题 | 禁用，仅标题（合理） |
| 3097 | 页面卡“编辑” | 只请求焦点，不进入明确编辑态 |
| 3124 | 卡片工具条缩放百分比 | 只读文本，不是控件 |
| 3959-3999 | 多选条 | 缺少混合属性/批量样式 |
| 2405/2424/2487/2501 | 锁定对象的方向键/删除/对齐/组合 | 锁定未拦截 |

### 3.2 官方存在但当前缺失的按钮/菜单（证据等级）

| 对象 | 官方能力 | 证据 | 当前 |
|---|---|---|---|
| Text | 字体族/样式/字重菜单 | SRC | 缺 |
| Text | Copy as Image/Markdown/link | SRC | 缺 |
| Shape | 文字工具/字体样式菜单 | SRC | 缺 |
| Shape | 边框 2–12 滑块/圆角 UI | SRC | 部分缺 |
| Note | 完整背景色板、Note 块操作 | SRC/PRIOR | 部分缺 |
| Connector | rough/dash、标签样式、图层 | SRC/PRIOR | 缺 |
| Image | 锁定/替换/说明 | UNVERIFIED | 缺 |
| Embed | 创建/编辑/尺寸 | SRC 配置/PRIOR | 缺 |
| Mindmap | 主题 Style 1–4、拖拽改接 | SRC/PRIOR | 缺 |
| Frame | 嵌套/删除成员/锁定 | PRIOR/UNVERIFIED | 缺 |

> 官方按钮的精确顺序/图标仍未验证；阶段 C 只能先实现有 SRC 证据的属性，PRIOR/UNVERIFIED 项需先补证。

## 4. UI-only / 空实现清单

- `TextTools` 链接/附件：禁用占位（3820-3821）。
- 页面卡编辑：仅聚焦（3097）。
- 卡片缩放百分比：只读（3124）。
- “插入到文档”：只导出标题/正文，图片/待办/格式丢失（3167-3171）。
- 锁定保护不完整（见 3.1）。
- 连接线单选条件不一致（2388）。
- 图片/连线/笔迹无锁定；连接线无图层菜单。
- 多选无混合属性；无剪贴板 paste（Ctrl+V 未映射）。
- Frame 删除不联动成员（需确认官方语义）。
- 数据库/共享块仅文档块，不是画布对象。

## 5. 需要统一重构的公共组件

| 组件 | 当前问题 | 目标 |
|---|---|---|
| 对象类型/能力注册表 | `Shape.type` + `cardKind` + 独立列表；按钮散落 | `CanvasObjectType` + `ObjectCapability` registry |
| Contextual Toolbar Host | 10 套独立 `AnimatedVisibility`/按钮/菜单 | 单宿主 + section/model |
| Position Resolver | 固定 dp offset、部分无 clamp、无 Insets/IME | 统一锚点 + 边界/Insets/IME |
| 对象属性绑定 | 各工具条直接读写字段 | `value(ctx)/commit(ctx)` + 历史事务 |
| 锁定/分组/图层 | 仅 shape；实现不一致 | 统一对象状态与拦截 |
| 文本模型 | 画布文本无 range/fontFamily/weight/style | 选区 + 字体字段（格式 25） |
| 浮动菜单 | 分散 DropdownMenu + 经验 offset | Trigger 锚点 + 宿主互斥 |
| 剪贴板 | 无 paste | 统一 Copy/Paste/Duplicate |
| 视觉 token | 40/34/30dp、不同圆角/阴影 | 统一 toolbar/popup token |

## 6. 测试覆盖与风险

| 区域 | 现有测试 | 缺口 |
|---|---|---|
| 对象模型/持久化 | PageFormat、PageHistory、NoteStyle、Frame、Mindmap、Connector、Image 等 239 项 | 无 instrumentation；无官方对照 fixture |
| 工具栏/选中 | 无 Compose UI 测试；模拟器人工 | 需要结构化 bounds/属性绑定测试 |
| 文本/IME | 无 | 中文组合、删除、光标、选区 |
| 手写笔/压力 | 无 | 真机矩阵 |
| 浮窗定位 | 无 | 缩放/边缘/Insets/IME 自动断言 |
| 视觉 | 人工截图 | 无 golden/diff 基线 |

## 7. 最小风险修复顺序（建议）

1. **统一对象能力/工具栏宿主/定位 token**（阶段 C）：不改页面格式，先把 10 套工具条迁到一个宿主；建立结构化 UI 测试。风险：中；收益：阻断后续重复实现。
2. **Text / Note 优先（阶段 D）**：Text 增加 fontFamily/fontStyle/fontWeight、字号范围、选区模型；Note 增加完整背景色板/插入文档保真；需要页面格式 25（新增字段）→ 备份/迁移/回滚。风险：高（格式与 IME）；收益：最高用户可见。
3. **Shape**：形状内文字、旋转、按类型默认值、Scribbled；涉及格式 25 和渲染。风险：高。
4. **Image/Connector**：锁定、替换、边框/说明；rough/dash、标签样式、色板、图层。风险：中。
5. **Brush/Frame**：压感/平滑、嵌套 Frame、删除联动；需真机/官方证据。风险：中高。
6. **Mindmap/Embed/Link**：官方布局/主题/拖拽；Embed 是全新对象，需先补官方产品证据。风险：高。
7. **视觉/功能回归（阶段 F）**：golden 截图、bounds 断言、对象级回归；官方截图缺失项保持 UNVERIFIED。

## 8. 停止点

本次只完成阶段 A/B。未修改任何业务代码；后续阶段需用户确认参考版本、官方截图获取方式、页面格式 25 迁移策略和 Text/Note 的首批范围后再开始。
