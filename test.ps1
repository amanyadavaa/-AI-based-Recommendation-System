# OmniRecommend AI Test Runner for PowerShell
Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host "      COMPILING & EXECUTING AUTOMATED VERIFICATION TEST SUITE     " -ForegroundColor Green
Write-Host "==================================================================" -ForegroundColor Cyan

if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

$javaFiles = Get-ChildItem -Path "src" -Filter "*.java" -Recurse | Select-Object -ExpandProperty FullName
javac -cp "bin;lib/*" -d bin $javaFiles

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Test compilation failed!" -ForegroundColor Red
    Exit 1
}

Write-Host "`n[1/3] Running Phase 1 Test Suite..." -ForegroundColor Yellow
java -cp "bin;lib/*" com.guvi.recommend.test.Phase1TestSuite
if ($LASTEXITCODE -ne 0) { Exit 1 }

Write-Host "`n[2/3] Running Phase 2 Test Suite..." -ForegroundColor Yellow
java -cp "bin;lib/*" com.guvi.recommend.test.Phase2TestSuite
if ($LASTEXITCODE -ne 0) { Exit 1 }

Write-Host "`n[3/3] Running Phase 3 Test Suite (OOP, JDBC & Desktop GUI)..." -ForegroundColor Yellow
java -cp "bin;lib/*" com.guvi.recommend.test.Phase3TestSuite
if ($LASTEXITCODE -ne 0) { Exit 1 }

Write-Host "`n==================================================================" -ForegroundColor Green
Write-Host "  ALL TEST SUITES (PHASE 1, PHASE 2, PHASE 3) PASSED SUCCESSFULLY!" -ForegroundColor Green
Write-Host "==================================================================" -ForegroundColor Green
