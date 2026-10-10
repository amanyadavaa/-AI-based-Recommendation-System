@echo off
title OmniRecommend AI - GUVI Project 69
cls
echo ====================================================================
echo    GUVI PROJECT #69: AI-BASED UNIVERSAL RECOMMENDATION SYSTEM
echo ====================================================================
echo.

:: Check Java
where javac >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java Development Kit (JDK) 'javac' not found in PATH!
    echo Please install JDK 17+ or ensure javac is in your system PATH.
    pause
    exit /b 1
)

:: Create bin directory if not exists
if not exist "bin" mkdir bin

echo [1/2] Compiling Java source code...
javac -cp "bin;lib/*" -d bin src\com\guvi\recommend\config\*.java src\com\guvi\recommend\db\*.java src\com\guvi\recommend\db\dao\*.java src\com\guvi\recommend\exception\*.java src\com\guvi\recommend\gui\*.java src\com\guvi\recommend\gui\components\*.java src\com\guvi\recommend\model\*.java src\com\guvi\recommend\service\*.java src\com\guvi\recommend\util\*.java src\com\guvi\recommend\web\*.java src\com\guvi\recommend\Main.java
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b 1
)
echo [SUCCESS] Compilation completed successfully!
echo.

echo [2/2] Launching Universal AI Recommendation Server...
echo --------------------------------------------------------------------
echo Web Interface URL: http://localhost:8080/
echo --------------------------------------------------------------------

:: Open browser automatically after 1 second in background
start "" cmd /c "timeout /t 2 >nul & start http://localhost:8080/"

java -cp "bin;lib/*" com.guvi.recommend.Main 8080
pause
