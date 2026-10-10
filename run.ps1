# OmniRecommend AI Runner for PowerShell
Write-Host "====================================================================" -ForegroundColor Cyan
Write-Host "   GUVI PROJECT #69: AI-BASED UNIVERSAL RECOMMENDATION SYSTEM      " -ForegroundColor Green
Write-Host "====================================================================" -ForegroundColor Cyan

if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

Write-Host "[1/2] Compiling Java source files..." -ForegroundColor Yellow
$javaFiles = Get-ChildItem -Path "src" -Filter "*.java" -Recurse | Select-Object -ExpandProperty FullName
javac -cp "bin;lib/*" -d bin $javaFiles

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Compilation failed!" -ForegroundColor Red
    Exit 1
}

Write-Host "[SUCCESS] Compilation completed!" -ForegroundColor Green
Write-Host "[2/2] Starting server at http://localhost:8080/ ..." -ForegroundColor Yellow

Start-Process "http://localhost:8080/"
java -cp "bin;lib/*" com.guvi.recommend.Main 8080
