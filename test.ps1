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

Write-Host "`n[1/6] Running Phase 1 Test Suite..." -ForegroundColor Yellow
java -cp "bin;lib/*" com.guvi.recommend.test.Phase1TestSuite
if ($LASTEXITCODE -ne 0) { Exit 1 }

Write-Host "`n[2/6] Running Phase 2 Test Suite..." -ForegroundColor Yellow
java -cp "bin;lib/*" com.guvi.recommend.test.Phase2TestSuite
if ($LASTEXITCODE -ne 0) { Exit 1 }

Write-Host "`n[3/6] Running Phase 3 Test Suite (OOP, JDBC & Desktop GUI)..." -ForegroundColor Yellow
java -cp "bin;lib/*" com.guvi.recommend.test.Phase3TestSuite
if ($LASTEXITCODE -ne 0) { Exit 1 }

Write-Host "`n[4/6] Running Phase 4 Test Suite (Admin, Analytics & REST API)..." -ForegroundColor Yellow
java -cp "bin;lib/*" com.guvi.recommend.test.Phase4TestSuite
if ($LASTEXITCODE -ne 0) { Exit 1 }

Write-Host "`n[5/6] Running Phase 5 Test Suite (Web Dashboard & Interactive UI)..." -ForegroundColor Yellow
java -cp "bin;lib/*" com.guvi.recommend.test.Phase5TestSuite
if ($LASTEXITCODE -ne 0) { Exit 1 }

Write-Host "`n[6/6] Running Phase 6 Test Suite (Verification, Packaging & UI Benchmark)..." -ForegroundColor Yellow
java -cp "bin;lib/*" com.guvi.recommend.test.Phase6TestSuite
if ($LASTEXITCODE -ne 0) { Exit 1 }

Write-Host "`n==================================================================" -ForegroundColor Green
Write-Host "  ALL TEST SUITES (PHASES 1, 2, 3, 4, 5 & 6) PASSED SUCCESSFULLY!    " -ForegroundColor Green
Write-Host "==================================================================" -ForegroundColor Green
