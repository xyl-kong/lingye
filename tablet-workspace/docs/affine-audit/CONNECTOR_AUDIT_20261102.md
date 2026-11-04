# AFFiNE 连线功能审计与实现清单（2026-11-02）

审计对象：官网 AFFiNE 连线（Connector）。证据以锁定源码为准，辅以现有模拟器截图。

## 1. 官方源码证据

- 仓库：`toeverything/AFFiNE`，commit `8cef09e1dd62b47a3cbc9f352aa10b3a90c75d34`。
- 本地副本（临时目录）：`C:\Users\17482\Documents\ChatGPT\deepseek\.tmp\connector-audit\`
  - `toolbar_config.ts` ← `blocksuite/affine/gfx/connector/src/toolbar/config.ts`
  - `connector-tool.ts` ← `.../src/connector-tool.ts`
  - `toolbar_quick-tool.ts` ← `.../src/toolbar/quick-tool.ts`
  - `connector-manager.ts` ← `.../src/connector-manager.ts`
  - `view_view.ts` ← `.../src/view/view.ts`
- 常量：`blocksuite/affine/model/src/consts/connector.ts`、`line.ts`。
- 模型：`blocksuite/affine/model/src/elements/connector/connector.ts`。

## 2. 官方能力（源码确认）

### 2.1 连接线工具（创建）

- 两种模式：`Dragging`（拖拽创建）与 `Quick`（快速连接）。
- 拖拽：指针按下时用 `ConnectionOverlay.renderConnector(point)` 找出**指针下的元素**及其连接位置（Connection = id + position），从该元素的最近连接点开始；拖动时持续更新 target（排除源元素），松手完成。
- 快速连接：`quickConnect(point, element)` 从元素开始创建，指针移动时找目标，点击目标完成；无目标时取消（`_allowCancel` + `deactivate`）。
- 形状锚点：普通元素使用 `ConnectorEndpointLocations`（四边/中心等），三角形使用 `ConnectorEndpointLocationsOnTriangle`。
- 创建过程中有 ConnectionOverlay：高亮可连接元素与最近连接点，并绘制预览线。
- 创建结束后：`dragEnd` 调用 `setTool(DefaultTool)` 并把新连接线设为选中（`selection.set`）。
- 默认值（consts）：起点 `None`、终点 `Arrow`、模式 `Curve`；线宽默认 `Four`（工具栏 fallback）；标签最大宽度 280。

### 2.2 连接线对象工具条

当选中连接线时显示（`connectorToolbarConfig`）：

| 项 | 官方内容 |
|---|---|
| 描边 | 颜色选择器（含自定义色）+ 线宽（2/4/6/8/10/12）+ 线型（实线/虚线等 `LINE_STYLE_LIST`） |
| 风格 | Scribbled/Rough 切换（feature flag） |
| 起点样式 | None / Arrow / Triangle / Circle / Diamond 图标菜单 |
| 翻转方向 | 交换 front/rear 端点样式 |
| 终点样式 | 同上图标菜单 |
| 形状 | Curve / Elbowed(Orthogonal) / Straight 图标菜单 |
| 文字 | 无标签且有且仅有一条时显示“Add text”；有标签时显示文字格式（字号/字重/斜体/对齐等） |

### 2.3 其它

- 连接线标签就地编辑，`labelXYWH` 保持中心，最大宽度 280；支持标签样式。
- 连接线可吸附到 Group（快速连接时对 Group 使用 sourceBounds）。
- 端点样式只有 5 种：None/Arrow/Triangle/Circle/Diamond；默认 front=None、rear=Arrow。
- 未确认项：不同 Frame/锁定元素的连接限制、协作场景下的实时跟随，源码未在本次审计范围内逐项验证。

## 3. 当前实现（应用内）

代码位置：

- 工具分支：`MainActivity.kt` 画布 `detectDragGestures` 的 `tool == "连线"` 分支（约 3944-3962）。
- 绑定与几何：`CanvasGroups.kt` 的 `resolvedConnector`、`bindConnectorEnd`、`nearestShapePort`、`shapePort`；`ConnectorGeometry.kt`。
- 连接线工具条：`MainActivity.kt` 约 5779-5879。
- 参数条：`tool == "连线"` 时显示曲线/折线/直线、宽度、颜色。
- 入口：底部工具条“连接参数”、文本框工具条“绘制连接线”、矩形一级工具条“绘制连接线”（2026-11-02 新增）。

当前行为（实测 `conn-1.png`、`conn-2.png`）：

- 点击底部连线按钮后进入连线工具，显示参数条与所有形状的连接点。
- 拖拽可创建连线，但**只有拖拽起点/终点距离某个端口 24 世界单位内才会绑定**；从形状内部起笔（如本例椭圆中心附近）不会绑定源形状，形状移动后连线不会跟随。
- 创建后仍停留在连线工具，**不自动选中新建连线**，也**不回到选择工具**；需要再点最左工具才能调整。
- 创建过程没有预览线/目标高亮，松手前看不到将连到哪里。
- 从文本编辑状态点击文本工具条的连线按钮不会退出编辑/隐藏键盘。
- 默认：线宽 2、终点圆点、起点无、曲线；与官方（宽 4、终点箭头、起点无、曲线）不一致。
- 连接线工具条：仅两个固定颜色、没有完整色板/自定义色；没有线宽与线型控件；端点样式为文字列表；没有“仅交换端点样式”的翻转（现有“反向连接”会交换整条线的起终点几何）；标签用模态对话框编辑，无就地编辑/文字格式；没有线型字段（页面格式 25 无 strokeStyle）。

## 4. 差异清单

级别：P0 = 核心创建链路错误；P1 = 工具条能力缺失；P2 = 细节/未验证。

| ID | 项 | 官方 | 当前 | 处理 |
|---|---|---|---|---|
| CN-01 | 源绑定 | 指针下元素即源，自动选最近连接点 | 仅靠近端口 24 单位才绑定 | P0 实施 |
| CN-02 | 目标绑定 | 指针下元素为目标，自动吸附最近连接点 | 同上 | P0 实施 |
| CN-03 | 创建完成状态 | 回到默认工具并选中新连线 | 停留连线工具、不选中 | P0 实施 |
| CN-04 | 创建预览 | 预览线 + 目标/连接点高亮 | 无 | P0 实施 |
| CN-05 | 文本编辑进入连线 | 退出编辑 | 保持编辑/键盘 | P0 实施 |
| CN-06 | 默认值 | 宽 4、终点箭头、曲线 | 宽 2、终点圆点、曲线 | P0 实施 |
| CN-07 | 快速连接 | 点击元素→点击元素完成 | 无 | P1 实施（简版） |
| CN-08 | 工具条颜色 | 完整色板 + 自定义 | 两个固定色 | P1 实施 |
| CN-09 | 线宽/线型 | 2–12 六档 + 实线/虚线 | 无控件、无线型字段 | P1 线宽实施；线型需格式 26，暂缓 |
| CN-10 | 起点/终点样式 | 图标菜单 5 种 | 文字菜单 5 种 | P1 改为图标菜单 |
| CN-11 | 翻转方向 | 仅交换端点样式 | 反向连接交换整条线 | P1 实施（新增仅交换样式） |
| CN-12 | 连接形状 | 图标菜单 3 种 | 文字菜单 3 种 | P1 改为图标菜单 |
| CN-13 | 标签 | 就地编辑 + 格式 | 模态对话框 | P1 暂缓（记录） |
| CN-14 | 标签最大宽度 280 | 有 | 无限制 | P2 |
| CN-15 | Group/Frame 连接 | 支持 Group | 未验证 | P2 |

## 5. 本轮执行范围

本轮先完成 P0（CN-01~06）与 P1 中的 CN-07、CN-08、CN-10~12；CN-09 的线型与 CN-13 的就地标签编辑因涉及页面格式/编辑器重构，列为后续项。

验收标准：

1. 从形状内部起笔/落笔都能绑定该形状，形状移动后连线端点跟随。
2. 拖拽过程中能看到预览线与目标形状/连接点高亮。
3. 松手后回到选择工具，新连线被选中并显示连接线工具条。
4. 从文本编辑/文本工具条进入连线时先退出编辑并收起键盘。
5. 新建连线默认宽 4、终点箭头、曲线。
6. 连接线工具条可用完整色板、线宽档位、图标式端点/形状菜单，并有“仅翻转端点样式”按钮。
7. 点击形状→点击另一形状可完成一次快速连接（Esc/空白取消）。

## 6. 实施记录（2026-11-02）

已完成：

- CN-01/02 源/目标绑定：新增 `ConnectorInteraction.kt` 的 `Page.shapeIndexAt` 与 `Page.nearestPortOnShape`；连线工具在按下时按指针下的形状选择最近锚点，移动时持续吸附目标形状，不再要求距离端口 24 单位。
- CN-03 创建后状态：松手创建连线后立即 `selection` 到新连线、`chooseTool("选择")` 回到选择工具并显示连接线工具条。
- CN-04 预览：拖动时绘制蓝色虚线预览、终点圆点、目标形状薄荷色描边，并高亮目标锚点（`cp-1.png`）。
- CN-05 文本编辑进入连线：新增 `enterConnectorTool`，文本/形状工具条与底部工具条统一使用；进入连线前退出编辑并隐藏键盘。
- CN-06 默认值：`WorkspaceSettings` 新安装默认宽 4、终点箭头、曲线；`Page.addConnector` 默认终点箭头、宽度 4；旧页面解码仍使用历史默认（dot/2px），避免改变既有页面外观。
- CN-07 快速连接：连线工具下点击形状记录 quick source 并描边高亮，再点击另一形状即创建连线、选中新连线并回到选择工具（`cn-9.png`、`cn-10.png`）；点击空白或再点同一形状取消。
- CN-08 颜色：连接线工具条改为完整色板 + 自定义色（292dp 菜单，与文本/形状工具条一致）。
- CN-09 线宽：工具条增加 `N px` 控件与 2/4/6/8/10/12 六档圆点菜单（`ct-3.png`）；线型字段仍需页面格式变更，未实施。
- CN-10/12 端点与形状菜单：改为图标行/图标按钮，起点/终点各 5 种、形状 3 种（`ct-4.png`）。
- CN-11 翻转方向：新增“仅交换起点/终点样式”按钮，不再交换整条线几何。
- 工具条几何：连接线工具条改为与文本框工具条一致（36dp 高、24×36 按钮、8dp 间距、1dp 边框、8dp 圆角、6dp 阴影）；颜色/宽度菜单 292dp 居中下 18dp，其余菜单 208dp。

未完成/后续：

- CN-09 线型（实线/虚线）需要页面格式 26 增加 `strokeStyle` 字段与迁移方案；本轮未改格式。
- CN-13 标签就地编辑与文字格式（字号/字重/斜体/对齐）；当前仍为模态对话框。
- CN-14 标签最大宽度 280 与位置约束。
- CN-15 Group/Frame 作为连接源/目标的完整语义未逐项验证。
- 端点图标复用现有 `END_TRIANGLE`/`END_DIAMOND` 资源，视觉上与官方图标存在差异，需后续逐项对照。
- 快速连接目前由“连线工具点击形状”触发，尚未实现官方悬浮在形状旁的小连接按钮。

验证证据：`conn-1.png`/`conn-2.png`（旧行为）、`cn-1.png`/`cn-2.png`（拖拽绑定与移动跟随）、`cn-9.png`/`cn-10.png`（快速连接）、`cp-1.png`（拖动预览）、`ct-1.png`（紧凑连接线工具条）、`ct-2.png`（色板）、`ct-3.png`（线宽）、`ct-4.png`（端点图标菜单）。

发布：`releases/lingye-v0.15-connector-release-20261102.apk`，2,594,497 字节，SHA-256 `28D123922820E91FB5E1B374F4EF01FC0DCD87B42FFCF13554B22333992CBFCB`；309 项 JVM 测试 0 失败；页面格式仍 25，`ToolbarTest` 测试页因连线回归增至 2,942 字节。
