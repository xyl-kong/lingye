# AFFiNE 功能补齐实施顺序

本文件定义后续计划；用户已明确要求按现有问题继续改进，完成项保留证据，未完成项仍需逐项验收。

## 2026-10-30 增量完成

- 已实现并验证 Android Back 分层返回。
- 已实现页面、视图模式和工具跨强停恢复。
- 已实现页面撤销历史独立文件持久化，并合并连续输入撤销。
- 已将历史加载移到工作区后台 bootstrap。
- 已实现紧凑窗口侧栏自动收起与 icon-only 工具条。
- 已升级页面格式 21，加入多文件夹、Tags、Favorites，并保持旧单文件夹页面兼容。
- 已加入官方 `@blocksuite/icons` 的 Tags、Favorite、Favorited 路径。
- 已恢复画布平移偏移和稳定 ID 对象选区。
- 已升级页面格式 22，为连接线增加独立起点/终点样式和中点文字。
- 已将半透明面板改为同色实底白面、1dp 边框和轻阴影，修复明显颜色区块带。
- 已将日常自动保存拆为“主线程轻快照 + 后台最终编码/写盘”，并加入快照隔离测试。
- 已补连接线五种端点样式，并持久化文档/页面列表滚动位置。
- 已加入 Markdown 导入导出，覆盖标题、正文、标题块、待办和共享文字。
- 已加入单层项目符号、编号列表、引用、代码和分隔线，并限制图片/数据库/共享块被误转为文本。
- 已修复分隔线转换丢弃正文的风险，并隐藏代码/分隔线上无意义的文字格式按钮。
- Markdown 已支持新增块往返；带语言标记的代码围栏可正确导入，代码语言和高亮仍待模型升级。
- About 页版本已改为读取 `BuildConfig.VERSION_NAME`，最终设备显示 0.13。
- 文档块尾的分散文字按钮已收进“块操作”图标菜单，类型选择使用独立二级菜单，删除项在平板实测中完整可见。
- 文档文本块已加入 `/` 斜杠菜单，支持中英文关键词过滤和触控类型转换；键盘导航仍待补。
- 文档块已加入手型长按拖拽手柄、最近块中心落点、重启持久化和撤销/重做；仍未实现边缘自动滚动及嵌套子块拖动。
- 应用启动页已改为独立“全部文档”主页，提供列表/卡片、最近修改/标题排序和新建普通文档/白板入口。

仍未完成：缩放和滚动位置的进程恢复、主线程页面编码完整迁移、窄至 360dp/200% 字号回归、双文件夹实际 UI 回归、块拖拽/嵌套、代码语言与高亮、全量文档/画布/数据库/同步能力。

## 1. 实施原则

1. 先修生命周期、数据安全和性能底座，再增加内容类型。
2. 每批只引入一个可回滚的 schema/迁移版本。
3. 先在特性分支或独立 worktree 验证，不直接覆盖用户工作树。
4. UI 存在不算完成；必须同时有数据往返、失败路径和实际设备/模拟器证据。
5. 不将“接近 AFFiNE 视觉”写成“功能完全对齐”。
6. 任何云/AI/协作动作必须明确数据发送范围与隐私边界。

## 2. Phase 0：先冻结测试基线（P0）

### T0.1 建立 Android instrumentation 测试

- 新建 `app/src/androidTest`，不改现有业务行为。
- 覆盖：启动、页面创建、文档/白板切换、设置语言、全屏、菜单、返回键、键盘收起、强停重启。
- 依赖：稳定测试数据工厂和可查询语义 ID。
- 完成标准：核心导航冒烟测试在 Pixel Tablet API 35 可重复运行。

### T0.2 建立历史格式 fixtures

- 为 Page v1、v2、v3、v4、v5、v6、v7、v8、v9、v10、v11、v12、v13、v17、v18、v19、v20 建最小样本或程序化 fixture。
- 为 LocalDatabase v1–v4、SharedBlock v1–v3 建样本。
- 完成标准：迁移矩阵每次构建都运行，损坏/未知版本安全拒绝。

