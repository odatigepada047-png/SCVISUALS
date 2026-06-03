# Builds scvis_native.dll (JNI) for Windows x64
$ErrorActionPreference = "Stop"
$root = $PSScriptRoot
$nativeDir = Join-Path $root "native"
$buildDir = Join-Path $nativeDir "build"
$outDll = Join-Path $buildDir "Release\scvis_native.dll"
$destDir = Join-Path $root "src\client\resources\native\win_x64"
$destDll = Join-Path $destDir "scvis_native.dll"

if (-not $env:JAVA_HOME) {
    $javaCmd = Get-Command java -ErrorAction SilentlyContinue
    if ($javaCmd) {
        $env:JAVA_HOME = (Split-Path (Split-Path $javaCmd.Source))
    }
}
if (-not $env:JAVA_HOME) {
    throw "JAVA_HOME is not set. Point it to your JDK (21+)."
}

$cmake = Get-Command cmake -ErrorAction SilentlyContinue
if (-not $cmake) {
    $vsCmake = "C:\Program Files\Microsoft Visual Studio\2022\Community\Common7\IDE\CommonExtensions\Microsoft\CMake\CMake\bin\cmake.exe"
    if (Test-Path $vsCmake) { $cmake = $vsCmake } else { throw "cmake not found" }
} else {
    $cmake = $cmake.Source
}

Write-Host "Configuring native build (JAVA_HOME=$env:JAVA_HOME)..."
if (-not (Test-Path (Join-Path $buildDir "CMakeCache.txt"))) {
    New-Item -ItemType Directory -Force -Path $buildDir | Out-Null
    & $cmake -S $nativeDir -B $buildDir -G "Visual Studio 17 2022" -A x64
    if ($LASTEXITCODE -ne 0) { throw "cmake configure failed" }
}

Write-Host "Building scvis_native.dll..."
& $cmake --build $buildDir --config Release
if ($LASTEXITCODE -ne 0) { throw "cmake build failed" }

if (-not (Test-Path $outDll)) {
    throw "Build failed: $outDll not found"
}

New-Item -ItemType Directory -Force -Path $destDir | Out-Null
Copy-Item -Force $outDll $destDll
Copy-Item -Force $outDll (Join-Path $root "native\scvis_native.dll")

Write-Host "Done:"
Write-Host "  $destDll"
