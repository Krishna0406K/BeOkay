@echo off
color 0A
title Test Voice Setup

echo ========================================
echo   Testing Voice Setup
echo ========================================
echo.

echo [1/5] Checking Chat API (port 8000)...
curl -s http://localhost:8000/health >nul 2>&1
if errorlevel 1 (
    echo [X] Chat API NOT running
    echo     Run: start_both_servers.bat
) else (
    echo [OK] Chat API running
)
echo.

echo [2/5] Checking Voice API (port 8001)...
curl -s http://localhost:8001/voice/health >nul 2>&1
if errorlevel 1 (
    echo [X] Voice API NOT running
    echo     Run: start_both_servers.bat
) else (
    echo [OK] Voice API running
)
echo.

echo [3/5] Checking ADB...
set ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe
if not exist "%ADB%" (
    echo [X] ADB not found
    echo     Install Android SDK
) else (
    echo [OK] ADB found
)
echo.

echo [4/5] Checking Device Connection...
"%ADB%" devices 2>nul | findstr /R "device$" >nul
if errorlevel 1 (
    echo [X] No device connected
    echo     Connect phone or start emulator
) else (
    echo [OK] Device connected
)
echo.

echo [5/5] Checking ADB Reverse...
"%ADB%" reverse --list 2>nul | findstr "tcp:8000" >nul
if errorlevel 1 (
    echo [X] ADB reverse NOT set up
    echo     Run: fix_connection.bat
) else (
    echo [OK] ADB reverse active
)
echo.

echo ========================================
echo   Test Complete
echo ========================================
echo.
echo If all checks pass, you can run the app!
echo.
pause
