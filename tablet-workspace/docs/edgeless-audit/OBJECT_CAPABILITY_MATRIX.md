# 对象能力矩阵（阶段 A/B）

审计日期：2026-11-01。逐项拆分对象状态、触发方式、工具栏、属性、预期结果、当前实现与验收测试。

**状态定义**：`DONE`=当前有真实业务且已通过单元/模拟器验证；`PARTIAL`=主路径存在但缺少官方关键子能力；`MISSING`=无实现；`UI_ONLY`=有控件但无真实数据/操作；`UNVERIFIED`=当前有实现但缺少针对性验证，或官方行为无法确认。
**官方证据等级**：`SRC`=本地 AFFiNE 源码可证；`PRIOR`=既有审计的 BlockSuite/产品结论，本次无法重读源码；`UNVERIFIED`=无可用证据。

## 1. Text（自由文本）

当前对象：`Shape(type="text")`，字段 `body/textColor/fontSize/textAlign/width/height`（`Page.kt:20-41`）。创建 `Cards.kt:48-101`；上下文工具栏 `MainActivity.kt:3449-3591`；内联编辑 `MainActivity.kt:4235-4242`。

| ID | 能力/状态 | 触发 | 官方证据 | 当前实现 | 状态 | 验收测试/缺口 |
|---|---|---|---|---|---|---|
| TXT-01 | 创建自由文本 | 文本工具 → 正文 | `affine:edgeless-text` 存在，`text.tsx:29-43` | `addTextBox(400,180,...)`；`addCanvasTextBlock("body")` | DONE | 创建后画布出现文本对象；撤销移除 |
| TXT-02 | 单选文本 | 单击文本 | 官方选择态 PRIOR | hitTest → `selectedTextIndex` | DONE | 选中出现工具条 |
| TXT-03 | 双击原位编辑 | 双击 / 工具条“文本” | 官方内联编辑 PRIOR | `cardEdit=index`；body-only BasicTextField | DONE | 双击进入编辑；Back 退出并收键盘 |
| TXT-04 | 文本颜色 | 工具条色板 | `text.tsx:76-94` | `copy(textColor=...)`（3523-3538） | DONE | 改色→撤销→重开保持 |
| TXT-05 | 字号 | 字号菜单 | `shape.tsx:46-53,314-327`（文本侧同属字体设置） | 16/20/24/32/40/48 + +/-（3477-3521） | PARTIAL | 官方 16–64；当前上限 48，无任意值输入 |
| TXT-06 | 对齐 | 对齐按钮 | `text.tsx:39-74` | left→center→right 循环（3540-3543） | DONE | 三态切换并持久化 |
| TXT-07 | 字体族 | 字体菜单 | `text.tsx:96-109` | Sans/Serif/Mono，写入 fontFamily | DONE | 整块工具条切换，重启保持 |
| TXT-08 | 字体样式（斜体） | 样式菜单 | `text.tsx:111-124` | fontStyle + span 斜体 | DONE | 整块斜体与选区斜体 |
| TXT-09 | 字重 | 字重菜单 | `text.tsx:126-139` | fontWeight 100–900，UI 400/500/600/700 | DONE | 整块与 span 粗体 |
| TXT-10 | 文本范围格式/选区 | 选中文字 | 官方富文本 PRIOR（BlockSuite） | TextSpan + 选区 B/I/U 工具栏 | DONE（Text 范围） | 仅 Text 对象；Note 正文不含 span |
| TXT-11 | 自动换行/最大宽度 | 编辑/改变尺寸 | `docs/text.json` 有 `hasMaxWidth/scale`（PRIOR 解释） | 按对象宽度换行，最多 8 行；显式换行保留 | PARTIAL | 无自动增高/复杂断词 |
| TXT-12 | 移动/缩放 | 拖动/四角 | 官方对象操作 PRIOR | 通用 selection move/resize | DONE | 缩放后文字按新宽度换行 |
| TXT-13 | 复制/图层/锁定/删除 | 工具条 | PRIOR | duplicate/layer/lock/delete | DONE | 锁定后不可拖动；删除可撤销 |
| TXT-14 | 复制为图片/Markdown/链接 | 官方 More 菜单 | `editor-config/toolbar/index.ts:277-413`；`copy-as-image.ts:107-217` | 无 | MISSING | 至少 Copy as Image 需在路线图评估 |
| TXT-15 | 撤销/重做/持久化 | 任意修改 | PageHistory/格式 25 | 整页快照；字体/span 写入格式 25 | DONE | 重开后字体/字重/字号/选区保持 |

