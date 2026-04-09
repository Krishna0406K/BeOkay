@echo off
color 0B
title ManoDost AI - Connection Fix

echo ========================================
echo   ManoDost AI - Connection Fix
echo ========================================
echo.

set ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe

REM Step 1: Check if servers are running
echo Step 1: Checking Backend Servers...
echo.

curl http://localhost:8000/health >nul 2>&1
if errorlevel 1 (
    echo [X] Chat API on port 8000 is NOT running
    echo     Please run: start_all_services.bat
    echo.
    pause
    exit /b 1
)
echo [OK] Chat API on port 8000 is running

curl http://localhost:8001/voice/health >nul 2>&1
if errorlevel 1 (
    echo [X] Voice API on port 8001 is NOT running
    echo     Please run: start_all_services.bat
    echo.
    pause
    exit /b 1
)
echo [OK] Voice API on port 8001 is running
echo.

REM Step 2: Check ADB
echo Step 2: Checking ADB...
if not exist "%ADB%" (
    echo [X] ADB not found
    echo     Please install Android SDK
    echo.
    pause
    exit /b 1
)
echo [OK] ADB found
echo.

REM Step 3: Check device connection
echo Step 3: Checking Device Connection...
"%ADB%" devices 2>nul | findstr /R "device$" >nul
if errorlevel 1 (
    color 0E
    echo.
    echo ========================================
    echo   NO DEVICE CONNECTED!
    echo ========================================
    echo.
    echo Please connect your device:
    echo.
    echo Option 1: Physical Device
    echo   1. Connect phone via USB cable
    echo   2. Enable USB Debugging on phone
    echo   3. Tap Allow on phone popup
    echo.
    echo Option 2: Emulator
    echo   1. Open Android Studio
    echo   2. Click Device Manager
    echo   3. Start an emulator
    echo.
    echo Then run this script again!
    echo.
    pause
    exit /b 1
)
echo [OK] Device connected
echo.

REM Step 4: Set up ADB reverse
echo Step 4: Setting up ADB Reverse...
"%ADB%" kill-server >nul 2>&1
timeout /t 1 /nobreak >nul
"%ADB%" start-server >nul 2>&1
timeout /t 2 /nobreak >nul

"%ADB%" reverse tcp:8000 tcp:8000 >nul 2>&1
if errorlevel 1 (
    echo [X] Failed to forward port 8000
) else (
    echo [OK] Port 8000 forwarded
)

"%ADB%" reverse tcp:8001 tcp:8001 >nul 2>&1
if errorlevel 1 (
    echo [X] Failed to forward port 8001
) else (
    echo [OK] Port 8001 forwarded
)
echo.

REM Step 5: Verify
echo Step 5: Verifying ADB Reverse...
"%ADB%" reverse --list 2>nul | findstr "tcp:8000" >nul
if errorlevel 1 (
    echo [X] Port 8000 reverse NOT active
) else (
    echo [OK] Port 8000 reverse active
)

"%ADB%" reverse --list 2>nul | findstr "tcp:8001" >nul
if errorlevel 1 (
    echo [X] Port 8001 reverse NOT active
) else (
    echo [OK] Port 8001 reverse active
)
echo.

REM Summary
color 0A
echo ========================================
echo   CONNECTION SETUP COMPLETE!
echo ========================================
echo.
echo Backend: Chat API and Voice API running
echo Device: Connected with ADB reverse
echo Ports: 8000 and 8001 forwarded
echo.
echo You can now run the app in Android Studio!
echo.
echo ========================================
echo.
echo Monitoring connection...
echo Keep this window open!
echo Press Ctrl+C to stop.
echo.

:monitor
    timeout /t 10 /nobreak >nul
    "%ADB%" reverse --list 2>nul | findstr "tcp:8000" >nul
    if errorlevel 1 (
        echo [%time%] Connection lost! Restoring...
        "%ADB%" reverse tcp:8000 tcp:8000 >nul 2>&1
        "%ADB%" reverse tcp:8001 tcp:8001 >nul 2>&1
        echo [%time%] Connection restored
    ) else (
        echo [%time%] Connection OK
    )
goto monitor
