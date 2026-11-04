# AFFiNE Android 平板功能对齐审计：参考基线

审计日期：2026-10-30（Asia/Shanghai）

项目基线：审计开始时为 0.9 / 页面格式 20；2026-10-30 增量修复后为 `com.example.tabletworkspace` 0.15、`versionCode=15`、页面格式 22。

## 1. 证据等级

本审计把证据分为以下级别，矩阵中的状态必须按 `ACCEPTANCE_CRITERIA.md` 解释：

- E1：实际产品操作观察，记录 URL、页面状态和可见控件。
- E2：锁定 commit 的官方源码，路径和符号可复核。
- E3：当前项目源码、自动测试或构建产物。
- E4：当前 APK 在 Pixel Tablet API 35 模拟器中的实际操作。
- U：访问受阻、行为不稳定或证据只覆盖一部分，不能用于确认完整功能。

官网宣传文字只能帮助命名产品能力，不能单独证明菜单、状态、边界条件或移动端交互。

## 2. 权威参考

### REF-WEB-20261030

- 官网：<https://affine.pro/>
- 访问结果：HTTP 200，页面标题 `AFFiNE — Open-Source Knowledge Base Software`。
- 用途：确认产品定位和现有入口；不作为完整功能清单的唯一证据。
- 访问日期：2026-10-30。

### REF-DOCS-20261030

- 文档站：<https://docs.affine.pro/>
- 访问结果：HTTP 403，Cloudflare 人机验证页。
- 处理：没有绕过验证。文档站的页面细节统一记为 `UNVERIFIED`；已通过锁定源码和实际产品补足可确认部分。
- 访问日期：2026-10-30。

### REF-AFFINE-GIT

- 仓库：<https://github.com/toeverything/AFFiNE>
- 默认分支：`canary`
- 核对 commit：`8cef09e1dd62b47a3cbc9f352aa10b3a90c75d34`
- 根 `package.json` 版本：`0.27.5`
- 核对日期：2026-10-30。
- 重要源码：
  - `packages/frontend/apps/android/App/app/src/main/AndroidManifest.xml`
  - `packages/frontend/apps/android/App/app/src/main/java/app/affine/pro/AffineEditorWebView.kt`
  - `packages/frontend/apps/android/App/app/src/main/java/app/affine/pro/AffineImeBridge.kt`
  - `packages/frontend/apps/android/App/app/src/main/java/app/affine/pro/AffineInputConnection.kt`
- 已确认：AFFiNE Android 客户端是 Capacitor/WebView 外壳，编辑器仍由 BlockSuite Web 实现。Manifest 声明单个主 `MainActivity`、互联网权限、`singleTask`、广泛的 `configChanges`，以及独立 AI Activity。

### REF-BLOCKSUITE-GIT

- 仓库：<https://github.com/toeverything/blocksuite>
- 默认分支：`main`
- 核对 commit：`5cb5cb68471ca692f3c162258f0087cb22fcb82d`
- 包版本：根 `package.json` 为 `0.16.0`，`packages/affine/all/package.json` 为 `0.22.4`；两者用途不同，不能用根值代替当前 BlockSuite 包版本。
- 核对日期：2026-10-30。
- 重点源码：
  - `packages/affine/blocks/root/src/edgeless/configs/toolbar/more.ts`
  - `packages/affine/blocks/root/src/edgeless/configs/toolbar/misc.ts`
  - `packages/affine/blocks/note/src/configs/toolbar.ts`
  - `packages/affine/blocks/edgeless-text/src/edgeless-toolbar/config.ts`
  - `packages/affine/gfx/text/src/toolbar/config.ts`
  - `packages/affine/gfx/shape/src/toolbar/config.ts`
  - `packages/affine/gfx/connector/src/toolbar/config.ts`
  - `packages/affine/gfx/brush/src/toolbar/components/pen/pen-menu.ts`

## 3. 实际产品观察

### REF-PRODUCT-DEMO

- 产品入口：<https://app.affine.pro/>
- Demo Workspace：`/_HGSe39jZxWDA9DXLqxCL/`
- 实际操作页面：
  - `All docs`：<https://app.affine.pro/workspace/_HGSe39jZxWDA9DXLqxCL/all>
  - `How to use folder and Tags`：<https://app.affine.pro/workspace/_HGSe39jZxWDA9DXLqxCL/VHcRqw4fB3>
- 访问日期：2026-10-30。

实际可见并已确认：

- 顶层导航包含 Docs、Collections、Tags。
- 页面列表支持按 Today / Never Updated 分组和 Select all。
- 文档信息面板包含 Workspace properties、Tags、Created、Updated。
- 展开文档属性后可见：Doc mode（Page / Edgeless）、Journal、Template、Created by、Edgeless theme（Auto / Light / Dark）、Page width（Standard / Full width）、Add property、Config properties。
- 文档内容明确提示：文档可以属于多个文件夹，并能展开信息管理 Tags。
- 文档存在 Bi-Directional Links 面板。
- 无界画布 URL 使用 `?mode=edgeless`。
- 实际产品上下文工具条的 More 菜单按 Selection Group、Reordering Group、Clipboard Group、Conversions Group、Deleting Group 分组。
- 实际观察到的对象动作包括 Ask AI、Insert into Page、Rename、Ungroup、Draw connector、Lock、More；More 中包括 Frame section、Group section、四个图层动作、Copy、Copy as Image、Copy as Markdown、Copy link to block、Duplicate、Create linked doc、Delete。
- Note 文本可双击原位编辑；画笔参数以当前工具浮层展示。

