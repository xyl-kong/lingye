# 阶段 D1 实施记录：Text 字体、选区格式与格式 25 迁移

日期：2026-11-01  
状态：D1 已完成并在模拟器迁移；D2 Note 首版已完成（见 `STAGE_D2_IMPLEMENTATION.md`）。  
页面格式：24 → **25**（D2 不再升格式）。  
备份、字段定义与回滚详见 [PAGE_FORMAT_25.md](PAGE_FORMAT_25.md)。

## 1. 完成内容

| 项 | 实现 |
|---|---|
| 数据模型 | `Shape` 新增 `fontFamily`、`fontStyle`、`fontWeight`、`textSpans`；默认 `sans` / `normal` / `400` / 空 |
| 页面格式 | 版本 25，在 v24 的 `shadowStyle` 后按 Shape 追加；解码接受 1–25 |
| Span 模型 | `TextSpan(start,end,flags)`，flags 为粗体 1、斜体 2、下划线 4；UTF-16 偏移 |
| Span 规则 | 规范化（裁剪/排序/合并相邻/合并重叠）、编辑后偏移调整、按行切分渲染 run |
| 字重 | 存储 100–900 步长 100；UI 首版 400/500/600/700 |
| 字体族 | Sans / Serif / Mono，Android Typeface 映射 |
| 整块格式 | 文本对象工具条提供字体族、斜体、字重、字号（16–64）、颜色、对齐 |
| 选区格式 | 内联编辑非折叠选区显示 B / I / U；切换后立即写入 span 并进入撤销历史 |
| 渲染 | 画布按 span 选择 Typeface、字重、斜体、下划线；粗体 span 在基础字重 <700 时提升到 700 |
| 换行 | 按文本对象宽度贪心换行，优先在空格断行，保留显式空行；画布最多绘制 8 行 |
| 编辑保真 | 内联编辑使用对象的字体/字号/字重/样式/对齐；文本编辑层取消 12dp 内边距，宽度与画布一致 |
| 迁移 | 启动加载前逐页检测版本；v1–v24 解码后以 v25 原子重写；失败文件保留并单独报告 |
| 历史兼容 | 加载历史时把旧版快照规范化为 v25，避免“历史与页面不一致”导致撤销栈被清空 |

## 2. 代码位置

- `app/src/main/java/com/example/tabletworkspace/Page.kt`
  - `pageFormatVersion()`、Shape 新字段、v25 编码/解码、非 Text 对象 span 拒绝。
- `app/src/main/java/com/example/tabletworkspace/TextFormatting.kt`
  - `TextSpan`、`TextRun`、`TextLine`；`normalize`、`isValid`、`rangeFlags`、`toggleRange`、`adjustForEdit`、`wrap`、`runs`。
- `app/src/main/java/com/example/tabletworkspace/PageStore.kt`
  - `migratePagesToFormat25()`；`loadAll()` 启动迁移；`PageFormatMigration` 统计。
- `app/src/main/java/com/example/tabletworkspace/PageHistory.kt`
  - `canonicalSnapshot()`；v24 快照在加载时升级，撤销/重做保持。
- `app/src/main/java/com/example/tabletworkspace/MainActivity.kt`
  - span 感知画布渲染、按宽度换行、文本工具条字体/字重/字号、`TextFieldValue` 内联编辑、选区 B/I/U、编辑层尺寸/对齐。
- `app/src/main/java/com/example/tabletworkspace/SettingsPage.kt`
  - 关于页页面格式改为 25。

## 3. 迁移结果（模拟器）

| 项 | 结果 |
|---|---|
| 迁移前页面 | 195 个 `.page`，均为 v24 |
| 迁移后页面 | 195 个原页面 + 1 个测试页，全部首 4 字节 `00 00 00 19`（25） |
| 迁移失败 | 0 |
| 历史文件 | 迁移前 13 个，全部保留；测试页操作后 14 个 |
| 活动文档 | 迁移后恢复为 191；测试白板已移入回收站 |
| 外观 | 旧页面默认 sans/normal/400、无 span，外观不变 |
| 持久化 | 测试文本 `Serif / 700 / 40px / span 0–6 flags=7` 强停重启后保持 |
| 撤销/重做 | 迁移后的历史可用；撤销移除下划线，重做恢复 |

