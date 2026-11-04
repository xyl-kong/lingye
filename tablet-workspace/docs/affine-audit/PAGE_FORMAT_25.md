# 页面格式 25：Text 字体/选区格式与迁移记录

日期：2026-11-01  
状态：**已实施；模拟器上的 195 个原有页面已迁移为格式 25。**  
范围：阶段 D 的 Text 字体与选区格式；Note 本轮不新增页面字段。

## 0. 完整备份（已完成）

实施前两次核对格式 24 备份：

| 项 | 值 |
|---|---|
| 实施前备份 | `docs/affine-audit/evidence/pre-format25-20261101.tar` |
| 大小 | 487,424 字节 |
| SHA-256 | `F68C378E0BC9D9B24334EC864F8EA742E0698AF6D3E0BF57E6EAED17108161D1` |
| 原始格式 24 备份 | `docs/affine-audit/evidence/format24-backup.tar`（PC 副本 `D:\download\cladue-work\format24-backup-20261101.tar`） |
| 原始备份 SHA-256（实施前复核未变） | `9D02F2D3EC04BBFAE416E2CF56D6A6CF19CD35F885ED88F61D3A945258C70194` |
| 迁移后备份 | `docs/affine-audit/evidence/post-format25-20261101.tar`，515,584 字节，SHA-256 `4C838ADDB6FD3C3FB3887A42164124F4222A1B0712D8D70C21543F09487AC174`（196 页、14 历史） |
| 内容 | 195 个 `.page`、13 个 `.history`、`shared_prefs/ink_preferences.xml`、目录 |
| 图片 | `files/images` 为空（0 个文件）；62 个页面含 `.img` 引用，图片在 2026-11-01 测试事故中丢失，备份无法恢复图片 |
| 页面格式 | 24（AlphaRenamed 摘要文件首 4 字节 `00 00 00 18`） |
| 备份方式 | 停止应用后以 root 在 `/data/data/com.example.tabletworkspace` 打包 `files shared_prefs`；未覆盖任何现有数据 |

回滚时使用该 tar 恢复 pages/history/偏好；不要直接对非空目录做覆盖式解包。

## 1. 格式 24 的相关字段

`Page.kt:136-176` 的编码顺序末尾为：

```text
... 页面元数据、块、形状、连线、图片、笔迹、模板、图层、图标 ...
shapes.forEach { string shadowStyle }        // v24 新增
```

每个 `Shape` 当前已有：`title/body/textColor/fontSize/textAlign/cardKind/shadowStyle`；没有字体族、字体样式、字重、选区 span（`Page.kt:20-41`）。

Text 目前是 `Shape(type="text")`，画布渲染使用 `fontSize/textColor/textAlign`（`MainActivity.kt:2885-2904`）。公式上无法表达官方 Text 的 fontFamily/fontStyle/fontWeight 或选区级格式。

官方证据（本地 AFFiNE 0.27.5 源码）：

- `affine-src/.../edgeless/text.tsx:76-139`：颜色、字体族、字体样式、字重、对齐。
- `affine-src/.../edgeless/shape.tsx:269-347`：形状内文字的字体族/样式/字重/字号/对齐。
- 官方枚举成员与默认值在 BlockSuite `@blocksuite/affine/model`，本机不可读，具体默认值仍为 UNVERIFIED。

## 2. 提议的格式 25 字段

在格式 24 的 `shadowStyle` 之后**按 Shape 追加**，保持 append-only、旧字段位置不变：

```text
for each shape:
    string fontFamily        // "sans" | "serif" | "mono"
    string fontStyle         // "normal" | "italic"
    int    fontWeight        // 100..900，步长 100；默认 400；首版 UI 仅 400/500/600/700
    int    spanCount         // 0..N
    for each span:
        int start            // UTF-16 索引，0 <= start < end <= body.length
        int end
        int flags            // bit0 bold, bit1 italic, bit2 underline
```

约定：

