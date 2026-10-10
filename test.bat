@echo off
title OmniRecommend AI - Automated Test Runner
cls
echo ==================================================================
echo       COMPILING & EXECUTING AUTOMATED VERIFICATION TEST SUITE     
echo ==================================================================
echo.

if not exist "bin" mkdir bin

javac -cp "bin;lib/*" -d bin src\com\guvi\recommend\config\*.java src\com\guvi\recommend\db\*.java src\com\guvi\recommend\db\dao\*.java src\com\guvi\recommend\exception\*.java src\com\guvi\recommend\gui\*.java src\com\guvi\recommend\gui\components\*.java src\com\guvi\recommend\model\*.java src\com\guvi\recommend\service\*.java src\com\guvi\recommend\util\*.java src\com\guvi\recommend\web\*.java src\com\guvi\recommend\test\*.java
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Test compilation failed!
    pause
    exit /b 1
)

echo [1/7] Running Phase 1 Test Suite...
java -cp "bin;lib/*" com.guvi.recommend.test.Phase1TestSuite
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Phase 1 tests failed!
    pause
    exit /b 1
)

echo.
echo [2/7] Running Phase 2 Test Suite...
java -cp "bin;lib/*" com.guvi.recommend.test.Phase2TestSuite
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Phase 2 tests failed!
    pause
    exit /b 1
)

echo.
echo [3/7] Running Phase 3 Test Suite (OOP, JDBC & Desktop GUI)...
java -cp "bin;lib/*" com.guvi.recommend.test.Phase3TestSuite
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Phase 3 tests failed!
    pause
    exit /b 1
)

echo.
echo [4/7] Running Phase 4 Test Suite (Admin, Analytics & REST API)...
java -cp "bin;lib/*" com.guvi.recommend.test.Phase4TestSuite
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Phase 4 tests failed!
    pause
    exit /b 1
)

echo.
echo [5/7] Running Phase 5 Test Suite (Web Dashboard & Interactive UI)...
java -cp "bin;lib/*" com.guvi.recommend.test.Phase5TestSuite
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Phase 5 tests failed!
    pause
    exit /b 1
)

echo.
echo [6/7] Running Phase 6 Test Suite (Verification, Packaging & UI Benchmark)...
java -cp "bin;lib/*" com.guvi.recommend.test.Phase6TestSuite
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Phase 6 tests failed!
    pause
    exit /b 1
)

echo.
echo [7/7] Running Phase 7 Test Suite (Security, Online Learning & DevOps)...
java -cp "bin;lib/*" com.guvi.recommend.test.Phase7TestSuite
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Phase 7 tests failed!
    pause
    exit /b 1
)

echo.
echo ==================================================================
echo  ALL TEST SUITES (PHASES 1, 2, 3, 4, 5, 6 & 7) PASSED SUCCESSFULLY!
echo ==================================================================
pause
