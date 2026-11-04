# 视觉差异报告（阶段 A/B）

审计日期：2026-11-01。范围：对象工具栏、参数条、菜单、选中框/控制柄、深色/窄屏状态。**本报告不把“看起来差不多”当作通过。**

## 0. 可用证据与限制

| 证据 | 状态 | 说明 |
|---|---|---|
| AFFiNE 官方 Edgeless 实时产品 | 不可用 | `docs.affine.pro` 403；无浏览器自动化；没有官方 Android App |
| AFFiNE 本地源码 | 部分可用 | 仅能证明设置面板的属性/控件存在（`affine-src/.../edgeless/*.tsx`），**不能**证明工具栏像素布局 |
| BlockSuite 工具栏源码 | 不可用 | `blocksuite-reference` 工作树为空；只能引用既有审计 PRIOR |
| 当前应用截图 | 可用 | `docs/affine-audit/evidence/*.png`；均为当前应用，不是官方截图 |
| 官方产品截图 | 未提供 | 既有审计的 `REFERENCE_BASELINE.md` 有文字观察，但本环境无法重放 |

因此：**对象工具栏的官方精确位置、宽高、间距、圆角、字体、图标、阴影在本文一律为 UNVERIFIED**；本文只做“当前视觉 token 与结构差异”和“可验证的本地设置一致性”。

## 1. 当前视觉 token（CUR）

| 项 | 当前值 | 证据 |
|---|---|---|
| 主工具栏按钮 | 40dp 高；线型 40/60dp 宽；插画 40dp | `VisualDesign.kt:391-402,561-577` |
| 主工具条 Surface | 圆角 20dp、阴影 12dp、1dp 边框 | `MainActivity.kt:3994-4000` |
| 主工具条内边距/间距 | 7/5dp、2dp | 4010-4013 |
| 参数条 Surface | 圆角 14dp、阴影 8dp、1dp 边框 | 2145-2153 |
| 参数 chip | 30dp 高 | 2169-2188 |
| 参数宽度点 | 28dp | 2156-2165 |
| 对象工具条按钮 | 34dp（`CompactIconAction`） | `VisualDesign.kt:480-496` |
| 对象工具条偏移 | 对象左上 `y-54..58dp`，部分 x 未 clamp | 3089-3091,3116,3730 |
| 底部工具条 | 底部 28dp；按钮 40dp | 3806 |
| 缩放条 | 右侧 16dp、底部 36dp | 4164-4167 |
| 底部菜单下移 | 24dp；与工具栏约 16dp | 4058,4075,4105 |
| Frame 参数条 | 宽视口左移 110dp | 3804,3865 |
| 选中底色 | 工具栏/chip/宽度点 = `LingyeColors.Subtle` 灰 | `VisualDesign.kt:385,557`；`MainActivity.kt:2159,2180` |
| 页头文档/白板选中底色 | 白色 | `MainActivity.kt:1321-1333` |
| 选择框 | 白色外描边 + 主色线 + 四角圆点 | 画布绘制（B 报告） |

## 2. 当前状态截图对应

| 状态 | 当前证据 | 可用于什么 | 不能证明什么 |
|---|---|---|---|
| 主工具栏/默认 | `toolbar-polish-base.png`、`toolbar-polish-final.png` | 当前布局/选中底色/缩放对齐 | 官方一致 |
| 对象选中（卡片） | `note-style-card-toolbar.png`、`toolbar-test-*` | 当前按钮数量/顺序 | 官方按钮顺序 |
| 形状选中 | `audit-shape-selected*.png`、`toolbar-test-shape.png` | 当前填充/边框/图层 | 官方形状文字样式 |
| 连接线选中 | `connector-selected.png`、`connector-marker*.png` | 当前端点/路径/标签 | 官方 rough/dash/标签字体 |
| Frame 选中 | `frame-selected-toolbar.png`、`frame-background-menu.png` | 当前属性/导航/演示 | 官方 Frame 嵌套/属性全集 |
| 思维节点 | `mindmap-node-toolbar.png`、`mindmap-layout-menu.png` | 当前节点操作 | 官方 3 布局/4 主题 |
| 文本参数条 | `toolbar-polish-text-bar.png`、`toolbar-polish-text-h1.png` | 当前文本条/字号 | 官方字体族/样式/字重 |
| 图片选中 | `toolbar-test-*` / 现有图片编辑截图 | 裁剪/旋转/翻转 | 官方 Image 属性 |
| 多选 | `multiselect-selected.png`、`toolbar-multiselect.png` | 当前对齐/组合 | 官方混合属性 |
| 菜单 | `toolbar-polish-more-gap.png`、`toolbar-mindmap-*`、`toolbar-content-*` | 当前间距/内容 | 官方菜单顺序/图标 |
| 窄屏/深色 | `narrow-param-*.png`、`font200-param-*.png`、`dark-theme-*.png` | 当前适配 | 官方平板布局 |