## 2. Note（笔记卡片）

当前对象：`Shape(type="card", cardKind=note/todo/...)`，不是官方 `affine:note` 块容器（`Cards.kt:18-37`）。工具条 `MainActivity.kt:3111-3178`。

| ID | 能力/状态 | 触发 | 官方证据 | 当前实现 | 状态 | 验收测试/缺口 |
|---|---|---|---|---|---|---|
| NOTE-01 | 创建笔记/五种预设 | 内容菜单 | `note.tsx:47-265` 为 Note 默认样式；官方 Note 为块 | `addCard` 240×150 | DONE | 创建笔记/待办/项目/会议/灵感 |
| NOTE-02 | 单选/多选 | 单击/多选 | PRIOR | `selectedCardIndex`；多选通用 | DONE | 卡片工具条出现 |
| NOTE-03 | 内联编辑标题+正文 | 双击/编辑按钮 | 官方 Note 内联块编辑 PRIOR | title/body 两个 BasicTextField（4216-4242） | DONE | 输入、撤销、重开保持 |
| NOTE-04 | 背景色 | 工具条/设置 | `note.tsx:106-124` | 工具条色点打开完整 `ColorPaletteMenu`（经典十色 + 自定义色卡/取色器）+ 设置默认色 | DONE | 绿/蓝/黄等完整色板可选，重开保持 |
| NOTE-05 | 圆角 | More 菜单 | `note.tsx:29-45,126-145` | 0/8/16/24/32 | DONE | 当前值 ✓、持久化 |
| NOTE-06 | 阴影 | More 菜单 | `note.tsx:147-166` | none/box/sticker | DONE | 贴纸/卡片/无阴影预览 |
| NOTE-07 | 边框样式/厚度 | 设置/工具条 | `note.tsx:57-104,249-264` | 设置 solid/dashed/none；2–12 | DONE | 选中卡片改边框后重开保持 |
| NOTE-08 | 待办勾选 | 行级复选框/工具条 | 官方 Note 内 To-do PRIOR | 画布行级复选框可点选，`☐/☑`、`[ ]/[x]/[]` 均支持；工具条保留“勾选下一项” | DONE | 点第 2 行只切换第 2 行；锁定卡片不可点选 |
| NOTE-09 | 保存/插入卡片模板 | 工具条/内容菜单 | 官方 Template PRIOR | `saveCardTemplate/insertCardTemplate` | DONE | 独立副本；模板颜色/内容 |
| NOTE-10 | 插入到文档 | 工具条 | 官方 Insert into Page PRIOR | `CardDocumentImport`：标题→heading；待办→todo（含勾选）；项目符号/编号/引用/标题/分隔线/代码围栏→对应 block | PARTIAL | 图片与行内格式在扁平的 title/body 模型中无表示，无法保真 |
| NOTE-11 | 复制/图层/锁定/删除 | 工具条 | PRIOR | duplicate/layer/lock/trash | DONE | 锁定后不可拖动；删除可撤销 |
| NOTE-12 | Note 作为块容器（子块、嵌套） | 官方 Note 内部编辑 | 官方 `affine:note` 模型 PRIOR | 扁平的 `title/body` 字符串 | MISSING | 无法插入/排序子块；与官方 Note 语义不同 |
| NOTE-13 | Note 显示模式/属性 | 官方 Note 工具条 | 官方有 display mode 等 PRIOR | 无 | MISSING | 需官方产品证据确认具体项 |
| NOTE-14 | 持久化 | 任意修改 | 格式 25 | 字段写入；背景/阴影/圆角均持久化 | DONE | 重开保持 |

