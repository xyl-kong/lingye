# 无界画布工具栏工作清单

更新日期：2026-11-01

状态说明：`[x]` 仅用于已有源码、构建或模拟器交互证据的条目；尚未完成或仅部分实现的项目保持 `[ ]`。

## A. 本轮已完成

- [x] 第一按钮在拖拽和选择之间切换。
- [x] 恢复手形、框选、连接、链接四个前序按钮和原截图分组顺序。
- [x] 框选比例参数：自定义、1:1、4:3、16:9、2:1。
- [x] 连接参数：曲线、折线、直线；四档线宽；十种颜色；圆点、箭头、无端点。
- [x] 形状参数：矩形、圆形、菱形、三角形、圆角矩形；十种填充色。
- [x] 文本参数条：图片、链接、附件、正文、H1–H6、代码、引用、项目符号、编号、待办。
- [x] 画笔参数：九种颜色、五档圆形粗细、仅手写笔。
- [x] 思维导图菜单：文字、气泡图、向右树、向下树、组织图、大纲。
- [x] 内容数据菜单：文本、图片、数据库、卡片和模板。
- [x] 移除左下缩放按钮组。
- [x] 裁剪画布范围，防止对象覆盖顶部栏。
- [x] 使用 `@blocksuite/icons` 官方线型图标。
- [x] 使用用户提供的官方截图裁切内容类彩色素材。
- [x] 文本子集参数条限制最大宽度，参数区可左右滑动，“关闭”按钮固定显示。
- [x] 在模拟器实测文本参数条横向滑动：起点可见前项，滑动后可见“待办”，关闭按钮始终保持可见。
- [x] H1–H6 保存与重开复检：各级标题在强停重启后保持级别与文字；顺带修复两个缺陷（见 D 节 2026-10-30 标题复检）。
- [x] Frame 属性：重命名、六种背景色，选中 Frame 显示专属工具条。
- [x] Frame 导航：Frame 列表（含成员数）与定位拟合；“更多 → 视图”同入口。
- [x] Frame 基础层级：移动携带完全包含的对象、内容点击穿透、Frame 绘制在内容之下、复制包含成员。官方完整语义仍为 `PARTIAL`（见 B 节）。
- [x] “链接已有文档”：画布链接卡片、以页面 UUID 稳定引用、双击/工具条打开、返回来源页面、重命名跟随与失效状态。
- [x] 底部工具栏 UI 统一与去重：主按钮统一 44×44dp 与选中背景；参数条统一表面、chip、色点和粗细控件；删除 More 的基础图形/内容分区、思维导图“文字”、链接“添加页面卡片”、内容菜单“文本框”、文本条“图片”；链接按钮改为直接打开选择器。核对来源与保留差异见 `docs/affine-audit/TOOLBAR_INVENTORY.md`。
- [x] 区域橡皮：橡皮参数条新增“整笔/区域”切换；区域模式拖过即整体删除被覆盖的图形、图片、笔迹和连线（锁定对象除外），拖动时显示灰色轨迹，松手后一次撤销，模式随偏好保存。
- [x] 节点级思维导图：节点树与 mm- 绑定连线；节点工具条支持添加子/同级、双击编辑、切换五种布局、改接上级、删除子树（含确认与撤销）；布局引擎自动排布并保持根节点位置。
- [x] 工具栏插画图标重设计：先给出纸片/贴纸/墨线三个方向稿与 34dp 实际尺寸预览，选定方向 A 后将六个按钮重绘为白色纸片 VectorDrawable，统一描边与单点强调色，替换原混用 PNG。
- [x] 键盘快捷键与提示：白板支持官方工具键（V/H/F/C/T/P/E/S/M/L/@）与 Ctrl+Z、Ctrl+Shift+Z、Ctrl+Y、Ctrl+A、Delete、Esc、方向键、Ctrl+=/-/0；工具按钮悬停或长按显示名称与键帽提示。
- [x] 画笔类型与参数集：画笔/高亮两种类型，画笔 2/4/6/8/10/12、高亮 10/14/18/22/26/30 六档粗细，十色短色板，高亮 30% 透明度并写入笔迹颜色，⇧P 切换，类型与参数跨重启保持。
- [x] 参数子集窄窗口与无界编辑区边界适配复核：新增 `CanvasLayout.parameterBarMaxWidth`，所有浮动参数条受视口宽度约束并在内部横向滑动；640dp 下画笔条可滑到“仅手写笔”，连线、形状、文本条不被裁切；200% 字体下画笔与文本条完整；画布对象工具条按视口宽度裁剪。
- [x] 窄窗口首页与文档块工具条：首页头部在内容宽度小于 560dp 时改为两行（标题/计数单行 + 多选按钮在第一行，视图切换、排序、新建文档在第二行右对齐）；文档块类型条改用 `FlowRow`，窄宽度自动换行，所有块入口完整可见。
- [x] Frame 嵌套与演示：完全包含的 Frame 作为外层成员参与移动、复制与导航计数，绘制时外层先于内层；Frame 工具条、导航与“更多 → 视图”新增演示入口，支持填充/适应、上一帧/下一帧、Esc/返回退出，退出恢复原视图与工具；删除 Frame 保留内容，与官方 Ungroup 语义一致。
- [x] 设置弹窗与全局界面整理：设置改为 AFFiNE 风格模态弹窗（工作区卡、分类导航、关闭按钮，640dp 下可用），新增键盘快捷键只读页；文档/白板顶栏移除“已保存/保存中”胶囊；文本参数条与底部工具栏改为按内容自适应宽度，消除尾部空白；侧栏隐藏按钮去掉白色方块背景，并与下方“+”按钮同宽同中心（38dp 触控区）；侧栏左上角改用应用启动图标。
- [x] 设置专项改进：按 AFFiNE `8cef09e` 设置结构分为“常规/工作区”，统一分区与设置行样式；为条目加入问号帮助浮窗；新增工作区名称/副标题、正文字体、网格显示/大小/默认吸附、侧栏链接文档；快捷键页补充“对象操作”分组；数据/关于补充帮助说明。详见 `docs/affine-audit/SETTINGS_IMPROVEMENTS.md`。
- [x] 设置弹窗官方布局校正：按官方截图重做为左侧浅灰导航（“设置”标题 + 工作区卡 + 常规/工作区两组）、右侧纯白内容与右上关闭；导航收敛为外观/编辑器/键盘快捷键/关于灵页 + 偏好设置/储存；画布默认值并入编辑器“文档/白板”标签；主题三选一（跟随系统/深色禁用）、语言改为下拉、新增“打开页面链接”返回行为、页脚与响应式堆叠。
- [x] 编辑器与快捷键板块核对补齐：正文字号 12–24、日期格式、代码块行号、页面宽度标准/全宽、显示页面信息、Frame 默认背景、连接线默认线宽；新增选择与编组快捷键（Ctrl+D/G/Shift+G、Ctrl+]/[、Ctrl+Shift+L/R/T/B）并接入画布。详见 `SETTINGS_IMPROVEMENTS.md` 第 6 节。
- [x] 编辑器适用化组件与视觉重设计：白板主题适配为画布浅色/深色，连接线增加手绘风格，文本默认颜色/字号/对齐；外观主题移除不可用分段；颜色选择器改为白卡+色点+深色标签；日期格式改内联芯片；缩短说明、调整右栏留白与代码行号对齐。详见 `SETTINGS_IMPROVEMENTS.md` 第 7 节。
- [x] 全局色圈与笔记样式：新增经典十色色圈、自定义色卡（最多 12，SharedPreferences）与 RGB/HSV 折叠式取色器；设置与画布所有颜色入口统一接入；编辑器 > 白板新增笔记背景/圆角/阴影/边框样式/厚度与预览；滚轮缩放改为右下角按钮缩放（−/百分比/+）。详见第 8 节。
- [x] 极窄窗口与分屏设置导航：设置弹窗内容宽度小于 520dp 时改为单栏（分类列表 → 分区详情 + 返回按钮），宽屏保持两栏；系统返回先回列表再关闭弹窗；360dp 首页、编辑器与白板底栏同步复核。
- [x] 分段切换滑动动画：选中黑框按各选项实际位置在 220ms 内平滑滑动，文字随黑框覆盖切换白色；设置页全部 `SegmentedChoice` 与首页列表/卡片视图切换生效；换行日期 chip 保持静态。
- [x] 深色界面主题：新增“跟随系统/浅色/深色”设置并持久化；侧栏、首页、文档、画布工具栏与参数条、设置、弹窗、取色器、概览统一使用深色令牌；深色下分段选中框与开关反转为白底/深色文字；系统栏图标随主题切换。
- [x] 应用级与文档快捷键：Ctrl+K 聚焦快速搜索、Ctrl+N 新建文档、Ctrl+Shift+N 新建白板、Ctrl+, 打开设置；文档块支持 Ctrl+B/I/U 与 Ctrl+Alt+1–6/0 标题切换；快捷键设置页补充“应用”与“文档编辑”分组。
- [x] 页面图标与双向链接：页面格式 23 增加 emoji 图标；侧栏、首页和编辑页顶栏显示，页面菜单与顶栏可选择/移除；侧栏与页面信息显示反向链接并可跳转到来源页。
- [x] 单笔记阴影与圆角：`Shape.shadowStyle` 写入页面格式 24，新建笔记使用全局默认；选中笔记可在“图层与更多”菜单中单独调整阴影与圆角，修改随页面保存并在重启后保持。
- [x] 底栏状态互斥与连点修复：工具按钮与弹出菜单同一时间只保留一项选中态，思维导图菜单使用五种布局预览图标；连点菜单按钮不再跳回上一个图形工具，底栏主要功能已逐项实测。

证据：

- `docs/affine-audit/evidence/canvas-text-parameters-scroll-start.png`
- `docs/affine-audit/evidence/canvas-text-parameters-scroll-end.png`
- `docs/affine-audit/evidence/heading-reopen-after-fix.png`

## B. 待继续处理

- [ ] `PARTIAL` 官方完整 Frame 语义：属性、导航、几何层级、嵌套 Frame 与演示模式已完成；持久化父子关系（官方 `childElementIds`）、文档大纲目录、演示顺序自定义与演示设置仍未实现。
- [ ] `PARTIAL` “链接已有文档”：画布链接卡片、稳定引用、打开与返回、失效状态、反向链接已完成；大纲目录、行内链接和引用计数未实现。
- [ ] `PARTIAL` 节点级思维导图：官方使用左/放射/右三种布局、四套主题并支持拖拽节点改接；本实现为菜单改接与五种本地布局，尚无拖拽改接、主题样式和节点折叠。
- [ ] `PARTIAL` 区域橡皮与官方仍有差异：官方拖动时把目标降为 0.3 透明度，本实现显示灰色轨迹；区域模式不擦除画布背景。
- [ ] `MISSING` 官方媒体素材库分类、搜索和缩略图数据。
- [ ] `PARTIAL` 键盘快捷键与提示：白板工具/命令键已实现并用 adb 键盘事件实测；悬停提示已实现但模拟器无法注入悬停事件，长按提示已实测；快捷键自定义、文档块编辑 Tab/Enter 未实现。
- [ ] `PARTIAL` 画笔：官方两类型、两套六档粗细与短色板已实现；压感、平滑/预测、透明色、专用混合模式和真实手写笔延迟未验证。
- [ ] `PARTIAL` 底部工具栏仍保留浮动参数条（官方为按钮滑出菜单）；官方 Template 面板和 Slide 预设直接在视口中心创建未实现；Note 工具入口已有，但 Note 块容器/子块语义未实现。
- [ ] `PARTIAL` 设置项补齐：官方布局、帮助、工作区信息、字体、页面宽度、日期格式、代码行号、网格、侧栏链接文档与内部链接返回行为、页面图标、双向链接、深色主题、滚轮缩放、应用级与文档编辑快捷键已完成；新建文档 Ask 模式和快捷键自定义仍未实现，其他差异保持 `PARTIAL`。
- [ ] `PARTIAL` Note 块容器：背景色板、行级待办、插入文档类型保真已完成；子块/嵌套/显示模式与图片、行内格式仍缺失，需格式 26 设计，详见 `STAGE_D2_IMPLEMENTATION.md`。

未完成项不得在界面中伪装成可用；当前以禁用状态或 `PARTIAL`/`MISSING` 审计状态记录。

## C. 验收清单

- [x] `compileReleaseKotlin testDebugUnitTest assembleRelease` 在英文路径开发副本 `deepseek\.tmp\project-fix` 构建成功（源码与原项目经 SHA-256 核对一致）。
- [x] 单元测试：288 项，0 失败，0 错误（英文路径开发副本；含 D1 Text 字体/选区、D2 Note 色板/行级待办/插入文档、PageMigrationMatrixTest 13 个历史版本等）。
- [ ] 原项目中文路径下 `testDebugUnitTest` 为 36 项全部 `ClassNotFoundException`（`initializationError`），属于 README 记录的中文路径限制，不是测试断言失败；需在英文路径运行或迁移后复测。
- [x] 模拟器检查手形/选择切换。
- [x] 模拟器检查框选比例参数。
- [x] 模拟器检查连接路径、线宽、颜色和终点。
- [x] 模拟器检查形状类型和填充色。
- [x] 模拟器检查文本 H1–H6、内容菜单和横向滑动。
- [x] 模拟器检查 H1–H6 强停重开、标题块转段落、斜杠菜单转换。
- [x] 模拟器检查 Frame 创建、重命名、背景色、移动携带成员、点击穿透、复制、导航定位与强停重开。
- [x] 模拟器检查链接已有文档：选择器、创建、双击打开目标、返回来源、重命名跟随、回收失效与恢复、强停重开保持。
- [x] 模拟器检查工具栏统一与去重：主按钮尺寸/选中态、Frame/连接/形状/画笔/文本/多选参数条、More 仅剩选择操作与视图、思维导图无“文字”、内容菜单无“文本框”、链接直接打开选择器。
- [x] 模拟器检查区域橡皮：整笔/区域切换、拖动轨迹、删除被覆盖对象并保留未覆盖对象、撤销/重做、模式与擦除结果强停重开保持；整笔模式仍只删除笔迹。
- [x] 模拟器检查思维导图：创建、选中节点工具条、添加子节点、切换布局、改接上级、删除子树确认、撤销、双击编辑文字与强停重开保持。
- [x] 模拟器检查新插画图标：六个按钮在 34dp 工具栏与放大截图下可辨认，选中态由按钮底色承担。
- [x] 模拟器检查快捷键与提示：P/Esc/F 切换、Ctrl+Z 撤销、Ctrl+Shift+Z 重做、Ctrl+A 全选、Delete 删除、长按画笔显示“画笔 P”提示；鼠标/手写笔悬停因模拟器无法注入未做设备验证。
- [x] 模拟器检查画笔类型：画笔/高亮参数条、⇧P 切换、高亮半透明笔迹、宽高亮笔触、强停重开后笔迹与类型保持。
- [x] 模拟器检查窄窗口参数条：1280×1600（640dp）下画笔/高亮条可横向滑动到“仅手写笔”，连线、形状、文本条内容不被视口裁切，文本条“关闭”固定；选中画布对象时工具条不超出右边缘。
- [x] 模拟器检查 200% 系统字体（2560×1600）：画笔参数条与文本参数条完整显示，chip、色点、粗细点和“关闭”均无裁切。
- [x] 模拟器检查窄窗口首页与文档块工具条：1280×1600（640dp）侧栏展开时“全部文档/191 个文档”保持单行，视图切换、排序、新建文档位于第二行；空白文档块条换行为两行且全部入口可见；2560×1600 宽屏下两处均保持单行。
- [x] 模拟器检查 Frame 嵌套与演示：在大 Frame 内创建小 Frame 后，导航显示外层 3 个对象、内层 0 个对象；演示模式从“更多 → 视图”启动后隐藏侧栏与工具栏、按填充铺满视口，→ 键切到 2/2，Esc 退出并恢复原视图与工具条；新建测试 Frame 已撤销，页面恢复为两个 Hero Frame。
- [x] 模拟器检查设置与界面整理：2560×1600 与 640dp 下设置弹窗结构完整、快捷键页可读、关闭后回到原视图；文档顶栏无“已保存/保存中”；宽屏与 640dp 白板文本条、底栏无尾部空白且窄屏可滚动；侧栏左上角为启动图标，隐藏按钮为无背景图标。
- [x] 模拟器检查设置专项：问号浮窗可打开；工作区名称改为 Studio 后侧栏立即更新并已恢复为灵页；衬线字体应用到文档标题；关闭显示网格后白板无点阵、重新开启后恢复；链接文档开关开启时侧栏出现“链接的文档 / AlphaPage”，撤销测试链接后消失；640dp 下分组导航与帮助浮窗正常。
- [x] 模拟器检查官方布局校正：2560×1600 下左栏标题/工作区卡/常规与工作区分组、右栏标题与右对齐控件、页脚正常；编辑器“文档/白板”标签切换正常；640dp 下控件自动堆叠且主题三项完整显示；偏好设置与储存页标题/分区正确。
- [x] 模拟器检查编辑器与快捷键：标准宽度文档居中、全宽铺满；关闭页面信息后顶栏信息按钮消失并恢复；代码块行号显示 1/2 并随文字对齐；日期格式下拉四种；快捷键页显示选择与编组分组；测试后已恢复默认并删除测试代码块。
- [x] 模拟器检查编辑器视觉重设计：主题仅显示可用“浅色”状态块；画布主题分段与深色画布生效；日期格式内联芯片不遮挡相邻行；颜色选择器白卡+色点标签在墨黑/深灰下仍可读；文本默认样式与手绘连线控件布局正常。
- [x] 模拟器检查全局色圈与笔记样式：经典色圈 + 自定义色卡在设置和画布菜单显示；RGB/HSV 折叠区与滑块可用；自定义色卡可保存并清空；笔记预览与背景/圆角/阴影选项正确；画布右下角按钮缩放 100%→125% 并可复位；R8 release 启动、设置、画布与 Lottie 文件夹图标正常。
- [x] 模拟器检查 360dp（720×1600）：首页、文档编辑器、白板底栏与设置单栏列表/详情/系统返回均可用；外观、编辑器、键盘快捷键、储存详情无折行溢出或控件裁切，白板底栏可横向滚动。
- [x] 模拟器检查 540dp/640dp 与真实分屏（2560×1600，Chrome + 灵页各 640dp）：设置单栏/两栏切换正确，宽屏与 640dp 控件无重叠。
- [x] 模拟器慢放验证分段切换动画：动画时长 10× 时抓取到黑框位于两个选项之间的中间帧；设置页编辑器“文档/白板”与首页列表/卡片切换均滑动到位，终点与选中态一致，360dp 下无裁切。
- [x] 模拟器检查深色主题：宽屏与 360dp 下首页、文档、白板（浅色与深色画布）、设置、取色器、概览、卡片视图均为深色且文字可读；浅色主题与改动前一致；R8 release 启动与主题切换正常。
- [x] 模拟器检查主题持久化与跟随系统：选择深色后强停重启仍为深色；“跟随系统”在系统深色/浅色切换时同步；系统切回浅色后恢复浅色。测试中误移入回收站的 FrameTest 已恢复，文档数回到 191。
- [x] 模拟器检查应用快捷键：Ctrl+K 聚焦快速搜索并可过滤列表；Ctrl+N 新建文档、Ctrl+Shift+N 新建白板并进入对应页面；Ctrl+, 打开设置；R8 release 同样通过。
- [x] 模拟器检查文档格式快捷键：在 AlphaRenamed 的段落块上 Ctrl+B/I/U 切换、Ctrl+Alt+1 转为标题、Ctrl+Alt+0 转回正文，转换后焦点保持在原块；测试后块格式恢复原状。
- [x] 模拟器检查页面图标：安装格式 23 后 v22 页面正常读取；为 AlphaRenamed 选择 🚀 后顶栏、侧栏和首页即时显示，强停重启保持；移除后侧栏即时恢复；页面文件首 4 字节为 23。
- [x] 模拟器检查反向链接：AlphaRenamed → AlphaPage 的测试链接在侧栏显示“链接的文档”；目标页显示“反向链接 / AlphaRenamed”，点击可返回来源页；测试链接已通过页面撤销移除，页面内容恢复。
- [x] 页面格式 23 迁移与备份：安装格式 23 前导出 v22 的 pages/history tar（195 页、12 个历史文件，SHA-256 `985D6F4AEA331D0E1BAEB50DD22BED94E30A49B8EA1DD04C6022975268BE0B0D`）；v22 页面可读、按需升级，回滚步骤见 `PAGE_FORMAT_23.md`。
- [x] 模拟器检查单笔记样式：新建笔记默认卡片阴影与中圆角；选中后改为贴纸阴影＋超大圆角，强制重启后保持；删除测试笔记后画布恢复，页面文件首 4 字节为 24。
- [x] 页面格式 24 迁移与备份：安装前导出 v23 的 pages/history tar（195 页、13 个历史文件，SHA-256 `C40F362740A1D42263DEA2E45D9DE6D8076AFD7003C88A6D3AE5698DD261B7F4`）；旧页面阴影默认 `box`，当前全局也是 `box`，升级外观不变；回滚步骤见 `PAGE_FORMAT_24.md`。
- [x] 检查画布对象不覆盖顶部栏。
- [x] 同步本轮源码、素材、证据截图和 APK 到原项目。

## D. 同步记录（2026-10-30）

- 从开发副本同步：`MainActivity.kt`、`TOOLBAR_PARITY.md`、`WORK_CHECKLIST.md`（新增）、四张文本参数证据截图。修改前的原文件备份在 `deepseek\.tmp\original-sync-backup-2026-10-30\`。
- 发布 APK：`releases/lingye-v0.15-text-toolbar-release-20261030.apk`（SHA-256 `BE3A9DF98C5647391AA6E71703E5EDDB54103A24B4C4C318A0143FF6CBCF2259`）。
- 未覆盖原项目的 `AGENTS.md`、`.gitignore`、`start-tablet-emulator.ps1` 和根目录已有截图。
- 上述 B 节未完成项未在本次同步中实现，保持 `PARTIAL`/`MISSING`。

### 2026-10-30 标题复检

在模拟器复检 H1–H6 保存/重开时发现两个缺陷，均已修复并实测：

1. 标题块中通过斜杠菜单转换为其他块类型会崩溃（`IllegalArgumentException: 不支持的文档块类型`，见 `evidence/heading-slash-crash-before.log`）。修复方式：`Page.kt` 新增 `HeadingBlocks.level` 识别 `heading`/`heading1`–`heading6`，块类型转换与文字样式判定改为按级别接受；修复后 H3 块菜单显示“更改块类型”和文字样式，斜杠菜单转换正常（`evidence/heading-h3-menu-before.png` 为修复前，`evidence/heading-h3-menu-after-fix.png`、`evidence/heading-h3-change-type-menu.png`、`evidence/heading-slash-convert-after-fix.png` 为修复后）。
2. Markdown 导入导出丢失标题级别。修复后按级别导出 `##`–`######`（Markdown 最多六级，H6 与 H5 共用最深一级），导入恢复对应级别；旧 `heading` 类型行为不变。

验证：`HeadingBlocksTest`（8 项）覆盖识别、保存重开、转换、格式与 Markdown 往返；模拟器完成创建 H1–H6、强停重启、H3 转段落、H6 斜杠菜单转换（`evidence/heading-reopen-after-fix.png`）。发布 APK：`releases/lingye-v0.15-heading-levels-release-20261030.apk`（SHA-256 `B07DF35F54F3F568D90DFFBA6E5ADAFAAAC0F7009B247D15092882109966DFCE`）。

### 2026-10-31 Frame 属性、导航与层级

新增 `FrameGeometry.kt`（纯逻辑，可单测）并接入画布：

- `FrameSemantics.hit`：Frame 仅在边框带和标题条命中，内部空白点击穿透到成员对象。
- `Page.frameMembers`：按“边界完全位于 Frame 内”收集图形、图片、笔迹和两端未绑定的连线；`moveSelections`/`duplicateSelections` 通过 `withFrameMembers` 携带成员。
- 绘制改为先画 Frame 再画其他对象，Frame 背景使用 `Shape.color`。
- 选中 Frame 显示专属工具条：重命名、背景色、定位、Frame 导航、复制、删除；“更多 → 视图”新增 Frame 导航入口。

模拟器实测：16:9 创建、重命名 Hero、背景色、拖动携带内部矩形且外部矩形不动、内部空白点击穿透、复制包含成员、导航列表“Hero · 1 个对象”与定位拟合、强停重开后名称/颜色/成员位置/导航保持。证据见 `evidence/frame-*.png`。官方完整语义（嵌套、删除联动、演示模式、大纲目录、持久化父子关系）未实现，状态保持 `PARTIAL`。

发布 APK：`releases/lingye-v0.15-frame-release-20261031.apk`（SHA-256 `8E9D9A5316449A42E21E164BBAA525CC23688938AC5C5C03CC75B876328C6E48`）。

### 2026-10-31 链接已有文档

新增 `PageLinks.kt` 并在画布接入：

- 链接形状 `pageLink` 在 `body` 保存目标页面 UUID；标题渲染时从页面列表实时解析，重命名自动跟随。
- `linkToolsMenu` 的“链接已有文档”由禁用改为打开页面选择器，选中后创建链接卡片。
- 双击卡片或选中后点击工具条链接按钮调用 `openLinkedPage`：按内容选择文档/白板视图；返回键经 `returnToOriginPage` 回到来源页面与来源视图。
- 目标被移入回收站或缺失时卡片显示“目标页面不存在”，恢复后自动恢复；链接随现有格式 22 保存，无迁移。

模拟器实测：选择 AlphaPage 创建 → 双击打开其文档 → 返回 FrameTest 白板 → 目标重命名为 AlphaRenamed 后卡片跟随 → 移入回收站显示失效 → 恢复后恢复显示 → 强停重开链接保持。证据见 `evidence/linked-doc-*.png`。未实现反向链接、大纲目录、行内链接与引用计数，保持 `PARTIAL`。

发布 APK：`releases/lingye-v0.15-linked-doc-release-20261031.apk`（SHA-256 `CBE579B0DFC0B37ACC1C76BF94860D35EC9EC17723A78971BAD7719D28A9D7B1`）。

### 2026-10-31 底部工具栏统一与去重

核对：BlockSuite commit `5cb5cb6` 的底栏 senior/quick tool 源码（`raw.githubusercontent.com` 公开文件）、AFFiNE 发布 API（最新稳定版 `v0.27.5-beta.2`，无工具栏专项条目）、`TOOLBAR_INVENTORY.md` 记录的完整来源。

实现：

- `VisualDesign.kt`：`ToolIconButton` 与 `IllustratedToolButton` 统一 44×44dp、12dp 圆角、PrimarySoft 选中背景；线型图标 22dp、插画 32dp。
- `MainActivity.kt`：新增 `CanvasParamSurface`、`CanvasParamWidthDot`，统一 Frame/连接/形状/画笔/文本/多选参数条；Frame 比例改用 `CanvasParameterChip`；画笔颜色与连接线、形状共用 `CanvasParameterColor`；文本条“T”改为“正文”。
- 去重：More 删除基础图形与内容分区；思维导图删除“文字”；链接删除“添加页面卡片”并改为直接打开选择器；内容菜单删除“文本框”；文本条删除“图片”。

模拟器实测各参数条与菜单，证据见 `evidence/toolbar-*.png`。官方把参数放在按钮滑出菜单、Note/Template 工具等差异记录为 `PARTIAL`。

发布 APK：`releases/lingye-v0.15-toolbar-unified-release-20261031.apk`（SHA-256 `2B18558B1C42D9C824804D9D4A43B629C114425AAAF05CC419B848C63BD3A8D5`）。

### 2026-10-31 区域橡皮

核对官方 BlockSuite `5cb5cb6` 的 `gfx/brush/src/eraser-tool.ts`：官方橡皮拖动时用 16px 宽的橡皮路径收集相交元素，松手后整体删除；本实现据此新增区域模式。

- `AreaEraser.kt`：线段与元素相交判定（图形/图片用膨胀矩形裁剪、笔迹用点-线段与线段-线段距离、连线用采样点）、按类型与索引安全删除、锁定对象跳过。
- `MainActivity.kt`：橡皮参数条新增“整笔/区域”chip；区域模式下拖动记录命中集合并显示灰色轨迹，松手删除并记入一次撤销；模式写入 `eraser_area` 偏好。
- 保留差异：官方拖动时把目标降为 0.3 透明度，本实现以轨迹预览替代；不擦除画布背景；不做局部像素擦除。

模拟器实测：区域拖过删除笔迹与左侧矩形、右侧矩形保留；撤销恢复、重做再次删除；强停重开后擦除结果与区域模式保持；整笔模式仍只删除笔迹。证据见 `evidence/eraser-*.png`。

发布 APK：`releases/lingye-v0.15-area-eraser-release-20261031.apk`（SHA-256 `0531032B9EDD926B1DF0622A9D4489A47660C2BF23FF06FBBB024D27806D99CD`）。

### 2026-10-31 节点级思维导图

核对官方 BlockSuite `5cb5cb6` 的 `gfx/mindmap` 工具栏与拖拽交互：官方布局为左/放射/右三种、四套主题，节点可拖拽改接。本实现按当前平板交互改为菜单式改接与五种本地布局。

- `Mindmap.kt`：节点为 `mindNode` 形状，`cardKind` 保存 `mindmap:<布局>:<导图 id>`；边为 `mm-` 前缀的绑定连线；支持创建根节点、添加子/同级节点、改接（含环路与跨图校验）、删除子树、五种布局自动排布（根节点位置保持）。
- `MainActivity.kt`：创建菜单使用布局引擎；选中节点显示工具条（添加子节点、添加同级、编辑、切换布局、改接上级、删除）；双击节点原位编辑；删除前确认并显示节点数；工具条按视口宽度裁剪避免溢出。
- 保留差异：拖拽改接、主题样式、节点折叠未实现；官方三种布局与本实现五种布局的命名不同但语义覆盖。

模拟器实测：创建向右树、添加子节点、切换向下树、把分支3改接到分支2、删除分支2（3 个节点确认）、撤销恢复、双击把节点文字改为 CoreIdea、强停重开后布局/结构/文字保持。证据见 `evidence/mindmap-*.png`。

发布 APK：`releases/lingye-v0.15-mindmap-nodes-release-20261031.apk`（SHA-256 `B5AF81B76BBC94B873E7B3C1845AD46C3B39EDC2CB50A46B8BF3A88432EDE86E`）。

### 2026-10-31 插画图标重设计

用户要求重新设计工具栏插画图标组，先给方向与预览。三个方向稿（纸片 + 单点强调 / 彩色贴纸 / 墨线 + 单点填色）保存在 `docs/affine-audit/icon-directions-2026-10-31.html`，按 34dp 实际尺寸与放大稿预览；用户选定方向 A。

- 六个按钮重绘为 VectorDrawable：`canvas_tool_text_v2.xml`、`_pen_v2`、`_eraser_v2`、`_shape_v2`、`_mindmap_v2`、`_content_v2`（64 视口 / 34dp）。
- 统一白纸片、`#D9DCE3` 描边、单点强调色（黄/蓝/珊瑚/薄荷），纸影用 10% 底层偏移模拟；选中态由按钮 `PrimarySoft` 背景承担。
- 旧 PNG 保留但不再引用，便于回退；思维导图按钮不再借用 `canvas_tool_link` 素材。

模拟器实测工具栏实际尺寸与选中态，证据见 `evidence/toolbar-icons-paper.png`、`toolbar-icons-paper-active.png`。200 项单元测试与 Release 构建通过。

发布 APK：`releases/lingye-v0.15-icons-release-20261031.apk`（SHA-256 `9D54ABB23CA381CB3F0E5D3E1A558B97F03EB7C8CEDA09F7B6B65F372B715CF3`）。

### 2026-10-31 键盘快捷键与提示

核对官方工具提示：BlockSuite `5cb5cb6` 中 Hand H、Select V、Frame F、Connector C、Link @、Pen P、Eraser E、Shape S、Note N；官方提示为“名称 + 键帽”的深色浮层（`tooltip-content-with-shortcut`）。

- `CanvasShortcuts.kt`：纯逻辑键位映射（工具键、Ctrl+Z/Shift、Ctrl+Y、Ctrl+A、Delete/Backspace、Esc、缩放与方向键），附单元测试。
- `MainActivity.kt`：画布可获得焦点并处理 `onPreviewKeyEvent`；文本/卡片编辑或菜单打开时不拦截按键；工具键、撤销/重做、删除、全选、Esc、方向键（Shift 微调）、Ctrl + =/-/0 缩放；点击画布重新聚焦。
- `VisualDesign.kt`：`ShortcutTooltipHost` 在悬停 320ms 或长按后显示深色提示与等宽键帽；`ToolIconButton`/`IllustratedToolButton` 改为 combinedClickable，长按只显示提示、不触发点击；底部栏传入官方快捷键。
- 验证限制：模拟器 `input motionevent` 不支持 HOVER 动作，且注入悬停被策略阻止；鼠标/手写笔悬停路径未做设备验证，已记录为 PARTIAL；长按提示与全部键盘行为已实测。
- 发现并修复：注入的 Ctrl+Z 没有 `utf16CodePoint`，最初按键映射取字符失败；改为按 `event.key` 映射字母后撤销/全选正常。

证据：`evidence/shortcut-pen.png`、`shortcut-escape.png`、`shortcut-frame.png`、`shortcut-undo-before.png`、`shortcut-undo-after.png`、`shortcut-select-all.png`、`tooltip-long-press.png`。

发布 APK：`releases/lingye-v0.15-shortcuts-release-20261031.apk`（SHA-256 `09B46F20F926CCA5F265B0986B7399CCC9D33D3542961CCC90364E961972AC24`）。

### 2026-10-31 画笔类型与参数集

核对官方 `gfx/brush/src/toolbar/components/pen/pen-menu.ts` 与 `model/src/consts/line.ts`：画笔/高亮两种类型、画笔粗细 2/4/6/8/10/12、高亮 10/14/18/22/26/30、高亮颜色 alpha 0.3、⇧P 选高亮。

- `BrushSettings.kt`：两套粗细、十色短色板、30% 透明度换算与绘制参数选择；高亮 alpha 写入笔迹颜色，沿用页面格式 22。
- 画笔参数条新增“画笔/高亮”chip，颜色与粗细按类型独立记忆；`P`/`⇧P` 切换，画笔按钮提示随类型显示；类型与高亮参数写入偏好，跨重启保持。
- 未实现：压感、平滑/预测、透明色与专用混合模式。

模拟器实测：画笔条六档细点、高亮条六档粗点、半透明黄色宽笔触、⇧P 切换、强停重开后笔迹与类型保持。证据见 `evidence/brush-*.png`。

发布 APK：`releases/lingye-v0.15-brush-types-release-20261031.apk`（SHA-256 `6F34D2261B0BE4337A1C60E418D0CBD4CB4509B1FD3769AC1AA288B055F54245`）。

### 2026-10-31 参数子集窄窗口与无界边界

复核对象：底部浮动参数条（Frame 比例、连线、形状、画笔/高亮、橡皮、文本、多选）与画布对象工具条，在 640dp 窄窗口、2560×1600 宽屏与 200% 系统字体下的显示。

- `CanvasLayout.kt`：新增 `parameterBarMaxWidth(viewportWidthDp, margin = 8f)`，返回视口宽度减两侧边距（最小 240dp）；`CanvasLayoutTest` 新增 2 项对应测试。
- `MainActivity.kt`：Frame、连线、形状、画笔/橡皮、多选五类参数条统一加 `Modifier.widthIn(max = paramBarMaxWidth)`，内容行保留或补上 `horizontalScroll`，超出宽度时在条内滑动而不是被屏幕裁掉；文本条原有的“参数区滚动 + 关闭固定”保持不变。
- 画布对象工具条继续用 `CanvasLayout.toolbarX` 按视口裁剪横向位置。

模拟器实测（640dp，1280×1600）：画笔/高亮参数条起始只显示到黑色色点，横向滑动后“仅手写笔”完整出现，参数条右端圆角与边界位于视口内（`evidence/narrow-param-pen-start.png`、`narrow-param-pen-scrolled.png`）；连线条到“颜色 10”可滑动（`narrow-param-connector.png`）；形状条五项与十色完整（`narrow-param-shape.png`）；文本条“关闭”固定、其余项滚动（`narrow-param-text.png`）。

200% 系统字体（2560×1600）：画笔条与文本条均完整显示，无 chip 或按钮裁切（`evidence/font200-param-pen.png`、`font200-param-text.png`）。

发现并已修复（见下节）：640dp 且侧栏展开时首页标题与文档数折行（`narrow-home-header-wrap.png`）；文档编辑区窄宽度下块类型工具条右侧裁切（`narrow-document-block-toolbar-clip.png`）。

发布 APK：`releases/lingye-v0.15-narrow-params-release-20261031.apk`（SHA-256 `8F53531216F2C383CCBD476D1C877ECE883E502CBECC954EC451E66104A88DBF`）。

### 2026-10-31 窄窗口首页与文档块工具条

- `AllDocumentsHome.kt`：标题与文档数限制为单行，超出省略；用 `BoxWithConstraints` 判断可用宽度，小于 560dp 时头部改为两行——第一行保留标题/计数与多选按钮，第二行右对齐视图切换、排序、新建文档；宽屏保持原单行布局。
- `MainActivity.kt`：文档块类型条由横向滚动 `Row` 改为 `FlowRow`（2dp 间距），窄内容宽度自动换行，不再出现右缘半截图标。
- 实测（640dp，侧栏展开）：首页“全部文档 / 191 个文档”均单行，第二行操作完整（`evidence/narrow-home-header-two-row.png`）；空白文档块条换为两行，段落/标题/待办/图片/数据库/共享块/更多块全部可见（`narrow-document-block-toolbar-wrapped.png`）。
- 640dp 侧栏收起时头部仍为单行（`narrow-home-header-single-row.png`）；2560×1600 宽屏下头部与块条保持单行，无回归（`wide-home-header-single-row.png`、`wide-document-block-toolbar-single-row.png`）。
- 构建：214 项单测 0 失败，Release APK 已安装并完成上述验证。

发布 APK：`releases/lingye-v0.15-narrow-header-release-20261031.apk`（SHA-256 `E46E996AC37D0AACE7F1311E4A81E05C55F2D50ABA12D1B42DE2794E0A1F53B9`）。

### 2026-10-31 Frame 嵌套与演示模式

核对官方 BlockSuite `5cb5cb6` 的 `packages/affine/blocks/frame`（浅克隆）：Frame 通过 `childElementIds` 持有子元素，可嵌套；官方演示为 `frameNavigator` 工具配合视口缩放与 `presentation-toolbar`（填充/适应、上一帧/下一帧、退出）；Frame 工具条的 Ungroup 先清空子元素关系再移除 Frame 本身，内容保留。

- `FrameGeometry.kt`：`frameMembers` 纳入完全包含的嵌套 Frame；新增 `fill`（覆盖视口）与演示专用缩放范围 `PRESENTATION_MIN_SCALE`/`PRESENTATION_MAX_SCALE`；`FrameSemanticsTest` 新增嵌套成员、外层移动一次携带内层、fill 裁剪与缩放上限 4 项断言。
- `MainActivity.kt`：绘制时按面积从大到小排列 Frame，保证外层先画；Frame 工具条、“Frame 导航”每项与“更多 → 视图”新增演示入口；演示中隐藏底部工具栏与右上全屏按钮，底部显示“上一帧 / 序号 / 标题 / 适应或填充 / 下一帧 / 退出”浮条；方向键、PageUp/PageDown、空格翻页，Esc 与系统返回退出；退出恢复进入前的缩放、平移、工具与全屏状态；演示中拖动/捏合可平移缩放，缩放范围为 0.1–16，帧标题在演示中隐藏。
- 删除 Frame 仍只移除 Frame，内容保留，与官方 Ungroup 语义一致；未实现持久化父子关系、文档大纲目录、演示顺序与演示设置，保持 `PARTIAL`。

模拟器实测：在 600×600 像素 Frame 内创建最小 Frame 后，导航显示外层“3 个对象”、内层“0 个对象”（`evidence/frame-nested-navigator.png`）；演示填充铺满视口（`frame-presentation-fill.png`），→ 切到 2/2（`frame-presentation-second.png`），Esc 退出恢复工具栏与视图（`frame-presentation-exit.png`）。测试创建的 Frame 已撤销，FrameTest 页面恢复为两个 Hero Frame。外层移动携带内层由 `FrameSemanticsTest.movingOuterFrameMovesNestedFrameAndContentsOnce` 覆盖。

发布 APK：`releases/lingye-v0.15-frame-nested-release-20261031.apk`（SHA-256 `A18B5751C298B168F9C13BCB80C773F15F9880BBBDDAC357CD6CFD2422ECF675`）。

### 2026-10-31 设置弹窗与全局界面整理

- 设置：由整页改为 `Dialog` 模态弹窗（占用窗口 94%×90%，圆角 24dp），点击侧栏“设置”时不再切换或退出当前画布；弹窗保留原有分类与全部设置，新增工作区卡片（应用启动图标 + 灵页/离线创意空间）与右上关闭按钮；新增“键盘快捷键”分类，按官方键位列出工具键与命令键，注明暂不支持自定义。深色主题、外部链接打开方式与侧边栏链接文档列表未实现，外观页保留仅浅色主题说明（B 节记录）。
- 顶栏：移除自动保存的“已保存/保存中”胶囊，文档与白板共用顶栏不再显示保存状态。
- 留白：文本参数条去除 `fillMaxWidth`，参数区使用 `weight(1f, fill = false)`，内容不足时与“关闭”紧贴，超出时在条内滚动；底部工具栏改为按内容自适应宽度并始终可横向滚动，移除内容后的尾部空白与 More 与撤销之间多余的 4dp 间隔。
- 侧栏：隐藏侧栏按钮改为无背景图标，尺寸 38dp、与下方“+”按钮同宽同中心；左上角品牌图标替换为 `ic_launcher_background` + `ic_launcher_foreground` 组成的应用启动图标（`evidence/sidebar-toggle-aligned.png`、`sidebar-brand-icons-aligned.png`）。

模拟器实测：2560×1600 下设置弹窗、快捷键页与关闭返回（`evidence/settings-modal-wide.png`、`settings-shortcuts.png`）；640dp 下弹窗完整（`settings-modal-narrow.png`）；文档顶栏无保存胶囊（`editor-header-no-save-pill.png`）；宽屏文本条与底栏紧贴内容（`toolbar-content-width-wide.png`），640dp 下文本条可滚动且关闭固定、底栏完整（`toolbar-content-width-narrow.png`）；侧栏启动图标与无背景隐藏按钮（`sidebar-launcher-icon-and-plain-toggle.png`）。

发布 APK：`releases/lingye-v0.15-settings-ui-release-20261031.apk`（SHA-256 `3545BB3E3309E81B5B4D5A82B0B98C3DF6002F17453DD75643FDE8F1726F8234`）。

侧栏对齐补充：隐藏/展开按钮改为 38dp 触控区，图标与下方“+”按钮同中心；发布 APK：`releases/lingye-v0.15-sidebar-align-release-20261031.apk`（SHA-256 `0B7EB4C55033418BEBE3C76AD134F03DA91BF0E6B06286A3ABD757B7416A5A89`）。

### 2026-10-31 设置专项改进

核对 AFFiNE `8cef09e1dd62b47a3cbc9f352aa10b3a90c75d34`（canary，2026-10-27）的 `desktop/dialogs/setting` 与 `setting-components`：官方为左侧“设置标题 + 账户卡 + 常规/工作区两组导航”，右侧统一为名称、说明、右侧控件；条目覆盖外观、编辑器、快捷键、数据/储存、工作区与关于。离线单机版不移植账号、通知、会议、计费、成员、集成、云嵌入。

- 结构：设置弹窗导航分为“常规”（外观、编辑器、画布、键盘快捷键、数据、关于）与“工作区”（工作区信息）；统一 `SettingsHeading`/`SettingsGroup`/`SettingsControlRow`/`SettingsStackRow`/`SettingsToggleRow`，分区、说明、控件间距一致；左侧显示应用启动图标与可编辑工作区名称。
- 帮助：每个条目名称后的圆形“?”打开 280dp 帮助浮窗，说明作用、影响范围与建议值；覆盖主题、语言、动效、图片、侧栏链接、字号/字体、默认打开方式、日期标题、画布颜色、网格/吸附、连线、画笔、备份/导入导出、页面格式与快捷键分组。
- 新增能力：`WorkspaceSettings` 增加 `workspaceName`/`workspaceSubtitle`、`editorFontFamily`（Sans/Serif/Mono）、`showGrid`/`gridSize`（20/28/36）/`snapToGridDefault`、`showLinkedDocs`，全部写入 SharedPreferences；侧栏品牌实时更新；文档标题、正文与文本块应用字体；画布网格与吸附使用设置值；侧栏在开关开启时列出当前页链接文档（验证用链接已撤销）。
- 快捷键页按“工具/命令/对象操作”分组，保留只读说明；数据与关于补充帮助文案。
- 单元测试新增 `WorkspaceSettingsTest` 4 项；总计 221 项 0 失败。

模拟器实测：宽屏与 640dp 设置弹窗（`evidence/settings-overhaul-wide.png`、`settings-overhaul-narrow.png`）；帮助浮窗（`settings-help-popover.png`）；工作区信息（`settings-workspace.png`）；编辑器字体（`settings-editor.png`、`editor-serif-title.png`）；网格设置与开关前后（`settings-canvas-grid.png`、`canvas-grid-off.png`、`canvas-grid-on.png`）；快捷键分组（`settings-shortcuts-groups.png`）；侧栏链接文档（`sidebar-linked-docs.png`）。测试后将工作区名称恢复为“灵页”、字体恢复无衬线、网格恢复显示、测试链接撤销。

发布 APK：`releases/lingye-v0.15-settings-pro-release-20261031.apk`（SHA-256 `E35B737BECD1C63E5FBEE99660DFB35E25AD72F7263A649914BFF164643978B4`）。

### 2026-10-31 设置弹窗按官方布局校正

对照用户提供的 AFFiNE 设置截图与 `desktop/dialogs/setting` 源码重新校正布局与功能布局：

- 外壳：左栏浅灰 `#F7F7F9` 通栏（“设置”标题、工作区卡 + “本机”徽标、常规/工作区分组），右栏纯白内容与右上关闭按钮；取消旧版内层卡片与阴影。
- 导航：常规（外观、编辑器、键盘快捷键、关于灵页）+ 工作区（偏好设置、储存）；画布默认值合并到编辑器页的“文档/白板”标签。
- 内容：页面标题改为“外观设置/编辑器设置/偏好设置/储存”等；统一名称/说明左对齐、控件右对齐（主题 320dp、语言与链接 240dp），小于 460dp 自动堆叠；分区横线与官方一致。
- 功能：主题显示跟随系统/浅色/深色三项（前两项禁用并说明原因）；语言改为下拉框；新增“打开页面链接”返回来源/直接打开设置（`linkReturnToSource`，已接入 `openLinkedPage`）；平滑图片、动效、侧栏链接文档、工作区信息、字体、网格、快捷键与储存页保留。
- 页脚：右栏底部固定“灵页 · 离线优先的文档与白板工作区”，不添加外部链接。

模拟器实测：2560×1600 外观页、编辑器文档/白板、偏好设置、储存页（`evidence/settings-official-wide.png`、`settings-official-editor.png`、`settings-official-editor-whiteboard.png`、`settings-official-preferences.png`、`settings-official-storage.png`）；640dp 响应式堆叠（`settings-official-narrow.png`）。构建 221 项测试 0 失败。

发布 APK：`releases/lingye-v0.15-settings-official-release-20261031.apk`（SHA-256 `07CB6CB6DDBF94DD32CF10F68E46AA2A9F65598651B50208B7178531E767F1A0`）。
### 2026-10-31 编辑器与快捷键板块核对补齐

核对 AFFiNE `8cef09e` 的 `editor/general.tsx`、`editor/page.tsx`、`editor/edgeless/*` 与 `shortcuts/index.tsx`：

- 编辑器 General：字号范围扩为 12–24；新增日期格式（4 种）与代码块行号；字体、日期、行号均持久化。
- 编辑器 Page：新增页面宽度标准/全宽（标准最大 860dp 居中）与显示页面信息开关；页面选项菜单的信息入口保留。
- 编辑器 Edgeless：新增新 Frame 背景色与连接线默认线宽；既有背景/卡片/形状/网格/画笔默认值保留。
- 快捷键：页面新增“选择与编组”组；画布支持 Ctrl+D 复制、Ctrl+G/Shift+G 编组/取消编组、Ctrl+]/[ 置顶/置底、Ctrl+Shift+L/R/T/B 四向对齐；硬件键盘映射补齐 D/G/B/R 与方括号。
- 未实现并在页面/清单中明确：新建文档 Ask 模式、页面图标、双向链接、深色主题、滚轮缩放、连接线粗糙/虚线、Note/文本/思维导图主题、应用级 Ctrl+K/N// 与文档编辑粗体/斜体/标题快捷键、快捷键自定义。

模拟器：标准/全宽文档、隐藏页面信息、代码块行号 1/2、日期格式菜单、快捷键新分组（`evidence/settings-editor-general.png`、`settings-editor-page.png`、`settings-editor-date-format.png`、`editor-standard-width.png`、`editor-full-width.png`、`editor-code-line-numbers.png`、`settings-shortcuts-expanded.png`）。测试后恢复默认并删除测试代码块；222 项测试 0 失败。

发布 APK：`releases/lingye-v0.15-editor-shortcuts-release-20261031.apk`（SHA-256 `D9ED051F000A2BA81395448BD19DFBB72EC514641157B80BE080135C1AABDDCF`）。
### 2026-10-31 编辑器适用化组件与视觉重设计

- 官方编辑器项适用化：白板默认主题 → “画布主题 浅色/深色”（深色底 `#1C1D22` 与浅色网格）；连接线 rough → “手绘连线”绘制抖动；文本默认样式 → 默认文字颜色/字号（12–32）/对齐，新文本框使用。
- 视觉修正：外观页主题由三段（含禁用项）改为单一“浅色”状态块；颜色选择器从颜色胶囊改为白卡 + 14dp 色点 + 深色标签，修复墨黑/深灰标签不可读；日期格式改内联芯片，避免下拉遮挡相邻行；右栏内容结束留白增至 76dp；代码块行高与行号对齐；编辑器说明文案缩短，细节移入帮助浮窗。
- 不适用或未实现项继续不提供伪 UI：AI、拼写检查、中键粘贴、页面图标、双向链接、Ask 模式、滚轮缩放；见 `SETTINGS_IMPROVEMENTS.md` 7.1。

模拟器：浅色主题状态块、画布主题分段、深色画布、日期芯片、颜色选择器、文本默认样式、手绘连线布局（`evidence/settings-theme-light-only.png`、`settings-canvas-theme.png`、`editor-dark-canvas.png`、`settings-date-format-chips.png`、`settings-color-choices.png`、`settings-text-defaults.png`）。223 项测试 0 失败；测试后恢复浅色画布、关闭日期命名与代码行号。

发布 APK：`releases/lingye-v0.15-editor-polish-release-20261031.apk`（SHA-256 `A8D616C5D50AF8AF05D1E90BED930CAF63581A4CA91C0168A90D7A55DFE375F7`）。
### 2026-10-31 全局色圈、取色器、笔记样式与按钮缩放

- 颜色：新增 `ColorPicker.kt`（经典十色色圈、最多 12 个自定义色卡、RGB/HSV 折叠式取色器、Hex 预览、保存/清空色卡）；设置页全部颜色行、画布形状/连线/画笔参数条、图形填充/边框、Frame/文本/笔迹颜色菜单统一接入。
- 笔记：核对官方 `edgeless/note.tsx`，新增背景、圆角（无/小/中/大/超大 = 0/8/16/24/32）、阴影（无/卡片/贴纸）、边框样式（实线/虚线/无）与厚度（2–12）以及实时预览；新笔记使用背景/圆角/边框，阴影按全局渲染。
- 缩放：官方“滚轮缩放”改为“按钮缩放”开关，画布右下角显示 `− 百分比 +`，百分比复位，隐藏于演示模式。
- 构建：非混淆 release 触发 ART VerifyError，改为 release 启用 R8 并新增 `app/proguard-rules.pro`（`-dontwarn javax.annotation.**`、`org.slf4j.**`）；R8 release 已实测启动、设置、画布、取色与 Lottie 文件夹图标正常。

模拟器：设置色圈、RGB/HSV 折叠区、画布颜色菜单、笔记样式预览、按钮缩放 125%/复位（`evidence/settings-color-circles.png`、`color-picker-rgb.png`、`color-picker-hsv.png`、`canvas-color-menu-circles.png`、`settings-note-style.png`、`canvas-zoom-buttons.png`、`settings-note-and-zoom.png`）。自定义色卡可保存并清空；未单独做重启复检，持久化代码已写入 SharedPreferences。228 项测试 0 失败。

发布 APK：`releases/lingye-v0.15-color-note-release-20261031.apk`（SHA-256 `13D08D6350865D19E136ED2D68D9F0A405A5073A60353784B9EA8E461A88D058`）。

### 2026-10-31 紧凑控件全局调整与笔记预览补充

- 滑块：新增 `CompactSlider`（26dp 高、3dp 轨道、8dp 端点内缩、10dp 实心主色圆点，无白色外环），支持点按/拖动、步进吸附与无障碍进度语义；替换设置页字号、笔记边框厚度、默认文字字号、画笔粗细，取色器 RGB 滑块与图片裁剪四个滑块；全局不再使用 Material `Slider`。
- 分段选择：按用户提供的官方截图改为右侧紧凑灰色胶囊（`#F4F4F6`、3dp 内边距、8dp 圆角）＋白色选中片（1dp 阴影、深色文字），取消等分布局与蓝色文字；圆角/阴影/边框、正文字体、页面宽度、默认打开方式、画布主题、网格大小、连线路径/终点/线宽、文本对齐全部改为右侧控件行；窄窗口自动堆叠为左对齐胶囊。
- 紧凑按钮：主工具按钮 44→40dp（图标 22→20、文字宽 66→60），插画工具 44→40dp（图 32→28），圆形小操作 38→34dp（图标 18→16），`IconTextAction` 由 Material 48dp 改为 30dp 紧凑按钮（图标 16dp），画布参数 chip 34→30dp、粗细点 32→28dp、参数面板圆角 18→14dp/阴影 12→8dp、工具分隔线 27→22dp；设置导航、下拉框、动作按钮、色圈与“+”按钮同步收紧。
- 笔记预览：增加独立“笔记预览”小标题；预览卡显示示例标题、说明与待办行，并按当前背景、圆角、阴影、边框样式与厚度实时绘制；虚线边框用 `PathEffect` 绘制，无边框与实线分别处理。
- 构建与测试：228 项单元测试 0 失败；R8 release 安装运行正常。
- 模拟器实测：2560×1600 设置页紧凑分段、颜色取色器紧凑滑块、笔记预览、画布紧凑工具栏；1280×1600（640dp）下分段控件不溢出、自动堆叠（`evidence/settings-compact-segmented.png`、`settings-compact-note-preview.png`、`color-picker-compact-slider.png`、`canvas-compact-toolbar.png`、`settings-compact-narrow.png`）。测试中误触的默认字号已恢复为 20px。

发布 APK：`releases/lingye-v0.15-compact-ui-release-20261031.apk`（SHA-256 `4F4D27BFF099B72747F7497AF7E6468A11B9698DBDAFBE3DDA1E982E5A0DEAC5`）。

### 2026-10-31 设置弹窗收紧、配色方案行删除与开关重设计

- 外观页：删除“主题 / 配色方案 / 浅色”整组以及底部的浅色主题说明；首组改为“语言”，直接显示“显示语言”，不再保留无实际作用的主题状态块。
- 弹窗外壳：宽高由 94%×90% 调整为 98%×94%，圆角 24→20dp、阴影 24→16dp；侧栏 280→248dp（窄屏 192→176dp），侧栏内边距与内容左右/上下内边距同步收紧；右栏内容左右留白宽屏 48/76→28/44dp，窄屏 24/32→18/46dp，顶部 28→18dp、底部 12→10dp，页脚 12→8dp。
- 关闭按钮：改为 32dp 白色圆形按钮 + 1dp 边框与 4dp 阴影，并预留右栏 44–46dp 末端空间，避免 640dp 下与语言下拉框重叠。
- 开关：新增 `CompactSwitch`（38×22dp 胶囊、16dp 白色圆形滑块、开=主色/关=`#D8D8E0`、160ms 颜色与弹簧位移动画），替换设置页全部 `Switch` 与取色器“保存到自定义色卡”开关；设置行整行可点按（`toggleable` + `Role.Switch`），帮助“?”按钮保留独立点击。修复旧的关闭态滑块使用浅色 `outline`、在浅灰轨道上几乎不可见的问题。
- 测试：228 项单元测试 0 失败；R8 release 安装运行。实测宽屏与 640dp 设置弹窗、开关开/关/整行切换；测试中误触的画笔粗细已恢复为 1px，页面宽度恢复“标准”，默认打开方式恢复“文档”，默认字号保持用户值 19px。
- 证据：`evidence/settings-dialog-tight-wide.png`、`settings-dialog-tight-narrow.png`、`settings-switch-off.png`、`settings-switch-on.png`、`settings-switch-row-toggle.png`。

发布 APK：`releases/lingye-v0.15-settings-tight-release-20261031.apk`（SHA-256 `B4E3ED69FA28E90EA80EDAF3522A0B762791C4D9E9533CAD6B75110489AEA028`）。

### 2026-10-31 纤细黑白色切换控件与弹窗最终收紧

- 弹窗：94%×90% → 99.8%×95%，圆角 24→16dp、阴影 24→12dp；左右几乎贴边，上下保留约 20dp 避让系统栏。侧栏内边距收至 10/10dp（窄屏 8/10dp），内容顶部 28→12dp、底部 12→8dp、页脚 12/6dp，关闭按钮外边距 14→8dp。
- 分段切换：按用户提供的 Free / Premium / Monthly / Annual 参考重做全局 `SegmentedChoice`——白色 34dp 轨道、1dp 浅灰描边、5dp 柔和阴影；选中项为 28dp 黑色胶囊、白色 SemiBold 文字，右缘白色箭头缺口带 0.6dp 深色描边；未选中文字 `#9C9C9C`。日期格式单选 chip 同步使用白底细描边/黑色选中。
- 开关：`CompactSwitch` 改为黑白高对比，开=`#0B0B0B` 轨道＋白色滑块，关=`#E9E9EC` 轨道＋1dp `#D5D5DA` 描边＋白滑块（0.5dp `#B8B8C0` 描边）；整行仍可点按，关闭态滑块清晰可见。
- 测试：228 项单元测试 0 失败；R8 release 安装运行。宽屏与 640dp 实测分段控件、五档圆角与开关，无裁切；误触的笔记边框厚度已恢复为 2px，画笔粗细保持 1px，默认字号保持 19px。
- 证据：`evidence/settings-dialog-full-wide.png`、`settings-dialog-full-narrow.png`、`settings-toggle-slender-wide.png`、`settings-toggle-slender-narrow.png`、`settings-switch-black-white.png`。

发布 APK：`releases/lingye-v0.15-slender-toggle-release-20261031.apk`（SHA-256 `3EE9505AB9F56615A595AF42B4CCD1E9D69162D5D659F60177C9D792DA1D7D70`）。

### 2026-10-31 白色卡片切换与弹窗等比缩小

- 弹窗：改为 **92%×92% 等比缩小**，圆角 18dp、阴影 16dp；只缩放设置弹窗本身，应用主页面、画布和系统显示尺寸保持原样。四边留白一致，640dp 与宽屏均无控件重叠。
- 分段切换：最终按用户指定结构实现——纯白 34dp 卡片（18dp 圆角、1dp `#E4E4E4` 描边、5dp 阴影）；选中项为文字下方黑色 `#0B0B0B` 圆角框，上下白边由 3dp 缩至 2dp，选中文字白色、未选中文字黑色 `#1A1A1A`；移除全部白色箭头、黑色底轨和右侧黑色分组。
- 全局同步：日期格式单选 chip 改为纯白卡片＋黑色选中框；首页列表/卡片视图切换改为纯白卡片＋黑色选中图标。
- 测试：228 项单元测试 0 失败；R8 release 安装运行。宽屏与 640dp 实测弹窗、卡片、首页切换；误触的正文字体恢复“无衬线”、页面宽度恢复“标准”，画笔粗细 1px、笔记边框 2px、默认字号 19px 保持一致。
- 证据：`evidence/settings-white-card-wide.png`、`settings-white-card-narrow.png`、`home-white-card-toggle.png`、`settings-dialog-92.png`。

发布 APK：`releases/lingye-v0.15-white-card-release-20261031.apk`（SHA-256 `71BC90672E6C6D84A5F5283EF9D4495678835B32B4938AD69963011B0DFDB585`）。

### 2026-10-31 极窄窗口与分屏设置导航

- 设置页新增窄窗口单栏模式：弹窗内容宽度小于 520dp（约对应 565dp 以下窗口）时，先显示工作区卡与分类列表；点选分类进入分区详情，详情顶部提供 Canvas 绘制的返回箭头。
- 系统返回：详情页先回分类列表并保留选中项，列表页再关闭弹窗；关闭按钮在列表与详情均固定在右上角。
- 复用结构：抽出 `SettingsWorkspaceHeader`、`SettingsNavGroups`、`SettingsBackButton`、`SettingsCloseButton`；分区内容由同一 `categoryContent` lambda 渲染，窄屏不再维护第二套设置页。单栏导航行高增至 44dp。
- 宽屏（弹窗内容 ≥520dp）保持原两栏布局、间距与关闭按钮位置不变；540dp 窗口因弹窗 92% 缩小后内容为 497dp，自动进入单栏。
- 复核 360dp（720×1600）：首页、文档编辑器正常；白板底栏可横向滚动到末端；设置列表、外观、编辑器、键盘快捷键、储存详情均可读。
- 复核真实分屏（2560×1600，Chrome + 灵页各 640dp）与 640dp、2560×1600：首页与设置布局正常，无控件重叠。
- 测试：228 项单元测试 0 失败；R8 release 安装运行，同一包名保留原数据。
- 证据：`evidence/settings-compact-nav-360.png`、`settings-compact-appearance-360.png`、`settings-compact-editor-360.png`、`settings-compact-shortcuts-360.png`、`settings-compact-storage-360.png`、`settings-compact-detail-540.png`、`settings-split-640.png`、`settings-two-column-640-regression.png`、`settings-two-column-wide-regression.png`、`narrow-home-360.png`、`narrow-editor-360.png`、`narrow-whiteboard-360.png`、`narrow-whiteboard-toolbar-end-360.png`。

发布 APK：`releases/lingye-v0.15-narrow-settings-release-20261031.apk`（SHA-256 `50481C8C1F6F46DD87CC8D3826EF60A1C6C81293AD5ECA92984204AD702C397D`）。

### 2026-10-31 分段切换滑动动画

- 设置页 `SegmentedChoice`：白色卡片内的黑色选中框改为按选项实际测量位置绘制的独立指示器，切换时用 `Animatable` 在 220ms 内以 `FastOutSlowInEasing` 滑动 `left` 与 `width`；首帧直接使用测量目标，避免选中框闪空。
- 文字颜色：按指示器中心是否覆盖当前选项切换白色/黑色，滑动过程中不会出现白字白底或黑字黑底；选项宽度变化时动画目标随之更新。
- 首页列表/卡片视图切换：新增 `AnimatedViewModeToggle`，34dp 黑色指示器在 2dp 内边距的白色卡片内滑动，图标颜色随覆盖切换，静态外观与旧版一致。
- 换行日期格式 chip 仍为静态选中（跨行滑动语义不适用），深色主题仍未实现。
- 验证：系统动画时长临时调至 10× 抓帧，设置页“文档/白板”和首页视图切换均捕捉到黑框位于两选项之间的中间帧；终点位置与静态选中态一致；360dp 下控件无裁切。动画时长已恢复 1×。
- 测试：228 项单元测试 0 失败；R8 release 安装运行。
- 证据：`evidence/settings-segment-animation-mid.png`、`settings-segment-animation-end.png`、`home-view-toggle-animation-mid.png`、`home-view-toggle-animation-end.png`、`settings-segment-360-animation.png`。

发布 APK：`releases/lingye-v0.15-segment-animation-release-20261031.apk`（SHA-256 `DE3D76571907233947FC75D81F1A1EDDBFCB031C0B6450C6E8AD490E466D907F`）。

### 2026-10-31 深色界面主题

- 设置：外观页新增“主题”分组，提供跟随系统/浅色/深色，写入 `theme_mode`，默认跟随系统；深色可手动选择或随系统切换。
- 颜色体系：`LingyeColors` 改为动态令牌（Ink、Muted、Primary、Surface、SurfaceHigh、Sidebar、Subtle、Chip、Line 等），并新增深色调色板；`LingyeTheme` 在浅色/深色 Material 方案间切换。
- 覆盖范围：侧栏、首页列表/卡片、文档、画布工具栏与参数条、设置弹窗/导航/控件、下拉菜单、取色器、概览、加载页、状态提示与系统栏图标。
- 深色反色：分段选中框与开关改为白色主体＋深色文字/滑块；分隔线、边框、选中导航与状态色使用深色令牌。
- 画布内容：页面卡片与链接卡片随主题；笔记/卡片保留用户设置的颜色；Frame 标题按背景亮度选择深/浅文字。画布底色仍由“编辑器 → 白板 → 画布主题”单独控制。
- 验证：浅色主题与改动前一致；深色在宽屏与 360dp 下检查首页、文档、白板（浅色与深色画布）、设置、取色器、概览、卡片与加载页；跟随系统双向切换；深色强停重启保持；R8 release 安装运行。
- 数据：测试中 FrameTest 被误移入回收站，已从回收站恢复，文档数回到 191；画布主题测试后恢复“浅色”，主题恢复“跟随系统”。系统主题切换测试时模拟器曾无响应，重启后数据、设置与页面完整。
- 测试：229 项单元测试 0 失败。
- 证据：`evidence/dark-theme-home-wide.png`、`dark-theme-settings-wide.png`、`dark-theme-editor-settings.png`、`dark-theme-whiteboard-settings.png`、`dark-theme-canvas-light-bg.png`、`dark-theme-canvas-dark-bg.png`、`dark-theme-overview.png`、`dark-theme-cards.png`、`dark-theme-color-picker.png`、`dark-theme-storage.png`、`dark-theme-settings-360.png`、`dark-theme-appearance-360.png`、`dark-theme-system-dark.png`、`dark-theme-system-light.png`、`dark-theme-loading.png`、`light-theme-home-wide.png`、`light-theme-canvas.png`。

发布 APK：`releases/lingye-v0.15-dark-theme-release-20261031.apk`（SHA-256 `207232B3EFB6D45124C3C5FBBAB1CEBA2226AD63C8EF59E9BBE7B63A2236762D`）。

### 2026-10-31 应用级与文档编辑快捷键

- 应用快捷键（Activity 级分发，不受 Compose 焦点限制）：Ctrl+K 打开侧栏并聚焦快速搜索，Ctrl+N 新建文档，Ctrl+Shift+N 新建白板，Ctrl+, 打开设置；macOS Command 键同样可用。新建前会先保存当前页面，创建后进入对应文档/白板。
- 文档编辑快捷键（文本块获得焦点时）：Ctrl+B 粗体、Ctrl+I 斜体、Ctrl+U 下划线；Ctrl+Alt+1–6 转为对应级别标题，Ctrl+Alt+0 转回正文。格式作用于当前块，转换标题后焦点保持在原块。
- 结构：新增 `WorkspaceShortcuts` 纯映射对象与单元测试；`BlocksEditor` 的块 key 由“索引+类型”改为仅索引，避免标题转换时重建文本框导致焦点丢失。
- 快捷键设置页新增“应用”和“文档编辑”分组；画布工具与命令分组保留。
- 固定映射：自定义键位仍未提供，设置页已明确说明。
- 验证：模拟器实测搜索聚焦与过滤、新建文档/白板、打开设置；在 AlphaRenamed 段落块上实测粗体/斜体/下划线、标题 1 与正文转换，焦点保持，测试后块格式恢复。R8 release 实测 Ctrl+K 与 Ctrl+,。
- 测试数据：Ctrl+N/Shift+N 创建的两个空白测试页已按用户指示移入回收站，文档数回到 191；回收站现有 4 项（含原有 2 项）。
- 测试：232 项单元测试 0 失败。
- 证据：`evidence/app-shortcut-search-focused.png`、`app-shortcut-search-filter.png`、`app-shortcut-settings.png`、`app-shortcut-new-document.png`、`app-shortcut-new-whiteboard.png`、`doc-shortcut-bold.png`、`doc-shortcut-italic.png`、`doc-shortcut-underline.png`、`doc-shortcut-heading1.png`、`doc-shortcut-paragraph.png`、`settings-shortcuts-app-document.png`。

发布 APK：`releases/lingye-v0.15-shortcuts-release-20261031.apk`（SHA-256 `7DAEF1AA2C5D4FF2EEF6803E976C4A7E53CBE7A90EA90F37DB6056D249FD5239`）。

### 2026-10-31 页面图标与双向链接（页面格式 23）

- 页面图标：`Page.icon` 保存 emoji 短字符串；页面格式由 22 升至 23，字段追加在 v22 数据末尾。旧格式页面可读，图标为空时按内容显示文档/白板线型兜底；页面在下次保存时升级为 23。
- 图标入口：编辑页顶栏图标或页面菜单“更改图标”打开 24 个 emoji 的选择器；支持“移除图标”。侧栏、首页列表/卡片和编辑页顶栏即时同步；侧栏项目 key 纳入图标与标题，避免列表缓存导致旧图标残留。
- 双向链接：新增 `PageLinks.backlinks(target, pages)`，扫描未回收页面中指向当前页的链接卡片；侧栏在“链接的文档”之后显示“反向链接”，页面信息弹窗显示反向链接数量与前 6 个来源。点击来源页会走现有链接返回逻辑。
- 迁移/回滚/备份：安装 v23 前导出 v22 数据 tar（195 个页面、12 个历史文件，486,912 字节，SHA-256 `985D6F4AEA331D0E1BAEB50DD22BED94E30A49B8EA1DD04C6022975268BE0B0D`），保存于 `evidence/format22-backup.tar`；详细迁移、回滚步骤与 v22 兼容测试见 `PAGE_FORMAT_23.md`。根目录 `AGENTS.md` 按约束未改动。
- 验证：模拟器确认 v22 页面读取、图标即时显示/重启保持/移除、侧栏与顶栏同步；AlphaRenamed → AlphaPage 测试链接显示“链接的文档”，目标页显示“反向链接 / AlphaRenamed”并可返回；测试链接已撤销，测试图标已移除。
- 测试：236 项单元测试 0 失败（新增格式 23 与反向链接测试）。
- 证据：`evidence/page-icon-picker.png`、`page-icon-header.png`、`page-icon-list-sidebar.png`、`page-icon-removed.png`、`backlinks-source-sidebar.png`、`backlinks-target-sidebar.png`、`backlinks-jump.png`、`page-format23-home.png`、`page-format23-about.png`、`format22-backup.tar`。

发布 APK：`releases/lingye-v0.15-page-icons-backlinks-release-20261031.apk`（SHA-256 `C4064B8BD7312189EA6AF3F10A04503DE21C36DE0601901066B8E2662D04EF9E`）。

### 2026-10-31 单笔记阴影与圆角（页面格式 24）

- 样式字段：`Shape` 新增 `shadowStyle`（none/box/sticker），页面格式 23 → 24，在图标字段之后按 Shape 追加阴影字符串；旧页面缺少该字段时默认 `box`。
- 新建笔记：`Page.addCard` 接收当前全局 `noteShadow`、`noteCornerRadius`、边框样式/厚度和背景色，写入新笔记自身。
- 单笔记编辑：选中笔记工具条的“图层与更多”（`layerMenu`）内新增“笔记样式”分组，分为“阴影”（无/卡片/贴纸）与“圆角”（无/小/中/大/超大，0/8/16/24/32）两段，当前值带 ✓；修改写入该 Shape 并进入页面历史。独立图标入口因窄屏裁切风险被合并进现有菜单。
- 设置页说明同步为“只影响新笔记；选中已有笔记后，可在其工具条的‘图层与更多’中单独调整”；关于页显示页面格式 24。
- 迁移/回滚/备份：安装 v24 前导出 v23 数据 tar（195 页、13 个历史文件，480,768 字节，SHA-256 `C40F362740A1D42263DEA2E45D9DE6D8076AFD7003C88A6D3AE5698DD261B7F4`），保存于 `evidence/format23-backup.tar`；详细步骤见 `PAGE_FORMAT_24.md`。根目录 `AGENTS.md` 按约束未改动。
- 验证：新建笔记默认卡片阴影/中圆角；改为贴纸阴影＋超大圆角后强制重启保持；页面文件首 4 字节为 24；测试笔记已删除，AlphaRenamed 画布恢复。设置页两处帮助文字在 ? 弹层中完整换行。R8 release 安装后白板、卡片工具条与“笔记样式”分组正常显示，测试笔记同样已删除。
- 测试：239 项单元测试 0 失败（新增 `PageNoteStyleTest` 3 项）。
- 证据：`evidence/note-style-card-toolbar.png`、`note-style-menu-merged.png`、`note-style-sticker-huge-final.png`、`note-style-persisted-final.png`、`note-style-menu-persisted.png`、`note-style-restored-final.png`、`note-style-settings-help-corner.png`、`note-style-settings-help-shadow.png`、`page-format24-about.png`、`format23-backup.tar`。旧 `note-style-toolbar.png`、`note-shadow-menu.png`、`note-corner-menu.png` 为已被合并菜单取代的两个独立入口记录。

发布 APK：`releases/lingye-v0.15-note-styles-release-20261031.apk`（SHA-256 `52DB296C07FB0B09C1B97180E24A5258C4BF2741B26D687A05097F0C6DE7E398`）；调试包 `releases/lingye-v0.15-note-styles-debug-20261031.apk`（SHA-256 `868C4C840515E5B92FBF0C80A96A974A8C0C5495E1868EEC2FA31090FD57F965`）。

### 2026-10-31 底栏状态互斥与思维导图菜单复检

- 复检底栏：形状工具激活时打开思维导图菜单，形状与思维导图按钮同时高亮，形状参数条仍显示在菜单下方；内容与更多菜单也会叠加在工具选中态之上。
- 修复：`CanvasEditor` 新增 `dismissToolMenus`、`chooseTool`、`selectMenuOption` 和 `toolbarMenuOpen`；打开文本、思维导图、内容、更多时先收起其他弹层，画布底层切换到选择工具，隐藏框选、连接、画笔/橡皮、形状、多选参数条，只保留被打开菜单按钮的选中底色。
- 交互：选择工具会关闭其他弹层；文本、思维导图、内容、更多改为互斥开关；T/M 快捷键与工具栏按钮共用同一逻辑。
- 思维导图菜单五项改用布局预览图标（气泡/向右/向下/组织/大纲）；实测“向右树”生成向右堆叠分支，插入后撤销恢复画布。
- 文档修正：连接终点不在绘制参数条（选中连线后编辑）；画笔为十色/六档；橡皮含整笔/区域；More 含演示 Frame。
- 验证：debug 与 R8 release 下形状→思维导图、形状→文本、形状→内容均只保留一项高亮，菜单关闭后形状参数条恢复；239 项单元测试 0 失败。
- 证据：`evidence/toolbar-mindmap-overlap-before.png`、`toolbar-mindmap-exclusive-release.png`、`toolbar-shape-only.png`、`toolbar-text-exclusive.png`、`toolbar-content-exclusive.png`、`toolbar-mindmap-right-tree.png`、`toolbar-mindmap-restored.png`、`toolbar-final-state.png`。

发布 APK：`releases/lingye-v0.15-toolbar-popup-release-20261031.apk`（SHA-256 `FE49C4EE1EAE71D5C3DF0BD7E987B6E17862C35D49BE6DD7E0AD36F2902B910D`）；调试包 `releases/lingye-v0.15-toolbar-popup-debug-20261031.apk`（SHA-256 `B724B01EFBA2E3B7F99623C90701354B652A60B9C6F7DE7FC886CC9270158283`）。

### 2026-10-31 底栏连点修复与功能实测

- 连点问题：在形状工具激活时，第一次点击思维导图打开菜单，第二次点击关闭菜单后工具栏跳回形状按钮和形状参数条。原因是菜单关闭后恢复了打开前仍处于激活状态的形状工具。
- 修复：打开文本、思维导图、内容、更多时画布底层切换到选择工具，但被打开菜单按钮保持为当前选项；连点同一个按钮时第一次打开、第二次关闭，关闭后按钮仍高亮，不会跳回形状/画笔或选择箭头，直到选择其他工具。选择思维导图布局后同样保持思维导图按钮为当前项。
- 功能实测（debug + R8 release）：手形/选择切换、框选 4:3 创建、连接直线/6px/红色绘制、链接选择器、正文插入、高亮笔绘制、整笔橡皮、圆形蓝色填充、气泡/向右/大纲思维导图、笔记卡片插入、多选与网格吸附开关、全屏画布、Frame 导航、演示 Frame、撤销/重做均通过。
- 测试清理：所有插入内容已撤销；页面文件解码核对为 10 个 Shape、16 个文字块，与测试前一致；形状色恢复为 `#FFD866`，连线恢复为曲线/2px/`#62748A`，测试期间写入的偏好已还原并保留 `prefs-backup-before-restore.xml`。
- 异常记录：测试期间一次重启后该页曾变成 0 个画布对象（21 个文字块）。已用 15:02 的页面快照 `current.page` 覆盖恢复，重启后内容保持；该现象与测试中连续撤销后的状态有关，尚未单独复现或定位，后续需要专项验证。
- 未逐项重测：H1–H6/代码/引用/列表/待办、图片、数据库在文档中的呈现、模板、普通画笔/区域橡皮、其余形状与布局、链接跳转、节点编辑；这些沿用此前轮次验证和 239 项单元测试，不标记为本轮新验证。
- 证据：`evidence/toolbar-doubletap-persist-open.png`、`toolbar-doubletap-persist-closed.png`、`toolbar-doubletap-content.png`、`toolbar-test-frame.png`、`toolbar-test-connector.png`、`toolbar-test-link.png`、`toolbar-test-text.png`、`toolbar-test-pen.png`、`toolbar-test-eraser.png`、`toolbar-test-shape.png`、`toolbar-test-mindmap.png`、`toolbar-test-content.png`、`toolbar-test-more.png`、`toolbar-test-fullscreen.png`、`toolbar-test-presentation.png`、`toolbar-test-undo.png`、`toolbar-test-redo.png`。

发布 APK：`releases/lingye-v0.15-toolbar-persist-release-20261031.apk`（SHA-256 `439565D0D47D0390483CFFD9E7C8E48EEE64D939BCCD98D0025226E880E60314`）；调试包 `releases/lingye-v0.15-toolbar-persist-debug-20261031.apk`（SHA-256 `2453929F6C449F22E5303A45A569F6191387628874F6C158869E42BBEBC18ECD`）。

### 2026-11-01 底栏视觉与交互修正（用户六项）

- 文本工具条：H1–H6、代码、引用、项目符号、编号、待办改为直接创建画布文本对象（标题按级别设置字号，其余带前缀），解决此前只写入文档块、画布上“点了没反应”的问题；移除无用的“关闭”按钮，再次点文本工具或选择其他工具收起。
- 菜单间距：思维导图、内容与数据、更多三个下拉菜单下移 24dp，菜单底部与工具栏间距约 16dp，三个菜单统一且不遮挡工具栏。
- 缩放控件：右下缩放条底部内边距 104dp → 36dp，与底部工具栏水平对齐。
- 选中态与弹跳：主工具栏、参数 chip 与粗细点的选中底色由浅蓝改为灰色 `LingyeColors.Subtle`；`ToolIconButton`/`IllustratedToolButton` 增加 0.94→1.14→1 的 spring 弹跳缩放，仅三个竖点左侧的按钮启用，更多/撤销/重做/参数条不启用。
- 页头切换：`CompactIconAction` 新增 `selectedBackground`，文档/白板两个选中按钮的底色改为白色。
- Frame 比例浮层：宽视口下向左平移 110dp，左缘贴近框选按钮；垂直间距保持 8dp 不变，窄视口不偏移。
- 验证：debug 与 R8 release 实测六个文本插入项、三个菜单间距、缩放对齐、页头切换和 Frame 浮层位置；弹跳已接入并连续点击无裁切，未做逐帧截图；239 项单元测试 0 失败。
- 证据：`evidence/toolbar-polish-base.png`、`toolbar-polish-text-bar.png`、`toolbar-polish-text-h1.png`、`toolbar-polish-text-code.png`、`toolbar-polish-text-quote.png`、`toolbar-polish-text-bullets.png`、`toolbar-polish-text-numbered.png`、`toolbar-polish-text-todo.png`、`toolbar-polish-more-gap.png`、`toolbar-polish-frame-popup.png`、`toolbar-polish-final.png`。

发布 APK：`releases/lingye-v0.15-toolbar-polish-release-20261101.apk`（SHA-256 `8F85678CA51797B37452472C3E8A4E37EC40FA0B2146B2276D4FD001F0C4B50C`）；调试包 `releases/lingye-v0.15-toolbar-polish-debug-20261101.apk`（SHA-256 `A6A79281CF4996E727DB2C7DCEC0E29141C6BF97289EF67473B7B526D1DEBDA3`）。
### 2026-11-01 阶段 C：统一对象能力与上下文工具栏宿主

- 新增 `CanvasObjectModel.kt`：`CanvasObjectKind`（Text/Note/Shape/Image/Connector/Stroke/Frame/Mindmap/Link/PageCard）与 `CanvasCapability` 能力集合；`CanvasObjectSpecs.kindOf` 从现有 Shape.type/cardKind/独立列表推导类型，不改变页面格式。
- 新增 `ContextualToolbar.kt`：`CanvasToolbarAnchor`、`ContextualToolbarTokens`、纯函数 `resolveToolbarPlacement`、`ContextualToolbarHost`。10 个对象上下文工具条全部改用统一宿主 Surface 与定位：左右/底部 clamp、上方不足翻到下方、IME 上沿避让、测量前 alpha=0。
- `MainActivity.kt`：计算 canvas 尺寸与 `WindowInsets.ime`；为工具条传 `primaryObjectKind` 并打 `testTag("object-toolbar-<kind>")`；删除旧 `floatingToolbarX`；连接线工具条增加单选条件，修复多选时仍显示单选工具条的问题。
- 页面格式保持 24；未修改 Page.encode/decode；能力模型暂用于类型/testTag，按钮可见性仍由各对象工具条控制（C4–C7 未完成）。
- 测试：新增 `CanvasObjectModelTest` 4 项、`ContextualToolbarPositionTest` 7 项；共 250 项单元测试 0 失败。
- 验证：debug 与 R8 release 实测页面卡、形状（左/右）、Frame、文本工具条均使用统一宿主；形状工具条在对象上方，右侧无溢出；页面内容未变。
- 证据：`evidence/stage-c-pagecard.png`、`stage-c-shape.png`、`stage-c-shape-right.png`、`stage-c-frame.png`、`stage-c-text.png`、`stage-c-final.png`；实施记录见 `docs/edgeless-audit/STAGE_C_IMPLEMENTATION.md`。

发布 APK：`releases/lingye-v0.15-stage-c-release-20261101.apk`（SHA-256 `700E58833254040C74D11EA56343200B257540618DDF6D13D8857C130C58EED1`）；调试包 `releases/lingye-v0.15-stage-c-debug-20261101.apk`（SHA-256 `274F74353C12784993263FD341E28470E828082661848DF2AB9F8B4A00471142`）。
### 2026-11-01 阶段 C 续：C4–C7

- C4：新增 `ContextualMenuHost`，思维导图/内容/更多三个底部菜单统一 gap；打开任一工具菜单时隐藏对象上下文工具条（10 个工具条加 `!toolbarMenuOpen`）。
- C5：新增 `CanvasPropertyEditor`，统一从当前对象读取并单次 `changed()`；Text 字号/颜色/对齐、Note 颜色/阴影/圆角、Shape 类型/填充/边框、Frame 背景已迁移，其余属性按钮仍直接赋值。
- C6：新增 `CanvasObjectSpecs.isLocked/hasLocked/unlocked` 与 Page 扩展；移动/缩放/删除/对齐/分组/取消组合/复制遵守锁定，复制产生未锁定副本；含锁定成员的组不能取消组合。图片/连线/笔迹在格式 24 无 locked 字段。
- C7：新增 `CanvasUiTokens` 统一按钮/参数条/底栏/缩放尺寸；新增 3 项 Compose UI 测试并通过 `connectedDebugAndroidTest`（位置、顶部翻转、IME 避让）。
- 测试：259 项 JVM 单元测试 0 失败；3 项 Compose UI 测试通过。
- 事故与恢复：`connectedDebugAndroidTest` 卸载应用并清空模拟器数据；已从 `format23-backup.tar` 恢复 195 页/13 历史、`current.page` 恢复 AlphaRenamed 最新格式 24 页、`prefs-restore.xml` 恢复设置，现有 191 个活动文档。`files/images` 为空；恢复页面中 62 页包含 `.img` 引用，图片很可能缺失且无原始文件可恢复。已另存恢复后备份 `evidence/restored-20261101-backup.tar`（SHA-256 `19D21ED6F947D47A9397EF454A7DB83E5F2B7881AA7822081A61E5780F59D700`）；详见 `docs/edgeless-audit/DATA_RECOVERY_20261101.md`。后续 UI 测试前必须先完整备份。
- 证据：`evidence/stage-c2-object-toolbar.png`、`stage-c2-menu-exclusive.png`；`docs/edgeless-audit/STAGE_C_IMPLEMENTATION.md`。

发布 APK：`releases/lingye-v0.15-stage-c2-release-20261101.apk`（SHA-256 `E4AB5319CC906452BD8EACB2EDFC1371EC29AC9443C2484EE56DEAB17A7E1B66`）；调试包 `releases/lingye-v0.15-stage-c2-debug-20261101.apk`（SHA-256 `0F941B2408BA756F55231464D915F27CE0267CB0667574DBDBA7C187CDEE02F4`）。
### 2026-11-01 阶段 D 前置：格式 24 完整备份与格式 25 方案

- 完整备份：停止应用后导出 `files`（195 个 `.page`、13 个 `.history`、空 `images`）与 `shared_prefs`，共 215 个 tar 条目，487,424 字节，SHA-256 `9D02F2D3EC04BBFAE416E2CF56D6A6CF19CD35F885ED88F61D3A945258C70194`，保存为 `docs/affine-audit/evidence/format24-backup.tar`。
- 格式 25 方案已写入 `docs/affine-audit/PAGE_FORMAT_25.md`：在格式 24 末尾按 Shape 追加 fontFamily/fontStyle/fontWeight 与选区 span；默认 sans/normal/400/无 span，旧页外观不变；包含迁移、回滚、校验和测试计划。
- 调整后的决策：字重存储 100–900、默认 400、首版 UI 400/500/600/700；span 字段通用但 v25 只覆盖 Text；Note 卡片正文不写 span；Note 块容器/子块延期到独立阶段；选区工具栏仅在文本编辑且有非折叠选区时显示。
- 状态：方案已按上述决策调整，尚未修改 Page.kt 或任何页面格式；当前仍为 24。开始实施前再次核对备份 SHA。

### 2026-11-01 阶段 D1：Text 字体/选区格式与页面格式 25

- 数据模型：`Shape` 新增 `fontFamily`/`fontStyle`/`fontWeight`/`textSpans`，格式 25 在 v24 `shadowStyle` 后按 Shape 追加；默认 sans/normal/400/空 span；解码接受 1–25。
- Span：新增 `TextFormatting.kt`，支持规范化、范围切换、编辑后偏移调整、按行切分与按对象宽度换行；UTF-16 偏移；仅 Text 对象允许 span，编码/解码均拒绝其他类型携带 span。
- 渲染：Sans/Serif/Mono、字重 100–900、斜体、下划线；粗体 span 在基础字重 <700 时提升到 700；画布按宽度换行，最多 8 行。
- 编辑：内联编辑使用 `TextFieldValue` 和对象字体/字号/字重/对齐；非折叠选区显示 B/I/U；编辑层取消 12dp 内边距，使换行与画布一致。
- 迁移：`PageStore.loadAll()` 在读取前逐页检测版本，v1–v24 以 v25 原子重写；195 个原有页面全部升级，失败 0。`PageHistory` 把 v24 快照规范化为 v25，避免撤销栈被清空。
- 备份：实施前新备份 `evidence/pre-format25-20261101.tar`，487,424 字节，SHA-256 `F68C378E0BC9D9B24334EC864F8EA742E0698AF6D3E0BF57E6EAED17108161D1`；`format24-backup.tar` 复核仍为 `9D02F2D3EC04BBFAE416E2CF56D6A6CF19CD35F885ED88F61D3A945258C70194`；迁移后备份 `evidence/post-format25-20261101.tar`，515,584 字节，SHA-256 `4C838ADDB6FD3C3FB3887A42164124F4222A1B0712D8D70C21543F09487AC174`。
- 验证：测试白板插入文本（Serif/700/40px），全选后 B/I/U 生效，画布渲染粗体/斜体/下划线；强停重启后主体属性与 span 保持；撤销移除下划线、重做恢复；编辑层字号与换行与画布一致；R8 Release 渲染与设置页“页面格式 25”正常；测试页已移入回收站，活动文档回到 191。
- 测试：274 项 JVM 单元测试 0 失败；`TextFormattingTest` 8 项、`PageTextStyleTest` 6 项，含 v24 fixture、真实 v25 持久化夹具与非 Text span 拒绝。
- 证据：`evidence/f25-launch.png`、`f25-persist2.png`、`f25-persist-toolbar.png`、`f25-selchips.png`、`f25-styled.png`、`f25-styledcanvas.png`、`f25-undo.png`、`f25-redo.png`、`f25-editfidelity3.png`、`f25-release-text.png`、`f25-about.png`、`f25-text-test.page`；详见 `PAGE_FORMAT_25.md` 与 `STAGE_D_IMPLEMENTATION.md`。
- 未完成：D2 Note（背景完整色板、插入文档保真、行级待办、块容器）尚未开始；图片仍缺失。

发布 APK：`releases/lingye-v0.15-format25-release-20261101.apk`（2,512,577 字节，SHA-256 `0E31B19C14B3D535642479EA832ECCF90F925747CA1C5E3D87812DF1DF4B9F87`）；调试包 `releases/lingye-v0.15-format25-debug-20261101.apk`（14,872,088 字节，SHA-256 `3821B87DE4963F58092DCA27D1B2EDB7871A1986C5455CD12924B7F8511AF7BA`）。

### 2026-11-01 阶段 D2：Note 非格式增强

- 背景色：笔记工具条由固定蓝/黄改为当前色点 + 完整 `ColorPaletteMenu`（经典十色、自定义色卡、RGB/HSV 取色器）；选择后写入 Shape.color 并进入撤销历史。
- 插入到文档：新增 `CardDocumentImport`，保留标题 heading、段落、`☐/☑`/`[ ]/[x]/[]` 待办及勾选状态、项目符号、编号、引用、H1–H6、分隔线和三反引号代码围栏；图片与行内格式因 Note 扁平模型无法保真，明确记为限制。
- 行级待办：画布按行绘制复选框，点击哪一行只切换该行；`cardTodoAtScreen` 使用与绘制一致的屏幕坐标与行距；`☐/☑` 与 Markdown 标记均支持；锁定卡片不可切换；工具条保留“勾选下一项”。
- 持久化与兼容：页面格式保持 25，未改编码；背景和行级勾选强停重启后保持；插入的文档块在文档视图重开后保持。
- 备份：D2 前 `evidence/pre-d2-20261101.tar`（515,584 字节，SHA-256 `4C838ADDB6FD3C3FB3887A42164124F4222A1B0712D8D70C21543F09487AC174`）；D2 后 `evidence/post-d2-20261101.tar`（520,192 字节，SHA-256 `0C9DF73E1C50A3EC5A2AFF281980519DE6A8D5E23A04F37CF0441B1B53014C82`）。
- 验证：测试白板中笔记正文为 `Item A / [] Task A / [x] Task B / 1. Numbered`；行级点击、绿色背景、完整色板、插入文档、强停重启和 R8 Release 均通过；测试页已移入回收站，活动文档回到 191；197 个页面文件仍全部为格式 25。
- 测试：283 项 JVM 单元测试 0 失败（新增 `CardDocumentImportTest` 5 项、`CardTodoTest` 3 项）。
- 证据：`evidence/d2-note.png`、`d2-notecheck.png`、`d2-todotoggle4.png`、`d2-todouncheck.png`、`d2-colormenu.png`、`d2-colorgreen.png`、`d2-insertdoc.png`、`d2-persisted.png`、`d2-docpersist.png`、`d2-release.png`、`d2-trashed.png`；详见 `STAGE_D2_IMPLEMENTATION.md`。

发布 APK：`releases/lingye-v0.15-stage-d2-release-20261101.apk`（2,512,577 字节，SHA-256 `B194E402E6AF9670538F25B4312910C51028F33835CC9549A63D3F3843AC582B`）；调试包 `releases/lingye-v0.15-stage-d2-debug-20261101.apk`（14,878,171 字节，SHA-256 `4970F76F4B9BD8F2800E9AE6980ACB6C08FFC8825110BAF60A27002D6AE113F4`）。

### 2026-11-01 任务与目标审计 / T01 历史迁移矩阵

- 新增 `docs/affine-audit/TASKS_20261101.md`：按 G1 数据安全、G2 核心编辑、G3 对象系统、G4 Android 适配、G5 AFFiNE 证据分层，重列 T01–T12 优先级；明确当前基线为 v0.15、格式 25、191 活动文档。
- T01：三份历史备份确认设备实际出现 v4、v5、v6、v8、v11–v16、v22–v24；为不把用户页面内容写入源码，新增 `LegacyPageFixtures` 生成每个版本的最小页面，并新增 `PageMigrationMatrixTest` 4 项，覆盖解码→v25 往返、默认字体字段、v26 拒绝、截断和损坏尾部拒绝。
- 结果：288 项 JVM 测试 0 失败；本项不改页面格式、不改 APK 数据。REL-009 仍保持 PARTIAL，因为尚无合成 1–25 全链和降级导出。
- T02：新增 `DATA_SAFETY_INDEX.md`，集中记录 5 份备份的 SHA、197 页/15 历史基线、迁移/回滚原则与图片缺失事实。
- 文档漂移修正：`AGENTS.md` 保持原样（用户约束），其“格式 22”已过时；`WORK_CHECKLIST` B 节清理已完成的页面图标/双向链接/深色主题/滚轮缩放/快捷键条目，补充 Note 块容器缺口；`FEATURE_MATRIX` REL-009 与 `UNVERIFIED_ITEMS` U-PROJ-007 同步更新。
- 下一步按 `TASKS_20261101.md` 的建议顺序：T03 保存/启动基准、T04 大画布/大文档压力、T05 工具条迁移收尾，再做 T06/T07 格式 26 设计。

### 2026-11-01 T03/T04 性能基线

- 新增 `BenchmarkFixtureWriterTest`，生成 1000 shapes（175,118 B）与 1000 blocks（95,010 B）测试页；测试数据不进入 APK。
- 冷启动：197 页中位数 1,177 ms；加入两个大页后 199 页中位数 1,188 ms，差异约 11 ms。
- 1000 shapes 平移/缩放：50th 81 ms、95th 113 ms；普通画布同操作 48/73 ms，说明对象量带来额外开销，但模拟器本身已有明显 jank。
- 1000 blocks 文档滚动：20 帧全部 jank，50/95/99 均 150 ms；确认 `BlocksEditor` 使用 `Column + verticalScroll`，未虚拟化。
- 设备上两个 Benchmark 页面已移入回收站，活动文档回到 191；当前 199 个页面文件、17 个历史文件，全部格式 25。
- 回归：288 项 JVM 测试 0 失败；未改页面格式、业务代码或 APK。
- 证据：`evidence/bench-canvas-open.png`、`bench-canvas-whiteboard.png`、`bench-document.png`、`bench-canvas-normal.png`、`bench-final-list2.png`；详见 `PERFORMANCE_BASELINE_20261101.md`。
- 后续拆分：T04a 文档 LazyColumn 虚拟化、T03a 设备保存计时、T04b 大画布绘制/命中优化。

### 2026-11-01 T04a 文档 LazyColumn 虚拟化

- `BlocksEditor` 由 `Column + verticalScroll` 改为 `LazyColumn`：标题/正文 header item、1000 blocks 使用 `itemsIndexed`、添加块工具条 footer item。
- 会话新增 `documentScrollIndex`，与 offset 一起保存/恢复；离开页面再打开可回到 Paragraph 33–40 附近。
- 1000 blocks `dumpsys gfxinfo`：Debug 50th 69 ms、95th 101 ms；Release 50th **61 ms**、95th **73 ms**（改前 150 ms）。
- 已验证：滚动、标题/正文、块编辑焦点与键盘、拖拽重排、滚动恢复、R8 Release。底栏“添加块”点击和跨远距离拖拽未重测。
- 288 项 JVM 测试 0 失败；页面格式保持 25；测试页面已移入回收站，活动文档回到 191。
- 证据：`evidence/lazy-document-after.png`、`lazy-release-after2.png`、`lazy-release-top.png`、`lazy-drag2.png`、`lazy-scroll-restore.png`、`lazy-normal-doc.png`、`lazy-final-list.png`；详见 `PERFORMANCE_BASELINE_20261101.md`。

发布 APK：`releases/lingye-v0.15-doc-lazy-release-20261101.apk`（2,512,577 字节，SHA-256 `24DDEF23B2BC9020EDA2F4FA7A44676F917149022D35E6AE168243FA58A4575A`）；调试包 `releases/lingye-v0.15-doc-lazy-debug-20261101.apk`（14,881,315 字节，SHA-256 `79375088C927E6B47112C3FDDEBA5CD9D4434568D643D45A5B989BE57E075460`）。

### 2026-11-01 无界画布移除页面摘要卡片

- 用户明确：无界画布不需要页面卡片；画布上的每张卡片都应可自由创建、选择和删除。
- 移除页面卡的 `Surface` 渲染、点击选中、编辑/移动/关闭工具条，以及 `pageCardSelected`、`pageCardTitleFocus`、对应返回键/快捷键/焦点分支。
- `Page.title/text/cardX/cardY` 字段和页面格式 25 保持不变；文档模式标题/正文、列表预览、普通笔记/待办/图形/图片/连线对象不受影响。
- 验证：AlphaPage 白板不再出现页面卡；新建白板从内容菜单创建笔记卡、选中并删除后恢复空白；测试页已移入回收站，活动文档回到 191。
- 证据：`evidence/no-pagecard-release.png`、`card-create.png`、`card-selected.png`、`card-deleted.png`、`no-pagecard-final2.png`。

发布 APK：`releases/lingye-v0.15-no-page-card-release-20261101.apk`（2,496,193 字节，SHA-256 `F13A75F4E6D2B92B4797B67A178023704E69BCEEF860BF7397D30D8D28698B61`）；调试包 `releases/lingye-v0.15-no-page-card-debug-20261101.apk`（14,865,984 字节，SHA-256 `E97D1F25BD09CF889C77B101A5D8D25587E9EBED8EB4A1A8D439461CE219B291`）。

### 2026-11-01 移除“已移入回收站”提示

- 用户要求不需要绿色“已移入回收站”提示；`trashPage`/`trashPages` 成功后不再设置 `notice`，并清空旧提示。
- 其他提示（备份导出、恢复、重命名等）保留。
- 验证：新建测试白板 → 移入回收站 → 返回列表，无横幅；活动文档 191，202 个页面文件、20 个历史文件全部格式 25。
- 证据：`evidence/no-trash-banner2.png`、`no-banner-release.png`。

发布 APK：`releases/lingye-v0.15-no-trash-banner-release-20261101.apk`（2,496,193 字节，SHA-256 `D0DB03621E341610E8B9A4A4BB1D5349D011BF254CCD432A32B554BCB79D67CC`）；调试包 `releases/lingye-v0.15-no-trash-banner-debug-20261101.apk`（14,865,925 字节，SHA-256 `209B65E6DD1AF3940C93612FB1F741A6B2F59AA1B0E985A61A62F8A6B80F8DAA`）。

### 2026-11-01 文档界面与无界画布职责审计

- 审计文档：`docs/affine-audit/DOC_VS_EDGELESS_AUDIT.md`；证据为用户当前文档界面图、官方文档界面图，以及本地 AFFiNE `detail-page-header.tsx`、`mode-switch.tsx`。
- 结论：顶部两个图标是文档/无界画布模式切换，与官方一致；不清楚的是文档内容底部的常驻 `段落/标题/待办/图片/数据库/共享块/更多块` 块类型栏。官方没有这条常驻栏，块插入依赖块手柄/加号和 `/` 斜杠菜单。
- 当前底栏与斜杠菜单重复 8 类文本块，但图片/数据库/共享块只能从底栏进入；直接删除会造成入口丢失。
- 推荐方案 A：移除文档底栏；块左侧加单个 `+` 菜单；扩展斜杠菜单补图片/数据库/共享块；无界画布底栏保持原样。方案 B（单一悬浮“插入”按钮）为低改动备选。
- 已按方案 A 实施：`BlocksEditor` footer 移除；块左侧 `+` 可滚动菜单；`/` 菜单扩为 11 类；空文档显示“+ 输入文字或 /”；图片/数据库/共享块保留入口。
- 新增 `Page.addDatabaseAfter`、`imageInsertIndex`、共享块选择对话框；修复块删除后的 `itemsIndexed`/索引越界崩溃。
- 验证：文档模式无块类型底栏；空文档可创建首块；数据库通过 `+` 插入；斜杠菜单可滚动到图片/数据库/共享块；无界画布底栏不变。测试页已移入回收站，活动文档 191；289 项 JVM 测试 0 失败。
- 证据：`evidence/doc-mode-current-20261101.png`、`affine-doc-official-20261101.png`、`doc-plus2.png`、`doc-plus-menu2.png`、`new-doc-empty.png`、`new-doc-database.png`、`slash-menu3.png`、`doc-block-insert-release2.png`。

发布 APK：`releases/lingye-v0.15-doc-block-insert-release-20261101.apk`（2,512,577 字节，SHA-256 `8DC1535BC70732761F9C25297F1651782EDA42C8E8D3D95F1AE1132636B8515D`）；调试包 `releases/lingye-v0.15-doc-block-insert-debug-20261101.apk`（14,875,461 字节，SHA-256 `F0F81208AB6B88F8A125DC27E516592BAC0E87B95DD3767426B76DFCE5F080BE`）。

### 2026-11-01 文档基本信息面板与分段控件样式

- 用户提供 AFFiNE 官方文档 Info/Properties 截图，要求文档模式显示基本信息，并保持 UI 规整和风格一致。
- 文档页头内嵌 `DocumentInfoCard`：添加/更改图标、标题、信息、工作区属性、标签、文档模式、Journal、模板、创建/更新时间、创建者、无界配色方案、页面宽度、添加属性、隐藏 6 个属性。
- 正文下移到信息面板之后；底部 `DocumentLinksPanel` 显示 `Bi-Directional Links`、反向链接/外链计数和隐藏/显示。
- 文档内三个分段控件统一为白色胶囊轨道 + 黑色选中胶囊 + 白色选中文字，使用 `SegmentSelected`/`OnSegmentSelected`/`SegmentInk`/`SegmentBorder` 与 5dp 阴影；与设置页分段样式一致。
- 修正左边距：`DocumentLinksPanel` 增加 72dp 左内边距，使链接分隔线、标题和计数与标题/信息/正文/块文字同列；块的 `+`/拖拽手柄保留在左侧沟槽。
- 验证：文档模式/无界切换；全宽/标准切换并恢复；Release 中信息面板、分段样式和链接区对齐；活动文档 191；289 项 JVM 测试 0 失败。
- 限制：Journal/模板/添加属性为占位；创建者固定“本机”；旧页面创建时间为 mtime 近似；页面宽度和配色为全局设置。详见 `DOC_INFO_PANEL.md`、`UNVERIFIED_ITEMS.md` U-PROJ-014。
- 证据：`evidence/seg2.png`、`seg6.png`、`seg7.png`、`rel2.png`、`rel-full.png`。

发布 APK：`releases/lingye-v0.15-doc-info-segment-release-20261101.apk`（2,512,577 字节，SHA-256 `0E65B78DF02D737CACA44BBD0066C788D1D56EBE5A4D6E81AAE2B1EBB37AD9D7`）；调试包 `releases/lingye-v0.15-doc-info-segment-debug-20261101.apk`（14,912,155 字节，SHA-256 `1C9B612A9271F8B0A04AA17EB04B3C3744E7303CC2D4B2C7B5FB9E9599560EF8`）。

### 2026-11-01 误回收站事故修复

- 安装调试构建后活动文档从 191 降到 22；诊断确认 202 个页面可解码、180 个处于回收站。
- 对照 `post-d2-20261101.tar`，169 个页面在事故前为活动且原本不在回收站；仅对这 169 页清除 `trashed` 并重新编码。
- 结果：活动文档恢复 191；5 个测试/性能页保留在回收站；总页面 202、历史 20。修复页 mtime 变为修复时刻，创建/更新时间可能显示“今天”。
- 修复后备份：`evidence/post-repair-20261101.tar`（2,578,944 字节，SHA-256 `D1EBA70E444E7486B540670214DE379022E8045A70C0E2E82C5F3ACE56474BF9`）。
- 根因未最终确认，最可能是界面误触批量回收站；后续禁止真实页面批量回收站测试。详见 `DATA_SAFETY_INDEX.md`。




### 2026-11-01 无界画布文字框官方对齐

- 先完成官方 LIVE + GitHub 源码审计：`TEXT_BOX_COMPONENT_AUDIT_20261101.md`；用户确认 Ask AI 不添加，其余按报告实施。
- 块级工具条：Display in Page / Note Style / Size / Scale / Draw connector / Lock / More；36dp 高、24×36 图标、8dp 间距、8dp 圆角。
- Note Style：292dp 外宽、上方展开；Fill 主题色+白+透明+自定义；Shadow 6 种；Border 6 档宽度+3 线型；Radius 6 点+输入。
- Scale：68dp 宽、上方、50/100/200+自定义输入；Element More 13 项官方顺序；文本选区工具条 480dp，Aa/对齐/B/I/U/更多可用，其余禁用。
- 编辑态移除“完成”按钮和框内 B/I/U chip；加号缩至 24dp；Lock 隐藏工具条并显示解锁徽标；Display in Page 在文档模式显示/移除。
- 页面格式仍 25；私有元数据使用 `cardKind`；289 项 JVM 测试 0 失败；活动文档 191。
- 限制：Size 为 Auto height 切换（高度拟合使用近似字符宽度）；Frame/Group、Copy as Image、Turn linked、Code/Equation/Link/Strike/Highlight/Linked Doc 禁用；Custom color 仍为模态框；文本颜色无独立入口；Ask AI 未添加。详见 `TEXT_BOX_OFFICIAL_UI_20261101.md`。
- 发布：`releases/lingye-v0.15-text-box-autoheight-release-20261101.apk`（2,545,345 字节，SHA-256 `3C9643335D4007AAE165F92EBE3751FDF0D6D29719B8AB051B62ACC3AC98A8BD`）；调试包 15,046,474 字节，SHA-256 `5F39775B74AAEF9433E36F632B2457D288B109BC700B69A9D909EA33C35AC630`。

### 2026-11-01 回收站二级菜单、多选与移除成功提示条

- 用户要求：回收站二级浮窗加入“清空回收站”和“多选”；单文档二级浮窗加入“多选”；随后要求所有绿色成功提示条不再显示。
- 回收站：侧栏回收站行增加三点菜单，含“清空回收站”（Coral）和“多选”；有回收站内容时可用，空时禁用。
- 多选窗口：560dp 宽、最大 620dp 高；列出回收站文档，支持逐项勾选、全选、恢复所选、永久删除；永久删除前有确认框并显示数量与“不可撤销”提示；清空回收站同样有确认框。
- 数据层：`PageStore.deletePermanently` 删除页面文件、`.bak` 与对应历史文件；恢复/删除后同步 `pages` 状态、选中页与修订号。
- 单文档：页头三点菜单在“查看概览”之后加入“多选”，跳转全部文档并预选当前文档；`AllDocumentsHome` 新增 `startInSelectionMode`、`initialSelectedPageId`、`onInitialSelectionConsumed`。
- 提示条：移除内容区顶部的绿色 `notice` 横幅（备份/恢复/重命名/恢复所选/永久删除等均不再显示）；错误横幅保留。
- 验证：Release 安装后实测回收站三点菜单、多选窗口、全选、滚动、1 项永久删除、清空确认（取消）、单文档多选预选；测试页 TrashDeleteTest 已永久删除，页面数 203→204→203，活动文档 192。恢复所选路径在测试中未成功触发，未单独核对。
- 事故：12:29 出现无法解释的批量文件丢失（203→4 页、23→2 历史），已用 `pre-trash-ui-20261101.tar` 恢复；详见 `DATA_SAFETY_INDEX.md` 3.2。事故后未再执行清空全部。
- 发布：`releases/lingye-v0.15-trash-menu-release-20261101.apk`（2,545,345 字节，SHA-256 `B4DCFEE8AFFC42FB683EB70E07C46712A40E48A6A717719749AFB489A897C93F`）。289 项 JVM 测试 0 失败；页面格式保持 25。

### 2026-11-01 文本选区工具条与多级浮窗复刻

- 用户提供 AFFiNE 页面模式选区工具条截图，要求按绿色框内组件复刻，并逐项对比官网原页；随后补充“多级弹窗、间距和展开方式也要照搬”。
- LIVE 复测：Edge + Playwright 访问 `app.affine.pro` Demo Workspace，选中标题文字；记录工具条 594×38、按钮 24×36、文字样式/对齐/高亮触发 40×36；导出 DOM 与 29 个 SVG 路径。
- 工具条：T、对齐、B、Code、公式、I、Link、S、U、高亮、创建链接文档、评论、更多；Ask AI 不添加。
- 浮窗：文字样式 198×408（13 行）、对齐 198×108、高亮 198×602（Color/Background）、更多 198×176、链接 350×48；均按触发按钮居中、工具条下方 5px 展开，视口不足时钳制。
- 功能：新增 STRIKE/CODE/LINK 与前景/背景 4bit 颜色；B/I/U/删除线/代码/高亮/文字样式/对齐/复制类动作可用；链接 URL 存 `cardKind` 的 `link:` token，随文本编辑调整，重启后保持。
- 未实现：行内公式（明确提示）、评论（提示需账户与同步）、列表/引用（禁用）、链接点击打开；创建链接文档改为链接到现有页面。
- 验证：294 项 JVM 测试 0 失败；ToolbarTest 测试页选区工具条与五个浮窗截图；链接覆盖安装后仍保持；页面格式仍 25。
- 限制：800dp 高屏上，13 行文字样式与 18 行高亮菜单会按视口钳制到工具条上方；选中后点击格式按钮的焦点保持仍需精确触摸回归。
- 发布：`releases/lingye-v0.15-text-selection-toolbar-release-20261101.apk`（2,561,729 字节，SHA-256 `E02EA313D4A93DE17AE8114868CDAF5267D091FCD2C136D1B24984A91D0CE11D`）。

### 2026-11-01 行内公式、本地评论与链接点击

- 行内公式：新增 `TextInlineMath`（希腊字母、运算符、上下标、`\frac`、`\sqrt` 的 TeX→Unicode 子集）；公式按钮打开输入浮条，确认后替换选中文字为渲染文本，TeX 存 `cardKind` 的 `math:` token，MATH flag 以衬线斜体渲染。
- 评论：新增 `TextComments`（`comment:` token），浮窗可查看/添加/删除，选区加 COMMENT flag 并以浅黄底显示；无云端同步。
- 链接点击：`TextLinkHitTest` 在非编辑态命中链接文字，打开 `http(s)` 外链或 `lingye://page/<id>`；模拟器点击测试文字已打开 Chrome。
- 持久化：`TextExtras.tokens` 在缩放/垂直对齐/自动高度重写时保留 link/comment/math token；文本编辑同步调整三种范围；页面格式仍 25。
- 菜单/浮窗打开前隐藏 IME，避免 Popup 落在键盘后。
- 测试：新增 `TextRichFeaturesTest` 5 项，总计 299 项 JVM 测试 0 失败。
- 源码事故：编辑 `MainActivity.kt` 时 PowerShell 5.1 以 ANSI 读取导致中文损坏；用 GBK 反转 + 发布 APK 的 DEX 字符串恢复，21 处符号/引号手工修复；编译与测试通过，个别文案无法逐字核对。
- 未完成：公式/评论浮窗的稳定模拟器截图与逐项触控回归；选中后点击格式按钮的焦点保持。
- 发布：`releases/lingye-v0.15-inline-math-comments-release-20261101.apk`（2,578,113 字节，SHA-256 `685486D254F55DE9C81FA8F5B25EC56063A19595853E80B3FF7E8BC675062F66`）。

### 2026-11-02 底部工具栏二级浮窗间距统一

- 用户截图反馈：思维导图布局菜单与内容/数据菜单顶部有多余空白、底部被底部工具栏遮挡，二级浮窗间距不一致。
- `ContextualMenuHost` 从 `DropdownMenu` 改为自建 `Popup`：锚定触发按钮，优先上方 8dp 展开，空间不足转下方，并对窗口边界钳制。
- 三个底部菜单（思维导图、内容与数据、更多工具）统一为 Surface 8dp 内边距、30dp 行高、22dp 分组标题、6dp 行圆角；移除 Material `DropdownMenuItem` 的默认 48dp 行高与额外内边距。
- 验证：`evidence/affine-inline-20261101/menu-fix-1.png`、`menu-fix-2.png`、`menu-fix-7.png`；浮窗首行无空白、末行完整可见、与工具栏保持 8dp 间距。
- 更多工具菜单复用同一宿主与行组件，未取得独立截图。
- 299 项 JVM 测试 0 失败；发布：`releases/lingye-v0.15-secondary-menu-spacing-release-20261102.apk`（2,578,113 字节，SHA-256 `CDD9585A497B9A2A5BC1DFCA745AF2D4BC2032846752A4250D7E09F0C35CA3DB`）。

### 2026-11-02 选区框、加号与文本内边距修正

- 用户反馈：官方选中框与当前实现存在差距；左右加号官方没有底部卡片，当前实现有；同时要求修正文本与边框之间的默认距离。
- 选中框：`selectionFrame` 改为单层 1.5dp 主色描边，去掉 5dp 白色底层；四角 6dp 白底 + 1.6dp 主色描边手柄；文字框圆角 4dp，普通对象 8dp；保留左上 3×2 拖拽点。
- 加号：`TextAdjacentPlus` 去掉白色卡片 Surface，改为 24dp 透明点击区内的 20dp 裸加号（1.6dp 圆头线，`LingyeColors.Muted`）；左右与文字框间距 10dp→16dp。
- 内边距：画布渲染、编辑覆盖层（`MainActivity.kt:6183`）、自动高度拟合（`autoFitTextHeight`，`2912-2919`）与链接命中测试（`TextLinkHitTest`）统一 24dp；可编辑区域宽高按 `-48dp` 计算，最小高度兜底 40dp。
- 交互补充：点击已选中的文字框直接进入编辑（单击回退），双击进入编辑保持。
- 验证：`evidence/affine-inline-20261101/selbox-1.png`（细框、裸加号、24dp 内边距）、`selbox-4.png`（单击进入编辑）；`eq-0/1/2.png`（公式浮窗与 `x²+1` 渲染）、`cm-2/3/5/6/7.png`（评论浮窗、添加、浅黄底、删除）；`fmt-1/4/5/6/7.png`（B 加粗、删除线、U 与激活态、工具条保持）。
- 触摸方式：格式按钮用 uiautomator 报告的 bounds 精确点击；双击用 UiAutomator 1.0 `audit.TouchTest`（两次点击间隔 90–120ms）。
- 数据：页面 204、历史 24、活动文档 193、格式 25 均未变；仅编辑测试页 `ToolbarTest`（右侧测试文本保留加粗/下划线/删除线；测试中一次误触把选中文本替换为 “u”，已用应用内撤销恢复原文）。
- 299 项 JVM 测试 0 失败；安装包与发布文件 SHA-256 一致；发布：`releases/lingye-v0.15-selection-frame-padding-release-20261102.apk`（2,578,113 字节，SHA-256 `EF360A2631F4DD438C4CDD9D95F682BB79B91CC3AB4C65A7C1B0A9038BA94C8F`）。

### 2026-11-02 缩小画布后文字消失修复

- 用户反馈：画布缩到 33% 左右时文字完全看不见；文字框内边距没有随缩放变化，文字被挤出或裁剪。
- 根因：文字渲染内边距使用屏幕像素常数 `24.dp.toPx()`；缩小时文字框屏幕尺寸变小而内边距不变，`innerHeight = height - 2*48px` 变成 1px，`clipRect` 把文字裁掉。
- 修复：`MainActivity.kt` 文字渲染 `textPad = 24.dp.toPx() * scale`；编辑覆盖层内边距改为 `24.dp * scale`、字段最小高度改为 `(card.height*scale - 48.dp*scale)`；`TextLinkHitTest` 改为世界坐标（pad=24dp、em=fontSize dp），调用处不再传 density。
- 测试：新增 `TextLinkHitTestTest` 5 项（文字区命中/内边距不命中/行尾外不命中/水平/垂直对齐），总计 304 项 0 失败。
- 验证：41%、33%、31%、25% 缩放下两个文字框均可见（`zoom-2/3/4.png`、`zoom-12.png`）；31% 单击进入编辑，覆盖层文字与内边距按比例缩小（`zoom-10-crop.png`）；31% 点击链接文字打开 Chrome（`zoom-20.png`）。
- 数据：204 页、24 历史、193 活动、格式 25 未变；仅测试页 `ToolbarTest`。
- 发布：`releases/lingye-v0.15-zoom-text-padding-release-20261102.apk`（2,578,113 字节，SHA-256 `63EDA62E2B8ED9EC1C35987CB63DE35CB750E69D77851E0AF8DDABAAECC88CB8`）。

### 2026-11-02 缩放控件调整

- 用户要求缩放下限 10%、点击缩放数字回到 100%，随后要求去掉控件白色卡片并移到工具栏左侧、继续向左。
- 新增 `CanvasZoom`（MIN=0.10、MAX=4、DEFAULT=1）；按钮缩放、双指缩放、会话恢复三处统一使用，10% 视口可在重启后保留。
- 缩放控件去掉 `Surface` 卡片/阴影/边框，改为透明 Row；百分比命中区 56×36dp，带 `Role.Button` 和“重置缩放为 100%”语义。
- 位置改为画布左下角（`BottomStart` + 16dp、底部 `zoomBottomPadding`），与底部工具栏垂直居中；工具栏保持居中。
- 验证：`zoombar-3.png`（左下裸控件、与工具栏同高）、`zoombar-5.png`（最小 10%）、`zoombar-6.png`（点击数字回 100%）。
- 测试：新增 `CanvasZoomTest` 3 项、`WorkspaceSessionTest` 2 项（10% 保留、低于 10% 夹紧），总计 309 项 0 失败。
- 数据：204 页、24 历史、193 活动、格式 25 未变。
- 发布：`releases/lingye-v0.15-zoom-controls-release-20261102.apk`（2,578,113 字节，SHA-256 `FBD0FAE0039E803D37D5A90DDCDD4D8B5B290C47CE46776C0BF8C6F710AE4072`）。

### 2026-11-02 二级菜单点击穿透与自动收起

- 用户反馈：打开其他工具的二级菜单后，点击最左工具按钮第一次不切换选择/拖拽，需要第二次；要求点击非浮窗区域自动隐藏二级浮窗。
- 根因：底部二级菜单 `ContextualMenuHost` 是焦点 Popup，第一次外部点击被 Popup 窗口消费，只用于关闭菜单。
- 修复：菜单改为非焦点 Popup（`focusable=false`，不自动外部关闭）；画布在 `PointerEventPass.Initial` 检测到菜单/文本参数栏打开时先关闭并消费该次按下；`dismissToolMenus()` 与返回键关闭路径同步清空 `activeMenuButton`。
- 验证：`pop-4.png`（菜单打开时一次点击最左按钮即切到拖拽）、`pop-7.png`（点画布收起菜单且无副作用）、`pop-10/11.png`（文本参数栏点击画布收起）、`pop-8/9.png`（更多工具菜单行仍可点击）。
- 未覆盖：文字选区工具条的焦点浮窗仍会消费第一次外部点击；点击侧栏不会关闭底部菜单。
- 数据：204 页、24 历史、格式 25 未变；`ToolbarTest` 测试页新增两个测试矩形（946 字节）。
- 309 项 JVM 测试 0 失败；发布：`releases/lingye-v0.15-popup-dismiss-release-20261102.apk`（2,578,113 字节，SHA-256 `0218D4CF12BCE5447C657179D155B30856029BA0FDD73B83920E338E7C96C852`）。

### 2026-11-02 参数栏外部点击收起与工具栏简化

- 用户反馈：点击外部区域后形状参数栏没有隐藏；工具栏去掉弹跳/缩放动画，交互更简单。
- 参数栏：新增 `paramBarHidden`；Frame、连线、形状工具的参数栏在画布空白处点击（位移不超过滑动阈值）后收起，工具保持激活；再次点击工具按钮重新展开；拖动绘制不触发收起。
- 工具栏：`ToolIconButton`、`IllustratedToolButton` 移除 `Animatable` 弹跳、选中缩放和颜色过渡，改为即时背景；删除全部 `bounce` 参数与调用点。
- 验证：`bar-1.png`（展开）、`bar-2.png`（点击画布收起）、`bar-3.png`（重新展开并拖动绘制，参数栏保持）。
- 数据：204 页、24 历史、格式 25；`ToolbarTest` 因绘制回归增至 1,121 字节。
- 309 项 JVM 测试 0 失败；发布：`releases/lingye-v0.15-param-bar-dismiss-release-20261102.apk`（2,578,113 字节，SHA-256 `708AAB30986E6EC64BACE43D958D861CE7239D1B79840BD55C9ECB852EFFC8CB`）。

### 2026-11-02 外部点击回到选择工具

- 用户补充：点击外部隐藏二级浮窗后，还要自动回到最左边第一个工具（选择）。
- 画布交互统一调用 `chooseTool("选择")`：Frame/连线/形状参数栏轻点画布后收起并切回选择；底部菜单与文本参数栏在画布按下时收起并切回选择；拖动绘制不触发。
- 删除 `paramBarHidden` 状态与可见性判断，参数栏完全由当前工具决定。
- 验证：`back-1/2/3.png`（参数栏展开、点击画布收起并回到选择、再次进入形状工具后轻点仍回到选择且不产生多余图形）。
- 数据：204 页、24 历史、格式 25 未变。
- 309 项 JVM 测试 0 失败；发布：`releases/lingye-v0.15-outside-click-reset-release-20261102.apk`（2,578,113 字节，SHA-256 `B93C00D9AA7C420B5E59AE1821BA550D512360ADFAE44D66A980E5CEB9A64202`）。

### 2026-11-02 形状对象工具条横向形状菜单

- 用户提供官方对象工具条截图与当前竖向文字菜单截图，要求按官方修改。
- 触发按钮改为“当前形状图标 + 下拉箭头”，图标随形状类型变化。
- 菜单改为横向图标行（矩形、圆形、菱形、三角形、圆角矩形），40×40dp、当前项灰底高亮、无文字标签；点选后立即切换形状并关闭。
- 验证：`shape-4/5/6.png`（工具条与触发按钮、横向菜单、选择圆形后对象变为椭圆）。
- 数据：204 页、24 历史、格式 25；`ToolbarTest` 一个测试矩形改为椭圆（1,299 字节）。
- 309 项 JVM 测试 0 失败；发布：`releases/lingye-v0.15-shape-menu-release-20261102.apk`（2,578,113 字节，SHA-256 `26440B7FAA33A73D2B1F9718B4B730AC02A8E02F56471C04ADB18C374496F68A`）。

### 2026-11-02 形状工具条与二级菜单比例、自定义色卡

- 用户要求：形状一级工具条与二级菜单的尺寸比例、位置关系参考文本框工具条；缩小二级弹窗高度；自定义色卡移入“自定义颜色”弹窗并做成切换按钮。
- 一级工具条改为文本框工具条几何：36dp 高、24×36 按钮、8dp 间距、水平 6dp、1dp 边框、8dp 圆角、6dp 阴影；使用 `TextToolbarIconButton`/`TextToolbarSeparator`。
- 二级菜单统一 292dp 宽、以触发按钮居中、下方 18dp；颜色菜单单行显示 10 个预设色 + 加号；形状菜单 5 图标均匀排列；边框/图层面板 208dp。
- 自定义色卡从颜色菜单移除，`ColorEditorDialog` 增加“自定义色卡”开关与清空；新增 `CustomColorDialogHost` 把弹窗提升到应用根层，打开时先关闭颜色菜单。
- 验证：`shape-7/10/12/17.png`。
- 数据：204 页、24 历史、格式 25；`ToolbarTest` 含测试图形（2,007 字节）；偏好中保存 1 个测试自定义颜色。
- 309 项 JVM 测试 0 失败；发布：`releases/lingye-v0.15-toolbar-proportions-release-20261102.apk`（2,594,497 字节，SHA-256 `EB4B4CA9D9E3D67484D274A0C90755DB04484736CC981AB4DCBF9E8E2F9CA80D`）。

### 2026-11-02 选中背景尺寸、文字对比度与高亮残留

- 用户反馈：一级/二级弹窗选中背景过大、“1 px”看不清、单选组切换后旧高亮残留。
- `ToolIconButton` 选中底缩为 32×32（带文字 46×32），`IllustratedToolButton` 缩为 32×32，`TextToolbarIconButton` 缩为 24×28；命中区不变。
- “1 px”标签改为 Ink + Medium。
- 修复底部菜单通过自身按钮关闭时未清空 `activeMenuButton` 导致的高亮残留（思维导图/内容与数据/更多工具）。
- 验证：`stale-2-crop.png`、`hl-2-crop.png`、`hl-3.png`、`hl-4-crop.png`。
- 数据：204 页、24 历史、格式 25；`ToolbarTest` 2,189 字节。
- 309 项 JVM 测试 0 失败；发布：`releases/lingye-v0.15-highlight-state-release-20261102.apk`（2,594,497 字节，SHA-256 `629A77692BD49AC29EE6B343E3F9433516422D199FC85402DEA89343F4FA4753`）。

### 2026-11-02 “1 px”文字裁切与形状工具条连线入口

- “1 px”改为 52×36dp 点击槽、文字居中，修复垂直内边距过大导致字母 p 下伸部被工具条裁切的问题。
- 矩形一级工具条在边框宽度与复制之间加入连线入口（`setTool("连线")`）。
- 验证：`px-2.png`、`px-2-crop.png`。
- 数据：204 页、24 历史、格式 25；`ToolbarTest` 2,189 字节。
- 309 项 JVM 测试 0 失败；发布：`releases/lingye-v0.15-px-connector-release-20261102.apk`（2,594,497 字节，SHA-256 `2F65598531786C0A9DC904C1E5AF1AE9D174FDB6BEB3667ECD935D4FE460F292`）。

### 2026-11-02 连线功能审计与修复

- 审计 AFFiNE `blocksuite/affine/gfx/connector`（commit `8cef09e1…`）源码，形成 `CONNECTOR_AUDIT_20261102.md`。
- 创建：按指针下形状选最近锚点、拖动吸附目标、虚线预览与目标描边、松手创建后选中并回到选择工具。
- 入口：`enterConnectorTool` 统一文本/形状/底部入口，进入前退出编辑并隐藏键盘；支持点击形状→点击形状的快速连接。
- 默认：新安装宽 4、终点箭头、曲线；旧页面解码保持 dot/2px。
- 工具条：36dp 紧凑几何、完整色板/自定义色、线宽六档、端点/形状图标菜单、仅交换端点样式、标签/光点/删除。
- 未完成：虚线线型需格式 26；标签就地编辑/格式；标签最大宽度；Group/Frame；图标逐项对照；悬浮快捷连接按钮。
- 验证：`cn-1/2.png`、`cn-9/10.png`、`cp-1.png`、`ct-1..4.png`。
- 数据：204 页、24 历史、格式 25；`ToolbarTest` 2,942 字节。
- 309 项 JVM 测试 0 失败；发布：`releases/lingye-v0.15-connector-release-20261102.apk`（2,594,497 字节，SHA-256 `28D123922820E91FB5E1B374F4EF01FC0DCD87B42FFCF13554B22333992CBFCB`）。

### 2026-11-02 选择/拖拽复合按钮状态记忆

- 用户反馈：切到其他工具再点最左选择/拖拽按钮，会直接变成“拖拽小手”，没有恢复上次导航工具。
- 新增 `lastNavigationTool` 记忆；导航工具激活时按钮执行 Toggle，其他工具激活时恢复记忆值；非激活状态的图标/提示同步显示将恢复的工具。
- 验证：`nav-1/2/3.png`（恢复选择箭头、再次点击切拖拽、恢复拖拽小手）。
- 数据：204 页、24 历史、格式 25；`ToolbarTest` 测试页在测试中由 2,942 变为 971 字节，文本对象与 token 保留，测试图形消失，原因未确认（仅测试页）。
- 309 项 JVM 测试 0 失败；发布：`releases/lingye-v0.15-navigation-memory-release-20261102.apk`（2,594,497 字节，SHA-256 `9B8EB62218012E85371E6C4621D2322E90A03D48E3766E6188760477046504B7`）。

### 2026-11-02 智能参考线与边缘吸附

- 新增 `SmartGuides` 纯算法：每轴独立比较左/中/右（上/中/下）九个组合，阈值默认 5（含边界）；输出吸附后的包围盒、位移修正与参考线；共线元素合并为一条并集范围的参考线。
- 集成到选择工具拖动：拖动开始缓存未选中形状/图片包围盒，拖动中用累计位移计算候选并吸附（阈值 `5f / scale`，屏幕约 5px）；紫色参考线绘制在元素之上；拖拽结束/取消/工具切换/失焦清除；网格吸附开启时跳过。
- 测试：`SmartGuidesTest` 10 项，总计 319 项 0 失败。
- 验证：`sg5.png`（紫色竖线与吸附）、`sg6.png`（松手清除）、`sg3-end.png`（参考线范围）。
- 数据：204 页、24 历史、格式 25；`ToolbarTest` 1,513 字节。
- 发布：`releases/lingye-v0.15-smart-guides-release-20261102.apk`（2,594,497 字节，SHA-256 `F73F14A93557E8146390C75DB629FE86D719E7B2765A5CB5C7DC52D3CE1BEB0F`）。

### 2026-11-02 文档属性右侧栏

- 用户要求：把标题栏“更多（⋮）”选项做成 AFFiNE 官网文档信息面板那样的右侧属性栏；核对官网、列清单并完成；不接入智能体/AI 对话选项。
- 审计 AFFiNE `components/properties` 与 `components/workspace-property-types`：默认 9 属性、14 类添加项、三种可见性、`tags` 唯一、名称冲突自动编号；形成 `DOC_PROPERTIES_SIDEBAR_20261102.md`。
- 新增 `DocProperties.kt`：属性目录、9 个内置默认项、每页 Base64 编解码、名称去重；存储于每页 `SharedPreferences` 键 `doc_properties_<pageId>`，页面格式 25 不变。
- 新增 `DocPropertiesSidebar.kt`：320dp 右侧面板，`属性` / `添加更多属性` / `页面操作` 三区；属性行含图标、名称、可见性文案与 ⋯ 菜单；添加行对唯一类型显示“已添加”，其余显示 +。
- 标题栏 ⋮ 改为属性栏开关（选中态高亮），移除原 `pageHeaderMenu` 与 `moveMenu`；页面操作迁入侧栏：重命名、更改图标、编辑标签、查看概览、多选、撤销、重做、移动到文件夹、导出 Markdown、移入回收站。
- 已实现：官方默认顺序、可见性切换、添加属性、名称后缀、关闭重开与强停重启持久化。
- 当时未实现（后续补齐）：属性重命名/图标选择/类型行、删除确认框、自定义属性值填写；拖动排序仍未实现。按用户要求排除 AI/智能体入口。
- 验证：`ps-1.png`（默认列表与添加区）、`ps-2.png`（可见性菜单）、`ps-3.png`（空时隐藏 + 新增数字属性）、`ps-4.png`（页面操作区）、`ps-5.png`（强停重启后保留）。
- 测试：`DocPropertiesTest` 5 项，总计 324 项 0 失败。
- 数据：193 页、18 历史、活动文档 193、回收站 0、格式 25（索引中的 204/24 含 11 个已清理的回收站基准/测试页，活动文档数未变；本任务未删除页面）。
- 发布：`releases/lingye-v0.15-doc-properties-release-20261102.apk`（2,610,881 字节，SHA-256 `AF75B06ACC0C7B7EA1029319F42BDF43AF6D4327011F11B4583BDA5D9251A051`）。

### 2026-11-02 属性值填写与属性编辑菜单

- 用户要求：继续补齐自定义属性值填写，以及属性重命名、图标选择和删除确认。
- 数据模型：`DocPropertyInfo` 增加 `icon`、`value`；`DocPropertyType` 增加 `renameable`；`DocPropertyCatalog.valueKind` 区分文本/数字/复选框/日期/只读；`DocPropertyStore` 使用 `LPP2` 魔数版本化编码，旧五字段记录仍可解码。
- 侧栏菜单：名称输入 + ✓ 提交（`tags/createdAt/updatedAt` 只读）；16 图标网格；类型行；删除确认框。
- 文档信息区：按可见性渲染自定义属性；文本/数字/日期就地输入、复选框可勾选；空值与始终隐藏项在“还有 N 个属性”展开后仍可编辑；“添加属性”行打开右侧栏。
- 验证：`t39-menu.png`、`t39-icon-changed.png`（重命名 Score + 星形图标）、`t39-del-confirm.png`（确认框）、`t39-value-filled.png`（数字 42）、`t39-final.png`（重启后 hello/已勾选/日期保留）、`t39-hide6.png`（空值隐藏项展开后可填、填后回到主列表）、`t39-clean.png` / `t39-clean2.png`（清理后恢复默认 9 项）。
- 测试：`DocPropertiesTest` 8 项，总计 327 项 0 失败。
- 数据：193 页、18 历史、活动文档 193、回收站 0、格式 25；本任务只改测试页 `未命名页` 的属性附属数据，验证后已删除测试属性并恢复默认 9 项。
- 发布：`releases/lingye-v0.15-property-values-release-20261102.apk`（2,610,881 字节，SHA-256 `51F6C4706BB308A2A13DCD63D589248CF5A89F13B5CF0E9EE31E454D4C325F89`）。

### 2026-11-02 属性行拖动排序

- 用户要求：继续实现属性行拖动排序。
- 交互：属性行左侧 28dp 六点手柄；拖动行浮起并带阴影，目标行边缘显示 2dp 主题色插入线；松手重排并保存。
- 逻辑：新增 `DocPropertyCatalog.reorder` 纯函数（目标前/后插入并重排 order，未知/原地返回原列表）。
- 持久化：顺序写入每页 `SharedPreferences`，强停重启后保留。
- 验证：`drag-1.png`（手柄）、`drag-2.png`（浮起与插入线）、`drag-3.xml`（标签拖到 Journal 之后）、`drag-restart.xml`（重启保留）、`drag-up.xml` / `drag-final28.png`（拖回首位、恢复默认顺序）。
- 测试：`DocPropertiesTest` 12 项，总计 331 项 0 失败。
- 数据：193 页、18 历史、活动文档 193、回收站 0、格式 25；验证后测试页顺序恢复默认，未增删页面。
- 未实现：拖动过程中不自动滚动。
- 发布：`releases/lingye-v0.15-property-reorder-release-20261102.apk`（2,610,881 字节，SHA-256 `F5EF2D8436A2427A80C2DE47DAE0ADF6B7A754EE1F889F8A68AA6033058CF34C`）。

### 2026-11-02 属性入口图标与概览返回

- 用户要求：标题栏属性入口的 ⋮ 换成侧栏面板图标，仅改这一处；概览页增加返回键。
- 图标：`AppIcon.MORE` → `AppIcon.SIDEBAR`，仅属性栏开关这一处，保留“页面属性”提示与选中态。
- 返回：`WorkspaceOverview` 新增 `onBack` 和 40dp 圆形自绘返回箭头；点击回到当前页面文档模式。
- 验证：`icon-sidebar.png`（新图标与位置）、`ov-2.png`（概览返回箭头）、`ov-back.png`（返回原页面）。
- 测试：331 项 0 失败；数据 193 页/18 历史/活动 193/回收站 0/格式 25，未改页面。
- 发布：`releases/lingye-v0.15-sidebar-icon-overview-back-release-20261102.apk`（2,610,881 字节，SHA-256 `DD915F48B4CD7AF9CA7104A9F14E99B6B02B87371EE1AEB3FC0C96765783BCFE`）。

### 2026-11-02 导入入口与页面更多菜单

- 用户要求：左侧栏加导入按钮，按图 2 展示支持格式；顶栏加更多按钮，按图 3 复刻一级/二级浮窗；红色横线项不实现。
- 左侧栏新增 `AppIcon.IMPORT` 导入行；新增 `ImportDialog.kt` 的 9 格式弹窗（OneNote 禁用、? 帮助、底部提示）。
- 新增 `DocumentImport.kt`：HTML、docx、Markdown/HTML zip、快照 JSON 解析；Markdown 复用既有导入器；媒体文件暂不导入。
- 顶栏新增 ⋮ 菜单：264dp 一级、248dp Export 二级独立面板，二级位于一级左侧并按 Export 行对齐；实现重命名、收藏、信息、Frames、历史版本、复制、导入、Export、移入回收站。
- 新增 `Page.duplicate`、`PageHistory` 历史版本读取、HTML/快照导出、复制 Markdown；Frames 复用画板导航。
- 验证：`imp-1/2.png`、`imp-help.png`、`imp-picker.png`、`menu-2.png`、`menu-left.png`、`hist-1.png`、`frames-1.png`、`export-picker.png`。
- 测试：`DocumentImportTest` 6 项、`PageDuplicateTest` 1 项、`PageHistoryPersistenceTest` 历史版本 1 项，总计 339 项 0 失败。
- 数据：193 页、18 历史、活动文档 193、回收站 0、格式 25；未实际选择导入文件，未增删页面。
- 未完成：媒体/图片、Notion 数据库等专有结构、Word 样式/表格、完整 AFFiNE snapshot、历史版本时间戳。
- 发布：`releases/lingye-v0.15-import-menu-left-release-20261102.apk`（2,643,649 字节，SHA-256 `8544C08C09972331767F7F0FDF14613ABF53D02BD33425B5019D9F5CFBE47BBE`）。

### 2026-11-02 画布连线智能吸附与遮挡

- 用户要求：连线终点可吸附到四条边上的任意位置；四个边中点仍是强磁吸锚点；保持原本曲线；连线不得遮挡文本框/卡片；提供坐标算法与 TS 伪代码。
- 新增 `ConnectorSnapping.kt`：点到矩形边缘最近点、4 个锚点、30px 连续边缘阈值；锚点优先（24px），屏幕阈值换算世界单位。
- `ConnectorDraft` 增加 `snapKind/snapEdge`；新建连线与已有连线端点拖动共用同一吸附；锚点吸附绑定端口，普通边点投影到实际边点但不写 `endShape`；所有落点保持原路径样式，不强制折角。
- 跟随：`Page.moveSelections` 收集落在移动形状边界 2 世界单位内的自由端点，形状平移后同步移动端点；不需要页面格式迁移。
- 预览：锚点实心蓝点 + 大光晕；普通边点实心蓝点 + 小光晕；四边中点显示空心端口；虚线使用原曲线。
- 遮挡：Frame → 全部连线 → 其他对象；`hitTest` 延迟连线命中，卡片/文本优先接收点击。
- 验证：`cs-continuous.png`（边外 25px 吸附到任意边点）、`cs-blue4.png`（锚点蓝点）、`cs-edge-any-end.png`（原曲线、无折角）、`ep-3.png` / `ep-3-end.png`（已有端点拖动）、`follow-2/3/4.png`（移动卡片端点跟随、撤销恢复）、`occl-1.png`（卡片遮住连线）、`cs-final3.png`。
- 测试：`ConnectorSnappingTest` 14 项、`ConnectorFollowTest` 6 项，总计 359 项 0 失败。
- 数据：193 页、18 历史、活动文档 193、回收站 0、格式 25；测试页新增测试连线并移动/撤销卡片，未增删页面。
- 未完成：边缘跟随为平移推断，缩放/旋转不按比例；图片只支持边落点、无锚点；无自动绕障。
- 发布：`releases/lingye-v0.15-connector-edge-follow-release-20261102.apk`（2,643,649 字节，SHA-256 `2251E11442C24E7CC1AD79855C9122ADD88F7464E66E7B6BCB97ECB1410F2B60`）。
- 说明文档：`CONNECTOR_SNAPPING_20261102.md`（含 TypeScript/Konva 伪代码）。

### 2026-11-02 多线重合白色断线修复

- 用户反馈：多条连线重合时部分线变白/断线，官网重合处保持灰色。
- 根因：每条连线依次绘制“白色描边 → 彩色线”，后画连线的白边覆盖了先画连线的彩线。
- 修复：拆成 `drawConnectorUnderlay`（选中光晕 + 白边）与 `drawConnectorContent`（彩线、端点标记、标签、动画、选中手柄）；先为所有连线画 underlay，再统一画 content。
- 验证：`overlap-1.png`、`overlap-1-crop.png`（重合处保持灰色线条）。
- 测试：359 项 0 失败；数据未变；发布同 T43 `lingye-v0.15-connector-edge-follow-release-20261102.apk`。

### 2026-11-02 自定义颜色序列直接取色

- 用户要求：不是增加导入/搜索按钮，而是输入颜色序列直接取色。
- 新增 `parseColorSequence`：支持 `#RRGGBB`、`#RGB`、`#AARRGGBB`（忽略 alpha）、`r,g,b`/`r g b`/`rgb(r,g,b)`、1 起始自定义色卡序号。
- 自定义颜色弹窗新增“颜色序列”输入框；合法输入即时更新颜色和 RGB/HSV 滑块，非法输入红框提示；没有导入/搜索按钮。
- 模态说明：弹窗打开时画布只响应双指缩放，其余点击被遮罩拦截；取消/使用/返回关闭后恢复画布点击。
- 验证：`color-4/5/7/8.png`。
- 测试：`ColorPickerTest` 新增 2 项，总计 361 项 0 失败。
- 发布：`releases/lingye-v0.15-color-sequence-release-20261102.apk`（2,643,649 字节，SHA-256 `B30C774B7F756B56F69D3A0374D7641C65DE9A20EDAE65E8DEE40A3A93DBAD99`）。
- 说明文档：`COLOR_SEQUENCE_20261102.md`。

### 2026-11-02 华为风格白色取色板

- 用户要求：按华为“天生绘画”取色板重做自定义颜色弹窗，背景纯白。
- 新增白色 `ColorEditorDialog`：标题“颜色”+双预览块；四标签色盘/色环/滑块/色卡；HSV 面板；H/S/V 渐变滑块；历史色彩；3×9 新生色卡；Hex 输入；取消/使用。
- 色盘为 HSV 色轮+亮度；色环为色相环+中心当前色+S/V；滑块为 HSV 面板+三条滑块；色卡展示历史与新生。
- 保留颜色序列输入，使用颜色自动写入历史色卡；取消/使用/返回关闭，画布点击恢复。
- 验证：`hw-3/ wheel/ ring/ swatches/ close.png`。
- 测试：361 项 0 失败；数据未变；发布 `lingye-v0.15-huawei-color-picker-release-20261102.apk`（2,660,033 字节，SHA-256 `0DFEE4B4EC4C7A4FE14C326EB1889866A4F382D9DA21D0D66391CD68C1591B33`）。
- 未完成：屏幕吸管未接入；色盘/色环为自绘近似。
- 说明文档：`COLOR_PICKER_HUAWEI_20261102.md`。

### 2026-11-02 取色弹窗完整性与调色板图标

- 用户反馈：弹窗内容没显示全；加号应换成彩虹色轮；透明棋盘色卡被删除。
- 高度：外层 `BoxWithConstraints` 按屏幕 92% 限制中间滚动区，底部 Hex/取消/使用固定；新生 3 行与按钮均可见。
- 调色板：恢复透明棋盘圆（返回 `0x00000000`），加号替换为彩虹色轮圆；色块 18dp，保持单行。
- 验证：`palette-2/3/4.png`。
- 测试：361 项 0 失败；发布 `lingye-v0.15-color-picker-fit-release-20261102.apk`（2,660,033 字节，SHA-256 `937C65278EDB7A1DA300721EE0444DFF70B30F39E98F1036CC0CA6411C6395CD`）。
- 说明文档：`COLOR_PICKER_HUAWEI_20261102.md`。

### 2026-11-02 文本工具拖拽创建

- 用户反馈：文本工具总是插入固定位置。
- T 按钮/快捷键改为文本工具；参数栏按钮只选择待创建类型（正文/H1–H6/代码/引用/列表/待办），不再固定插入。
- 画布拖拽区域创建文本框，最小 120×56；创建后选中并回到选择工具。
- 移除文本工具打开时“按下即切换选择”的拦截。
- 验证：`text-3.png`；测试 361 项 0 失败；文档 `TEXT_TOOL_DRAG_20261102.md`。

### 2026-11-02 取色弹窗缩小与横线拖动

- 弹窗宽度上限 400dp，内容区按屏幕 76% 高度限制；SV 面板/滑块/色卡缩小，历史一行、新生三行完整显示。
- 顶部蓝线改为 140×20dp 拖动把手，拖动整个弹窗；水平 ±100dp、垂直 ±24dp 限位。
- 验证：`small-4.png`、`dragdlg-3.png`。
- 发布：`lingye-v0.15-text-drag-color-drag-release-20261102.apk`（2,660,033 字节，SHA-256 `31A720232863273AAE09B251A7B8AEBB8C347BCB6F0E3DE7AF4971FA63513EAF`）。
- 说明文档：`COLOR_PICKER_HUAWEI_20261102.md`。

### 2026-11-02 文本参数栏首次点击闪退

- 用户反馈：第一次点 T 参数栏闪一下消失，第二次才出现。
- 根因：工具切换副作用在 `tool != 选择/移动` 时关闭 `textToolsMenu`；第一次切到“文本”后立即关闭。
- 修复：副作用允许 `tool == "文本"` 保留参数栏；其他工具仍关闭。
- 验证：`text-first.png`；361 项测试 0 失败。
- 发布：`lingye-v0.15-text-tool-first-click-release-20261102.apk`（2,660,033 字节，SHA-256 `A43AA1111C190E2EA769467542345396F5B67BD8015EA9FA89A124BAEA74E3AA`）。

### 2026-11-02 页面行 ⋯ 菜单补齐

- 用户要求：检查页面行 ⋯ 浮窗中尚未构建的选中项并补齐。
- 审计：重命名、查看信息、加入收藏/取消收藏、从文件夹移出、移动到回收站已实现；“添加链接文档 / 复制 / 在新标签页打开”为禁用项。
- 添加链接文档：页面选择器列出未删除且非来源页的页面；选择后通过 `PageLinks.addLinkedDoc` 在来源页插入链接卡片，保存并切换到该页白板；打开选择器时关闭页面行菜单。
- 复制：`duplicatePage` 使用 `Page.duplicate` 生成标题带“副本”的新页面，保存并直接打开。
- 在新标签页打开：新增 `openTabs` 与 `PageTabStrip`；支持多标签、切换、关闭；后台标签关闭不切换当前页；页面移入回收站后标签自动清理。
- 修复：标签栏加 `statusBarsPadding()`，不再与状态栏重叠、点击不再被系统拦截。
- 验证：`pg-2.png`（菜单三项启用）、`pg-5/6/9.png`（两标签与切换）、`pg-14.png`（副本）、`pg-15/16.png`（链接选择器与画布卡片）、`pg-31.png`（关闭后台标签）、`pg-36/37/38/39.png`（修复后标签栏与选择器）。
- 数据：195 页、20 历史、活动文档 195、回收站 0、格式 25；新增两个测试页（T51Blank 与未命名页面 副本）。链接测试向 `28f74e84…` 添加的卡片已用画布删除恢复（292 字节、无 `pageLink`）。后续清理：两个测试页已移入回收站，活动文档 193、回收站 2。
- 361 项 JVM 测试 0 失败；发布：`lingye-v0.15-page-menu-actions-release-20261102.apk`（2,660,033 字节，SHA-256 `57F9BE8912D186DC7AB3C1CDCE4A28B9BE0D70FFD5E0E7E36B869457A8F597B2`）。
- 说明文档：`PAGE_MENU_ACTIONS_20261102.md`。

### 2026-11-03 Export 二级浮窗卡顿修复

- 用户反馈：点击 Export 后界面卡顿，菜单先向左移再回原位，二级浮窗最后才出现。
- 根因：一级与二级菜单共用同一个 `DropdownMenu`，点击 Export 时宽度 264→520dp、高度也增加；弹窗窗口逐帧重新测量/定位，二级面板的 `Spacer(height=exportTopDp)` 还会先以 0 高度出现。
- 修复：一级菜单固定 264dp；二级面板改为独立非焦点 `Popup`，用弹窗窗口屏幕原点 + 菜单/Export 行局部坐标定位，放在一级菜单左侧 8dp、与 Export 行顶部对齐；返回/外部点击仍由一级菜单处理。
- 验证：`exb-0/1/2.png`（无中间滑动帧）、`fx-3.png`（最终位置）、`fx-4.png`（复制 Markdown）、`fx-5.png`（外部关闭）、`fx-6.png`（返回关闭）、`fx-7.png`（二级打开时点一级重命名）、`fx-8.png`（HTML 打开系统文件选择器）。
- 数据：195 个页面文件、20 历史、活动文档 193、回收站 2、格式 25；本轮测试页 `T51Blank` 内误建的空数据库块与模板卡片已删除，随后两个测试页移入回收站。
- 361 项 JVM 测试 0 失败；发布：`lingye-v0.15-export-submenu-stable-release-20261103.apk`（2,660,033 字节，SHA-256 `CAA35D6B2754D4951A63E8A2A37173DADADB946424480E7A232E431B11111B73`）。
- 说明文档：`EXPORT_SUBMENU_STUTTER_20261103.md`。

### 2026-11-03 官方模板分类与画廊

- 用户要求：内容浮窗的选中项和二级标题按 AFFiNE 官方模板面板修改，保留图标，官网资源直接取用。
- 审计：官方 Edgeless 内容面板为搜索 + 8 个分类 + 3 列模板网格；图 2 可见 Arrows、Cheeky Piggies、Contorted Stickers、Paper、Brainstorming。
- 资源：直接下载官方 SVG（18/15/15/15 个）并栅格化；Brainstorming 5 个模板按官方面板截图；共 68 个 PNG、728,641 字节。
- UI：保留图片/数据库及图标；5 个旧卡片模板项改为 5 个官方分类标题；点击分类打开模板画廊，含搜索、分类标签、3 列网格、滚动、外部/返回关闭；末尾 Cards 标签保留原 5 个卡片预设。
- 插入：点击模板把官方 PNG 写入图片存储并调用 `page.addCanvasImage`；Cards 标签调用 `page.addCard`；搜索匹配模板名与分类名。
- 验证：`tg-1.png`（菜单）、`tg-2.png`（Arrows）、`tg-3.png`（Piggies）、`tg-4.png`（Brainstorming）、`tg-9.png`（搜索）、`tg-6/7.png`（插入与选中）、`tg-10.png`（返回关闭）、`tg-12.png`（外部关闭）、`tg-14/15/16.png`（Cards 与笔记卡插入/删除）。
- 数据：195 个页面文件、20 历史、活动文档 193、回收站 2、图片目录 0、格式 25；箭头与笔记卡测试插入均已删除，`ToolbarTest` 恢复 1,513 字节。
- 未完成：模板为图片而非可编辑模板；只接入图 2 的 5 类；Arrows 仅 18/185；Cards 为本应用保留标签；插入位置固定。
- 361 项 JVM 测试 0 失败；发布：`lingye-v0.15-official-template-gallery-release-20261103.apk`（3,391,279 字节，SHA-256 `E6EABB7B5A84E6608EDECD3204236A2A91C5B5EC3E95F566C37695D2B8F6A267`）。
- 说明文档：`TEMPLATE_GALLERY_20261103.md`。

### 2026-11-03 选中对象任意缩放

- 用户反馈：选中文本框后四角手柄只能等比缩放，需要任意改变宽高。
- 根因：`resizeSelectionsAnchored` 用指针到锚点的距离比生成单一 factor，宽高共用。
- 修复：抓角时记录选区几何、对角锚点和抓取偏移；拖动时分别按水平/垂直系数改变 `x/width` 与 `y/height`；每次从初始快照重算，锚点固定、选区最小 24、不翻转；形状、图片、笔迹和组合均支持。
- 验证：`rs-3.png`（选中）、`rs-4.png`（仅水平变窄、高度不变）、`rs-5.png`（仅垂直变高、宽度不变）、`rs-6.png`（撤销恢复）。
- 数据：195 个页面文件、20 历史、活动文档 193、回收站 2、图片目录 0、格式 25；测试页 `ToolbarTest` 拖拽后已撤销，页面文件 1,838 字节（测试页历史内容）。
- 新增 3 项任意缩放测试，总计 364 项 JVM 测试 0 失败；发布：`lingye-v0.15-free-resize-release-20261103.apk`（3,391,279 字节，SHA-256 `C73E9AA91CA7760E38432AC65DE28A3E97B4763C2D5A8EB61228667EDC887FB3`）。
- 说明文档：`FREE_RESIZE_20261103.md`。

### 2026-11-03 内容按钮直接打开模板画廊

- 用户要求：把二级内容菜单直接换成模板画廊，不再点一次分类进三级弹窗。
- 改动：内容按钮一次点击打开 `TemplateGalleryPopup`；删除原 `ContextualMenuHost` 二级菜单和 `cardMenu` 状态。
- 保留：搜索框右侧图片/数据库图标；Cards 标签保留原卡片预设；有工作区/旧页面模板时显示 Workspace 标签和管理入口。
- 验证：`dg-1.png`（一次点击直达画廊）、`dg-2.png`（图片打开系统选择器）、`dg-3.png`（数据库添加后已撤销）。
- 数据：195 个页面文件、20 历史、活动文档 193、回收站 2、图片目录 0、格式 25；测试页 `ToolbarTest` 1,838 字节，未增删页面。
- 364 项 JVM 测试 0 失败；发布：`lingye-v0.15-gallery-direct-release-20261103.apk`（3,391,279 字节，SHA-256 `6645134A13720BE8E41C75FB08531A32D6393CAF48485AE389C60B31E56A03BB`）。
- 说明文档：`GALLERY_DIRECT_OPEN_20261103.md`。

### 2026-11-03 边中点手柄与移动时工具条收起

- 用户要求：选中框补四条边中点；移动矩形时一级工具条临时隐藏，停止后重新出现，适用于所有可框选移动组件。
- 手柄：`selectionFrame` 增加四边中点（5.5dp）；左右中点只改宽、上下中点只改高，角柄双轴自由缩放；`applyFreeResize` 增加 `axis`。
- 工具条：卡片、形状、文本、链接、思维导图、Frame、图片、笔迹、连线统一增加 `!dragging`；拖动结束/取消恢复。
- 验证：`mh-1.png`（四边中点）、`mh-2.png`（仅改宽）、`mh-5.png`（仅改高）、`mh-8.png`（拖动中隐藏）、`mh-9.png`（停止后恢复）。
- 数据：195 个页面文件、20 历史、活动文档 193、回收站 2、图片目录 0、格式 25；测试页 `ToolbarTest` 2,015 字节。
- 新增 2 项边中点测试，总计 366 项 JVM 测试 0 失败；发布：`lingye-v0.15-midpoint-toolbar-release-20261103.apk`（3,391,279 字节，SHA-256 `3992DF5578C49F2480CE8BF988D64AAE296F313E578D3AD27ACAFEC7EFE79952`）。
- 说明文档：`MIDPOINT_HANDLES_TOOLBAR_20261103.md`。

### 2026-11-03 Journal 与模板页

- 用户要求：信息面板中的 Journal、模板复选框没有作用，核对官方语义并补齐。
- 官方审计：Journal 是日期属性，勾选设为今天并进入 Journals；模板是布尔属性，勾选后出现在 Template 列表，页面仍保留在 All docs。
- 实现：`PageMetadataStore` 保存 journal 日期与 template 布尔值（页面格式 25 不变）；Info 面板日期 chip/DatePickerDialog/清除与模板复选框；新增 Journals/Templates 侧栏与视图。
- Journals：周历、Today、前后周、按日期列页面、空日期创建日志、同日期已有页面直接打开。
- Templates：列表、打开、使用模板（复制并清除副本标记）、新建模板、Journal 日期作为模板名称、排除回收站。
- 验证：`jt-5/6/7/8/9/10/13/15/16.png`。
- 数据：页面文件 200、历史 20、图片 1、活动文档 192；回收站含本轮 4 个“未命名页 副本”、1 个“ToolbarTest 副本”及此前测试页，未永久删除。
- 新增 2 项日期本地化测试，总计 368 项 JVM 测试 0 失败；发布：`lingye-v0.15-journal-templates-release-20261103.apk`（3,407,663 字节，SHA-256 `AC7EE2F45670635687D5B5962525A145739287DCCB7F9BBBEA47D4F1AED3B50A`）。
- 说明文档：`JOURNAL_TEMPLATE_20261103.md`。

### 2026-11-03 技术架构审计与第一批优化

- 用户要求：系统梳理模块/职责/依赖，减少冗余与耦合，优化资源、渲染与性能，建立长期可维护结构。
- 审计：70 个 Kotlin 文件、约 20,149 行；MainActivity 7,455 行；缺少 Repository/UseCase；元数据分散；图片无缓存；Canvas 每帧解析/线性命中；显式保存主线程。
- AR-01：`DateTimeFormats.kt` 统一四类日期格式，删除 3 处重复实现，新增 3 项测试。
- AR-02：`PageStore` 增加 4–24MB LRU Bitmap 缓存。
- AR-03：新增 `PageImage.kt` 统一图片加载。
- AR-04：抽出 `DocumentInfoCard.kt`（423 行），MainActivity 减少约 400 行。
- AR-05：抽出 `PageListComponents.kt`（页面行操作菜单、标签栏）。
- 验证：371 项 JVM 测试 0 失败；Release 冒烟通过；冷启动 TotalTime 1,686 ms（模拟器基线）。
- 数据：200 页面文件、20 历史、1 测试图片、All Docs 192、格式 25；未增删页面。
- 发布：`lingye-v0.15-architecture-batch1-release-20261103.apk`（3,407,663 字节，SHA-256 `53F35F6736AEB3D32BFE0B6946A27F9C02E7AA02CF728502CC7240649ADDC03B`）。
- 说明文档：`ARCHITECTURE_AUDIT_20261103.md`（含 P0/P1/P2 后续清单）。

### 2026-11-03 页面元数据备份与恢复

- 架构审计 P0-3：文档属性、Journal/模板标记不在页面格式 25，也不在备份 ZIP。
- 新增 `PageMetadataSnapshot`/`PageMetadataSnapshotCodec`；`BackupArchive` 升为 `lingye-backup-2`，新增可选 `metadata.dat`，兼容 `lingye-backup-1`。
- `PageStore.backup` 收集每页文档属性 raw、Journal、模板；`restore` 原 ID→新 ID 映射并批量 commit，失败清理新页面/图片。
- 测试：新增 3 项 round-trip/旧备份/损坏拒绝，总计 374 项 JVM 测试 0 失败。
- 未验证：设备级完整导出+恢复会复制全部页面，本轮未执行；仅 JVM 格式与 round-trip 已验证。
- 发布：`lingye-v0.15-metadata-backup-release-20261103.apk`（3,407,663 字节，SHA-256 `F9D641740E620CC87AE578C3D79AE5E6D505C8B308741297E8E585FBD122BA05`）。
- 说明文档：`METADATA_BACKUP_20261103.md`。

### 2026-11-03 架构第二批：文本样式模块

- 抽出 `TextBoxStyleUi.kt`（378 行）：文本缩放/对齐/自动高度/页面显示、填充/阴影/边框/圆角样式面板。
- MainActivity 7,455 → 7,112 行；累计 7,989 → 7,112，减少 877 行；72 个 Kotlin 文件、约 20,316 行。
- 与元数据备份合并发布；备份格式 `lingye-backup-2`。
- 冒烟：白板选中文本框并打开样式面板，全部样式控件正常。
- 374 项 JVM 测试 0 失败；发布：`lingye-v0.15-architecture-batch2-release-20261103.apk`（3,407,663 字节，SHA-256 `8D49455A54EEAF5E9E0ECDEC0FDB827F4957A204DC16FC125E77D329B7593FAD`）。
- 说明文档：`ARCHITECTURE_AUDIT_20261103.md`、`METADATA_BACKUP_20261103.md`。

### 2026-11-03 架构第三批：元数据仓库与画布 chrome

- 新增 `PageMetadataRepository`：文档属性、Journal、模板与备份快照统一经由仓库；Workspace 不再直接访问两个元数据 store。
- `PageStore.backup` 改为接收 `PageMetadataSnapshot`，不再自行读取偏好；备份调用在 IO 线程组装快照。
- 抽出 `CanvasChrome.kt`：参数分隔线/表面/粗细点/chip/颜色行/缩放控件。
- MainActivity 7,010 行，累计 7,989 → 7,010（-979）；74 个 Kotlin 文件、约 20,361 行。
- 冒烟：模板勾选/取消经仓库写入；白板画布与缩放/参数控件正常。
- 374 项 JVM 测试 0 失败；发布：`lingye-v0.15-architecture-batch3-release-20261103.apk`（3,407,663 字节，SHA-256 `F1E7075D91910CF7520BDE1DA3128DD25A0E785680AC43749413DDD03403C7C9`）。
- 说明文档：`ARCHITECTURE_AUDIT_20261103.md`。

### 2026-11-03 架构第四批：文档块编辑器拆分

- 新增 `BlocksEditor.kt`（670 行）：块类型图标、`BlocksEditor`、块插入菜单、共享块/共享文本编辑器、数据库编辑器与数字字段整体移出 `MainActivity`。
- MainActivity 6,425 行，累计 7,989 → 6,425（-1,585）；75 个 Kotlin 文件。
- 冒烟：`ToolbarTest` 文档块输入/回车/退格、数据库编辑器各区域、白板画布与 8 个选中手柄正常；双击画布文本原位编辑在 batch3/batch4 表现一致，作为既有观察项。
- 数据：200 页面文件、20 历史、1 图片、All Docs 192、格式 25；未增删页面。
- 374 项 JVM 测试 0 失败；发布：`lingye-v0.15-architecture-batch4-release-20261103.apk`（3,407,663 字节，SHA-256 `3DCDC1BD878802D856AA2C1CA3511BC22E9337622043D66661F178AFFB5868DE`）。
- 说明文档：`ARCHITECTURE_AUDIT_20261103.md`、`DATA_SAFETY_INDEX.md`。

### 2026-11-03 架构第五批：CanvasEditor 外移与选择工具条拆分

- 新增 `CanvasEditor.kt`（3,697 行）：画布 Composable、`SystemBarsEffect`、`ConnectorRenderData`、`ConnectorMarkerRow`、`MindmapLayoutPreview`、`TextSpanVisualTransformation` 全部移出 `MainActivity`。
- 新增 `CanvasSelectionToolbars.kt`（383 行）：页面链接、思维导图节点、Frame、图片、笔迹五类选择工具条独立成 Composable。
- MainActivity 2,652 行，累计 7,989 → 2,652（-5,337）；77 个 Kotlin 文件、约 21,508 行。
- 冒烟：Frame/笔迹/思维导图/页面链接四类工具条交互正常；图片工具条仅编译通过（测试页无图片对象）。
- 数据：200 页面文件、20 历史、1 图片、All Docs 192、格式 25；未增删页面，`ToolbarTest` 仍为 2,173 字节。
- 374 项 JVM 测试 0 失败；发布：`lingye-v0.15-architecture-batch5-release-20261103.apk`（3,407,663 字节，SHA-256 `11C61B7F6D8F5E1F2C98B2E6A3845041B46FC297531B12DC78B79E07581D14DF`）。
- 说明文档：`ARCHITECTURE_AUDIT_20261103.md`、`DATA_SAFETY_INDEX.md`。

### 2026-11-03 架构第六批：画布绘制、点击输入与底部工具条

- 新增 `CanvasRendering.kt`（686 行）：全部画布绘制；新增 `CanvasInput.kt`（81 行）：点击/双击命中；新增 `CanvasBottomBar.kt`（570 行）：底部工具条、六类参数条与画廊/更多菜单入口。
- `CanvasEditor.kt` 2,885 行，`MainActivity` 2,652 行；80 个 Kotlin 文件、约 22,033 行。
- 冒烟：画布渲染、工具切换、Frame/文本/画笔/橡皮/形状参数条、内容画廊、多选参数条、点击选择正常。
- 未拆分：笔迹/橡皮状态机、框选/缩放拖拽、快捷键与弹窗宿主；图片工具条仍未交互验证。
- 数据：200 页面文件、20 历史、1 图片、All Docs 192、格式 25；未增删页面，`ToolbarTest` 仍为 2,173 字节。
- 374 项 JVM 测试 0 失败；发布：`lingye-v0.15-architecture-batch6-release-20261103.apk`（3,424,047 字节，SHA-256 `12EFCB2B1254A8B66C11A35DAD4D7D946236C8113DB087FBD922DD52B29F5BB9`）。
- 说明文档：`ARCHITECTURE_AUDIT_20261103.md`、`DATA_SAFETY_INDEX.md`。

### 2026-11-03 架构第七批：手势状态机与画布对话框

- 新增 `CanvasPointerInputs.kt`（508 行）：笔迹/橡皮状态机、移动/选择拖拽、框选、手柄缩放、连线拖拽、形状/Frame/文本拖拽创建。
- 新增 `CanvasDialogs.kt`（298 行）：九个画布对话框统一由 `CanvasDialogHost` 渲染。
- `CanvasEditor.kt` 2,508 行，`MainActivity` 2,652 行；82 个 Kotlin 文件、约 22,462 行。
- 冒烟：画笔落笔、区域橡皮、框选、拖拽与撤销、链接文档与 Frame 导航对话框正常。
- 未拆分：键盘快捷键、labelEditor、卡片原位编辑与少量局部弹窗；图片工具条仍未交互验证。
- 数据：200 页面文件、20 历史、1 图片、All Docs 192、格式 25；未增删页面。`ToolbarTest` 2,173 → 1,841 字节（绘制擦除+拖拽撤销，内容目视完整）。
- 374 项 JVM 测试 0 失败；发布：`lingye-v0.15-architecture-batch7-release-20261103.apk`（3,424,047 字节，SHA-256 `C7A1F346C04838CC51551265625843B65D5AE329B7DB8630A4F430664AF4EFD2`）。
- 说明文档：`ARCHITECTURE_AUDIT_20261103.md`、`DATA_SAFETY_INDEX.md`。

### 2026-11-03 架构第八批：CanvasEditorState、快捷键与原位编辑

- 新增 `CanvasEditorState.kt`（72 行）：统一持有画布状态，删除 `canvasStateOf` 适配器。
- 新增 `CanvasShortcutHandler.kt`（153 行）与 `CanvasInlineEditor.kt`（244 行）：键盘快捷键与卡片/文本原位编辑移出 `CanvasEditor`。
- 修复键盘：选中文本框 Delete 可删除对象；双击编辑自动聚焦/弹出输入法；编辑中 Delete 编辑文字；完成按钮退出编辑，退出后 Delete 删除对象；Escape 仅隐藏输入法；H/V 快捷键通过。
- `CanvasEditor.kt` 2,301 行，`MainActivity` 2,652 行；85 个 Kotlin 文件。
- 冒烟：菱形/文本框删除与 Ctrl+Z、原位编辑输入/退格、完成按钮通过；Escape 仅隐藏输入法。
- 数据：200 页面文件、20 历史、1 图片、All Docs 192、格式 25；未增删页面，`ToolbarTest` 为 1,841 字节（“不可选中文本”在第九批确认为残留原位编辑器）。
- 374 项 JVM 测试 0 失败；发布：`lingye-v0.15-architecture-batch8-release-20261103.apk`（3,424,047 字节，SHA-256 `BC41D6A0726A4A01554332A230BCD0308DF5C156DB9F188309A83E8F2086966C`）。
- 说明文档：`ARCHITECTURE_AUDIT_20261103.md`、`DATA_SAFETY_INDEX.md`。

### 2026-11-03 架构第九批：不可选中文本框诊断与连线工具条

- 诊断：所谓“撤销后不可选中文本”不在页面形状中，命中为 null；实际是 `cardEdit` 失效后原位编辑器 Surface 残留。重启进程消失，确认不是页面数据损坏。
- 修复：删除选中对象时清空 `cardEdit`；`LaunchedEffect(page.shapes.size, cardEdit)` 在编辑目标失效时自动退出，避免残留编辑器覆盖画布。
- 新增 `CanvasConnectorToolbar.kt`（329 行），连线工具条与“连接线文字”对话框移出；`CanvasEditor` 内已无 `AlertDialog`。
- `CanvasEditor.kt` 2,109 行，`MainActivity` 2,652 行；86 个 Kotlin 文件、约 22,814 行。
- 冒烟：连线工具条、连接线文字对话框、文本编辑→完成→Delete 无残留、Ctrl+Z 恢复均通过。
- 数据：200 页面文件、20 历史、1 图片、All Docs 192、格式 25；未增删页面，`ToolbarTest` 仍为 1,841 字节。
- 374 项 JVM 测试 0 失败；发布：`lingye-v0.15-architecture-batch9-release-20261103.apk`（3,424,047 字节，SHA-256 `79370744FD4B269957381A21CD814F1051803125F07C1A61F97A6AA70D58CDA6`）。
- 说明文档：`ARCHITECTURE_AUDIT_20261103.md`、`DATA_SAFETY_INDEX.md`。

### 2026-11-03 架构第十批：文本选区格式浮层

- 核对确认画布没有长按快捷菜单实现，长按/右键菜单只在文档块与工具提示中；本批不虚构该菜单。
- 新增 `CanvasTextFormattingToolbar.kt`（606 行），文本选区格式浮层与全部子菜单移出 `CanvasEditor`。
- `CanvasEditor.kt` 1,654 行，`MainActivity` 2,652 行；87 个 Kotlin 文件、约 22,965 行。
- 冒烟：编辑文本→Ctrl+A→格式浮层显示、完成退出、Ctrl+Z 正常；无崩溃。
- 数据：200 页面文件、20 历史、1 图片、All Docs 192、格式 25；未增删页面，`ToolbarTest` 仍为 1,841 字节。
- 374 项 JVM 测试 0 失败；发布：`lingye-v0.15-architecture-batch10-release-20261103.apk`（3,424,047 字节，SHA-256 `2690875C925387149653EFEB62A3BC0322BB86EBD830032AECF379B8494C980C`）。
- 说明文档：`ARCHITECTURE_AUDIT_20261103.md`、`DATA_SAFETY_INDEX.md`。

### 2026-11-03 架构第十一批：卡片/形状选择工具条

- 新增 `CanvasCardShapeToolbars.kt`（449 行），卡片与形状选择工具条移出 `CanvasEditor`。
- `CanvasEditor.kt` 1,343 行，`MainActivity` 2,652 行；88 个 Kotlin 文件、约 23,103 行。
- 冒烟：新建矩形工具条、模板画廊插入“笔记”卡片后的卡片工具条均正常；测试对象已删除。
- 数据：页面文件 200 → 201（新增一个回收站空白测试页，All Docs 仍 192）、历史 20、图片 1、格式 25；未永久删除页面。
- 374 项 JVM 测试 0 失败；发布：`lingye-v0.15-architecture-batch11-release-20261103.apk`（3,424,047 字节，SHA-256 `6F5AD79B790CC5866EDAE6EF2131CAE76354D0D7550AAE99FCEDC7C1C5C49692`）。
- 说明文档：`ARCHITECTURE_AUDIT_20261103.md`、`DATA_SAFETY_INDEX.md`。

### 2026-11-03 架构第十二批：文本对象工具条与相邻控件

- 新增 `CanvasTextToolbar.kt`（412 行），文本对象工具条、左右“相邻添加”按钮与锁定角标移出 `CanvasEditor`。
- `CanvasEditor.kt` 1,069 行，`MainActivity` 2,652 行；89 个 Kotlin 文件、约 23,241 行。
- 冒烟：选中文本后工具条与左右“+”按钮正常显示；无崩溃。
- 数据：201 页面文件、20 历史、1 图片、All Docs 192、格式 25；未增删活动页面，`ToolbarTest` 仍为 1,841 字节。
- 374 项 JVM 测试 0 失败；发布：`lingye-v0.15-architecture-batch12-release-20261103.apk`（3,424,047 字节，SHA-256 `067732A211B277F2199B5B4DEF312056E12664BA24859635D5E2CF51797484F5`）。
- 说明文档：`ARCHITECTURE_AUDIT_20261103.md`、`DATA_SAFETY_INDEX.md`。

### 2026-11-03 架构第十三批：内联编辑交互修正与底部宿主

- 按用户反馈与官方 AFFiNE 交互移除内联编辑器“完成”按钮：点击画布外部、`Esc`、`Ctrl+Enter` 退出；编辑中隐藏选中外框与四角手柄，文本框编辑层去掉阴影。
- 新增 `CanvasObjectToolbarHost.kt`（222 行）与 `CanvasPresentationBar.kt`（63 行），并修正演示条“下一页”箭头；`CanvasEditor.kt` 1,069 → 928 行；91 个 Kotlin 文件。
- 排查确认 `ToolbarTest` 黄色矩形是既有 `rect` 形状而非残留浮层，可正常选中。
- 冒烟：文本/形状工具条、相邻添加按钮、三种编辑退出方式、Frame 演示进入/适应-填充/退出均正常。
- 数据：201 页面文件、20 历史、1 图片、All Docs 192、格式 25；未增删页面，`ToolbarTest` 仍为 1,841 字节。
- 374 项 JVM 测试 0 失败；发布：`lingye-v0.15-architecture-batch13-release-20261103.apk`（3,424,047 字节，SHA-256 `38E87B07B1CC3358F553B347471F15E5A69EF98CB26A57DA7988D4937FC61734`）。
- 说明文档：`ARCHITECTURE_AUDIT_20261103.md`、`DATA_SAFETY_INDEX.md`。

### 2026-11-03 架构第十四批：单选目标与画布命令

- 新增 `CanvasSelectionTargets.kt`（40 行）与 `CanvasEditorCommands.kt`（162 行）：九类单选目标索引集中计算，删除/复制/组合/层级/对齐/微移/全选/缩放/Frame 定位/演示控制改为持有 `CanvasEditorState` 的命令对象。
- `CanvasEditorState` 收拢视口尺寸、Frame 比例、导航工具、选区恢复与演示状态；`CanvasEditor.kt` 928 → 815 行；93 个 Kotlin 文件。
- 新增 `CanvasEditorCommandsTest` 7 项；总计 381 项 JVM 测试 0 失败。
- 冒烟：文本/形状工具条、Delete+Ctrl+Z、Ctrl+A 全选、缩放 51%→64%→51%、Frame 新建/选中/演示/退出/删除均正常。
- 数据：201 页面文件、20 历史、1 图片、All Docs 192、格式 25；未增删页面，`ToolbarTest` 回到 1,841 字节。
- 发布：`lingye-v0.15-architecture-batch14-release-20261103.apk`（3,424,047 字节，SHA-256 `FF59CBF3404C0EE0E9A1B7E5EFF95663178D9D60B484760A8359B49748289C13`）。
- 说明文档：`ARCHITECTURE_AUDIT_20261103.md`、`DATA_SAFETY_INDEX.md`。

### 2026-11-04 P0 第一批：页面写队列

- 新增 `PageRepository.kt`：单线程写队列，主线程只快照，编码与落盘在写线程；同步写入等待并重抛失败，异步自动保存通过回调上报。
- MainActivity 的 21 处页面保存与 1 处永久删除改走仓库；`saveCurrent()` 等待落盘；自动保存成功后才推进 `savedRevision`；`onStop` 追加 `flush(3 s)`；恢复前 `flush`。
- 新增 `PageRepositoryTest` 8 项；总计 389 项 JVM 测试 0 失败。
- 冒烟：输入 A、B 自动保存（1,841→1,843 字节）、立即切页已落盘、force-stop 重启后保持、Ctrl+Z 恢复原文回到 1,841 字节。
- 数据：201 页面文件、20 历史、1 图片、格式 25；未增删页面。
- 发布：`lingye-v0.15-architecture-batch15-release-20261104.apk`（3,424,047 字节，SHA-256 `3665ED80AE930C8B4A0A08F545A527614AAB781E3849CA0A0414C803CAD26462`）。
- 说明文档：`ARCHITECTURE_AUDIT_20261103.md`、`DATA_SAFETY_INDEX.md`。

### 2026-11-04 P0 第二批：页面加载与会话/工具偏好

- 新增 `WorkspaceRepository.kt`：页面加载、工作区会话、界面语言、画布工具偏好统一收口；Composable 不再直接读写这些偏好键。
- 偏好访问抽象为 `WorkspacePreferenceStore` 接口，生产/测试实现可替换；工具默认值与钳制移入仓库。
- 修复会话恢复不读 `workspace_session_document_scroll_index` 的问题。
- 新增 `WorkspaceRepositoryTest` 6 项；总计 395 项 JVM 测试 0 失败。
- 冒烟：语言 English/简体中文往返且重启保持、设置页会话恢复、画笔“仅手写笔”写盘并还原。
- 数据：201 页面文件、20 历史、1 图片、`ToolbarTest` 1,841 字节、格式 25；未增删页面。
- 发布：`lingye-v0.15-architecture-batch16-release-20261104.apk`（3,424,047 字节，SHA-256 `4943F1489C5ED11C5535A80CA187C85DD8D47BF1698DA6359FA2BFAB9D8291B6`）。
- 说明文档：`ARCHITECTURE_AUDIT_20261103.md`、`DATA_SAFETY_INDEX.md`。

### 2026-11-04 P0 第三批：设置与元数据收口

- `WorkspaceSettings` 编解码改为 `WorkspacePreferenceStore` 扩展并一次提交 39 个键；`WorkspaceRepository.readSettings/writeSettings` 成为唯一入口。
- 画布工具偏好 7 个写入点全部改经仓库；MainActivity 不再直接读写 SharedPreferences。
- 备份恢复元数据写入移到 `PageMetadataRepository.applyRestored`；`PageStore.restore` 只接收元数据回调，并新增 `remapToImported` 纯函数与 2 项映射测试。
- 400 项 JVM 测试 0 失败。
- 冒烟：外观主题 dark/system 切换即时写盘并同步界面；数据 201 页面/20 历史/1 图片/格式 25 不变。
- 未验证：设备级完整备份恢复（U-PROJ-022）。
- 发布：`lingye-v0.15-architecture-batch17-release-20261104.apk`（3,424,047 字节，SHA-256 `42B9C5C0DB8B2F45B50E7F2A0DB8985CD8966F53E53D4304058E904664967374`）。
- 说明文档：`ARCHITECTURE_AUDIT_20261103.md`、`DATA_SAFETY_INDEX.md`。

### 2026-11-04 P1 第一批：缓存与相邻“＋”修复

- 新增 `TextLayoutCache`（LRU 400，换行/样式 run/测宽）、`CanvasTypefaces`（Typeface 复用）、`ConnectorSampleCache`（LRU 256）与本帧连线渲染/形状索引缓存。
- 修复用户报告的文本框左右“＋”移动后停在原位：改为值类型 `AdjacentTextAnchor` 并由 `geometryRevision` 重算；拖动时随工具条隐藏，靠近画布边缘时隐藏该侧按钮。
- 新增 8 项缓存测试；408 项 JVM 测试 0 失败。
- 冒烟：51%/80%/51% 缩放、连线点选、拖动与方向键移动后＋跟随。
- 未采集到可区分的性能差异（batch17/18 横扫采样均约 59% 卡顿、50 分位 44–46 ms）；命中空间索引仍待实现。
- 数据：201 页面文件、20 历史、1 图片、格式 25；`ToolbarTest` 1,937 字节（手动测试）。
- 发布：`lingye-v0.15-architecture-batch18-release-20261104.apk`（3,424,047 字节，SHA-256 `D3E4ABD9ED1FEF4F63A7EC639158EE6E5180B3399FCF6702756EA38BEAF90846`）。
- 说明文档：`ARCHITECTURE_AUDIT_20261103.md`、`DATA_SAFETY_INDEX.md`。

### 2026-11-04 P1 第二批：卡片/笔记相邻“＋”与命中索引

- 卡片与文本共用左右“＋”：选中卡片显示相邻按钮，点按复制出空卡片并进入编辑。
- `Page.hitTest` 改为每次点击构建一次对象索引，O(n²)→O(n)。
- 新增 `CanvasHitTestIndexTest` 5 项；413 项 JVM 测试 0 失败。
- 冒烟：插入笔记卡片→相邻“＋”→生成空卡片→删除测试卡片，页面回到 1,937 字节。
- 数据：201 页面文件、20 历史、1 图片、格式 25。
- 发布：`lingye-v0.15-architecture-batch19-release-20261104.apk`（3,424,047 字节，SHA-256 `BA18544DB20D0B720B1F1ED4483AC6FD21ED13AC30FDEFE9CFF8BDC0E5E07E6F`）。
- 说明文档：`ARCHITECTURE_AUDIT_20261103.md`、`DATA_SAFETY_INDEX.md`。

### 2026-11-04 P1 第三批：图片资源治理与格式扩展

- 设置 → 储存新增“未使用图片”统计与确认式清理；只删除未被页面（含回收站）、模板、共享块或页面撤销历史引用的图片，读取失败页面时拒绝执行。
- 图片选择器改 `OpenDocument`（`image/*` + `application/octet-stream`）；`PlatformImageDecoder` 在 API 28+ 用 `ImageDecoder` 回退（软件位图）。
- 格式矩阵：JPEG/PNG/WebP/GIF/BMP 稳定；HEIF 自 Android 8、AVIF 自 Android 14 起保证；动画仅首帧；SVG/TIFF/PSD 不支持。
- 新增 `ImageGovernanceTest` 4 项；417 项 JVM 测试 0 失败。
- 冒烟：合成孤儿图统计 1 · 221 KB 并清理，真实引用图保留；系统选择器导入 PNG 成功，撤销/重做渲染正常。
- 数据：201 页面文件、20 历史、2 图片（测试导入图被重做历史引用）、格式 25。
- 发布：`lingye-v0.15-architecture-batch20-release-20261104.apk`（3,440,431 字节，SHA-256 `D3236F42813F39109E7F156069ECB4F73FF7490C8399F533C7CB0B3C26A2CE3D`）。
- 说明文档：`ARCHITECTURE_AUDIT_20261103.md`、`DATA_SAFETY_INDEX.md`。

### 2026-11-04 P1 第四批：图片分桶采样与缓存统计

- 画布图片按显示尺寸在 256/512/1024/1600 四档内解码，缓存键 `name@bucket`；文档图片等仍用 1600 档。
- 删除图片清理全部分档缓存；设置 → 储存新增图片缓存条目、占用/上限、命中率。
- 新增 `ImageSampleBucketsTest` 5 项；422 项 JVM 测试 0 失败。
- 冒烟：51%/100% 图片渲染正常，缓存行 `1 · 250 KB / 24.0 MB · 0%`。
- 数据：撤销测试图片后页面 1,937 字节；201 页面文件、20 历史、2 图片、格式 25。
- 发布：`lingye-v0.15-architecture-batch21-release-20261104.apk`（3,440,431 字节，SHA-256 `C621D5C5149E6A3334FFDD542784AFA52C4EE5DB90371A1A856544D42780D973`）。
- 说明文档：`ARCHITECTURE_AUDIT_20261103.md`、`DATA_SAFETY_INDEX.md`。

### 2026-11-04 P1 第五批：UI 菜单组件统一与“?”居中

- 新增 `CanvasMenuComponents.kt`：图层菜单文案/菜单项、分组标题、勾选行；迁移卡片、形状、图片、笔迹、文本“更多”五处菜单。
- 新增 `HelpGlyph.kt`：按字体度量在圆内居中绘制“?”，替换设置页与导入对话框的帮助图标（修复用户反馈）。
- 422 项 JVM 测试 0 失败（UI 调整）。
- 冒烟：放大截图确认“?”居中；形状图层菜单、文本“更多”菜单正常。
- 数据：`ToolbarTest` 1,937 字节；201 页面文件、20 历史、2 图片、格式 25。
- 发布：`lingye-v0.15-architecture-batch22-release-20261104.apk`（3,440,431 字节，SHA-256 `1327E5A8360EF9C56A31D6EE70B51D71AFDBD513809AFFD854430DC1A2B4D6FF`）。
- 说明文档：`ARCHITECTURE_AUDIT_20261103.md`、`DATA_SAFETY_INDEX.md`。

### 2026-11-04 P1 第六批：侧栏页面行不可变模型与稳定 key

- 新增 `PageRowModel` 与 `PageRowModel.of`；侧栏行与行菜单改为只读模型。
- 列表 key 改为 `page:id`（文件夹树 `folder-page:$name:$id`），重命名/换图标只重组该行。
- 新增 `PageRowModelTest` 4 项；426 项 JVM 测试 0 失败。
- 冒烟：侧栏列表、选中、点击打开、回收站行正常。
- 数据：201 页面文件、20 历史、2 图片、`ToolbarTest` 1,937 字节、格式 25。
- 发布：`lingye-v0.15-architecture-batch23-release-20261104.apk`（3,440,431 字节，SHA-256 `D0EE618F7FF3DB8F5ADAB574FE1617E2309A37A2AAC874ABFBA4276F6449C7D3`）。
- 说明文档：`ARCHITECTURE_AUDIT_20261103.md`、`DATA_SAFETY_INDEX.md`。

### 2026-11-04 P1 第七批：首页列表/卡片不可变模型

- 新增 `DocumentRowModel`，首页列表行、卡片网格、收藏按钮与选择图标改为只读模型；key 仍为页面 ID。
- 新增 `DocumentRowModelTest` 4 项；430 项 JVM 测试 0 失败。
- 冒烟：列表/卡片显示正常，卡片收藏切换立即更新。
- 数据：201 页面文件、20 历史、2 图片、`ToolbarTest` 1,937 字节、格式 25；收藏冒烟重写了对应页面文件。
- 发布：`lingye-v0.15-architecture-batch24-release-20261104.apk`（3,440,431 字节，SHA-256 `0BA3998F1181E5281292FFF9E732055098A02D414A0A2C86BE7D94468C19342E`）。
- 说明文档：`ARCHITECTURE_AUDIT_20261103.md`、`DATA_SAFETY_INDEX.md`。

### 2026-11-04 P1 第八批：标签页模型与搜索索引

- `PageTabStrip` 改用 `PageRowModel`；新增 `PageSearchIndex` 与 `filterPagesIndexed`（按 revision/共享块失效，空查询不建索引）。
- 新增 `PageSearchIndexTest` 4 项；434 项 JVM 测试 0 失败。
- 冒烟：搜索标题与正文过滤正确；标签页显示/关闭正常。
- 数据：201 页面文件、20 历史、2 图片、`ToolbarTest` 1,937 字节、格式 25。
- 发布：`lingye-v0.15-architecture-batch25-release-20261104.apk`（3,440,431 字节，SHA-256 `05E8C9FA3F2AD49C7A6B6FE2FB54AEF1781BDFFC1CE24B79C33C1F161E01A05E`）。
- 说明文档：`ARCHITECTURE_AUDIT_20261103.md`、`DATA_SAFETY_INDEX.md`。

### 2026-11-04 P1 第九批：通用菜单行组件迁移

- 新增 `MenuRow`（18dp 图标/自定义前导、尾部提示、禁用/危险/勾选态）；块菜单分组改用 `MenuSectionLabel`。
- 迁移块操作菜单、块类型菜单与页面行操作菜单；页面菜单图标统一 18dp。
- 434 项 JVM 测试 0 失败（UI 调整）。
- 冒烟：块菜单（禁用项、B/I/U、红色删除）与页面菜单正常。
- 数据：201 页面文件、20 历史、2 图片、`ToolbarTest` 1,937 字节、格式 25。
- 发布：`lingye-v0.15-architecture-batch26-release-20261104.apk`（3,440,431 字节，SHA-256 `D08414DBF2C14D4004AF82D3D92061493ECBEFAC60CCF7428AA59A3B6812443B`）。
- 说明文档：`ARCHITECTURE_AUDIT_20261103.md`、`DATA_SAFETY_INDEX.md`。

### 2026-11-04 P1 第十批：剩余菜单行统一与 P2 计划

- `MenuRow` 增加“标题 + 说明”副标题；迁移 MainActivity 导出/根新建/页面/回收站/文件夹新增/文件夹选项菜单、首页排序/新建/移动文件夹、块插入菜单、文档属性与设置选项；Export 行保留尾部“›”实现。
- 新增 `P2_MODULE_PLAN.md`：`:core:model`/`:core:ui`/`:data:*`/`:feature:*`/`:app` 的依赖规则、S0–S6 迁移顺序与每步验收护栏；尚未开始代码迁移。
- 434 项 JVM 测试 0 失败（UI 调整）。
- 冒烟：排序（勾选态）、新建（图标+副标题）、页面菜单、文件夹新增/选项（禁用项与红色删除）、设置语言下拉（“✓ 简体中文”）显示正常；临时文件夹 `smoke27` 删除后侧栏回到 0 个文件夹。
- 误判记录：新建文件夹后侧栏看似不显示，实为 `LazyListState` 恢复的滚动偏移遮挡，上滚即可见；未保留猜测性刷新代码。
- 数据：201 页面文件、20 历史、2 图片、All Docs 192、`ToolbarTest` 1,937 字节、格式 25；未增删页面。
- 发布：`lingye-v0.15-architecture-batch27-release-20261104.apk`（3,440,431 字节，SHA-256 `5FD3A2E21092186C3DBEE4EFD15AE007C6FC9C35E4F7C69FF922CDEE74BCB496`）。
- 说明文档：`ARCHITECTURE_AUDIT_20261103.md`、`DATA_SAFETY_INDEX.md`、`P2_MODULE_PLAN.md`。

### 2026-11-04 P2 S0 试点：包边界与死代码清理

- 新建 `core` 包：`DateTimeFormats`、`TextLayoutCache`、`ConnectorSampleCache`、`PageSearchIndex`；新建 `platform.image` 包：`PlatformImageDecoder`、`ImageSampleBuckets`。调用方改为显式 import，applicationId/数据目录/页面格式 25/备份格式均未变。
- 删除 `BlocksEditor.kt` 的 `if (false)` 死菜单（74 行，646 → 572 行）。
- 434 项 JVM 测试 0 失败。
- 冒烟：启动、全部文档、打开 `ToolbarTest`、文档信息与数据库块正常；未插入新图片，画布图片解码未做交互验证。
- 数据：201 页面文件、20 历史、2 图片、All Docs 192、`ToolbarTest` 1,937 字节、格式 25；未增删页面。
- 发布：`lingye-v0.15-architecture-batch28-release-20261104.apk`（3,440,431 字节，SHA-256 `822A1744F2C0A7F1D76E28A2CB1D3B098FEF4B7A48DDFED50BD771ED550A5080`）。
- 说明文档：`ARCHITECTURE_AUDIT_20261103.md`、`DATA_SAFETY_INDEX.md`、`P2_MODULE_PLAN.md`。

### 2026-11-04 P2 S0 续：UI 模型迁入 `ui.model`

- 新增 `ui.model` 包：`PageRowModel`、`DocumentRowModel`、`documentRowModel`、`Page.previewText()`；列表与首页 UI 改为显式 import。
- 434 项 JVM 测试 0 失败。
- 冒烟：全部文档列表/卡片切换及标题、摘要、日期、收藏、画布标记显示正常。
- 数据：201 页面文件、20 历史、2 图片、All Docs 192、`ToolbarTest` 1,937 字节、格式 25；未增删页面。
- 发布：`lingye-v0.15-architecture-batch29-release-20261104.apk`（3,440,431 字节，SHA-256 `05EC5E07AECC925BADB501E50A00C6B77FA7BE20C0783232681DFF2DBEACBA8C`）。
- 说明文档：`ARCHITECTURE_AUDIT_20261103.md`、`DATA_SAFETY_INDEX.md`、`P2_MODULE_PLAN.md`。

### 2026-11-04 P2 S0 续：元数据层迁入 `data.metadata`

- 新增 `data.metadata` 包：`PageMetadataStore`、`PageMetadataRepository`、`PageMetadataSnapshot`/`Codec`、`remapToImported`；更新 PageStore、BackupArchive、MainActivity 与测试的 import。
- 434 项 JVM 测试 0 失败。
- 冒烟：打开 `ToolbarTest`，Journal/模板复选、创建/更新时间与数据库块显示正常。
- 数据：201 页面文件、20 历史、2 图片、All Docs 192、`ToolbarTest` 1,937 字节、格式 25；未增删页面。
- 发布：`lingye-v0.15-architecture-batch30-release-20261104.apk`（3,440,431 字节，SHA-256 `B0A9A82DE2BE689407F3B1111211AB309DD5EF340F5F6229F0201AD23F475039`）。
- 说明文档：`ARCHITECTURE_AUDIT_20261103.md`、`DATA_SAFETY_INDEX.md`、`P2_MODULE_PLAN.md`。

### 2026-11-04 P2 S0 续：页面仓库与历史迁入 `data.pages`

- 新增 `data.pages` 包：`PageRepository`、`PageHistory`；更新 MainActivity、PageStore、WorkspaceBootstrap 与 8 个测试的 import。
- 434 项 JVM 测试 0 失败。
- 冒烟：启动 → 打开 `ToolbarTest` → 返回全部文档；未触发保存。
- 数据：201 页面文件、20 历史、2 图片、All Docs 192、`ToolbarTest` 1,937 字节、格式 25；未增删页面。
- 发布：`lingye-v0.15-architecture-batch31-release-20261104.apk`（3,440,431 字节，SHA-256 `9026B0710FBFFD54572017FAE005532131E5626CD10354A402D5AC0365F48000`）。
- 说明文档：`ARCHITECTURE_AUDIT_20261103.md`、`DATA_SAFETY_INDEX.md`、`P2_MODULE_PLAN.md`。

### 2026-11-04 P2 S0 续：图片存储从 PageStore 拆出

- 新增 `data.images.PageImageStore` 与迁入的 `ImageGovernance`；PageStore 的图片 API 改为委托，公开签名不变。
- 434 项 JVM 测试 0 失败。
- 冒烟：打开 `ToolbarTest` 白板，文本/形状/连线正常；视口无图片对象，图片解码未验证。
- 数据：201 页面文件、20 历史、2 图片、All Docs 192、`ToolbarTest` 1,937 字节、格式 25；未增删页面。
- 发布：`lingye-v0.15-architecture-batch32-release-20261104.apk`（3,440,431 字节，SHA-256 `852560D52E94D57EB9287E042E92D523FAA8D52DB44AB4E931600AF26DDC64A6`）。
- 说明文档：`ARCHITECTURE_AUDIT_20261103.md`、`DATA_SAFETY_INDEX.md`、`P2_MODULE_PLAN.md`。

### 2026-11-04 P2 S0 续：PageStore 迁入 data.pages

- `PageStore` 迁入 `data.pages`，与 `PageRepository`/`PageHistory` 同包；更新 MainActivity、WorkspaceBootstrap、WorkspaceRepository 与 5 个画布/块/图片文件的 import。
- 434 项 JVM 测试 0 失败。
- 冒烟：启动 → 打开 `ToolbarTest` → 返回全部文档；无崩溃。
- 数据：201 页面文件、20 历史、2 图片、All Docs 192、`ToolbarTest` 1,937 字节、格式 25；未增删页面。
- 发布：`lingye-v0.15-architecture-batch33-release-20261104.apk`（3,440,431 字节，SHA-256 `8DAD1406DAAEEFA56486D2E071489676B15A166E3D3BC2D16EA173EC0C0069DA`）。
- 说明文档：`ARCHITECTURE_AUDIT_20261103.md`、`DATA_SAFETY_INDEX.md`、`P2_MODULE_PLAN.md`。

### 2026-11-04 P2 S0 续：备份与 SAF 读写拆入 data.backup

- 新增 `data.backup.BackupStore`：备份组装、恢复合并、元数据回调、URI 读写；PageStore 的六个方法改为委托。
- PageStore 207 → 170 行；备份格式与限制未变。
- 434 项 JVM 测试 0 失败。
- 冒烟：启动 → 打开 `ToolbarTest` → 返回全部文档；无崩溃。设备级备份恢复仍未验证。
- 数据：201 页面文件、20 历史、2 图片、All Docs 192、`ToolbarTest` 1,937 字节、格式 25；未增删页面。
- 发布：`lingye-v0.15-architecture-batch34-release-20261104.apk`（3,440,431 字节，SHA-256 `E47897F7826A0E5C5977F2790856C8B67D083169FA03BBF09797AA0C232F8CD9`）。
- 说明文档：`ARCHITECTURE_AUDIT_20261103.md`、`DATA_SAFETY_INDEX.md`、`P2_MODULE_PLAN.md`。

### 2026-11-06 导入/导出能力与说明完善

- 导入对话框去掉 Obsidian/Bear 的“实验性”；帮助文案补充目录、附件、wiki 链接、标签限制。
- 设置 → 储存新增“导入与导出能力”：完整 ZIP、Markdown、HTML、快照的内容与限制；单文件 20 MB、ZIP 100 MB / 300 条。
- HTML 导出内嵌文档图片为 data URI；Markdown 同机图片引用可往返，缺图降级为文本。
- 完整备份恢复重映射画布“链接文档”卡片到新页面 ID。
- 新增 5 项测试；439 项 JVM 测试 0 失败。
- 数据：201 页面文件、20 历史、2 图片、All Docs 192、`ToolbarTest` 1,937 字节、格式 25；未增删页面。
- 发布：`lingye-v0.15-architecture-batch35-release-20261106.apk`（3,440,443 字节，SHA-256 `65A7351DD68ECB5F506A926E9961848FAF1CA444A1907BFD031A4DC509DF24D8`）。
- 说明文档：`ARCHITECTURE_AUDIT_20261103.md`、`DATA_SAFETY_INDEX.md`、`README.md`。

### 2026-11-07 复选框对齐、Journal 行为与侧栏选中态

- 复选框对勾从字体 `✓` 改为 Canvas 几何路径，640dp 与 200% 字体下均居中。
- 信息面板 Journal 点击改为直接创建/打开当天日记；已有则只打开，不跳转侧栏、不重复创建；日期修改不导航。
- 侧栏选中背景改为旧项立即透明、新项 150ms 淡入，避免两项同时高亮；10× 慢放验证。
- 439 项 JVM 测试 0 失败；隔离 AVD 验证首次创建、重复打开、已有日记、返回文档；原验证工作区 192 文档未变。
- 发布：`lingye-v0.15-architecture-batch36-release-20261107.apk`（3,440,443 字节，SHA-256 `2125919C77EFAC2A840F0CAEC2AEE7A010C96F2B8AD7BA80A48BA79CFA6AE730`）。
- 说明文档：`ARCHITECTURE_AUDIT_20261103.md`、`DATA_SAFETY_INDEX.md`。