- 新字段默认值：`sans` / `normal` / `400` / 0 spans。与当前渲染完全一致，v24 页面升级后外观不变。
- Span 字段按 Shape 存储（保留通用性），但 v25 只有 `type="text"` 写入/渲染/编辑；Note 卡片正文首版不写 span，避免与官方 Note 的块级富文本语义混淆。
- Span 必须规范化为：`start < end`、按 start 排序、非重叠；相邻且 flags 相同的 span 合并。越界/重叠在编码时拒绝。
- 字符索引采用 Kotlin `String` 的 UTF-16 code unit 索引，与 `TextFieldValue.selection` 一致；组合字符/emoji 的 grapheme 边界需在 UI 层按 code point 对齐，避免在代理对中间切分。
- 字体族/样式/字重对 Text 生效；Shape 内文字编辑在后续工作包实现。字重存储范围放宽到 100–900，UI 首版仅显示 400/500/600/700，后续扩展 UI 不需要再升格式。
- 本轮**不加入** Note 子块/块容器/锁定扩展；这些若需要另立格式 26，避免把未实现能力写入数据。

### 2.1 调整后的三项决策

| 决策 | 采用方案 | 备选/取舍 |
|---|---|---|
| 字重范围 | 存储 100–900（步长 100）；默认 400；首版 UI 400/500/600/700 | 若只存 400–700，后续增加 100–300/800–900 需要再升格式；采用宽存储、窄 UI |
| Span 覆盖 | 字段通用（按 Shape），v25 只对 Text 写入/使用；Note 卡片正文暂不写 | 若同时覆盖 Note 卡片，需为卡片正文做 span 渲染，且与未来 Note 块容器迁移冲突；暂不做 |
| Note 块容器 | 延期到独立阶段；D2 只做背景色板、插入文档保真、行级待办等非格式增强 | 若在 v25 加入子块列表，会把 Text 字体/选区与 Note 容器两个高风险变更绑在一起；不采用 |

**理由**：宽字重存储降低二次升格式风险；Span 采用“通用字段、Text 先行”避免把卡片字符串固化为官方 Note 的临时替代；Note 容器是对象模型级改造，必须独立设计、独立备份/迁移。

## 3. 编码/解码

- 编码版本号 25；解码接受 1..25。
- v25 读取顺序严格接在 v24 的 `shadowStyle` 之后；v24 及更早页面使用默认值。
- 校验：
  - fontFamily ∈ {sans, serif, mono}
  - fontStyle ∈ {normal, italic}
  - fontWeight ∈ 100..900（UI 暂只提供 400/500/600/700）
  - spanCount ≥ 0 且不超过 10,000；每个 span 合法、body 长度匹配、非重叠
- 所有旧测试应为 v25 更新；v24 fixture 必须仍能打开。

## 4. 迁移记录（已执行）

1. **备份**：`pre-format25-20261101.tar`（SHA-256 `F68C378E...61D1`）；实施前再次核对 `format24-backup.tar` 的 SHA-256 仍为 `9D02F2D3...0194`。
2. **安装**：先安装 v25 debug（`adb install -r`，同签名，保留数据），强停后启动；页面数仍为 195、历史 13，未清空。
3. **自动迁移**：`PageStore.loadAll()` 在读取前调用 `migratePagesToFormat25()`；逐页读取首 4 字节，v1–v24 解码后以 v25 重写（`AtomicFile` 原子替换），无法解码的文件保持原样并计入失败列表。
4. **结果**：195 个原有页面全部为 `00 00 00 19`（25），失败 0；创建测试白板后共 196 个页面，新页也为 25。
5. **历史**：13 个 `.history` 文件未丢失；`PageHistory.fromEncoded` 会把 v24 快照规范化为 v25，避免因版本差异误判“历史与页面不一致”而清空撤销栈。模拟器实测撤销/重做可用。
6. **外观**：旧页面读取后字体默认 sans/normal/400、无 span，画布外观不变。
7. **测试页**：`evidence/f25-text-test.page` 为验证用白板页面（正文“双击编辑文本”，Serif、700、40px，span 0–6 flags=7），已加入 `app/src/test/resources/format25-styled.page` 作为持久化回归夹具。

## 5. 回滚步骤

