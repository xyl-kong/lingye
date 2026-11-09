# 灵页品牌图标更新 · 2026-11-08

## 来源与可核对范围

- 用户提供的 Figma 图标截图：浅灰圆角底、深色折角文档、荧黄负形字、灰色短横线与轻微投影；截图中的绿色箭头、紫色标题属于标注，不纳入图标。
- Figma 文件 `8quVWMhRExVPEfOnuZUjlu` 的 `02 · 灵页工业图标探索` 页面中可见 `App Icon / 负形页芯` 主组件及六个矢量子图层；页面列表中未显示此图标的变体组。
- Figma MCP 达到 Starter 计划调用上限，桌面端同时提示连接异常；因此无法读取每个图层的精确矢量坐标、颜色、效果值或确认是否有未同步的隐藏状态。本次 Android 矢量是依据用户所附截图重建，**并非 Figma 原始 SVG 导出**。精确像素一致性及 Figma 中所有隐藏状态标记为 `UNVERIFIED`。

## 实施范围

- `ic_launcher_background.xml`：浅灰色底。
- `ic_launcher_foreground.xml`：适应 Android 自适应图标安全区域的缩小版文档图形；启动画面使用此图形。
- `ic_launcher_monochrome.xml`：Android 主题图标的单色轮廓。
- `ic_brand_foreground.xml`：侧边栏与设置工作区卡使用较大比例的同一图形，保证小尺寸可辨。
- `ic_launcher.xml`：正常、圆形与系统主题图标统一从上述资源生成；没有改动文档内容、画布或业务数据。

## 验证

- `assembleDebug --offline` 成功；调试 APK 通过 `adb install -r` 安装到 `emulator-5556`，保留应用数据。
- 实测启动过渡、桌面图标与侧边栏图标：`docs/design/hypergryph-study/app-icon-study/emulator-splash-final.png`、`emulator-launcher-settled.png`、`emulator-sidebar-final.png`。
- Android 启动画面和桌面图标遵循系统提供的自适应图标遮罩，因此外轮廓会随启动器变成圆形；应用侧边栏和设置内保持圆角方形。系统主题图标资源已编译，**未在设备上切换系统主题图标选项验证**。

状态：Android 默认图标与应用内品牌图标为 `VERIFIED`；Figma 精确值、隐藏状态及 Android 主题图标视觉为 `UNVERIFIED`。