## 3. Shape（形状）

当前对象：`Shape(type=rect/roundRect/ellipse/triangle/diamond)`；工具条 `MainActivity.kt:3179-3321`；创建 `MainActivity.kt:2798-2807`。官方设置 `shape.tsx`。

| ID | 能力/状态 | 触发 | 官方证据 | 当前实现 | 状态 | 验收测试/缺口 |
|---|---|---|---|---|---|---|
| SHP-01 | 五种形状 | 形状工具 | `shape.tsx:177-202`；`docs/shape.json:30-151` | 5 种；绘制/切换 | DONE | 矩形/圆角/椭圆/三角/菱形均可创建 |
| SHP-02 | 填充色 | 参数条/工具条 | `shape.tsx:219-237` | 参数条色板；选中后填色菜单 | DONE | 新形状使用当前色；撤销恢复 |
| SHP-03 | 边框颜色 | 选中工具条 | `shape.tsx:239-257` | 边框色菜单（3235-3261） | DONE | “同填充”与自定义 |
| SHP-04 | 边框样式 | 选中工具条 | `shape.tsx:106-135` | solid/dashed/none（3262-3273） | DONE | 三态切换 |
| SHP-05 | 边框粗细 | 选中工具条 | `shape.tsx:259-267,498-513` | 1/2/4 循环（3274-3281） | PARTIAL | 官方 2–12 step 2；当前不含 6–12，且循环方式不直观 |
| SHP-06 | 圆角 | 圆角矩形/工具条 | `docs/shape.json` `radius:0.1` | `cornerRadius` 字段；仅圆角矩形创建时有默认 | PARTIAL | 选中普通形状无圆角入口；无 0–64 的 UI |
| SHP-07 | 形状内文字编辑 | 双击形状 | 官方 shape text 支持 PRIOR | 只有 card/text/mindNode 可 `cardEdit`；rect 无文字编辑 | MISSING | 需形状内文本模型与编辑 |
| SHP-08 | 文字色/字号/对齐 | 选中工具条 | `shape.tsx:314-347` | 无形状文字 UI | MISSING | 依赖 SHP-07 |
| SHP-09 | 字体族/样式/字重 | 选中工具条 | `shape.tsx:269-312` | 无 | MISSING | 需对象字段扩展 |
| SHP-10 | General/Scribbled 样式 | 官方设置 | `shape.tsx:81-104` | 无 | MISSING | 需官方视觉样本确认 |
| SHP-11 | 旋转 | 旋转手柄 | 官方旋转 PRIOR | Shape 无 rotation 字段 | MISSING | 需格式变更（建议 25 + 迁移/备份） |
| SHP-12 | 移动/缩放 | 拖动/四角 | PRIOR | 通用 12..2000 限制 | DONE | 缩放/吸附/撤销 |
| SHP-13 | 复制/图层/锁定/删除 | 工具条 | PRIOR | 均实现 | DONE | 锁定保护需补齐方向键/对齐/删除 |
| SHP-14 | 持久化 | 任意修改 | 格式 24 | 所有字段写入 | DONE | 重开保持 |
| SHP-15 | 按形状类型独立默认值 | 设置 | `shape:<shapeName>` 分键（`shape.tsx:96-104`） | 只有全局 `shapeColor` | MISSING | 需设置模型重构 |

## 4. Image（图片）

当前对象：`CanvasImage`（`Page.kt:43-45`）；工具条 `MainActivity.kt:3592-3650`；编辑 `CanvasImageEdit.kt`。