### T0.3 数据与性能基准

- 数据集：100、1,000、5,000 个画布对象；10、50、200 个文档块；10k 数据库记录。
- 指标：首帧、输入延迟、每 400 ms 自动保存耗时、内存、备份时间。
- 引用现有 `2026-10-30-interaction-performance.md` 作为小样本基线。
- 完成标准：形成可复跑的宏基准脚本和阈值。

## 3. Phase 1：生命周期与交互稳定性（P0）

### T1.1 修复 Android Back 导航

- 定义导航优先次序：Dialog/Menu → IME → 原位编辑 → Settings/Overview → Editor → Exit。
- 使用统一 navigation state，避免在 `MainActivity` 零散加回调。
- 验收：画布原位编辑时 Back 不退出应用；逐层返回有 instrumentation 证据。
- 依赖：T0.1。

### T1.2 增加会话状态保存

- 保存选中页面 ID、模式（Document/Whiteboard/Overview/Settings）、工具、pan/zoom、设置分类和必要弹窗层级。
- 用 `SavedStateHandle`/`Binder` 或小型 ViewModel；大内容不得塞入 Bundle。
- 验收：强停、Don’t keep activities、旋转、分屏后恢复可编辑状态。

### T1.3 页面 undo 持久化

- 定义页面历史保存策略：文件队列、最大步数、压缩与淘汰。
- 历史与页面写入建立顺序或事务，避免恢复出“新内容 + 旧历史”。
- 验收：切页、重启后 undo/redo 仍符合策略；跨页面操作隔离。

### T1.4 后台保存队列

- 将 `page.encode()` 和写盘移出 Compose 回调主路径。
- 串行化同一页面写入，合并重复 revision，支持关闭/切页时的 flush。
- 验收：大页面高频输入无超阈值阻塞，失败有可见错误和重试。

### T1.5 响应式工具栏

- 用窗口宽度与字体缩放定义 Compact/Medium/Expanded 布置。
- 所有低频动作进入分类收纳；主工具条可换行或收为溢出菜单。
- 验收：模拟 360–1280 dp、200% 字号、分屏和折叠屏无越界。

## 4. Phase 2：编辑内核决策（P1）

### T2.1 原生/BlockSuite 路线评估

比较三条路线：

- A. 继续原生 Compose，建立稳定 schema/registry。
- B. 文档和画布嵌入 BlockSuite WebView，原生壳负责文件/输入/备份。
- C. 文档走 BlockSuite，画布保留原生的混合架构。

评估维度：

- 离线可用性；
- Android IME/手写笔；
- 备份与版本；
- 性能；
- 跨设备同步；
- 包体积与安全；
- 迁移成本和长期可维护性。

完成标准：一份架构决策记录（ADR），明确不采用路线的原因。AD 完成前不批量增加新块类型。

### T2.2 统一 Block/Inline/Property schema

若选原生路线：

- 增加 block children、inline attributes、通用 props；
- 把共享块 span 合并为普通 rich-text span；
- 定义命令/事务边界。

若选 BlockSuite 路线：

- 定义 Bridge schema、文档 ID、资产 URL 和离线存储映射；
- 保留旧页面只读导入或迁移器。

依赖：T0.2、T2.1。

## 5. Phase 3：工作区与文档核心（P1）

### T3.1 页面属性与关系

- 迁移 `folder: String` → 稳定关系；
- 增加 Tags、多文件夹、Favorites、页面 icon/cover/width/theme；
- 统一页面搜索索引。

依赖：T2.2；禁止直接修改旧字段而不迁移。

### T3.2 文档 Block 第一组

- 行内 rich text；
- Slash menu；
- 块拖拽手柄；
- 多块选择；
- 列表、嵌套待办；
- 持久页面 undo。

### T3.3 文档 Block 第二组

- 静态表格；
- Code；
- LaTeX；
- 图片说明/替换；
- Divider/Callout。

### T3.4 引用与格式互操作