### REF-ANDROID-PLAY

- 产品：Google Play 标题 `AFFiNE - AI Notes & Visual Whiteboard`
- 包名：`app.affine.pro`
- 访问日期：2026-10-30。
- 用途：确认 Android 产品存在；移动端细节若未在本次实际 App 操作中验证，仍记为 `UNVERIFIED`。

## 4. 官方 Issue / 更新线索

### REF-ISSUE-14333

- <https://github.com/toeverything/AFFiNE/issues/14333>
- 标题：`[Feature Request]: Improve performance in edgeless mode for larger documents`
- 状态：Open。
- 2026-10-30 实际打开核对。
- 价值：AFFiNE 自身也把大画布/大量块性能视为开放风险。审计不能把“功能存在”误判为“复杂画布性能合格”。

### REF-ISSUE-15746

- <https://github.com/toeverything/AFFiNE/issues/15746>
- 标题：`[Bug]: Database table search finds nothing when one member cell holds a string instead of an array; linked-doc titles are never searchable`
- 状态：Open、Untriaged。
- 2026-10-30 实际打开核对。
- 引用版本：self-hosted 0.27.4。
- 价值：数据库单元格类型异常、Linked Doc 标题索引和数据库搜索完整性都属于需要专项验收的边界条件。

GitHub REST API 在审计期间触发了未认证速率限制，因此没有用 API 状态补充更多 Issue。无法再次核实的 Issue 不写成已确认事实。

## 5. 当前项目基线

- 技术栈：Kotlin 2.0.21、Android Gradle Plugin 8.13.0、Jetpack Compose BOM 2024.12.01。
- SDK：`compileSdk=36`、`minSdk=26`、`targetSdk=36`。
- 单一 Activity：`MainActivity`。
- 未发现 ViewModel、DI 容器、Room、WorkManager、CRDT/Yjs 依赖、网络层或实时协作层。
- 数据模型：`Page.kt` 中自研 `Page`、`Block`、`Shape`、`Connector`、`CanvasImage`。
- 页面存储：`PageStore.kt`，应用私有目录 + `AtomicFile` + 文件夹事务恢复日志。
- 页面格式：22；格式 21 新增多文件夹、Tags、Favorites，格式 22 新增连接线独立起点样式与文字，仍使用逐版本条件解码，没有独立 schema 迁移框架。
- 代码规模：27 个主源码文件、约 3,833 行；`MainActivity.kt` 约 1,463 行。
- 测试：修复后 33 个测试文件、147 个 testcase、0 failure、0 error、0 skipped（最终以 XTest XML 为准）。
- 构建：英文路径临时副本执行 `clean testDebugUnitTest assembleDebug` 成功，Debug APK 大小约 10.1 MB。
- 未发现 `app/src/androidTest`，没有 Compose UI、Activity 生命周期、真实触控、旋转、分屏或进程恢复的仪器测试。
- Manifest 没有 `INTERNET` 权限，也没有同步、分享、账号或网络声明。

项目 Git 状态：在受限环境中读取项目 `.git` 失败，`git status` / `git rev-parse --show-toplevel` 返回路径权限错误。当前不能声称存在已验证的 Git 提交基线。

## 6. 已执行验证

- 2026-10-30 在英文路径源码副本构建：`clean testDebugUnitTest assembleDebug` 成功。
- 从未被编辑的项目源码读取 XTest XML：27 suites，118 tests，0 failures，0 errors，0 skipped。
- Pixel Tablet API 35 x86_64 模拟器、2560 × 1600、Android 15：
  - 核对应用版本 0.9。
  - 实际检查文档/白板切换、点阵网格、形状选中态、形状上下文工具条、More Tools 分类、全屏、设置页、中英文布局和语言持久化。
  - 修复后验证：原位编辑按 Back 先收键盘/清编辑态，不再误退出应用。
  - 修复后验证：强停重启恢复页面、白板模式、当前工具、画布平移偏移和对象选区。
  - 修复后验证：页面撤销历史跨重启保留，连续输入合并为一次撤销。
  - 修复后验证：1280×800 物理像素紧凑窗口自动收起侧栏并隐藏工具文字，主要工具不越界。
  - 修复后验证：连接线可独立设置起止端点、添加并显示文字，面板渐层色带被统一为实底白面。
  - 修复后验证：Markdown 可导出共享文字并导入标题、段落、标题块和待办。

## 7. 参考边界

- “对齐 AFFiNE”指当前离线平板产品在约定范围内匹配可观察行为、数据完整性和输入体验，不要求复制 AFFiNE 的服务器、账号体系、品牌素材或私有数据格式。
- 云端、AI、协作和标准格式互操作仍列入矩阵，因为 AFFiNE 产品包含这些能力；当前未实现时标为 `MISSING`，不能通过把它移出矩阵来提高完成率。
- 官网文档站和未实测的移动端专属行为保持 `UNVERIFIED`。
