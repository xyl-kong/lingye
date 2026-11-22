param([switch]$Software)
$base = 'C:/Users/17482/.codex/visualizations/2026/10/28/01a18267-d2c0-7a63-96d3-472a7ae5bf6e/emulator-test'
$env:ANDROID_HOME = "$base/sdk"
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
$env:ANDROID_EMULATOR_HOME = $base
$env:ANDROID_AVD_HOME = "$base/avd"
$emulator = "$base/sdk/emulator/emulator.exe"
if (-not (Test-Path -LiteralPath $emulator)) { throw "找不到模拟器：$emulator" }
if ($Software) {
    Write-Warning '软件模拟在本机曾以 0xC0000005 异常退出；请优先在普通 PowerShell 中使用 WHPX 启动。'
    & $emulator -avd Lingye_Validation -gpu swiftshader_indirect -accel off
} else {
    & $emulator -accel-check
    if ($LASTEXITCODE -ne 0) { Write-Error '当前进程无法访问 WHPX。请在主机的普通 PowerShell 中运行此脚本；如果那里也失败，再检查 Windows Hypervisor Platform。'; exit 1 }
    & $emulator -avd Lingye_Validation -gpu swiftshader_indirect -accel on -no-snapshot
}

