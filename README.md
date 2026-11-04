# 灵页（Lingye）

面向 Android 平板的离线文档与无界白板应用，使用 Kotlin + Jetpack Compose 开发。

## 目录

- `tablet-workspace/`：Android 应用源码、资源、测试与文档。
  - `tablet-workspace/README.md`：功能说明、构建方式与使用限制（主文档）。
  - `tablet-workspace/docs/affine-audit/DEVELOPMENT_SUMMARY_20261104.md`：开发全流程、主要问题与限制。
  - `tablet-workspace/docs/affine-audit/ARCHITECTURE_AUDIT_20261103.md`：架构批次记录（batch1–34）。
  - `tablet-workspace/releases/lingye-v0.15-architecture-batch34-release-20261104.apk`：当前 Release。
- `docs/`：项目早期的设计规格与路线图。

## 当前状态

- 版本：0.15 开发版，页面格式 25。
- 验证：434 项 JVM 单元测试通过；Release 经 R8 构建并在 Android 15 平板模拟器冒烟。
- 最新 APK SHA-256：`E47897F7826A0E5C5977F2790856C8B67D083169FA03BBF09797AA0C232F8CD9`。

## 构建

使用 JDK 21、Android SDK 36、Gradle 8.13；`local.properties` 与调试签名不入库。

```powershell
$env:JAVA_HOME = "D:\apply\andriod\android-1\jbr"
# 中文路径下建议先镜像到英文目录再构建
robocopy "tablet-workspace\app\src" "<英文副本>\app\src" /MIR
gradle.bat -p "<英文副本>" testDebugUnitTest assembleRelease
```

## 数据与限制

- 新版可读取旧页面，旧版不能读取格式 25；卸载可能删除本地数据，请自行保留备份。
- 设备级完整备份恢复、真实手写笔/压感、画布图片解码交互与完整性能基线尚未验证。
- 详见 `tablet-workspace/docs/affine-audit/DEVELOPMENT_SUMMARY_20261104.md`。
