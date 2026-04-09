@echo off
color 0B
title ManoDost AI - Complete Service Startup

echo ========================================
echo   ManoDost AI - Starting All Services
echo ========================================
echo.

REM Check if Python is installed
python --version >nul 2>&1
if %errorLevel% neq 0 (
    color 0C
    echo ✗ Python is not installed or not in PATH
    echo.
    echo Please install Python 3.8+ and try again.
    pause
    exit /b 1
)

echo ✓ Python found
echo.

REM Install/Update dependencies
echo [1/4] Installing Python dependencies...
pip install -q flask-socketio groq 2>nul
if %errorLevel% equ 0 (
    echo ✓ Dependencies installed
) else (
    echo ⚠ Some dependencies may already be installed
)
echo.

REM Start Chat API (Port 8000)
echo [2/4] Starting Chat API on port 8000...
start "ManoDost Chat API" cmd /k "python app_api_no_auth.py"
timeout /t 3 /nobreak >nul
curl -s http://localhost:8000/health >nul 2>&1
if %errorLevel% equ 0 (
    echo ✓ Chat API is running
) else (
    echo ⚠ Chat API may still be starting...
)
echo.

REM Start Voice API (Port 8001)
echo [3/4] Starting Voice API on port 8001...
start "ManoDost Voice API" cmd /k "python voice_api.py"
timeout /t 3 /nobreak >nul
curl -s http://localhost:8001/voice/health >nul 2>&1
if %errorLevel% equ 0 (
    echo ✓ Voice API is running
) else (
    echo ⚠ Voice API may still be starting...
)
echo.

REM Setup ADB for physical device (optional)
echo [4/4] Setting up device connection (optional)...
set ADB="%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"

if exist %ADB% (
    %ADB% devices | findstr /R "device$" >nul 2>&1
    if %errorLevel% equ 0 (
        echo ✓ Device detected, setting up ADB reverse...
        %ADB% reverse tcp:8000 tcp:8000 >nul 2>&1
        %ADB% reverse tcp:8001 tcp:8001 >nul 2>&1
        echo ✓ ADB reverse configured for ports 8000 and 8001
    ) else (
        echo ⚠ No device connected (skip if using emulator)
    )
) else (
    echo ⚠ ADB not found (skip if using emulator)
)
echo.

REM Summary
echo ========================================
echo   🎉 ALL SERVICES STARTED!
echo ========================================
echo.
echo   Chat API:  http://localhost:8000
echo   Voice API: http://localhost:8001
echo.
echo   You can now:
echo   1. Open Android Studio
echo   2. Select your device (physical or emulator)
echo   3. Click Run
echo   4. Start using the app!
echo.
echo   To stop services: Close the terminal windows
echo.
echo ========================================

REM Keep this window open
echo.
echo Press any key to check service status...
pause >nul

REM Check status
cls
echo ========================================
echo   SERVICE STATUS CHECK
echo ========================================
echo.

echo Checking Chat API (port 8000)...
curl -s http://localhost:8000/health
echo.
echo.

echo Checking Voice API (port 8001)...
curl -s http://localhost:8001/voice/health
echo.
echo.

echo ========================================
echo   All services are running!
echo   Keep these windows open while using the app.
echo ========================================
echo.

pause
