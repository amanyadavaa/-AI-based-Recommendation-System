@echo off
title OmniRecommend AI - Phase 1 Test Runner
cls
echo ==================================================================
echo       COMPILING & EXECUTING PHASE 1 AUTOMATED TEST SUITE          
echo ==================================================================
echo.

if not exist "bin" mkdir bin

javac -d bin src\com\guvi\recommend\config\*.java src\com\guvi\recommend\model\*.java src\com\guvi\recommend\util\*.java src\com\guvi\recommend\test\*.java
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Test compilation failed!
    pause
    exit /b 1
)

java -cp bin com.guvi.recommend.test.Phase1TestSuite
pause
