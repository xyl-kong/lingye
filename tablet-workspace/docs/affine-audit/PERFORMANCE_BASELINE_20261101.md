# 性能基线（2026-11-01）

设备：emulator-5554，2560×1600，density 320，Release `lingye-v0.15-stage-d2-release-20261101.apk`（R8）。  
用途：T03 启动/保存基线 + T04 大画布/大文档压力；本文件是基线记录，不代表已优化。

## 1. 样本

使用 `BenchmarkFixtureWriterTest` 生成，不包含用户页面内容：

| 样本 | 内容 | 页面大小 |
|---|---|---|
| Benchmark Canvas | 1,000 个矩形（50×20 网格） | 175,118 B |
| Benchmark Document | 1,000 个段落块 | 95,010 B |

宿主 JVM 编解码耗时（非设备 I/O）：

| 样本 | encode | decode |
|---|---|---|
| Canvas 1,000 shapes | 9.17 ms | 12.16 ms |
| Document 1,000 blocks | 0.40 ms | 1.00 ms |

## 2. 冷启动

`am start -W`，强停后启动，单位 ms：

| 状态 | 5 次 TotalTime | 中位数 |
|---|---|---|
| 197 页基线 | 719 / 1485 / 1887 / 1006 / 1177 | 1,177 |
| 199 页（+2 个 1000 对象压力页） | 977 / 1097 / 1344 / 1464 / 1188 | 1,188 |

结论：两个大页面加载进入启动路径后，冷启动中位数仅约 +11 ms；启动瓶颈更多在 Compose/页面列表初始化，而不是两个大页面的解码。

## 3. 大画布（1,000 shapes）

操作：5 次往返平移 + 2 次放大/缩小；`dumpsys gfxinfo` 统计：

| 指标 | 1000 shapes | 普通画布（AlphaRenamed） |
|---|---|---|
| Total frames | 158 | 210 |
| Janky frames | 142（89.87%） | 163（77.62%） |
| 50th percentile | 81 ms | 48 ms |
| 95th percentile | 113 ms | 73 ms |
| 99th percentile | 133 ms | 85 ms |

普通画布本身在该模拟器上已有明显 jank，但 1000 shapes 的 50th 从 48 ms 增至 81 ms、95th 从 73 ms 增至 113 ms，说明对象数量带来可测的额外开销。  
结论：**不建议把 1000 对象作为当前可流畅交互的承诺**；需要对象级/绘制级优化与更低基准。

## 4. 大文档（1,000 blocks）

### 4.1 T04a 之前（Column + verticalScroll）

操作：4 次快速向上滑动；`dumpsys gfxinfo`：

| 指标 | 1000 blocks |
|---|---|
| Total frames | 20 |
| Janky frames | 20（100%） |
| 50th / 95th / 99th | 150 ms / 150 ms / 150 ms |

现象：文档块编辑器当前使用 `Column + verticalScroll` 一次性组合所有 block；1000 blocks 时只滚动少量段落也出现 150 ms 帧。  
结论：**大文档需要改为 LazyColumn/虚拟化**；这是 T04 的首要优化项。

### 4.2 T04a 之后（LazyColumn）

实现：`BlocksEditor` 改为 `LazyColumn`；标题/正文作为 header item，1000 个 block 作为 `itemsIndexed`，底部“添加块”工具条作为 footer item。会话滚动位置改为 `firstVisibleItemIndex + offset`（新增 `workspace_session_document_scroll_index`）。

| 构建 | Total frames | Janky | 50th | 95th | 99th |
|---|---|---|---|---|---|
| Debug | 52 | 43（82.69%） | 69 ms | 101 ms | 101 ms |
| R8 Release | 82 | 62（75.61%） | **61 ms** | **73 ms** | **81 ms** |

相对基线：50th 150 → 61 ms，95th 150 → 73 ms；仍高于 60 fps，但已减少约 2.5× 帧时间。模拟器本身普通画布也有 48–73 ms 帧，需区分设备基线。

已验证：

- 滚动到 Paragraph 23–46 正常，块按需组合，不是一次渲染 1000 项。
- 标题、正文、块列表、底部工具条均正常显示；滚动到顶部恢复 title/text。
- 点击块可聚焦并弹出键盘，文本编辑仍可用。
- 长按拖动 handle：Paragraph 0 移到 Paragraph 1 之后，拖拽重排仍工作。
- 离开页面再打开后恢复到 Paragraph 33–40 附近，滚动位置会话恢复生效。
- Release 同样通过，R8 未破坏 LazyColumn/块编辑。

未验证/限制：

- 底栏“添加块”按钮仅确认显示，未重新点击创建新块。
- `blockCenters` 只记录已组合的可见块；跨很远距离的拖拽目标仍受限，需要在后续回归中验证。
- 模拟器上 1000 blocks 仍有 75.6% jank；不能宣称达 60 fps，虚拟化只是消除“一次组合全部块”的主要问题。


## 5. T03 保存风险

- `MainActivity.onStop` 调用 `saveOnStop()`，内部 `saveCurrent()` 同步执行 `Page.encode` + `AtomicFile` 写入/历史写入。
- 1000 shapes 的 encode 在宿主 JVM 为 9.17 ms；设备上还要叠加历史快照与文件 I/O，当前没有 10 MB 页面基准。
- 结论：暂不直接重写生命周期保存；先在设备上测 `onStop` 的实际耗时和 10 MB 级编码，再决定是否需要后台保存/命令级历史。

## 6. 复现与清理

- fixture 由 `BenchmarkFixtureWriterTest` 写入 `app/build/benchmark-fixtures/`；测试样本不进入 APK。
- 模拟器上两个 Benchmark 页面已移入回收站；活动文档恢复为 191。
- 当前页面文件 202（含回收站测试/基准页），历史 20；全部仍为格式 25。
- 回归测试：288 项 JVM 测试，0 失败。
- 本轮未改页面格式、业务代码或发布 APK；仅新增测试/基准工具与文档。

## 7. 后续任务

| ID | 任务 | 触发条件 |
|---|---|---|
| T04a | 文档块 `Column + verticalScroll` → `LazyColumn`，处理拖拽/焦点/滚动恢复 | **首版完成**：50th 150→61 ms，详见第 4.2 节 |
| T04b | 大画布绘制/命中优化，建立 100/500/1000 对象分档 | 需要保留 1000 对象能力 |
| T03a | 设备端 `onStop`/显式保存计时，10 MB 页面基准 | 输出保存路径耗时后再改架构 |

## 8. T04a 发布

- Release：`releases/lingye-v0.15-doc-lazy-release-20261101.apk`，2,512,577 字节，SHA-256 `24DDEF23B2BC9020EDA2F4FA7A44676F917149022D35E6AE168243FA58A4575A`。
- Debug：`releases/lingye-v0.15-doc-lazy-debug-20261101.apk`，14,881,315 字节，SHA-256 `79375088C927E6B47112C3FDDEBA5CD9D4434568D643D45A5B989BE57E075460`。
- 288 项 JVM 测试 0 失败；页面格式保持 25；模拟器活动文档 191。



