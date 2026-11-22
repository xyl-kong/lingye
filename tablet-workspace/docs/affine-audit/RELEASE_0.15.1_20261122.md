# 灵页 0.15.1 安装包与更新记录

## 安装包

- 文件：`releases/lingye-v0.15.1-local-tools-release-20261122.apk`
- 包名：`com.example.tabletworkspace`；`versionCode=16`；`versionName=0.15.1`；最低 Android 8.0（API 26）。
- 大小：3,491,267 字节；SHA-256：`897184C2B47D23560C2FCFE47D86845A66B1D5A49AB0A6B188E1B045E1CBA1AC`。
- `apksigner verify` 通过 APK Signature Scheme v2。签名证书 SHA-256 为 `895af81bcaaece79672f7ccaa9235cb5ab3515d776bb1b6440b5813b5d684f4d`，与仓库上一版安装包相同，供开发测试升级安装；仍为 Android Debug 证书，不是正式发行签名。
- 安装前建议用应用内备份保存资料。页面格式 25、备份格式 `lingye-backup-2` 未变。

## 本次内容

1. 白板各编辑工具可用双指平移和缩放；第二根手指加入时取消未提交的单指编辑，避免捏合结束后继续落笔或创建对象。详情见 `TABLET_CANVAS_GESTURES_20261113.md`。
2. 白板文本选区悬浮栏新增一次性格式刷。复制源选区起始字符的粗体、斜体、下划线、删除线、行内代码、前景色和背景色，并应用于下一个目标选区；链接、评论、公式语义保持。详情见 `TEXT_FORMAT_PAINTER_20261122.md`。
3. 包含此前未推送的四个本地提交：编辑器设置与单页宽度备份、自定义字体、画布 PDF 导出、文档移动、侧栏和画布阴影对齐、卡片二级样式浮窗。相关审计见 `EDITOR_SETTINGS_REAUDIT_20261107.md`。

## 构建和验证

- 在英文路径的同源构建副本执行 `:app:testDebugUnitTest :app:assembleRelease :app:assembleDebugAndroidTest --offline --no-daemon`，**BUILD SUCCESSFUL**。R8 与 release lint 完成。
- 86 份 JVM 测试报告共 469 项，失败 0、错误 0、跳过 0。AndroidTest APK 已编译，未在设备运行。
- 构建时 Gradle 转换缓存两次出现 `metadata.bin` 读取错误；删除构建副本内损坏的转换缓存后重新构建成功。未改动项目源码或用户数据。
- 当前 AVD 缺少可用的 x86_64 硬件加速，软件模式启动异常退出。因此本版双指触控、格式刷选区悬浮定位、升级安装及实际视觉表现仍为 **UNVERIFIED**；构建与 JVM 测试通过不等于设备验收。

## 已知范围

- 格式刷只取源选区起始字符的视觉样式，作用一次后退出；不复制文本框级字号、字体和对齐，也不复制语义标记。复制中的临时样式不持久化，应用后的目标样式走页面保存与历史。
- 双指导航的手写笔与手掌并用行为需要真机检查；其他已知限制沿用 `README.md` 和功能矩阵。
