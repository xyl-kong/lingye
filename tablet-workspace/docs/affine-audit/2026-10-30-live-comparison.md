# 2026-10-30 实际页面补充对照

## 环境与范围

- AFFiNE：Web Demo 的 All docs、How to use folder and Tags 文档，以及同一文档 `?mode=edgeless`，本轮通过浏览器无障碍状态读取。浏览器交互与截图连续超时，未能本轮实际打开设置弹窗；设置端参考用户本对话提供的截图，不能称为本轮设置操作验证。Web 与 Android 的控件也不能直接等同。
- 灵页：`releases/lingye-v0.15-debug.apk`，包 `com.example.tabletworkspace`，versionCode 15；API 35 / Android 15 x86_64，2560×1600、320 dpi。安装到本次空测试实例，创建独立测试页。没有本轮构建，因此只证明该发布 APK 的行为，不证明现行源码与 APK 相同。
- 模拟器排障：默认 SDK 缺少 AVD 所需镜像，启动报 `Cannot find AVD system path`；`ANDROID_AVD_HOME` 指向既有 `emulator-test/avd`，`ANDROID_HOME` 和 `ANDROID_SDK_ROOT` 指向既有 `emulator-test/sdk` 后启动成功。`adb devices -l` 为 `emulator-5554 device`，`sys.boot_completed=1`。原先沙箱内 adb 因无法创建 `\\.android` 失败；本次通过有权限的进程执行。AVD 配置 `disk.dataPartition.path=<temp>`，本轮仅验证同一运行实例中的应用强停重启，不把模拟器关闭重开后的持久性当作已验证。
- 灵页当前截图：[文档](evidence/live-0.15-document.png)、[白板](evidence/live-0.15-board.png)、[设置](evidence/live-0.15-settings.png)。与用户提供的官网截图及本轮 Web 状态不是同设备/同视口，不作像素级等价比较。

## 逐项对照

| 项目 | AFFiNE 证据 | 灵页 0.15 本轮证据与差异 | 状态 |
|---|---|---|---|
| 页面结构 | Web 文档状态显示标题、Add icon、Info、正文和 Bi-Directional Links | 文档有标题框、正文框及底部添加块栏；无本轮实测的 Info/反链面板 | PARTIAL |
| 文档视觉 | 用户参考和 Demo 页结构以内容为中心 | 截图中标题、正文均为大面积描边表单；整体圆角外框及紫色导航更醒目 | 定性差异，待同视口截图对照 |
| 文档与画布 | 本轮同一 AFFiNE 文档在 Page 和 Edgeless 状态均显示标题与示例正文 | 新建页输入 `audit_text_20261030`，切白板后 UI 树可见文字；强停并重启应用后白板仍可见该文字 | 基础链路通过；不证明复杂块一致 |
| 白板布局 | 用户截图为点阵、页面 Note、底部悬浮工具；本轮 Edgeless 页面状态可见 Note 和 65% 缩放 | 实际截图有点阵、页面卡与底栏；底栏占用主工作区几乎全宽，图标附文字；未本轮验证缩放及全部对象 | PARTIAL |
| 画布工具 | 本轮 Web 状态存在多个无标签按钮，无法仅据 AX 树确认具体动作；用户截图提供底栏参考 | 点击画笔显示 2/4/8/12 粗细和仅手写笔选项；更多工具菜单显示矩形、圆形、连线、文本框、图片、卡片及视图操作，见 `evidence/live-audit-pen.xml`、`evidence/live-audit-more.xml`；绘制与取消未实测 | PARTIAL |
| 设置结构 | 用户截图：设置弹窗含外观、编辑器、键盘快捷键及工作区设置；可见主题、语言、平滑图片等 | 设备截图是工作区内设置页，含 Appearance、Editor、Canvas、Data、About；点击中文后界面切换，进入编辑器设置可见字号、默认模式、日期命名；见 `evidence/live-audit-zh.xml`、`evidence/live-audit-editor.xml`；深色主题未支持 | PARTIAL；选项保存和其他分类未逐项验证 |
| 文件组织 | 本轮 All docs 可见 Docs、Collections、Tags、Display、New doc、分组和 Select all | 设备侧栏可见 Pages、Saved、Trash、文件夹与搜索；英文标签在截图呈 `P… / S… / T…` | PARTIAL；标签截断是本配置下可复现的排版问题 |
| 图片编辑 | 本轮没有完成导入测试；源码显示白板图片裁剪/旋转/翻转 | 尚无此 APK 的本轮图片编辑实操作证据；文档和共享图片缺同等编辑路径 | UNVERIFIED/PARTIAL |

## 排版发现及复现

1. **侧栏英文标签截断**：0.15，API 35、2560×1600、320 dpi，语言 English，新建页后观察左侧 Pages/Saved/Trash 分段，三个文字均显示省略号。见 `evidence/live-0.15-document.png`。UI 树仍含完整名称，因此视觉用户无法直接读全，但不意味着无障碍名称缺失。应调整分段宽度或采用清晰图标及可访问标签，并覆盖大字号/窄窗口。
2. 文档的描边输入框、外框圆角阴影和高饱和紫色主按钮与用户提供的 AFFiNE 截图风格不一致。属于设计差异，不单凭此定义功能错误。
3. 白板底部大工具条遮挡画布底部更多面积，需在相同窗口及选择状态下测量，并检查对象在工具条下方时是否能选中和编辑；当前仅为截图观察，遮挡影响未实测。

## 仍需补证

- 解决浏览器交互和截图超时后，重新实际操作 AFFiNE 文档、对象上下文工具与设置，并以相同窗口大小取得图像。
- 测试文档块选区格式、拖拽、斜杠菜单；白板创建、选中、移动、缩放、取消、图片编辑、撤销和重启；逐类设置的修改与重启。
- 竖屏、分屏、150%/200% 字号、中英文、IME、TalkBack、真机手写笔及复杂数据边界均不由本轮覆盖。
- 复建当前源码并关联 APK 哈希；运行历史格式、备份恢复和故障注入。协作暂不开发。

## 后续尝试：浏览器与图形（2026-10-30）

- 模拟器仍为 `emulator-5554 device`；测试页与前次示例文字仍可见，本次未清除数据。
- 浏览器可重新加载 AFFiNE All docs 并返回无障碍树；对“Open this doc in AFFiNE app”提示的 Dismiss 点击调用返回，但后续状态读取在 15 秒内超时，因此无法确认提示是否关闭。截图读取也在 30 秒内超时；Edge 已打开窗口被最小化，窗口状态读取无法完成。浏览器问题尚未解决，不能据此声称本轮操作或截图了官网设置、文档和白板。
- 随后启动 Edge 新窗口作为恢复尝试，电脑控制在选择该窗口时提示无法可靠确定当前浏览器 URL，因而停止本轮电脑控制。没有进一步操作该窗口；这不是 AFFiNE 页面故障的证据，也不是故障根因已确定。
- 灵页测试页打开“更多工具”，点击矩形后在画布空白处模拟拖动；截图 `evidence/audit-shape-test.png` 未显示矩形。此前选择工具和触控状态没有足够证据定位原因，现记为**待复现和诊断**，不以一次未成功的模拟触控断定功能损坏，也不将其标为通过。菜单、工具选择和结果截图分别为 `evidence/audit-more-now.png`、`evidence/audit-rect-tool.png`、`evidence/audit-shape-test.png`。
- 本轮未重新构建，没有完成图形移动、缩放、撤销或设置持久化回归；先前结果和功能矩阵状态不因此升级。
