@echo off
setlocal
cd /d "%~dp0"
title OmniRecommend AI - Universal Recommendation System
cls
echo ====================================================================
echo    OMNIRECOMMEND AI: UNIVERSAL RECOMMENDATION SYSTEM
echo ====================================================================
echo.

where javac >nul 2>&1
if errorlevel 1 goto check_precompiled
goto compile_sources

:check_precompiled
if exist "bin\com\guvi\recommend\Main.class" (
    echo [INFO] JDK javac not in PATH, running pre-compiled binary...
    goto start_server
)
echo [ERROR] JDK javac compiler was not found in system PATH!
echo Please install JDK 17+ or ensure Java Development Kit is in your PATH.
echo.
pause
exit /b 1

:compile_sources
if not exist "bin" mkdir bin

echo [1/2] Compiling Java source code...
javac -cp "bin;lib/*" -d bin src\com\guvi\recommend\*.java src\com\guvi\recommend\config\*.java src\com\guvi\recommend\db\*.java src\com\guvi\recommend\db\dao\*.java src\com\guvi\recommend\exception\*.java src\com\guvi\recommend\gui\*.java src\com\guvi\recommend\gui\components\*.java src\com\guvi\recommend\model\*.java src\com\guvi\recommend\service\*.java src\com\guvi\recommend\util\*.java src\com\guvi\recommend\web\*.java src\com\guvi\recommend\web\security\*.java
if errorlevel 1 goto compile_error
echo [SUCCESS] Compilation completed successfully!
echo.
goto start_server

:compile_error
echo [ERROR] Compilation failed!
echo.
pause
exit /b 1

:start_server
echo [2/2] Launching Universal AI Recommendation Server...
echo --------------------------------------------------------------------
echo Web Interface URL: http://localhost:8080/
echo --------------------------------------------------------------------
echo.

REM Automatically open default browser after 2 seconds
start "" cmd /c "timeout /t 2 >nul & start http://localhost:8080/"

java -cp "bin;lib/*" com.guvi.recommend.Main 8080
if errorlevel 1 (
    echo.
    echo [NOTICE] Application process stopped.
)
echo.
echo Press any key to close this terminal...
pause >nul
