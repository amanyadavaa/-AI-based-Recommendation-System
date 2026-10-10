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

echo [1/3] Running Phase 1 Test Suite...
java -cp "bin;lib/*" com.guvi.recommend.test.Phase1TestSuite
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Phase 1 tests failed!
    pause
    exit /b 1
)

echo.
echo [2/3] Running Phase 2 Test Suite...
java -cp "bin;lib/*" com.guvi.recommend.test.Phase2TestSuite
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Phase 2 tests failed!
    pause
    exit /b 1
)

echo.
echo [3/3] Running Phase 3 Test Suite (OOP, JDBC & Desktop GUI)...
java -cp "bin;lib/*" com.guvi.recommend.test.Phase3TestSuite
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Phase 3 tests failed!
    pause
    exit /b 1
)

echo.
echo ==================================================================
echo  ALL TEST SUITES (PHASE 1, PHASE 2, PHASE 3) PASSED SUCCESSFULLY!
echo ==================================================================
pause
