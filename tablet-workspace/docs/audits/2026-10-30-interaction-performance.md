# 交互流畅度基线

测试日期：2026-10-30。对象为灵页 0.9 当前源码的调试构建。

## 环境

- Pixel Tablet API 35 x86_64 模拟器，Android 15。
- 物理分辨率 2560 × 1600，密度 320 dpi。
- Skia OpenGL 渲染管线。
- 系统窗口、过渡和动画缩放均为 1.0。
- 测试工作区约 10 个页面，包含共享块、图形、图片、笔迹和连线。

## 启动

在应用进程停止后执行冷启动：

```text
LaunchState: COLD
TotalTime: 1580 ms
WaitTime: 1583 ms
```

这是首帧启动时间；工作区数据已改为后台加载，加载期间显示独立进度界面。

## 连续交互采样

执行以下操作后立即读取 `dumpsys gfxinfo`：

1. 文档和白板模式之间连续切换 10 轮。
2. 在白板“移动”工具中快速拖动 20 次。
3. 进入概览页并连续滚动 12 次。
4. 在左侧页面列表连续滚动 12 次。

结果：

```text
Total frames rendered: 776
Janky frames: 9 (1.16%)
50th percentile: 17 ms
90th percentile: 23 ms
95th percentile: 26 ms
99th percentile: 57 ms
Number Missed Vsync: 0
Number Slow UI thread: 3
Number Slow issue draw commands: 9
Number Frame deadline missed: 9
```

## 结论与限制

- 常规切换、滚动和拖动的 95% 帧在 26 ms 内完成，当前交互已达到可继续迭代的基线。
- 仍有少量 UI 线程和绘制超时，极少数帧接近 57 ms，主要风险位于复杂画布重绘和 Compose 大组合树。
- 该结果来自调试版、x86_64 硬件加速模拟器和约 10 页工作区，不等同于真机性能验收。
- 尚未覆盖 100/1000 画布对象、真实手写笔高频输入、分屏、旋转、低电模式或持续一分钟以上的压力测试。

## 184 页工作区 Release 复核（2026-10-30 后续）

- 构建类型：关闭 debuggable、使用调试签名安装数据的 Release 变体；页面版本仍为 15，页面格式仍为 22。
- 冷启动：`am start -W` 返回 `TotalTime: 1187ms`、`WaitTime: 1189ms`。
- 列表滚动：8 次连续滚动后 `gfxinfo` 为 192 帧、24 个卡顿帧（12.50%）；50/90/95/99 分位为 23/36/38/42ms；Slow UI thread 1，Slow issue draw commands 24。
- 优化前同一模拟器工作区的列表滚动采样为 169 帧、26 个卡顿帧（15.38%）；更早一次采样为 31.41%。页面修改时间/空间统计已移入后台协程，侧栏筛选结果复用 `remember`，常用 AFFiNE 图标 Path 使用进程级缓存，列表增加稳定 `contentType`。
- 当前瓶颈已从 UI 线程业务计算转向绘制阶段；12.50% 仍未达到 5% 的严格目标，不作为“性能完成”证据。
- 最终 Release APK SHA-256：`185716A498FD8203D807D1AB4E25B094F4B1FF07596AD04E89515EB6D5A61F02`；单元测试 161 项全部通过。