## 3. 与官方设置的本地可证差异

以下只比较“官方存在的属性/控件”与当前 UI 是否有对应入口，不比较像素：

| 对象 | 官方设置（SRC） | 当前 UI | 差异 |
|---|---|---|---|
| Text | 颜色/字体族/样式/字重/对齐 | 颜色/字号/对齐 | 字体三项缺失；字号范围不同 |
| Shape | 5 类型/General-Scribbled/填充/边框色/样式/宽度/文字色/字体/字号/对齐 | 3 类型切换、填充、边框色/样式/宽度 | 文字、字体、Scribbled、按类型默认值缺失 |
| Note | 背景/圆角/阴影/边框 | 背景两色+设置、圆角/阴影/边框 | 背景色板不完整；Note 块语义缺失 |
| Frame | 背景 | 背景六色 | 官方 Transparent+色板；当前色板自定 |
| Connector | rough/路径/边框样式/宽度/颜色/起终端点/标签字体 | 路径/宽度/颜色/端点/标签文字 | rough/dash/标签字体/完整色板缺失 |
| Pen | 颜色/线宽 | 十色/六档 | 数值范围不同（官方 2–12，当前画笔 2–12/高亮 10–30） |
| Mindmap | LEFT/BALANCE/RIGHT + Style 1–4 | 5 结构 | 结构与主题不同 |

## 4. 结构性视觉差异（可验证）

1. **工具条不是统一组件**：10 个对象工具条各自定义按钮/间距/圆角/阴影；当前截图可见卡片/形状/连线工具条的按钮密度和分组不一致。
2. **选择框/控制柄**：当前为白色外描边+主色线+四角圆点；官方样式未验证，不能判定一致。
3. **工具栏选中态**：当前灰色底（近期改动），AFFiNE 官方颜色未验证；不能声称“与官方相同”。
4. **参数条与对象工具条并存**：底部参数条与对象工具条可能同时出现，官方是否允许同屏 UNVERIFIED。
5. **菜单定位**：底部菜单用 24dp 经验下移；对象菜单默认定位，间距不统一。
6. **图标体系**：底部工具使用 AFFiNE 图标路径/插画；对象工具条按钮使用 `CompactIconAction` 线型图标，与官方对象工具栏图标的对应关系未逐一验证。
7. **字体/字号**：当前对象工具条大量 11sp/9sp 文本；官方字号/字重 UNVERIFIED。

## 5. 需要补证的官方视觉证据

| 证据 | 用途 | 当前状态 |
|---|---|---|
| 官方 Edgeless 对象选中态截图（Text/Note/Shape/Image/Connector/Frame/Mindmap/Embed） | 按钮顺序、分组、图标、尺寸 | 缺失 |
| 官方对象工具条在 50%/100%/200% 缩放的截图 | 浮动定位/尺寸策略 | 缺失 |
| 官方菜单/二级弹窗截图 | 菜单项顺序、间距、禁用态 | 缺失 |
| 官方深色/窄屏平板截图 | 颜色/响应式 | 缺失 |
| 官方字体/图标资源清单 | 字体、图标尺寸 | 部分（`@blocksuite/icons 2.2.17` PRIOR） |
| 官方 Note/Shape 文本编辑态 | 选中框、光标、选区工具栏 | 缺失 |

## 6. 结构化验收方案（阶段 F）

1. **Compose 语义断言**：为每个工具条/菜单/参数条添加 `testTag`，断言存在、可见、enabled、bounds 在安全区。
2. **属性绑定断言**：选择对象 → 打开工具条 → 读取颜色/字号/对齐显示；修改 → 断言对象字段/画布重绘/撤销/重开。
3. **Bounds 断言**：在 25%/100%/400%、四角、横竖屏、分屏、软键盘下断言 `left/top/right/bottom` 与锚点/安全区关系。
4. **Golden 截图**：当前状态先建立基线；官方截图到位后再建立官方对照 golden；容差需明确（建议像素差 ≤2%，布局断言优先）。
5. **重复修改回归**：每次对象改动后跑：当前对象、共享工具栏、相关浮窗、其他对象非目标行为、视觉漂移检查（见 `IMPLEMENTATION_ROADMAP.md`）。

## 7. 结论

当前视觉可直接验证的是“内部一致性”和“Android 适配”，不是“官方高保真”。在官方截图/产品访问补齐前：
- 不得宣称对象工具栏、菜单、选中框与 AFFiNE 像素一致；
- 可以把当前 token/状态截图作为回归基线；
- 阶段 C 应优先统一组件与定位 token，减少后续视觉漂移，再逐对象补官方证据。