| ID | 能力/状态 | 触发 | 官方证据 | 当前实现 | 状态 | 验收测试/缺口 |
|---|---|---|---|---|---|---|
| IMG-01 | 插入图片 | 内容菜单 → 图片 | 官方 image 对象存在；设置面板无 Image 分节 → 细节 UNVERIFIED | 系统选择器 → `addCanvasImage` | DONE | 图片复制进私有存储 |
| IMG-02 | 单选/移动/缩放 | 单击/拖动 | PRIOR | hitTest + 通用 move/resize | DONE | 保留比例后尺寸限制 |
| IMG-03 | 裁剪 | 工具条 | PRIOR | `cropTarget` 对话框；`cropCanvasImage` | DONE | 四边裁剪、比例保持 |
| IMG-04 | 旋转 90° | 工具条 | PRIOR | `rotateCanvasImage` | DONE | 宽高交换、持久化 |
| IMG-05 | 水平/垂直翻转 | 工具条 | PRIOR | `flipCanvasImage` | DONE | 与裁剪组合 |
| IMG-06 | 复制/图层/删除 | 工具条 | PRIOR | duplicate/layer/trash | DONE | 撤销恢复 |
| IMG-07 | 锁定 | 工具条 | 官方对象锁定 PRIOR | `CanvasImage` 无 locked；工具条无锁 | MISSING | 需统一锁定模型 |
| IMG-08 | 替换图片 | 工具条/属性 | UNVERIFIED | 无 | MISSING | 需官方行为确认 |
| IMG-09 | 边框/圆角/说明文字 | 工具条/属性 | UNVERIFIED | 无 | MISSING | 需官方样本 |
| IMG-10 | 复制/粘贴 | 系统剪贴板 | PRIOR | Ctrl+V 未映射，无粘贴 | MISSING | 需剪贴板架构 |
| IMG-11 | 持久化/备份 | 保存 | 格式 24 / 备份 | 图片文件 + 字段 | DONE | 备份恢复、缺失素材报错 |

## 5. Connector（连接线）

当前对象：`Connector`（`Page.kt:43`）；工具条 `MainActivity.kt:3708-3798`；创建 `Page.kt:106-133`。

| ID | 能力/状态 | 触发 | 官方证据 | 当前实现 | 状态 | 验收测试/缺口 |
|---|---|---|---|---|---|---|
| CON-01 | 拖拽创建连线 | 连接工具 | PRIOR | 拖拽创建；可绑端口 | DONE | 端点吸附/绑定 |
| CON-02 | 单选 | 单击 | PRIOR | hitTest 采样折线 | DONE | 工具条出现 |
| CON-03 | 端口绑定/解绑 | 拖端点 | PRIOR | `bindConnectorEnd/detachConnectorEnd` | DONE | 最近端口高亮 |
| CON-04 | 路径直线/折线/曲线 | 工具条 | `connector.tsx:90-124` | straight/step/curve（3733-3740） | DONE | 三种渲染 |
| CON-05 | 线宽 | 工具条 | `connector.tsx:151-159` | 参数条 1/2/4/6；选中工具条无独立线宽控件 | PARTIAL | 官方 2–12 step 2；当前 1–8 模型 |
| CON-06 | 颜色 | 工具条 | `connector.tsx:161-184` | 色板（3768-3769 蓝/灰；参数条十色） | PARTIAL | 选中后的色板仅蓝/灰 |
| CON-07 | 起点端点样式 | 工具条 | `connector.tsx:186-199` | start marker 菜单（3741-3752） | DONE | 五态 |
| CON-08 | 终点端点样式 | 工具条 | `connector.tsx:201-214` | end marker 菜单（3753-3764） | DONE | 五态 |
| CON-09 | 标签文字 | 工具条 | `connector.tsx:243-353` | AlertDialog（3773-3795） | PARTIAL | 仅文字，无字体/字号/颜色/对齐 |
| CON-10 | 标签样式 | 标签编辑 | `connector.tsx:255-353` | 无 | MISSING | 需标签字体模型 |
| CON-11 | 翻转方向 | 工具条 | PRIOR | `reverseConnector`（3766） | DONE | 交换起止端口/marker |
| CON-12 | 动画光点 | 工具条 | PRIOR | `animated` 切换（3767） | DONE | 系统关闭动画时停止 |
| CON-13 | 虚线/rough 样式 | 设置/工具条 | `connector.tsx:63-76,126-149` | rough 无；dash 无字段 | MISSING | 需格式扩展 |
| CON-14 | 图层 | 工具条 | PRIOR | Connector 参与 layerIds，但工具条无图层菜单 | PARTIAL | 只能通过其他对象/顺序间接调整 |
| CON-15 | 删除/撤销/持久化 | 工具条 | PRIOR | 均实现 | DONE | 重开保持 |

