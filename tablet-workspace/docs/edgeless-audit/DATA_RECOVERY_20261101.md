# 2026-11-01 模拟器数据恢复记录（测试事故）

## 1. 事故

- 为阶段 C 新增 Compose UI 测试后执行 `connectedDebugAndroidTest`。该任务在测试结束时会卸载应用，导致模拟器内应用私有数据被清空。
- 恢复前启动应用显示“全部文档 0 个文档”，`files/pages`、`files/history`、`files/images` 均为空。
- 没有执行任何用户主动删除操作；是测试工具链的卸载行为。

## 2. 恢复来源

| 来源 | 内容 | 位置 |
|---|---|---|
| `format23-backup.tar` | 195 个 `.page`、13 个 `.history`（2026-10-31 20:58 备份） | `docs/affine-audit/evidence/format23-backup.tar` |
| `current.page` | AlphaRenamed 最新格式 24 快照（2026-10-31 23:03，10 Shape/16 文字块） | `D:\download\cladue-work\current.page` |
| `prefs-restore.xml` | 工作区设置（语言、主题、会话页、形状/连线/笔记默认值） | `D:\download\cladue-work\prefs-restore.xml` |

## 3. 恢复步骤

1. 在 PC 解压 `format23-backup.tar` 到 `D:\download\cladue-work\recovery-20261101`（不直接对应用目录做 tar 覆盖）。
2. 安装阶段 C debug APK，启动后停止应用，创建 `files/pages`、`files/history`、`files/images`。
3. `adb push` 195 个页面和 13 个历史文件到 `/data/local/tmp`，再用 `run-as cp -r .../. files/pages/` 写入空目录。
4. `run-as cp` 恢复 `shared_prefs/ink_preferences.xml`。
5. 用 `current.page` 覆盖 `05bbd691-9b72-4880-868e-2225e559f945.page`，确认首 4 字节为 `00 00 00 18`（格式 24）。
6. 恢复后目录权限曾因子命令 `chmod -R 600` 误去掉目录执行位，随后执行 `chmod 700 files/pages`、`chmod 700 files/history` 修复；文件仍可读。
7. 启动应用验证：中文界面、191 个活动文档、AlphaRenamed 最新内容可见。

## 4. 当前状态与未恢复内容

- 活动文档：191；页面文件：195（含回收站/额外页面）；历史文件：13。
- 预置偏好已恢复；AlphaRenamed 为格式 24 最新快照。
- **图片目录为空**：`format23-backup.tar` 只包含 pages/history，不包含 `files/images`。扫描恢复后的页面发现 **62 个页面包含 `.img` 图片引用**，因此这些页面的图片很可能无法显示（应用会提示“图片不可用”）。本次没有原始图片文件可恢复。
- 备份时间（2026-10-31 20:58）之后、`current.page` 快照之外的页面改动无法恢复；本会话的改动集中在 AlphaRenamed 与工具栏测试对象，未发现其他页面被修改的证据，但不能完全排除。
- 应用内“备份与恢复”ZIP 未参与本次恢复；没有找到更晚的完整备份。
- 恢复后的数据已另存为 `docs/affine-audit/evidence/restored-20261101-backup.tar`（486,912 字节，SHA-256 `19D21ED6F947D47A9397EF454A7DB83E5F2B7881AA7822081A61E5780F59D700`），包含恢复后的 files（pages/history/images 空目录）与设置偏好；后续测试前以此为基础。

## 5. 防止再次发生

- 执行 `connectedDebugAndroidTest` 前必须导出 **pages + history + images + shared.dat + templates.dat + prefs** 的完整数据 tar/ZIP，并记录 SHA-256。
- 优先使用不会卸载应用的测试运行方式（例如手动安装 app/test APK 后 `am instrument`，或确认 Gradle 测试选项保留数据）；若无法确认，默认视为会清空数据。
- UI 测试使用独立测试包/独立工作区，不依赖用户工作区数据。
- 恢复操作必须先在 PC 解压到临时目录，再写入空的应用目录；不要对应用目录直接做覆盖式 tar 解包。

## 6. 需要用户确认

1. 打开常用页面，检查图片是否缺失（`files/images` 为空）。
2. 确认 191 个活动文档和回收站内容是否符合预期。
3. 若需要找回更晚数据，只能从其他设备/外部备份提供。
