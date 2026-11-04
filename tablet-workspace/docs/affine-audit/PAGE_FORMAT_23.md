# 页面格式 23：页面图标与迁移记录

日期：2026-10-31

## 1. 变更内容

- 页面新增 `icon: String`，保存一个短 emoji 字符串（空字符串表示无图标，最长 16 个 UTF-16 单元，禁止控制字符）。
- 页面编码版本由 22 升至 23；格式 23 在原有 v22 数据末尾追加一个 `writeString(icon)`。
- 旧格式 1–22 仍可读取：v22 及更早页面解码后 `icon` 为空，界面使用文档/白板线型图标兜底。
- 页面不会在启动时被批量改写；某页在应用内被修改并保存时，才会以格式 23 写回。因此工作区可同时存在 v22 与 v23 页面。
- 备份 ZIP 使用同一 Page.encode，页面图标会自动包含在备份中。

## 2. 代码位置

- `app/src/main/java/com/example/tabletworkspace/Page.kt`
  - `Page.icon`
  - `encode()` 写入 `23` 与尾部图标
  - `decode()` 接受 1..23，`version >= 23` 读取图标
  - `snapshot()` / `duplicate()` 复制图标
- `PageIcons.kt`：图标候选集、合法性校验、`PageGlyph` 与选择器。
- `PageIconFormatTest.kt`：v23 往返、快照/复制、v22 兼容与校验测试。
- `PageLinks.kt`：`backlinks(target, pages)` 反向链接扫描。
- `MainActivity.kt`：页面菜单/顶栏图标入口、图标选择器、侧栏与页信息中的反向链接。

## 3. 格式 23 布局

格式 22 的全部字段保持不变，新增字段位于文件末尾：

```
... v22 数据 ...
string icon        // int 长度 + UTF-8 字节；空字符串为 0
```

读取时：

```kotlin
val version = input.readInt()
require(version in 1..23)
...
if (version >= 23) {
    val icon = input.readString()
    require(PageIcons.isValid(icon))
    page.icon = icon
}
```

## 4. 迁移与回滚

### 4.1 迁移

- 从 v22 升级：直接安装格式 23 版本，旧页面可正常打开；图标为空时显示线型兜底。
- 页面被编辑/改图标后保存为 v23。无需一次性迁移脚本，也无需清空数据。
- v22 应用无法读取 v23 页面（版本检查为 1..22），因此回滚前必须恢复备份。

### 4.2 备份证明

在安装格式 23 之前，从 v22 调试包导出：

- 文件：`docs/affine-audit/evidence/format22-backup.tar`
- SHA-256：`985D6F4AEA331D0E1BAEB50DD22BED94E30A49B8EA1DD04C6022975268BE0B0D`
- 大小：486,912 字节
- 内容：209 个条目，其中 195 个 `.page`、12 个 `.history`，包含 `files/pages/` 与 `files/history/` 目录。
- 导出方式：v22 调试包安装后，`adb exec-out run-as com.example.tabletworkspace tar cf - files/pages files/history`，在 PC 端保存为 tar。

### 4.3 回滚步骤

1. 安装可调试的格式 23 构建（用于获得 `run-as` 权限）：
   `releases/lingye-v0.15-page-icons-backlinks-debug-20261031.apk`
2. 停止应用：`adb shell am force-stop com.example.tabletworkspace`
3. 在 PC 端把备份写回应用私有目录（PowerShell/cmd 管道）：
   ```
   cmd /c "type docs\affine-audit\evidence\format22-backup.tar | adb shell run-as com.example.tabletworkspace tar xf -"
   ```
4. 安装并启动 v22 发布包：`releases/lingye-v0.15-shortcuts-release-20261031.apk`
5. 核对：活动文档 191、回收站 4 项、页面内容与 v22 测试时一致。

## 5. 验证

- `PageIconFormatTest`：v23 图标往返、snapshot/duplicate 复制、v22 手工字节流解码为空图标、非法图标拒绝。
- `PageLinksTest` 新增反向链接测试：仅返回未回收且包含指向目标页链接卡片的来源页。
- 236 项单元测试 0 失败。
- 模拟器：v22 数据启动后可读；设置 🚀 图标后侧栏/首页/顶栏显示，强停重启保持；移除图标后侧栏即时恢复；页面文件首 4 字节为 `00 00 00 17`（23）。
- 反向链接：AlphaRenamed → AlphaPage 的测试链接显示“链接的文档”；目标页显示“反向链接 / AlphaRenamed”，点击回到来源页；测试链接已通过页面撤销移除。

## 6. 约束说明

- 根目录 `AGENTS.md` 按“不得覆盖 AGENTS.md”的约束未改动，其中“页面格式 22”的描述已过时；当前格式以本文件、README 和设置页“页面格式 23”为准。
