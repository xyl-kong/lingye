# 阶段 D2 实施记录：Note 非格式增强

日期：2026-11-01  
状态：D2 首版已完成；页面格式保持 **25**，未新增/修改任何持久化字段。  
前置记录：[STAGE_D_IMPLEMENTATION.md](STAGE_D_IMPLEMENTATION.md)（Text/格式 25）。

## 1. 完成内容

| 项 | 实现 |
|---|---|
| 笔记背景完整色板 | 选中笔记后的工具条由固定蓝/黄两个色点改为当前颜色色点 + 下拉 `ColorPaletteMenu`：经典十色、自定义色卡与 RGB/HSV 取色器；选择后立即写入 Shape.color 并进入撤销历史 |
| 插入到文档保真 | 新增 `CardDocumentImport`：标题 → heading；`☐/☑`、`[ ]/[x]/[]` → todo（保留 checked）；`•/-/*` → bulleted；`1.`/`1)` → numbered；`> ` → quote；`#`–`######` → heading1–6；`---/***/___` → divider；三反引号围栏 → 单个 code block；其余 → paragraph |
| 行级待办 | 画布卡片按行绘制复选框（未勾选白底、勾选主色+白勾）；点击某行只切换该行，使用与画布完全相同的屏幕坐标/行距；锁定卡片不可切换；工具条仍保留“勾选下一项” |
| Markdown 待办兼容 | 行首 `☐`/`☑`、`[ ]`、`[]`、`[x]`、`[X]` 均可识别与切换；未知行不改变 |
| 撤销/持久化 | 背景色、行级勾选、插入文档均通过 `changed()` 记录整页快照；强停重启后保持 |

## 2. 代码位置

- `app/src/main/java/com/example/tabletworkspace/CardDocumentImport.kt`（新增）
  - 卡片标题/正文 → `Block` 列表；类型识别与代码围栏聚合。
- `app/src/main/java/com/example/tabletworkspace/Cards.kt`
  - `todoChecked/todoContent/setTodoChecked`；`toggleCardTodo` 支持 Markdown 标记；
  - `CardTodoTarget`、`cardTodoAtScreen`（屏幕坐标命中）与原有 `cardTodoAt`（世界坐标）。
- `app/src/main/java/com/example/tabletworkspace/MainActivity.kt`
  - 笔记工具条背景色下拉（`ColorPaletteMenu`）；
  - 插入文档调用 `CardDocumentImport.blocks`，锁定卡片跳过；
  - 画布卡片行级复选框绘制；
  - `onTap` 先检测 `cardTodoAtScreen`，命中则只切换对应行，否则走原有选择逻辑。

## 3. 模拟器验证

| 场景 | 结果 | 证据 |
|---|---|---|
| 新建白板 + 笔记 | 卡片正常创建 | `evidence/d2-note.png` |
| 编辑正文为段落/待办/编号 | 显示自定义行级复选框 | `evidence/d2-notecheck.png` |
| 点击第 2 行 | 只勾选第 2 行，其他行不变 | `evidence/d2-todotoggle4.png` |
| 点击第 3 行 | 只取消第 3 行勾选 | `evidence/d2-todouncheck.png` |
| 背景完整色板 | 十色 + 自定义入口完整显示 | `evidence/d2-colormenu.png` |
| 选择绿色背景 | 卡片变为绿色，文字可读 | `evidence/d2-colorgreen.png` |
| 插入到文档 | heading、paragraph、2 个 checked todo、numbered 全部保留 | `evidence/d2-insertdoc.png` |
| 强停重启 | 绿色背景、Task A 勾选、Task B 未勾选保持 | `evidence/d2-persisted.png` |
| 文档视图重开 | 插入的 heading/段落/待办/编号保持 | `evidence/d2-docpersist.png` |
| R8 Release | 启动、绿色笔记、行级复选框正常 | `evidence/d2-release.png` |
| 清理 | 测试页移入回收站，活动文档回到 191 | `evidence/d2-trashed.png` |

测试页：`06:10` 创建的“未命名页面”，内容为 `Item A / [] Task A / [x] Task B / 1. Numbered`；验证后已移入回收站。

## 4. 测试与构建

- JVM 单元测试：**283 项，0 失败，0 错误**。
- 新增：
  - `CardDocumentImportTest` 5 项：标题/段落、五种待办标记、列表/引用/标题/分隔线、代码围栏、空卡片。
  - `CardTodoTest` 新增 3 项：屏幕坐标行命中、Markdown 标记切换、空方括号标记。
- `assembleDebug` / `assembleRelease` 通过；Release 启用 R8，安装后上述界面正常。
- 页面格式：197 个页面文件全部仍为 `00 00 00 19`（25），无格式升级。

## 5. 备份

| 项 | 值 |
|---|---|
| D2 前备份 | `evidence/pre-d2-20261101.tar`，515,584 字节，SHA-256 `4C838ADDB6FD3C3FB3887A42164124F4222A1B0712D8D70C21543F09487AC174`（196 页、14 历史；与格式 25 迁移后备份一致） |
| D2 后备份 | `evidence/post-d2-20261101.tar`，520,192 字节，SHA-256 `0C9DF73E1C50A3EC5A2AFF281980519DE6A8D5E23A04F37CF0441B1B53014C82`（197 页、15 历史） |
| 回滚 | 安装格式 25 的上一版 APK，按 `PAGE_FORMAT_25.md` 第 5 节恢复；D2 未改格式，无需格式回滚 |

## 6. 未完成 / 限制

- Note 块容器/子块（官方 `affine:note`）仍未实现；当前仍是扁平的 `title/body` 字符串，需要单独设计格式 26。
- “插入到文档”的图片与行内格式无法保真：Note 模型没有图片或行内格式字段，无法在不升格式的前提下表达；本轮保真的是 block 类型、待办勾选状态、代码围栏与标题层级。
- 行级复选框只覆盖画布可见的前 5 行（与卡片绘制一致）；更长的正文需扩展卡片渲染/滚动模型。
- 背景色板复用全局 `ColorPaletteMenu`，自定义色卡依赖现有 SharedPreferences 颜色库。
- 测试页仍在回收站中（未永久删除），用户可自行恢复或清空。

## 7. 发布

- Release：`releases/lingye-v0.15-stage-d2-release-20261101.apk`，2,512,577 字节，SHA-256 `B194E402E6AF9670538F25B4312910C51028F33835CC9549A63D3F3843AC582B`。
- Debug：`releases/lingye-v0.15-stage-d2-debug-20261101.apk`，14,878,171 字节，SHA-256 `4970F76F4B9BD8F2800E9AE6980ACB6C08FFC8825110BAF60A27002D6AE113F4`。
