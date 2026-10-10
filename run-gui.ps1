# OmniRecommend AI Desktop GUI Runner for PowerShell
Write-Host "====================================================================" -ForegroundColor Cyan
Write-Host "   OMNIRECOMMEND AI: DESKTOP APPLICATION (SWING)                   " -ForegroundColor Green
Write-Host "====================================================================" -ForegroundColor Cyan

if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

Write-Host "[1/2] Compiling Java Desktop Application..." -ForegroundColor Yellow
$javaFiles = Get-ChildItem -Path "src" -Filter "*.java" -Recurse | Select-Object -ExpandProperty FullName
javac -cp "bin;lib/*" -d bin $javaFiles

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Compilation failed!" -ForegroundColor Red
    Exit 1
}

Write-Host "[SUCCESS] Compilation completed!" -ForegroundColor Green
Write-Host "[2/2] Launching Java Desktop Swing GUI..." -ForegroundColor Yellow
java -cp "bin;lib/*" com.guvi.recommend.gui.OmniRecommendDesktopApp