## 6. Brush（画笔与笔迹）

当前：`strokes` + `strokeStyles` + `strokeIds`（`Page.kt:7,63,91-92`）；参数条 `MainActivity.kt:3923-3958`；橡皮 `AreaEraser.kt`。

| ID | 能力/状态 | 触发 | 官方证据 | 当前实现 | 状态 | 验收测试/缺口 |
|---|---|---|---|---|---|---|
| BR-01 | 普通画笔/高亮笔 | 画笔工具 | `pen.tsx`；高亮参数 PRIOR | brushKind 两态；30% 高亮 | DONE | 绘制、颜色、线宽独立 |
| BR-02 | 颜色/线宽 | 参数条 | `pen.tsx:32-60` | 十色；画笔 2–12，高亮 10–30 | DONE | 撤销/重开保持 |
| BR-03 | 仅手写笔 | 参数条 | Android 适配，无官方桌面对应 | `penOnly` 过滤手指 | DONE | 模拟器手指被阻止 |
| BR-04 | 压感/倾斜 | 手写笔 | 官方 brush 有 pressure 字段（docs/pen.json）→具体行为 PRIOR/UNVERIFIED | 点列无 pressure | MISSING | 真机采样与渲染 |
| BR-05 | 平滑/预测 | 官方 | PRIOR/UNVERIFIED | 无 | MISSING | 延迟/平滑评估 |
| BR-06 | 整笔橡皮 | 橡皮工具 | PRIOR | 删除整条笔迹 | DONE | 撤销 |
| BR-07 | 区域橡皮 | 橡皮工具 | PRIOR | 删除轨迹覆盖对象 | DONE | 锁定对象除外 |
| BR-08 | 局部像素擦除 | 官方 | UNVERIFIED | 无 | MISSING | 需官方证据 |
| BR-09 | 选中/移动/删除笔迹 | 选择工具 | PRIOR | 笔迹工具条颜色/粗细/图层/删除 | PARTIAL | 不支持缩放；无锁定/分组 |
| BR-10 | 持久化 | 保存 | 格式 24 | 点列/样式/id | DONE | 重开保持 |

## 7. Frame（框架）

当前：`Shape(type=frame)`；工具条 `MainActivity.kt:3398-3448`；几何 `FrameGeometry.kt`。

