# 平板白板手势增量记录（2026-11-13）

## 本次行为

- 选择、画笔、橡皮、连线、形状、文字和框架工具下，双指触摸可平移及以双指中心为锚点缩放画布，缩放仍使用原有 `CanvasZoom` 范围。
- 第二根手指加入时，中止正在进行的单指编辑并通过现有 `PageGesture.cancel()` 回滚未提交的页面改动。双指操作结束前，剩余单指不会继续绘制、拖动或创建对象。
- 手写笔或笔尾橡皮正在按下时，不启动双指触摸导航。移动工具与演示模式沿用原来的变换手势。
- 轻点创建工具记录双指手势代数，避免捏合结束被当成轻点并切换工具。

## 架构范围

仅在现有 Compose 画布 Modifier 链增加独立指针输入处理器与手势状态；沿用原有页面数据、历史回滚、画布缩放约束和渲染。未增加依赖，未更换编辑器或画布模块。

## 验证

- 英文路径源码副本与原项目相关源码 SHA-256 一致；`testDebugUnitTest assembleDebug assembleDebugAndroidTest` 构建成功。
- JVM 测试 465 项，失败 0、错误 0、跳过 0。新增测试覆盖双指启动条件、平移、中心缩放与缩放上限。
- 2026-11-22 补验：在主机环境启动同一 AVD 后运行 `CanvasTwoFingerInputTest.pinchInPenModeNavigatesAndReleasesLock`，通过；同一 AndroidTest APK 的 4 项 Compose 测试全部通过。沙箱内的加速检查失败仅说明沙箱进程无法访问 WHPX；主机检查为 `WHPX(10.0.26200) is installed and usable`，冷启动约 24 秒，`sys.boot_completed=1`。详见 `EMULATOR_VALIDATION_20261122.md`。
- 模拟器手动验证画笔“仅手写笔”开启时模拟手指不会落笔，关闭后可以落笔，撤销/重做生效。真实手写笔、笔掌并用、各工具的完整双指手势矩阵和性能仍为 **UNVERIFIED**。
- 预览 APK：`releases/lingye-v0.15-gesture-preview-unverified-20261113.apk`，SHA-256 `51D701DC5FA8C15387589184D2A6D5DE10ABEE782602FF3105F26CB10AB4004E`。未向用户设备或模拟器安装。

## 后续验收

在平板上，分别用画笔、橡皮、选择、连线及形状工具测试单指编辑 → 第二指加入 → 双指平移缩放 → 逐指抬起；检查页面对象未意外提交、撤销栈与保存重开一致，再测试手写笔按下时手掌触摸不触发画布导航。
