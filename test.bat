@echo off
title OmniRecommend AI - Automated Test Runner
cls
echo ==================================================================
echo       COMPILING & EXECUTING AUTOMATED VERIFICATION TEST SUITE     
echo ==================================================================
echo.

if not exist "bin" mkdir bin

javac -d bin src\com\guvi\recommend\config\*.java src\com\guvi\recommend\model\*.java src\com\guvi\recommend\service\*.java src\com\guvi\recommend\util\*.java src\com\guvi\recommend\test\*.java
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Test compilation failed!
    pause
    exit /b 1
)

echo [1/2] Running Phase 1 Test Suite...
java -cp bin com.guvi.recommend.test.Phase1TestSuite
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Phase 1 tests failed!
    pause
    exit /b 1
)

echo.
echo [2/2] Running Phase 2 Test Suite...
java -cp bin com.guvi.recommend.test.Phase2TestSuite
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Phase 2 tests failed!
    pause
    exit /b 1
)

echo.
echo ==================================================================
echo  ALL TEST SUITES (PHASE 1 & PHASE 2) PASSED SUCCESSFULLY!
echo ==================================================================
pause
