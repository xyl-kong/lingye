# Android 15 模拟器排障与 0.15.1 设备补验（2026-11-22）

## 结论与复现环境

- 设备：`Lingye_Validation`，Android 15 / API 35，x86_64，2560×1600、320 dpi；应用 `com.example.tabletworkspace` 0.15.1（versionCode 16）。测试使用隔离 AVD，未清除其中原有页面。
- 受限沙箱内执行 `emulator-check.exe accel` 返回 6，并提示确认 WHPX；`-accel off` 软件启动以 Windows 访问异常 `-1073741819`（`0xC0000005`）退出。
- **同一主机环境**运行加速检查返回 0：`WHPX(10.0.26200) is installed and usable.` 因此不能把沙箱内失败解释为主机缺少 WHPX。用 `-accel on -gpu swiftshader_indirect -no-snapshot` 启动同一 AVD，约 24 秒完成启动，`adb devices` 为 `emulator-5554 device`，`sys.boot_completed=1`。
- `start-tablet-emulator.ps1` 已改为上述验证过的硬件加速和图形参数，并在加速检查失败时提示从主机普通 PowerShell 启动。脚本自身的有窗口交互启动尚未单独验收；本次验证的是同参数的无窗口启动。软件模式在此主机上仍不可用。

## 当前版本的实际操作

1. 保留 AVD 应用数据执行 `adb install -r` 升级至发行 APK，`am start` 成功；全部文档、已有文档、白板和设置页均能打开。界面截图：[全部文档](evidence/20261122-emulator/release-home.png)、[白板](evidence/20261122-emulator/release-board.png)、[外观设置](evidence/20261122-emulator/settings-check.png)、[储存/备份入口](evidence/20261122-emulator/storage-check.png)。设置页显示 209 个本地页面（含 11 个回收站页面），与全部文档页 198 个有效页面一致。
2. 新建隔离测试白板。“仅手写笔”开启时模拟手指滑动未生成笔迹；关闭后模拟手指可画线，[开启前](evidence/20261122-emulator/pen-stroke.png)与[关闭后](evidence/20261122-emulator/pen-stroke2.png)。撤销删除笔迹、重做恢复，[撤销状态](evidence/20261122-emulator/pen-undo.png)。这不能替代真实手写笔、压感和手掌误触测试。
3. 在白板拖拽创建文字框，长按选择 `ABCDEF` 后设置粗体并点击选区格式刷，按钮显示待应用状态，[选区工具栏](evidence/20261122-emulator/text-selected.png)、[格式刷已取样](evidence/20261122-emulator/format-painter-armed.png)。选择同一文本框另一段文字后出现“已应用格式”，目标变粗，[应用后](evidence/20261122-emulator/format-painter-applied.png)。撤销使目标恢复原样、重做再次加粗，[撤销](evidence/20261122-emulator/format-undo.png)、[重做](evidence/20261122-emulator/format-redo.png)。强停、保留数据重装发行包后测试页仍在；调试包的 `run-as` 检查到页面文件包含输入文字。跨文本框和复杂样式尚未操作验证。
4. 匹配的 Debug APK 与 AndroidTest APK 在同一 AVD 上运行 `adb shell am instrument -w -r com.example.tabletworkspace.test/androidx.test.runner.AndroidJUnitRunner`，结果 `OK (4 tests)`，包括一项画笔模式下双指捏合与手势锁释放测试、三项选区浮栏定位测试。直接对 R8 Release 运行该 Debug AndroidTest 曾报 `Process crashed`；换回匹配的 Debug 目标后四项通过，随后重新安装并启动 Release。AndroidTest 的通过结论只对应 Debug APK，发行包操作以上述手动验证为准。

## 仍未覆盖与发现的问题

- **PARTIAL**：四项 AndroidTest 和手动单指输入不能证明所有工具的双指竞争、手势取消、真机笔掌并用、延迟或大型页面性能。格式刷跨文本框、混合样式、取消及分屏/竖屏也未验证。
- **待复核，非已确认缺陷**：测试白板的文件修改时间为 11 月 22 日，但关闭设置回到“全部文档”，默认“最近修改”排序的列表顶部仍显示 11 月 7 日页面。复现条件：Release 0.15.1、上述 AVD、保留数据新建白板并编辑后回到全部文档。截图为[列表顶部](evidence/20261122-emulator/release-home.png)，需进一步核对排序缓存和页面保存完成时序；目前不能据此断言页面丢失，储存页和页面文件均显示测试页存在。
- 应用内完整备份导出、清空及恢复未在本轮运行；过去的隔离 AVD 备份记录不能代替本版全量数据验证。
- `FEATURE_MATRIX.csv` 可解析为 173 行，但原表仍有 3 个重复 Feature ID：`CV-043`、`UI-014`、`UI-015`。本轮没有合并或重编号，以免改变既有审计引用；使用矩阵按 ID 汇总完成度前需先澄清这些重复项。

## 启动方法

在**主机普通 PowerShell**中运行 `tablet-workspace/start-tablet-emulator.ps1`。受限工具沙箱内的 WHPX 检查不能代表主机可用性。若仅需要自动化验收，可用同一 SDK、AVD 环境变量和上述参数启动无窗口模拟器。设备数据位于隔离 AVD，不应作为唯一备份。
