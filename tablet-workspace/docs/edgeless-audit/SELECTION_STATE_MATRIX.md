# 选中状态矩阵（阶段 A/B）

审计日期：2026-11-01。当前实现位置均在 `app/src/main/java/com/example/tabletworkspace/MainActivity.kt`，除特别标注。

## 0. 官方选中模型证据等级

- BlockSuite `gfx/*/selection`、`edgeless-toolbar` 源码不在本机可读；官方单选/多选/编辑态切换只能引用既有审计（PRIOR），不能标 SRC。
- AFFiNE 前端本地源码可证的是对象设置与工具栏动作的存在（`affine-src/.../edgeless/*.tsx`、`editor-config/toolbar/index.ts`），不是完整选中状态机。
- 因此官方“选中态”行为在本文统一为 **PRIOR/UNVERIFIED**；当前行为为 **CUR**。

## 1. 当前状态变量

| 变量 | 类型/初始 | 行 | 作用 |
|---|---|---|---|
| `selection` | `CanvasSelection?` / StableSelectionDelegate | 2281 | 主选中对象（最后选中） |
| `selectedItems` | `List<CanvasSelection>` / StableSelectionListDelegate | 2281 | 全部选中项 |
| `multiSelect` | `Boolean` | 2281 | 追加/取消选择开关 |
| `endpointDrag` / `endpointPreview` | `Boolean?` / `Point?` | 2281 | 连线端点拖动 |
| `resizeAnchor` | `Point?` | 2281 | 四角缩放锚点 |
| `boxStart` / `boxEnd` | `Point?` | 2281 | 框选矩形 |
| `dragging` | `Boolean` | 2281 | 是否真正移动/缩放 |
| `pageCardSelected` | `Boolean` | 2283 | 页面卡编辑态 |
| `cardEdit` | `Int?` | 2331 | 卡片/文本/思维节点原位编辑索引 |
| `points` | `List<Point>` | 2281 | 当前画笔/擦除轨迹 |
| `selectionRestored` | `Boolean` | 2282 | 会话恢复完成 |

`CanvasSelection(type,index)` 的 type 只有 `"shape"`、`"image"`、`"connector"`、`"stroke"`（`CanvasSelection.kt:5`）；`index` 是列表索引，跨重启通过 stable id 恢复（`CanvasSelection.kt:37-57`）。

## 2. 派生单选状态

除特别说明，均要求 `selectedItems.size == 1`（2380-2388）：

| 对象 | 派生变量 | 条件 | 工具条 |
|---|---|---|---|
| 页面卡 | 已移除 | 用户确认无界画布不需要页面摘要卡；不再渲染/选中/编辑 | — |
| 笔记卡片 | `selectedCardIndex` | shape.type=="card" | 3111-3178 |
| 几何形状 | `selectedShapeIndex` | rect/roundRect/ellipse | 3179-3321 |
| Frame | `selectedFrameIndex` | frame | 3398-3448 |
| 链接卡 | `selectedPageLinkIndex` | pageLink | 3322-3354 |
| 思维节点 | `selectedMindNodeIndex` | mindNode | 3355-3397 |
| 文本 | `selectedTextIndex` | text | 3449-3591 |
| 图片 | `selectedImageIndex` | image | 3592-3650 |
| 笔迹 | `selectedStrokeIndex` | stroke | 3651-3707 |
| 连接线 | `selectedConnectorIndex` | **仅 selection.type=="connector"，未检查 selectedItems.size==1** | 3708-3798 |

**已知不一致**：连接线在多项选择时仍可能显示单选工具条（CUR，2388）；其余对象要求单项。

## 3. 状态转换矩阵

| 事件 | 前置状态 | 结果 | 工具栏/浮窗 | 代码 |
|---|---|---|---|---|
| 点击空白（选择工具） | 任意 | `selection=null`；`selectedItems=empty` | 清除对象工具条 | 2694 |
| 点击对象 | 无选中/已选中 | 单选；多选开启时追加/移除 | 对应对象工具条 | 2694 |
| 双击文本/卡片/思维节点 | 单选 | `cardEdit=index`；软键盘/焦点进入 | 原位编辑 Surface | 2687-2693,4206-4245 |
| 双击链接卡 | 单选 | 打开目标页 | 关闭画布编辑 | 2690-2693 |
| 框选拖拽 | 选择工具 | 框选结束 `selectInBox` | 多选条 | 2769,2778；`CanvasTransform.kt:60-83` |
| 拖动已选对象 | 单选/多选 | move；网格吸附 | 工具条跟随 | 2770-2790 |
| 拖动四角 | 单选/多选 | resize（shape/image/connector） | 工具条跟随 | 2787；`CanvasSelection.kt:22-28` |
| 锁定对象拖动 | 选中 | 阻止移动/缩放 | 工具条仍显示 | 2389-2390,2787 |
| 方向键 | 选中 | nudge（不检查锁定） | 无 | 2424 |
| 全选 | 任意 | 排除 locked shapes | 多选条 | 2432 |
| Esc | 有选中 | 先清选中；再关菜单/工具 | 清除 | 2587-2592 |
| 系统 Back | 有菜单/编辑/选中 | 按 `BackNavigationPolicy` 分层关闭 | 关闭 | 2627-2636 |
| 切换非选择工具 | 选中 | 清除页面卡编辑；清 `activeMenuButton`；保留/显示参数条 | 参数条 | 2364-2366 |
| 打开文本/思维导图/内容/更多菜单 | 任意工具 | 收起其他菜单，画布底层切选择工具，菜单按钮保持当前项 | 参数条隐藏 | 2297-2307,4058,4075,4105 |
| 关闭菜单 | 菜单开 | 当前菜单按钮仍高亮（activeMenuButton） | 无参数条（工具为选择） | 2307 |
| Frame 内部点击 | 无 | 命中边框/标题选中 Frame；内部空白穿透到成员 | Frame/成员工具条 | `FrameGeometry.kt:25-36` |
| 组选中 | 选择 shape+image | 组内成员一起展开 | 多选条 | `CanvasGroups.kt:27-31` |
| 演示模式 | 任意 | Esc/方向键切换 Frame | 演示浮条 | 2509-2513 |
| 会话恢复 | 启动 | 按 stable id 恢复 `selectedItems` | 工具条恢复 | 2338-2354 |