- Linked Doc；
- inline link/reference/mention；
- outline/backlinks；
- Markdown/HTML/PDF 的受控导出；
- AFFiNE/Markdown 导入样本。

## 6. Phase 4：无界画布对象模型（P1/P2）

### T4.1 统一画布节点和 action registry

- 每个节点类型声明选择工具、上下文动作、能力开关、序列化与复制规则。
- 替换多处硬编码 `selectedShapeIndex`、`selectedCardIndex` 分支。

### T4.2 Connector 完整模型

- 起点/终点独立 5 类标记；
- 曲线/折线/直线；
- 标签；
- 方向翻转；
- 组/Frame 内绑定；
- 删除对象时的稳定脱附。

### T4.3 Frame/Group/Mind Map

- 先做 Group 全类型和嵌套语义；
- 再做 Frame title、边界、TOC、Fit；
- 最后做 Mind Map 数据结构和快捷创建。

### T4.4 画布对象扩展

- 图形内文字；
- 自由旋转；
- 高亮笔；
- Linked Doc/Embed/Attachment/Bookmark；
- Note 显示模式、Slicer、尺寸；
- Copy as Image/Markdown/link。

### T4.5 手写输入专项

- 压力、倾斜、掌托取消；
- 笔迹平滑与预测；
- 局部/整笔擦除策略；
- 真实设备 S Pen、Lenovo、Xiaomi 等矩阵。

## 7. Phase 5：数据库（P2）

### T5.1 数据源与字段注册表

- 将字段类型从 `when` 分支提升为 schema/validator/editor/renderer 注册。
- 增加 URL、tag、member、relation、created/updated 等字段。

### T5.2 视图交互

- board 拖拽；
- calendar 直接编辑；
- record detail；
- 自定义 group-by；
- 多级 sort/filter；
- view 配置持久化。

### T5.3 关系与搜索

- 关联文档/记录；
- 失效链接处理；
- 关系标题搜索；
- 单条坏数据隔离。

## 8. Phase 6：同步、协作和 AI（P3/P4）

此阶段需要用户明确授权网络、账号、隐私和付费服务。不得作为本审计后的自动下一步。

### T6.1 账号与安全存储

- 网络权限、令牌安全存储、登出清理。

### T6.2 离线同步

- 本地 outbox、冲突策略、重试、遥测可控。

### T6.3 CRDT/实时协作

- 多端收敛、presence、远端选区、冲突恢复。

### T6.4 版本与权限

- 持久版本、差异、恢复、分享与角色权限。

### T6.5 AI

- 明确用户选中内容、发送目标、取消、审计与不泄露策略。

## 9. 下一阶段可直接执行的任务

1. 为 Back 导航和会话恢复编写失败测试，先复现 `AND-004` 与 `AND-003`。
2. 为 Page v1–v20、数据库 v1–v4、共享块 v1–v3 补 fixtures。
3. 增加 100/1,000/5,000 对象宏基准与 gfxinfo 记录。
4. 把 `page.encode()` 和保存调度抽离主线程，并验证失败重试。
5. 建立文档 BlockRegistry、InlineSchema 和命令层 ADR。
6. 建立画布 ObjectActionRegistry，先统一现有 6 类对象工具条。
7. 页面关系迁移：`folder` → 多文件夹/Tags/Favorites。
8. Connector 起止端点与标签模型。
9. 数据库字段/视图注册表和 record detail。
10. 建立真机设备矩阵，先测 S Pen 与 Gboard 中文。
11. 建立 Markdown/HTML/PDF 样本集和导入导出验收。
12. 等架构 ADR 和用户确认后，再决定是否引入 BlockSuite WebView 或长线自研。

## 10. 停止条件

- 任何操作涉及网络权限、账号、云存储、付费 API、外部上传时，必须先获得用户逐项确认。
- 任何页面格式变更必须有迁移、回滚和备份证明。
- 任何“完成”声明必须在 `FEATURE_MATRIX.csv` 中具有满足验收的有效证据。