迁移前备份：`evidence/pre-format25-20261101.tar`，487,424 字节，SHA-256 `F68C378E0BC9D9B24334EC864F8EA742E0698AF6D3E0BF57E6EAED17108161D1`。  
迁移后备份：`evidence/post-format25-20261101.tar`，515,584 字节，SHA-256 `4C838ADDB6FD3C3FB3887A42164124F4222A1B0712D8D70C21543F09487AC174`（196 页、14 历史）。  
原始格式 24 备份复核：`evidence/format24-backup.tar`，SHA-256 `9D02F2D3EC04BBFAE416E2CF56D6A6CF19CD35F885ED88F61D3A945258C70194`。

## 4. 测试

- JVM 单元测试：**274 项，0 失败，0 错误**。
- 新增/扩展：
  - `TextFormattingTest` 8 项：规范化、toggle、rangeFlags、编辑偏移、run 切分、字重校验、按空格/宽度换行、空行保留。
  - `PageTextStyleTest` 6 项：v25 往返、默认值、非法字体/字重/span、非 Text 携带 span 拒绝、v24 fixture 升级、真实 v25 持久化夹具、v24 历史可用。
  - `PageHistoryPersistenceTest`：历史持久化。
- 构建：`testDebugUnitTest assembleDebug assembleRelease` 通过；Release 启用 R8 与 `proguard-rules.pro`，启动与文本渲染正常。

## 5. 模拟器证据

| 文件 | 内容 |
|---|---|
| `evidence/f25-launch.png` | 安装 v25 debug 后数据完整（191 文档） |
| `evidence/f25-persist2.png` | 强停重启后 Serif/700/40、粗斜体下划线保持 |
| `evidence/f25-persist-toolbar.png` | 工具条显示 Serif / 700 / 40 |
| `evidence/f25-selchips.png` | 编辑态出现 B / I / U 选区格式按钮 |
| `evidence/f25-styled.png` | 全选后 B/I/U 同时激活 |
| `evidence/f25-styledcanvas.png` | 画布渲染粗体、斜体、下划线、衬线 |
| `evidence/f25-undo.png` / `f25-redo.png` | 撤销移除下划线，重做恢复 |
| `evidence/f25-editfidelity3.png` | 内联编辑字号、宽度、换行与画布一致 |
| `evidence/f25-release-text.png` | R8 Release 渲染保持 |
| `evidence/f25-about.png` | 设置关于页显示页面格式 25 |
| `evidence/f25-text-test.page` / `format25-styled.page` | v25 测试页面与回归夹具 |

## 6. 未完成 / 限制

- D2 Note 首版已完成：完整背景色板、行级待办复选框、插入文档保留 block 类型/待办状态/代码围栏；Note 块容器仍延期。详见 `STAGE_D2_IMPLEMENTATION.md`。
- 画布文本最多绘制 8 行；超出部分不显示，编辑层可继续滚动。后续可改为高度自适应或裁切提示。
- 换行按 code point 宽度贪心计算，未做复杂断词/连字符/双向文本排版。
- 图片仍缺失：`files/images` 为空，62 个页面含 `.img` 引用；与本轮无关且无法从现有备份恢复。
- 整块字体/字重/对齐与选区格式均只对 `type="text"` 生效；Note 卡片正文不写 span。
- API 26–27 没有 `Typeface.create(family, weight, italic)`，使用普通/粗体回退，无法精确呈现 500/600/900；API 28+ 使用真实字重。本次验证设备为 API 36。
- 文本在编辑层的垂直基线由 Compose 文本排版决定，与原生 Canvas 基线可能存在几像素差异，已通过相同字号与零内边距尽量对齐。

## 7. 发布

- Release：`releases/lingye-v0.15-format25-release-20261101.apk`，2,512,577 字节，SHA-256 `0E31B19C14B3D535642479EA832ECCF90F925747CA1C5E3D87812DF1DF4B9F87`。
- Debug：`releases/lingye-v0.15-format25-debug-20261101.apk`，14,872,088 字节，SHA-256 `3821B87DE4963F58092DCA27D1B2EDB7871A1986C5455CD12924B7F8511AF7BA`。
