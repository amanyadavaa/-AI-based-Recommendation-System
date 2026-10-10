@echo off
setlocal
cd /d "%~dp0"
title OmniRecommend AI - Desktop GUI Application
cls
echo ====================================================================
echo   OMNIRECOMMEND AI: DESKTOP APPLICATION (SWING)
echo ====================================================================
echo.

where javac >nul 2>&1
if errorlevel 1 goto check_precompiled
goto compile_sources

:check_precompiled
if exist "bin\com\guvi\recommend\gui\OmniRecommendDesktopApp.class" (
    echo [INFO] JDK javac not in PATH, running pre-compiled binary...
    goto start_gui
)
echo [ERROR] JDK javac compiler was not found in system PATH!
echo Please install JDK 17+ or ensure Java Development Kit is in your PATH.
echo.
pause
exit /b 1

:compile_sources
if not exist "bin" mkdir bin

echo [1/2] Compiling Desktop Application...
javac -cp "bin;lib/*" -d bin src\com\guvi\recommend\*.java src\com\guvi\recommend\config\*.java src\com\guvi\recommend\db\*.java src\com\guvi\recommend\db\dao\*.java src\com\guvi\recommend\exception\*.java src\com\guvi\recommend\gui\*.java src\com\guvi\recommend\gui\components\*.java src\com\guvi\recommend\model\*.java src\com\guvi\recommend\service\*.java src\com\guvi\recommend\util\*.java src\com\guvi\recommend\web\*.java src\com\guvi\recommend\web\security\*.java
if errorlevel 1 goto compile_error
echo [SUCCESS] Compilation completed!
echo.
goto start_gui

:compile_error
echo [ERROR] Compilation failed!
echo.
pause
exit /b 1

:start_gui
echo [2/2] Launching Java Desktop Swing GUI...
java -cp "bin;lib/*" com.guvi.recommend.gui.OmniRecommendDesktopApp
if errorlevel 1 (
    echo.
    echo [NOTICE] Desktop application closed.
)
echo.
echo Press any key to close this terminal...
pause >nul
