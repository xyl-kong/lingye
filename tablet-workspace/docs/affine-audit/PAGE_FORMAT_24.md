# 页面格式 24：单笔记阴影/圆角与迁移记录

日期：2026-10-31

## 1. 变更内容

- `Shape` 新增 `shadowStyle: String`，取值 `none` / `box` / `sticker`，默认 `box`。
- 笔记圆角此前已随 `Shape.cornerRadius` 保存；本轮为选中笔记增加圆角编辑入口，并确保新建笔记写入当前全局圆角。
- 页面编码版本由 23 升至 24；格式 24 在 v23 图标字段之后追加每个 Shape 的阴影样式：

```
... v23 数据（含 string icon）...
for each shape:
    string shadowStyle
```

- 旧格式 1–23 仍可读取：缺少阴影字段的页面默认 `box`，与默认全局设置一致；当前工作区全局阴影也是 `box`，因此升级不改变已有笔记外观。
- 页面在下次保存时升级为 v24；无需一次性迁移脚本。
- 选中笔记工具条的“图层与更多”菜单内新增“笔记样式”分组，分为阴影与圆角两段（当前值带 ✓）；修改写入该 Shape；全局设置只影响新笔记。

## 2. 代码位置

- `Page.kt`：`Shape.shadowStyle`、格式 24 编解码、保存校验。
- `Cards.kt`：`Page.addCard` 接收并写入 `shadowStyle`。
- `MainActivity.kt`：卡片绘制使用 `s.shadowStyle`；选中卡片工具条“图层与更多”内的阴影/圆角分组；新建卡片传入全局默认。
- `SettingsPage.kt`：圆角/阴影说明改为“只影响新笔记，选中已有笔记后可在其工具条的‘图层与更多’中单独调整”；关于页格式 24。
- `PageNoteStyleTest.kt`：格式 24 阴影/圆角往返、默认值与非法值校验。

## 3. 迁移与回滚

### 3.1 迁移

- 从 v23 升级：直接安装 v24，旧页面读取时阴影默认 `box`、圆角沿用已保存值；当前全局阴影为 `box`，外观不变。
- 新建笔记写入当前全局 `noteShadow`、`noteCornerRadius`、边框样式/厚度和背景色。
- 修改某个笔记的阴影/圆角后，该值随页面保存，重启保持；之后修改全局设置不会影响已有笔记。

### 3.2 备份证明

安装格式 24 前，从 v23 调试包导出：

- 文件：`docs/affine-audit/evidence/format23-backup.tar`
- SHA-256：`C40F362740A1D42263DEA2E45D9DE6D8076AFD7003C88A6D3AE5698DD261B7F4`
- 大小：480,768 字节
- 内容：210 个条目，195 个 `.page`、13 个 `.history`，包含 `files/pages/` 与 `files/history/`。

### 3.3 回滚步骤

1. 安装可调试的格式 24 构建：`releases/lingye-v0.15-note-styles-debug-20261031.apk`
2. `adb shell am force-stop com.example.tabletworkspace`
3. 把备份写回应用私有目录：
   ```
   cmd /c "type docs\affine-audit\evidence\format23-backup.tar | adb shell run-as com.example.tabletworkspace tar xf -"
   ```
4. 安装并启动 v23 发布包：`releases/lingye-v0.15-page-icons-backlinks-release-20261031.apk`
5. 核对活动文档 191、回收站 4 项。

## 4. 验证

- 239 项单元测试 0 失败（新增 `PageNoteStyleTest` 3 项）。
- 模拟器：新建笔记默认卡片阴影/中圆角；选中后改为贴纸阴影＋超大圆角，强制重启后保持；页面文件首 4 字节为 `00 00 00 18`（24）。
- 测试笔记已通过工具条删除，AlphaRenamed 画布恢复为测试前内容。
- R8 release 包安装后可从卡片工具条打开“图层与更多”并显示阴影/圆角分组，重启与页面切换正常。
- 证据：`note-style-card-toolbar.png`、`note-style-menu-merged.png`、`note-style-sticker-huge-final.png`、`note-style-persisted-final.png`、`note-style-menu-persisted.png`、`note-style-restored-final.png`、`note-style-settings-help-corner.png`、`note-style-settings-help-shadow.png`、`page-format24-about.png`。早期的 `note-style-toolbar.png`、`note-shadow-menu.png`、`note-corner-menu.png` 记录的是已被合并菜单取代的两个独立入口。

## 5. 约束说明

- 根目录 `AGENTS.md` 按“不得覆盖 AGENTS.md”的约束未改动；当前格式以本文件、README 和设置页“页面格式 24”为准。
