# 无界画布文字框官方对齐实施记录（2026-11-01）

范围：按 `TEXT_BOX_COMPONENT_AUDIT_20261101.md` 的审计结论改造无界画布文字框；**不添加 Ask AI**，其余按官方组件结构、尺寸、浮动层级和可用动作对齐。  
页面格式保持 **25**；未改 `Page`/`Shape` 编码字段。

## 1. 已实施

### 1.1 块级工具条

官方顺序（去掉 Ask AI）：

```text
Display in Page | Note Style | Size | Scale | Draw connector | Lock | More
```

- 容器：36dp 内容高度、1dp 边框、8dp 圆角、6dp 水平内边距、8dp 子项间距。
- 文本按钮 14sp；图标按钮 24dp 宽 × 36dp 高，图标 20dp。
- 表面色：`LingyeColors.SurfaceHigh`；边框 `LingyeColors.Line`；阴影 6dp。
- `Display in Page` 使用官方 SVG 路径 + 英文文案；切换后显示 `Displayed in Page`。
- Note Style 使用官方调色板 SVG 路径；Scale 使用官方 100% 标签 + 16dp 下拉箭头 SVG；Draw connector 使用官方连接线 SVG；Size 使用官方双箭头 SVG，点击切换 **Auto height**，没有二级浮窗。
- Auto height 开启时按文字行数拟合高度并写入 `cardKind` 的 `autoheight:1`/`h:` 元数据；关闭时恢复保存的原高度；按钮选中时使用浅灰背景，未选中时透明。
- 五个组件顺序严格为 Display in Page → Note Style → Size → Scale → Draw connector；容器宽度 36dp 高、24×36 图标按钮、8dp 间距、8dp 圆角。图标路径直接取自 LIVE DOM 的 SVG path（Display、Palette、Size、Chevron、Connector）。

证据：`evidence/exact3.png`、`evidence/exact3-panel.png`。

### 1.2 Note Style 面板

- 尺寸：292dp 外宽（276dp 内容 + 水平 8dp 内边距）；在工具条下方、与 Note Style 按钮中心对齐（与用户提供的官方截图一致）。
- 结构：Fill color / Shadow / Border / Corner Radius 四段，段间距 8dp；标题 14sp/22dp。
- Fill：8 个主题色 + White + Transparent + 自定义彩虹圆；颜色随 `LingyeColors.dark` 解析；默认新文字框填充为 White（深色下 #252525）。
- Shadow：6 种（无 / 盒状 / 贴纸 / 纸张 / 悬浮 / 胶片），选中项品牌色边框；无阴影预览带圆圈斜杠，胶片带黑边框；文字渲染新增对应阴影近似。
- Border：6 档宽度（2/4/6/8/10/12）+ solid/dashed/none；宽度与线型为并排滑块式控件，选中点为深色。
- Corner Radius：6 点（0/4/8/16/24/32）滑块 + 0..32 数字输入；选中点为深色。
- 自定义颜色仍使用现有 `ColorEditorDialog` 模态框，不是官方的同浮窗内 Colors/Custom tab；列入限制。

证据：`evidence/exact3-panel.png`、`evidence/final4-style.png`、`evidence/gap-style.png`、`evidence/final3-style.png`。

### 1.3 Scale 菜单

- 68dp 宽，按钮上方展开，底部与工具条约 10–15dp 间距。
- 50% / 100% / 200%，行高 30dp；选中行浅灰底。
- 自定义输入 68×25dp，无“应用”按钮；关闭菜单或输入结束时应用。
- 200% 同时缩放 fontSize/width/height（与 LIVE 观察一致）；仍使用 `Shape.cardKind = "scale:..."` 私有元数据。

证据：`evidence/gap-scale.png`。

### 1.4 元素 More 菜单

- 208dp 宽、30dp 行高、分组间距 8dp；顺序与官方一致：
  1. Frame section、Group section（禁用，本应用无对应模型）
  2. Bring to Front / Bring Forward / Send Backward / Send to Back（可用）
  3. Copy / Copy as Image（禁用）/ Copy as Markdown / Copy link to block / Duplicate
  4. Turn into linked doc（禁用）
  5. Delete
- Copy link 使用 `lingye://page/{page.id}?shape={shape.id}`；本应用尚无深链处理，仅复制文本。
- 菜单在工具条右侧展开；官方在无空间时顶部钳制，本实现为右侧展开。

证据：`evidence/gap-more.png`。

### 1.5 文本选区工具条

编辑态双击进入行内编辑后，选中文字范围出现独立工具条（**无 Ask AI**）：

```text
T（文字样式）| Alignment | B | Code | Inline Equation | I | Link | Strike | U | Highlight | Create Linked Doc | Comment | More
```

