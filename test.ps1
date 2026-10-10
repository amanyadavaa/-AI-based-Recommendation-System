# OmniRecommend AI - Automated Test Runner for PowerShell
Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host "      COMPILING & EXECUTING AUTOMATED TEST SUITES                 " -ForegroundColor Green
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

Write-Host "`n[1/2] Running Phase 1 Test Suite..." -ForegroundColor Yellow
java -cp bin com.guvi.recommend.test.Phase1TestSuite
if ($LASTEXITCODE -ne 0) { Exit 1 }

Write-Host "`n[2/2] Running Phase 2 Test Suite..." -ForegroundColor Yellow
java -cp bin com.guvi.recommend.test.Phase2TestSuite
if ($LASTEXITCODE -ne 0) { Exit 1 }

Write-Host "`n==================================================================" -ForegroundColor Cyan
Write-Host " ALL TEST SUITES (PHASE 1 & PHASE 2) PASSED SUCCESSFULLY!         " -ForegroundColor Green
Write-Host "==================================================================" -ForegroundColor Cyan
