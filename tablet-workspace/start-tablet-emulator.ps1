param([switch]$Software)
$base = 'C:/Users/17482/.codex/visualizations/2026/10/28/01a18267-d2c0-7a63-96d3-472a7ae5bf6e/emulator-test'
$env:ANDROID_HOME = "$base/sdk"
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
$env:ANDROID_EMULATOR_HOME = $base
$env:ANDROID_AVD_HOME = "$base/avd"
$emulator = "$base/sdk/emulator/emulator.exe"
if (-not (Test-Path -LiteralPath $emulator)) { throw "找不到模拟器：$emulator" }
if ($Software) {
    & $emulator -avd Lingye_Validation -gpu swiftshader_indirect -accel off
} else {
    & $emulator -accel-check
    if ($LASTEXITCODE -ne 0) { Write-Host '硬件加速未就绪，请启用 Windows Hypervisor Platform 并按提示重启。也可用 -Software 尝试低速软件模拟。'; exit 1 }
    & $emulator -avd Lingye_Validation -gpu auto
}