1. 备份当前 v25 数据（即使不可读也保留）。
2. 安装格式 24 的构建（`releases/lingye-v0.15-stage-c2-release-20261101.apk` 或更早的 v24 包）。
3. 清空/移走 `files/pages`、`files/history`（不要删除用户备份）；从 `format24-backup.tar` 恢复 pages/history/偏好。
4. 启动确认 191 个活动文档和 AlphaRenamed 内容；旧 v25 页面文件不回到应用目录。
5. 记录回滚原因和备份路径。

## 6. 测试结果

| 测试 | 内容 | 结果 |
|---|---|---|
| `PageTextStyleTest` 6 项 | v25 字体/字重/span 往返；默认值；非法 family/weight/span 拒绝；非 Text 携带 span 拒绝；v24 fixture 升级；真实 v25 页面夹具 | 通过 |
| `TextFormattingTest` 8 项 | normalize/toggle/rangeFlags/adjustForEdit/runs/字重校验/换行 | 通过 |
| `PageHistoryPersistenceTest` | 历史持久化；v24 历史快照升级后仍可撤销 | 通过 |
| 全部 JVM 单元测试 | 274 项 | 0 失败 |
| UI | 字体族/样式/字重/字号菜单、选区 B/I/U、IME、撤销/重做、强停重启 | 模拟器通过 |

## 7. 阶段 D 工作包

### D1 Text（已完成）

1. **D1.1 数据模型/格式 25**：Shape 字体字段 + span 编码；自动迁移/回滚；单元测试。
2. **D1.2 渲染**：sans/serif/mono Typeface、字重、斜体、下划线；字号 16–64；按对象宽度换行，最多绘制 8 行；选区/整块对齐。
3. **D1.3 选区**：内联编辑 `TextFieldValue`；选区 B/I/U；编辑时调整 span；撤销/重做。
4. **D1.4 编辑保真**：内联编辑使用与画布相同的字体、字号、字重、样式和对齐；文本对象编辑层取消 12dp 内边距，避免换行位置与画布不一致。
5. **D1.5 复制/导出**：Copy as Image/Markdown 仍列后续，未在本轮实现。

### D2 Note（首版已完成，不升格式）

1. 完整背景色板：工具条当前色点打开 `ColorPaletteMenu`，含经典十色、自定义色卡与取色器。已完成。
2. “插入到文档”：保留标题、段落、待办及勾选状态、列表、引用、标题层级、分隔线和代码围栏；图片/行内格式受扁平模型限制无法保真。已完成并记录限制。
3. 行级待办勾选接入画布：按行绘制复选框并点击切换，支持 `☐/☑` 与 Markdown 标记。已完成。
4. Note 块容器/子块仍为大型模型改造，未在 D2 首版实现；详见 `STAGE_D2_IMPLEMENTATION.md`。

## 8. 决策与实施状态

1. 格式 25 采用 append-only：字体字段 + span；**已实施并完成迁移**。
2. 字重：存储 100–900（步长 100），默认 400；首版 UI 提供 400/500/600/700。
3. Span：字段按 Shape 存储，但编码/解码都拒绝非 Text 对象的 span；v25 只覆盖 Text；Note 卡片正文不写 span。
4. Note 块容器/子块：延期到独立阶段，D2 只做非格式增强。
5. 选区格式工具栏：仅在文本编辑态且存在非折叠选区时显示；未编辑时的对象工具条提供整块字体/对齐设置。
6. 迁移是打开时的惰性升级（195 页一次性完成），不是独立迁移对话框；失败文件保留原样，由现有损坏页列表报告。

## 9. 发布与证据

- Release：`releases/lingye-v0.15-format25-release-20261101.apk`，2,512,577 字节，SHA-256 `0E31B19C14B3D535642479EA832ECCF90F925747CA1C5E3D87812DF1DF4B9F87`。
- Debug：`releases/lingye-v0.15-format25-debug-20261101.apk`，14,872,088 字节，SHA-256 `3821B87DE4963F58092DCA27D1B2EDB7871A1986C5455CD12924B7F8511AF7BA`。
- 实施细节、代码位置与完整证据清单见 `docs/affine-audit/STAGE_D_IMPLEMENTATION.md`。
- 模拟器复核：R8 Release 启动、页面读取、字体/字重/字号菜单、粗斜体下划线渲染、编辑保真、撤销/重做、设置页“页面格式 25”。


