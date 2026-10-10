@echo off
title OmniRecommend AI - Desktop GUI Application
cls
echo ====================================================================
echo   OMNIRECOMMEND AI: DESKTOP APPLICATION (SWING)
echo ====================================================================
echo.

where javac >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java Development Kit (JDK) not found in PATH!
    pause
    exit /b 1
)

if not exist "bin" mkdir bin

echo [1/2] Compiling Desktop Application...
javac -cp "bin;lib/*" -d bin src\com\guvi\recommend\config\*.java src\com\guvi\recommend\db\*.java src\com\guvi\recommend\db\dao\*.java src\com\guvi\recommend\exception\*.java src\com\guvi\recommend\gui\*.java src\com\guvi\recommend\gui\components\*.java src\com\guvi\recommend\model\*.java src\com\guvi\recommend\service\*.java src\com\guvi\recommend\util\*.java src\com\guvi\recommend\web\*.java
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b 1
)

echo [SUCCESS] Compilation completed!
echo.
echo [2/2] Launching Java Desktop Swing GUI...
java -cp "bin;lib/*" com.guvi.recommend.gui.OmniRecommendDesktopApp
