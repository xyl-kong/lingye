# 文档信息面板与双向链接（2026-11-01）

范围：文档模式的页头、信息面板、正文、块列表和双向链接区的布局与样式。页面格式保持 **25**，未改动 `Page` 字段或编码。

证据等级：**SRC** 为本应用源码；**PRIOR** 为用户提供的 AFFiNE 官方截图；**UNVERIFIED** 为尚缺官方 Android 证据或功能未实现。

## 1. 目标

1. 文档模式显示该文档的基本信息，而不是把信息藏进独立弹窗。
2. 视觉顺序与官方截图一致：图标、标题、信息、正文、块、双向链接。
3. 文档内所有分段控件使用同一种样式：白色胶囊轨道、黑色选中胶囊、白色选中文字、浅色描边与阴影。
4. 信息区、正文、块文字和链接区共享同一个内容左边界；块手柄和 `+` 占用左侧沟槽，不把正文推离内容列。

## 2. 页面结构

**SRC**：`MainActivity.kt` 文档分支（约 1555–1612 行）和 `BlocksEditor`（约 1955–2258 行）。

```text
LazyColumn（文档容器，左右 32dp、上下 24dp）
├─ header
│  └─ Column（start = 72dp）
│     ├─ 添加/更改图标
│     ├─ 页面标题
│     ├─ DocumentInfoCard
│     └─ 正文 BasicTextField
├─ 空文档入口（+ 输入文字或 /）
├─ itemsIndexed(blocks)
│  ├─ + 插入菜单
│  ├─ 拖拽手柄
│  ├─ 块正文
│  └─ 块更多菜单
└─ footer
   └─ DocumentLinksPanel（start = 72dp）
```

内容列左边界为 `32dp + 72dp = 104dp`（相对文档容器）；块手柄和加号位于 72dp 沟槽内。`DocumentLinksPanel` 也使用 72dp 起始内边距，因此其分隔线、“Bi-Directional Links”、反向链接和外链计数与标题/正文/块文字左对齐。这个修正针对用户指出的“左边距没对齐”。

## 3. 信息面板字段

**SRC**：`DocumentInfoCard`（约 2344–2443 行）。

| 字段 | 行为 | 语义/限制 |
|---|---|---|
| 添加/更改图标 | 点击打开现有图标选择器；标题上方显示当前 emoji | 已接入页面图标 |
| 标题 | 原位编辑，写入页面标题 | 已有能力 |
| 信息 | 可折叠；右侧 `▾/▸` | 默认展开 |
| 工作区属性 | 可折叠子分组 | 默认展开 |
| 标签 | 显示 `#tag`，点击打开现有标签编辑器 | 空时显示“属性为空” |
| 文档模式 | 页面 / 无界 分段控件；切换 `canvas` 状态 | 已验证可切到无界再返回 |
| Journal | 空复选框 | **占位**，未实现自动日记 |
| 模板 | 空复选框 | **占位**，未实现模板标记 |
| 创建时间 | 今天 / `yyyy年M月d日` / `MMM d, yyyy` | 现有页面从文件 mtime 播种，可能近似；事故修复页可能显示“今天” |
| 更新时间 | 同上 | 来自页面文件修改时间 |
| 创建者 | 圆形“本”头像 + “本机” | 本地单用户固定值，不是 AFFiNE 账户 |
| 无界配色方案 | 自动 / 浅色 / 深色 | 写入全局 `WorkspaceSettings.canvasTheme`；`auto` 在画布中按应用明暗解析 |
| 页面宽度 | 标准 / 全宽 | 写入全局 `documentFullWidth`；当前不是每页独立属性 |
| 添加属性 | 静态行 | **占位**，未实现自定义属性 |
| 还有 6 个属性 | 展开文件夹、收藏、页面图标、字符数、块数、画布对象 | 只读；展开后显示“隐藏了 6 个属性” |

信息区底部有分隔线；正文为空时显示“写下你的想法…”。

## 4. 分段控件统一样式

**SRC**：`InfoSegmented`（约 2470–2524 行）；颜色取自 `LingyeColors.SegmentSelected` / `OnSegmentSelected` / `SegmentInk` / `SegmentBorder` / `SurfaceHigh`。

- 轨道：`SurfaceHigh` 背景，`RoundedCornerShape(18.dp)`，1dp `SegmentBorder` 描边，5dp 阴影，高 34dp。
- 选中项：`SegmentSelected`（浅色模式近黑，深色模式近白）胶囊，`RoundedCornerShape(15.dp)`，高 32dp。
- 选中文字：`OnSegmentSelected`（黑底白字/白底黑字），12sp，SemiBold。
- 未选中文字：`SegmentInk`，12sp，Medium。
- 横向内边距 12dp，轨道内边距 3dp/2dp。
- 文档模式、无界配色方案、页面宽度三个控件全部复用该组件，替换了此前的“浅色轨道 + 白色选中卡片”。

该样式与设置页的 `SegmentedChoice` 视觉一致；设置页还带滑动指示器动画，文档信息面板为简化静态选中，不引入额外动画状态。

## 5. 双向链接区

**SRC**：`DocumentLinksPanel`（约 2526–2548 行）。

- 顶部与内容列对齐的分隔线。
- 标题 `Bi-Directional Links`，右侧“隐藏/显示”。
- 反向链接数：`PageLinks.backlinks(page, pages).size`。
- 外链数：`page.shapes.count { PageLinks.targetId(it) != null }`。
- 底部保留 48dp 空间，避免最后一行贴住屏幕边缘。
- 左内边距 72dp，修复与正文/信息区不一致的左边距。

## 6. 验证

- 构建：`testDebugUnitTest assembleRelease` 成功；289 项 JVM 测试，0 失败。
- 安装：R8 Release 覆盖安装到模拟器，数据保留，活动文档仍为 191。
- 视觉：`evidence/rel2.png`（Release 文档信息面板 + 黑色胶囊分段 + 链接区左对齐）；`evidence/rel-full.png`（全宽模式，链接区仍与内容列对齐）；`evidence/rel-standard2.png`（恢复标准宽度）；`evidence/seg6.png`、`evidence/seg7.png`（调试构建同样布局）。
- 交互：文档模式“页面/无界”切换已验证；全宽/标准切换已验证并恢复；信息区折叠状态按页面 ID 记忆，重启后回到默认展开。

## 7. 限制

1. Journal、模板、添加属性是静态占位，不能创建属性或自动日记。
2. 创建者固定为“本机”；没有账户、成员或头像同步。
3. 创建时间对旧页面是文件 mtime 近似值；事故修复页因重新编码，创建/更新时间可能都显示“今天”。
4. 页面宽度和无界配色方案是全局设置，不是每页独立属性；官方截图的每页语义尚未实现。
5. 信息面板目前只在文档模式出现；无界画布不显示该面板。
6. 双向链接只统计本机页面，不涉及云端反向链接或跨工作区链接。
