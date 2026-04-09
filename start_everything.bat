@echo off
color 0B
title ManoDost AI - Complete Startup

echo ========================================
echo   ManoDost AI - Complete Startup
echo ========================================
echo.
echo This will:
echo   1. Check device connection
echo   2. Set up ADB reverse
echo   3. Start the backend
echo   4. Keep connection alive
echo.
echo Press any key to start...
pause >nul
cls

set ADB="%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"

REM Step 1: Check device
echo ========================================
echo   Step 1: Checking Device Connection
echo ========================================
echo.

%ADB% devices
echo.

%ADB% devices | findstr /R "device$" >nul
if %errorLevel% neq 0 (
    color 0C
    echo ✗ No device found!
    echo.
    echo Please:
    echo   1. Connect your phone via USB
    echo   2. Enable USB Debugging
    echo   3. Tap "Allow" on the popup
    echo   4. Run this script again
    echo.
    pause
    exit /b 1
)

echo ✓ Device connected!
echo.
pause

REM Step 2: Set up ADB reverse
cls
echo ========================================
echo   Step 2: Setting Up ADB Reverse
echo ========================================
echo.

echo Restarting ADB server...
%ADB% kill-server
timeout /t 2 /nobreak >nul
%ADB% start-server
timeout /t 2 /nobreak >nul
echo.

echo Setting up port forwarding...
%ADB% reverse tcp:8000 tcp:8000
echo.

if %errorLevel% equ 0 (
    echo ✓ ADB reverse configured!
    echo   Device localhost:8000 → PC localhost:8000
) else (
    color 0C
    echo ✗ Failed to set up ADB reverse
    echo.
    pause
    exit /b 1
)
echo.
pause

REM Step 3: Check backend
cls
echo ========================================
echo   Step 3: Checking Backend
echo ========================================
echo.

curl -s http://localhost:8000/health >nul 2>&1
if %errorLevel% equ 0 (
    echo ✓ Backend is already running!
    curl -s http://localhost:8000/health
    echo.
    echo.
    echo Backend is ready. Skipping to connection monitor...
    timeout /t 3 /nobreak >nul
) else (
    echo Backend is not running.
    echo.
    echo Starting backend in a new window...
    start "ManoDost Backend" cmd /k "python app_api_no_auth.py"
    echo.
    echo Waiting for backend to start...
    timeout /t 5 /nobreak >nul
    
    REM Check again
    curl -s http://localhost:8000/health >nul 2>&1
    if %errorLevel% equ 0 (
        echo ✓ Backend started successfully!
    ) else (
        color 0C
        echo ✗ Backend failed to start
        echo.
        echo Please check the backend window for errors.
        echo.
        pause
        exit /b 1
    )
)
echo.
pause

REM Step 4: Keep connection alive
cls
echo ========================================
echo   Step 4: Connection Monitor
echo ========================================
echo.
echo Starting connection monitor...
echo This will keep ADB reverse active.
echo.
echo ✓ All systems ready!
echo.
echo You can now:
echo   1. Open Android Studio
echo   2. Select your physical device
echo   3. Click Run
echo   4. Start chatting!
echo.
echo This window will monitor the connection.
echo Keep it open while using the app!
echo.
echo Press Ctrl+C to stop.
echo.
echo ========================================
echo.

:loop
    echo [%time%] Checking connection...
    
    %ADB% reverse --list | findstr "tcp:8000" >nul
    
    if %errorLevel% neq 0 (
        echo [%time%] Connection lost! Restoring...
        %ADB% reverse tcp:8000 tcp:8000
        
        if %errorLevel% equ 0 (
            echo [%time%] ✓ Connection restored
        ) else (
            color 0C
            echo [%time%] ✗ Failed to restore connection
            echo [%time%] Please check if device is still connected
            color 0B
        )
    ) else (
        echo [%time%] ✓ Connection active
    )
    
    timeout /t 10 /nobreak >nul
    
goto loop
