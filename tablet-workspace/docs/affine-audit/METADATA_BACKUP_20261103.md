# 页面元数据备份与恢复（2026-11-03）

上一轮架构审计发现：文档属性、Journal/模板标记存放在 SharedPreferences，不在页面格式 25 内，也不在备份 ZIP 内。本批把这三类元数据纳入备份，并在恢复时映射到新页面 ID。

## 1. 数据模型

`PageMetadataSnapshot`：

- `docProperties: Map<pageId, raw>`：`DocPropertyStore.encode` 的 Base64 字符串，只包含显式保存过属性的页面。
- `journalDates: Map<pageId, yyyy-MM-dd>`。
- `templatePageIds: Set<pageId>`。

`PageMetadataSnapshotCodec` 使用版本化二进制（magic `LPM1`）：

- 页面 ID 必须为规范 UUID；
- Journal 日期必须是 `yyyy-MM-dd`；
- 文档属性 raw 必须能通过 `DocPropertyStore.decode`；
- 单字符串上限 2 MB、总量 20 MB、页面数 100,000；
- 尾部必须无多余字节。

## 2. 备份格式

- `BackupArchive` 格式从 `lingye-backup-1` 升为 `lingye-backup-2`，新增可选 `metadata.dat`。
- 解码同时接受 `-1` 和 `-2`；旧备份没有 `metadata.dat` 时 `metadata = null`，恢复后保持默认属性/无 Journal/模板标记。
- ZIP 路径白名单加入 `metadata.dat`；仍限制单条目 20 MB、总量 100 MB、条目数 10,000。

## 3. 导出与恢复

导出（`PageStore.backup`）：

- 元数据快照由 `PageMetadataRepository.snapshot(pages)` 组装，`PageStore.backup` 只接收快照，不再直接读取 `ink_preferences`/`page_metadata`；
- 快照包含每页 `doc_properties_<id>` 原始值、Journal 日期和模板标记；
- 只在非空时写入 `metadata.dat`。

恢复（`PageStore.restore`）：

- 备份页面恢复时会生成新页面 ID；使用原页面与新页面的顺序映射 `oldId -> newId`。
- 文档属性通过一次 `SharedPreferences.Editor.commit()` 写入新 ID；
- Journal/模板通过 `PageMetadataStore.writeBatch()` 一次 `commit()` 写入；
- 元数据写入失败会抛错，已创建的页面/图片文件由现有 catch 清理；
- 恢复仍是“复制为新的页面”，不是原位替换。

## 4. 测试

新增 `PageMetadataSnapshotTest` 3 项：

1. 属性/Journal/模板 round-trip；
2. `BackupArchive` 携带元数据，未带元数据的旧式备份解码为 null；
3. 非法日期、损坏 magic 拒绝。

总计 374 项 JVM 测试 0 失败。

## 5. 限制与未验证

1. **未在真机/模拟器上执行完整备份导出 + 恢复**：恢复会复制全部页面，风险高；本轮只做 JVM 层 round-trip 与格式校验。恢复后页面/属性/Journal/模板的实际设备验证仍待独立测试环境。
2. 恢复元数据失败时，页面/图片文件会清理，但已 commit 的属性/标记可能残留到新页面 ID（新 ID 没有页面文件，属于无害孤立偏好）；未做偏好回滚。
3. 未显式保存过文档属性的页面不会写入元数据；恢复后仍使用内置默认属性。
4. Journal/模板标记只备份 true/有日期项；false/无日期不写入。
5. 备份格式 2 不被旧版本应用接受；旧版本恢复新备份会报“备份格式无效”。

## 6. 发布

- `releases/lingye-v0.15-metadata-backup-release-20261103.apk`，3,407,663 字节，SHA-256 `F9D641740E620CC87AE578C3D79AE5E6D505C8B308741297E8E585FBD122BA05`；模拟器已安装同一 APK。
- 页面格式仍为 25；未增删页面；All Docs 192、页面文件 200、历史 20、图片 1（ToolbarTest 引用）。
