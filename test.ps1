# OmniRecommend AI - Phase 1 Test Runner for PowerShell
Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host "      COMPILING & EXECUTING PHASE 1 AUTOMATED TEST SUITE          " -ForegroundColor Green
Write-Host "==================================================================" -ForegroundColor Cyan

if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

$testFiles = Get-ChildItem -Path "src" -Filter "*.java" -Recurse | Select-Object -ExpandProperty FullName
javac -d bin $testFiles

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Test compilation failed!" -ForegroundColor Red
    Exit 1
}

java -cp bin com.guvi.recommend.test.Phase1TestSuite