## 4. 各对象工具条与状态

| 对象 | 工具条按钮（CUR） | 缺失/风险 |
|---|---|---|
| 卡片 | 编辑、勾选下一项（todo）、保存模板、复制、缩放百分比（只读）、蓝/黄、图层与更多（图层 4 项 + 阴影 3 项 + 圆角 5 项）、插入文档、锁定、删除 | 颜色仅两色；插入文档丢图片/待办/格式 |
| 几何形状 | 形状类型（rect/roundRect/ellipse）、填充、边框色、边框样式、边框宽度循环、复制、图层、锁定、删除 | 无圆角 UI、无文字编辑、无旋转、边框宽度仅 1/2/4 |
| 文本 | 文本（编辑）、字号菜单+/-、颜色、对齐、复制、锁定、更多（图层/删除） | 无字体族/样式/字重、无范围格式 |
| 图片 | 裁剪、旋转、水平翻转、垂直翻转、复制、图层、删除 | 无锁定、替换、说明 |
| 连接线 | 路径、起点 marker、终点 marker、标签、翻转、动画、蓝/灰、删除 | 无图层、无标签样式、色板仅两色、无 rough/dash |
| 笔迹 | 颜色、减细、宽度文本、加粗、图层、删除 | 无缩放、无锁定/分组 |
| Frame | 重命名、背景色、定位、导航、演示、复制、删除 | 无锁定、无嵌套、无删除成员 |
| 思维节点 | 添加子、添加同级、编辑、布局菜单、改接、删除 | 无主题、无拖拽改接 |
| 链接卡 | 打开、复制、删除 | 无官方 Link 的其他语义 |
| 页面卡 | 编辑（仅聚焦）、移动、关闭 | 无属性/复制/样式 |
| 多选 | 复制、对齐（左/右/上/下）、组合、取消组合（有组时）、绑定连线（仅 2 个 shape）、保存模板、删除 | connector/stroke 不参与分组；无混合类型属性编辑 |

## 5. 多选与分组

- 多选开启：单击对象追加/移除；`selectedItems` 保存全部成员（2694）。
- 组合：`groupSelections` 仅给 shape/image 写入 group id；connector/stroke 不进组（`CanvasGroups.kt:16-21`）。
- 展开：`selectionMembers` 只按 shape/image group 展开（`CanvasGroups.kt:27-31`）；Frame 成员是另一套几何展开（`FrameGeometry.kt:104-109`）。
- 多选工具条只显示复制/对齐/组合/取消组合/绑定/模板/删除；没有混合属性面板（CUR，3959-3999）。
- 官方多选/组的行为为 PRIOR/UNVERIFIED；当前不能宣称一致。

## 6. 编辑态与焦点

| 编辑态 | 进入 | 退出 | 键盘/IME |
|---|---|---|---|
| 卡片原位编辑 | 双击卡片 / 工具条编辑 | “完成”按钮、Back、切工具 | 两个 BasicTextField；每次输入 `changedText`；无 IME action |
| 文本原位编辑 | 双击文本 / “文本”胶囊 | 完成、Back、切工具 | body-only BasicTextField |
| 思维节点编辑 | 双击节点 / 编辑 | 完成、Back | title-only |
| 页面卡编辑 | 点击页面卡 | 关闭、Back | title focus 60ms 延迟 |
| 连接线标签 | 工具条标签按钮 | AlertDialog 保存/取消 | 无内联编辑 |
| 文档块编辑 | 文档视图 | 普通焦点 | 文档快捷键/IME |

`cardEdit` 或 `pageCardSelected` 为真时，画布快捷键直接返回 false（2559），按键进入文本框；Back 策略先处理内联编辑（2652-2656）。软键盘 Insets、输入法组合文本、选区工具栏均未专项处理（详见 ANDROID_INTERACTION_SPEC）。

## 7. 选中恢复与持久化

- 选区不入页面文件；只通过 `WorkspaceSession` 保存 stable ids（`CanvasSelection.kt:53-57`；`WorkspaceSession.kt`）。
- 页面/缩放/平移/工具/视图在同一会话恢复链路中保存；重启时 `restoreSelections` 重建 `selectedItems`（2338-2354）。
- 对象索引变化（删除/重排）后，stable id 能避免选区错位；但文档块无 stable id。

## 8. 状态覆盖缺口

1. 连接线单选条件不一致（2388）。
2. 锁定只在拖动/缩放和全选生效；方向键、删除、对齐、组合未统一拦截（2389-2390,2405,2424,2487,2501）。
3. 图片/连线/笔迹没有锁定状态；官方对象锁定语义无法对齐。
4. 多选没有混合属性编辑；官方多选行为未验证。
5. 页面卡编辑按钮仅聚焦，未进入明确的编辑状态。
6. 无文本选区状态（range），因此没有选区级工具栏与格式撤销。
7. 无剪贴板状态（Ctrl+V/paste），复制只能通过工具条 duplicate。
8. Frame 嵌套/成员删除联动未建模，选中语义与官方不完整。
