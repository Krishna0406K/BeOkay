@echo off
echo ========================================
echo   ManoDost AI - Keep Connection Alive
echo ========================================
echo.
echo This script will maintain ADB reverse connection.
echo Keep this window open while using the app!
echo.
echo Press Ctrl+C to stop.
echo.

set ADB="%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"

:loop
    echo [%time%] Checking ADB reverse connection...
    
    REM Check if reverse is active
    %ADB% reverse --list | findstr "tcp:8000" >nul
    
    if %errorLevel% neq 0 (
        echo [%time%] Connection lost! Re-establishing...
        %ADB% reverse tcp:8000 tcp:8000
        
        if %errorLevel% equ 0 (
            echo [%time%] Connection restored! ✓
        ) else (
            echo [%time%] Failed to restore connection! ✗
            echo [%time%] Please check if device is still connected.
        )
    ) else (
        echo [%time%] Connection active ✓
    )
    
    REM Wait 10 seconds before checking again
    timeout /t 10 /nobreak >nul
    
goto loop