| ID | 能力/状态 | 触发 | 官方证据 | 当前实现 | 状态 | 验收测试/缺口 |
|---|---|---|---|---|---|---|
| FR-01 | 拖拽创建 Frame | Frame 工具 | PRIOR | `addShape("frame",...)` | DONE | 比例约束 |
| FR-02 | Custom/1:1/4:3/16:9/2:1 | 参数条 | PRIOR（BlockSuite Frame 菜单） | `frameRatio`（3865-3878） | DONE | 选择预设后拖拽 |
| FR-03 | 选择（边框/标题） | 点击 | PRIOR | `FrameSemantics.hit` 边框带/标题 | DONE | 内部空白穿透 |
| FR-04 | 重命名 | 工具条 | PRIOR | `frameRename` 对话框 | DONE | 名称持久化 |
| FR-05 | 背景色 | 工具条 | `frame.tsx:22-52` | 六种浅色（3415-3432） | PARTIAL | 官方为 Transparent + 填充色板 |
| FR-06 | 成员移动/复制 | 拖动/复制 | PRIOR | 几何成员随动；复制含成员 | DONE | 拖出后失效 |
| FR-07 | 成员删除联动 | 删除 Frame | PRIOR/UNVERIFIED | 不删除成员 | MISSING | 需官方确认行为 |
| FR-08 | Frame 导航/演示 | More/工具条 | PRIOR | 导航对话框、演示模式 | DONE | 1/2 切换、适应/关闭 |
| FR-09 | 嵌套 Frame | — | 官方 PRIOR/UNVERIFIED | 无持久父子关系 | MISSING | 需官方确认 |
| FR-10 | 锁定/分组 | 工具条 | UNVERIFIED | Frame 是 Shape，锁定字段存在但工具条无锁定 | PARTIAL | 锁定后可移动成员？ |
| FR-11 | 持久化 | 保存 | 格式 24 | 名称/背景/位置/成员几何 | DONE | 重开保持 |

## 8. Mind Map（思维导图）

当前：`mindNode` shapes + 连线；菜单 `MainActivity.kt:4051-4067`；节点工具条 `3355-3397`；`Mindmap.kt`。

| ID | 能力/状态 | 触发 | 官方证据 | 当前实现 | 状态 | 验收测试/缺口 |
|---|---|---|---|---|---|---|
| MM-01 | 创建结构 | 思维导图菜单 | `mind-map.tsx:54-94` 官方 3 布局 + 4 样式 | 5 种结构：气泡/向右/向下/组织/大纲 | PARTIAL | 结构多于官方，缺官方布局/主题语义 |
| MM-02 | 选中节点 | 单击 | PRIOR | `selectedMindNodeIndex` | DONE | 节点工具条 |
| MM-03 | 添加子/同级节点 | 工具条 | PRIOR | `Mindmap.addNode/addSibling` | DONE | 上限 100 子节点 |
| MM-04 | 编辑节点文字 | 双击/编辑 | PRIOR | cardEdit title-only | DONE | 文字持久化 |
| MM-05 | 改接上级 | 工具条 | 官方拖拽 PRIOR | 对话框选择上级 | PARTIAL | 无拖拽改接 |
| MM-06 | 切换布局 | 工具条 | `mind-map.tsx:54-79` | LAYOUTS 五种；重排 | PARTIAL | 官方 LEFT/BALANCE/RIGHT |
| MM-07 | 删除子树 | 工具条 | PRIOR | `deleteSubtree` + 确认 | DONE | 撤销 |
| MM-08 | 主题样式 | 官方 Style 1–4 | `mind-map.tsx:20-37`；`mindmap-preview.ts` | 无主题 | MISSING | 需主题渲染 |
| MM-09 | 拖拽改接 | 官方 | PRIOR | 无 | MISSING | 需手势与命中 |
| MM-10 | 整图选择/移动/复制 | 对象操作 | UNVERIFIED | 节点逐个选择；无整图选择 | MISSING | 需官方确认 |
| MM-11 | 持久化 | 保存 | 格式 24 | cardKind 记录 layout/mapId；重开 reflow | DONE | 重开保持 |

## 9. Link Card（链接卡片）

当前：`Shape(type=pageLink, cardKind=linked-doc)`；`PageLinks.kt`；工具条 `3322-3354`。