- 2026-11-01 晚按 AFFiNE 页面模式 LIVE 重新复刻：594×38 外框、24×36 图标按钮、40×36 文字样式/对齐/高亮触发、8dp 间距、官方 SVG 图标；Comment 按用户截图添加（LIVE 无此按钮）。
- 可用：文字样式（Text/H1–H6/Code Block）、Alignment、Bold、Italic、Underline、Strikethrough、Inline Code、Highlight（Color/Background）、Link（存 `cardKind` 的 `link:` token，点击打开外链/页面）、Inline Equation（TeX→Unicode 子集，存 `math:` token）、Comment（本地评论，存 `comment:` token）、Create Linked Doc（链接到现有页面）、More（Copy / Copy as Markdown / Copy link to block / Duplicate / Delete）。
- 未实现：列表/引用（自由文本框无块模型）；公式非 KaTeX 全量；评论无云端同步；公式/评论浮窗的稳定触控截图未完成。
- 详细 LIVE 尺寸、浮窗几何和证据见 `TEXT_SELECTION_TOOLBAR_20261101.md`。
- 编辑态不再显示“完成”按钮；点击画布或返回键退出编辑。框内 B/I/U chip 已移除。

证据：`evidence/affine-inline-20261101/menu-13.png`、`menu-16.png`、`menu-17.png`、`menu-18.png`、`menu-20.png`、`menu-21.png`、`menu-23.png`。

### 1.6 选择框、加号、锁定、Display in Page

- 选择框保留蓝色边框、白色四角手柄、左上拖拽点阵；锁定文字不画选择框。
- 加号缩小到 24dp，左右各一，垂直居中；点击创建相邻文字并进入编辑。
- Lock：工具条和选择框消失，元素右上方显示 24dp 解锁徽标；点击解锁。
- Auto height：Size 图标点击后在“拟合文字高度”和“恢复之前高度”之间切换；不显示第二级菜单；文字编辑、字号/样式变化时重新计算拟合高度。
- Display in Page：用 `cardKind` 的 `page:1` 元数据标记；文档模式在正文下方渲染该文字内容；再次点击移除。官方 toast 的 Undo/View in Toc 未实现。
- 新文字框默认 White 填充、Box 阴影、8dp 圆角（`Cards.addTextBox`）。

证据：`evidence/gap-lock.png`、`evidence/final2-display.png`、`evidence/final2-doc.png`、`evidence/display-off2.png`、`evidence/final2-sel.png`。

## 2. 数据与兼容

- 页面格式仍为 **25**；没有新增/修改二进制字段。
- 新增元数据放在既有 `Shape.cardKind` 字符串中：`scale:100`、`valign:middle`、`page:1`、`link:<start>:<end>:<base64url>`；旧页面没有这些标记时按 100%/top/未显示/无链接处理。
- `TextSpan` 新增 `STRIKE`、`CODE`、前景/背景 4 bit 颜色、`LINK`、`MATH`、`COMMENT`；仍写入格式 25 的 flags，不新增二进制字段。公式为 TeX→Unicode 子集，评论为本地存储。
- 活动文档当前为 193：包含本次新建的 `ToolbarTest` 选区工具条测试页；另有 11:42 前后误建的空白测试页仍未清理。

## 3. 验证

- 构建：`testDebugUnitTest assembleRelease` 成功；**294 项 JVM 测试，0 失败**。
- 安装：R8 Release 覆盖安装到模拟器，数据保留。
- 已验证：
  - 块级工具条顺序/间距/图标/Display in Page/Note Style/Scale/Connector/Lock/More；
  - Note Style 面板上方展开、6 阴影、6 边框宽度、6 圆角点、Fill 色板；
  - Scale 50/100/200/自定义输入与 200% 缩放；
  - 文本选区工具条出现、Bold/Italic/Underline/Strikethrough/Inline Code/Highlight/Link 入口按 LIVE 复刻；文字样式/对齐/高亮/更多/链接浮窗已截图；
  - Lock 隐藏工具条并显示徽标，点击解锁；
  - 加号创建相邻文字；
  - Display in Page 在文档模式显示并在再次点击后移除。
- 未验证/未完成：
  - Auto height 的高度拟合是按 `TextFormatting.wrap` + 近似字符宽度计算，未与官方字体度量逐像素比对；最终按钮切换截图受测试页选中状态影响，未完成；
  - Frame/Group section、Copy as Image、Turn into linked doc；
  - Inline Equation、Comment、列表/引用；链接点击打开；
  - 官方同浮窗内 Custom color tab（当前为模态对话框）；
  - 文本颜色独立入口（当前随 Fill 自动选择对比色，无单独控件）；
  - Ask AI（按用户要求不添加）；
  - Android 深色主题下的全部新面板逐项截图（代码使用主题色，但未逐项回归）。

## 4. 发布

- Release：`releases/lingye-v0.15-text-box-autoheight-release-20261101.apk`
  - 2,545,345 字节
  - SHA-256 `3C9643335D4007AAE165F92EBE3751FDF0D6D29719B8AB051B62ACC3AC98A8BD`
- Debug：`releases/lingye-v0.15-text-box-autoheight-debug-20261101.apk`
  - 15,046,474 字节
  - SHA-256 `5F39775B74AAEF9433E36F632B2457D288B109BC700B69A9D909EA33C35AC630`
- 页面格式：25（未变）。
