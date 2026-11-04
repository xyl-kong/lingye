# 官方模板分类与画廊（2026-11-03）

用户要求：把图 1“内容与数据”浮窗中的选中项和二级标题改成图 2 的官方内容；不修改选中项图标，改二级标题里的内容；能从官网直接拿到的资源直接取用。要求先审计官网，再列任务清单执行。

> 2026-11-03 更新：内容按钮现在一次点击直接打开本画廊，原二级菜单已删除；图片/数据库入口移到搜索框右侧，Cards 保留原卡片预设，存在工作区/旧页面模板时显示 Workspace 标签。详见 `GALLERY_DIRECT_OPEN_20261103.md`。

## 1. 官网审计

在 AFFiNE 网页版（app.affine.pro，Demo Workspace）新建文档并切换到 Edgeless，点击底部工具栏的内容（网格）按钮，实际打开的面板为：

- 顶部：搜索框（Search file or anything…）。
- 分类：横向标签栏，共 8 个分类：
  - Arrows
  - Cheeky Piggies
  - Contorted Stickers
  - Paper
  - Brainstorming
  - Marketing
  - Presentation
  - Project Management
- 图 2 中可见前 5 个分类，后 3 个在横向滚动范围内。
- 内容：3 列白色卡片网格，卡片内是模板预览；没有文字标题。
- 资源：前 4 类为官方 SVG，托管在 `prod.affineassets.com/assets/`；Brainstorming 等为可编辑画布模板，网页中以实时预览渲染。

本次直接取用的官方资源：

| 分类 | 资源 | 数量 |
|---|---|---|
| Arrows | `arrow-1…18` SVG（官网共 185 个，首屏加载 18 个） | 18 |
| Cheeky Piggies | Crybaby、Drool、Fuming 等 SVG | 15 |
| Contorted Stickers | AFFiNE、AI、Cat 等 SVG | 15 |
| Paper | +1、A lot of question、AFFiNE AI 等 SVG | 15 |
| Brainstorming | 官方面板中的 5 个模板预览，按原生面板像素截图 | 5 |

SVG 直接下载后以 320px 透明 PNG 栅格化，共 68 个文件、728,641 字节，放入 `res/drawable-nodpi`。

## 2. 实现

- `内容与数据` 浮窗保留“图片 / 数据库”及原图标，分隔线下的 5 个旧模板项（笔记、待办、项目计划、会议记录、灵感）替换为 5 个官方分类标题：Arrows、Cheeky Piggies、Contorted Stickers、Paper、Brainstorming。
- 点击任一分类关闭小菜单，打开 `TemplateGalleryPopup`：
  - 顶部搜索框；输入时按模板名称和分类名称过滤。
  - 横向分类标签栏，选中项白底加粗；末尾保留 `Cards` 标签，承载原 5 个卡片预设。
  - 3 列模板网格，白色卡片 + 官方预览；可纵向滚动。
  - 点击外部或按返回关闭。
- 新增 `TemplateGalleryData.kt` 登记分类、模板标题与 drawable 资源 ID。
- 新增 `TemplateGalleryPanel.kt`：面板、搜索、标签、网格、独立 Popup 定位；`Cards` 标签点击后调用 `page.addCard` 插入原卡片预设。
- `PageStore.importImageBytes(bytes)` 从页面导入逻辑中抽出；点击模板时把 PNG 写入应用图片存储，并调用 `page.addCanvasImage` 在当前页插入图片对象。
- 插入位置暂为世界坐标 (400,140)，尺寸 240×240；Brainstorming 宽屏预览为 320 宽。

## 3. 验证

- `tg-1.png`：内容菜单显示 图片、数据库、Arrows、Cheeky Piggies、Contorted Stickers、Paper、Brainstorming。
- `tg-2.png`：Arrows 画廊，官方 18 个箭头 SVG，3 列网格、搜索、分类标签。
- `tg-3.png`：Cheeky Piggies 分类，官方 15 个贴纸。
- `tg-4.png`：Brainstorming 分类，5 个官方模板预览。
- `tg-9.png`：搜索 `pig` 后显示全部 Cheeky Piggies 模板（分类名参与匹配）。
- `tg-6.png`：点击第一个箭头模板，画廊关闭，画布出现箭头图片。
- `tg-7.png`：插入的箭头可被选择，图片工具条可用。
- `tg-10.png`：返回键关闭画廊并回到页面。
- `tg-12.png`：点击面板外部关闭画廊。
- `tg-14.png`：画廊末尾的 Cards 标签，原 5 个卡片预设仍在。
- `tg-15.png`：点击“笔记”卡片，画廊关闭并插入笔记卡。
- `tg-16.png`：插入的笔记卡可被选择，卡片工具条可用；测试后已删除。
- 361 项 JVM 测试 0 失败。

## 4. 数据

- 页面格式 25；195 个页面文件、20 历史、活动文档 193、回收站 2、图片目录 0。
- 插入测试在既有测试页 `ToolbarTest` 上完成，先后删除插入的箭头图片与笔记卡；页面恢复为 1,513 字节，测试产生的孤立图片文件已删除。
- 未增删页面，未改动用户页面。

## 5. 限制

1. 模板作为**图片对象**插入，不是 AFFiNE 的可编辑矢量/画布模板；箭头、贴纸、Paper 为官方 SVG 栅格图，Brainstorming 为官方面板截图。
2. 只实现图 2 可见的 5 个分类；官方另有 Marketing、Presentation、Project Management 未接入。
3. Arrows 仅打包首屏 18 个（官方共 185 个），其余分类各 15 个；暂无懒加载/分页。
4. 原“笔记 / 待办 / 项目计划 / 会议记录 / 灵感”卡片预设不再出现在一级内容菜单，改为画廊末尾的 `Cards` 标签；该标签是本应用保留项，不属于 AFFiNE 官方分类。
5. 插入位置固定为 (400,140)，不跟随当前视口中心，也不支持拖放定位。
6. 搜索按模板文件名与分类名匹配，没有官方全文搜索。

## 6. 发布

- `releases/lingye-v0.15-official-template-gallery-release-20261103.apk`，3,391,279 字节，SHA-256 `E6EABB7B5A84E6608EDECD3204236A2A91C5B5EC3E95F566C37695D2B8F6A267`；模拟器已安装同一 APK。