| ID | 能力/状态 | 触发 | 官方证据 | 当前实现 | 状态 | 验收测试/缺口 |
|---|---|---|---|---|---|---|
| LINK-01 | 创建链接卡片 | 链接工具 → 页面选择 | 官方 Link 直接动作 PRIOR | `addLinkedDoc` | DONE | 191 页列表 |
| LINK-02 | 打开目标/返回 | 双击/工具条 | PRIOR | `openLinkedPage/returnToOrigin` | DONE | 目标文档/白板模式 |
| LINK-03 | 标题跟随重命名 | 目标重命名 | PRIOR | 渲染时解析目标页面 | DONE | 重命名后标题更新 |
| LINK-04 | 目标缺失状态 | 目标回收/删除 | PRIOR | 显示失效 | DONE | 恢复后恢复显示 |
| LINK-05 | 反向链接 | 侧栏/信息 | PRIOR | `PageLinks.backlinks` | DONE | 跳转来源 |
| LINK-06 | 复制/删除 | 工具条 | PRIOR | duplicate/delete | DONE | 撤销 |
| LINK-07 | 官方 Link 的嵌入/书签语义 | 官方 Link 菜单 | 官方存在多种 embed PRIOR/UNVERIFIED | 仅页面链接卡 | PARTIAL | 需官方产品验证 |

## 10. Embed（嵌入）

| ID | 能力/状态 | 官方证据 | 当前实现 | 状态 |
|---|---|---|---|---|
| EMB-01 | Embed 对象类型 | `editor-config/toolbar/index.ts:503-1077` 有 embed/surface 配置（SRC） | 无 | MISSING |
| EMB-02 | Linked Doc / Synced Doc / Reference / iframe | 同上（SRC 配置存在；交互 UNVERIFIED） | 无（只有 pageLink 卡片） | MISSING |
| EMB-03 | Embed 创建/编辑/尺寸/持久化 | UNVERIFIED | 无 | MISSING |

## 11. 文档块对象（数据库/共享/图片块）

这些不是官方画布对象，但被当前内容菜单/文本条暴露，必须在对象审计中区分：

| ID | 能力 | 当前实现 | 状态 | 证据 |
|---|---|---|---|---|
| DOC-01 | 数据库块创建/表格/看板/列表/日历 | `LocalDatabase` v4 | DONE（文档块） | `LocalDatabase.kt:17-60` |
| DOC-02 | 共享块引用/同步/共享撤销 | `SharedBlockCatalog` v3 | DONE（文档块） | `SharedBlocks.kt:22-72` |
| DOC-03 | 文档图片块 | Block type=image + 图片文件 | DONE | `PageStore.kt:79-91` |
| DOC-04 | 作为画布对象编辑 | 无画布对象 | MISSING | `MainActivity.kt:3167-3171` 仅插入到文档 |

## 12. Page Card（页面卡）

| ID | 能力 | 当前实现 | 状态 | 证据 |
|---|---|---|---|---|
| PC-01 | 选中/原位编辑标题和正文 | `pageCardSelected` + BasicTextField | DONE | `MainActivity.kt:3800-3801` |
| PC-02 | 上下文工具条（编辑/移动/关闭） | 三按钮 | PARTIAL | `MainActivity.kt:3088-3110`；编辑仅聚焦 |
| PC-03 | 样式/属性/复制 | 无 | MISSING | 官方页面卡属性 UNVERIFIED |

## 13. 汇总

- **真实业务已存在**：文本、笔记卡片、五种形状、图片、连接线、画笔/高亮/橡皮、Frame、思维导图、链接卡片、页面卡、文档块对象。
- **对象级最大缺口**：Text 字体族/样式/字重与范围格式；Note 块容器语义；Shape 文字/旋转/按类型默认值/Scribbled；Image 锁定/替换/说明；Connector rough/标签样式/图层；Brush 压感/平滑；Frame 嵌套/删除联动；Mindmap 官方布局/主题/拖拽改接；Embed 全部。
- **不能宣称“与官方一致”的部分**：所有官方产品工具栏外观与操作流程（无浏览器/官方 Android App），以及 BlockSuite 枚举/默认值（源码缺失）。这些在后续文档统一标 UNVERIFIED。
