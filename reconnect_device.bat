@echo off
echo ========================================
echo   ManoDost AI - Device Reconnection
echo ========================================
echo.

set ADB="%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"

echo Step 1: Checking connected devices...
%ADB% devices
echo.

echo Step 2: Killing ADB server...
%ADB% kill-server
timeout /t 2 /nobreak >nul

echo Step 3: Starting ADB server...
%ADB% start-server
timeout /t 2 /nobreak >nul
echo.

echo Step 4: Checking devices again...
%ADB% devices
echo.

echo Step 5: Setting up ADB reverse for port 8000...
%ADB% reverse tcp:8000 tcp:8000
echo.

if %errorLevel% equ 0 (
    echo ========================================
    echo   SUCCESS!
    echo ========================================
    echo.
    echo ADB reverse configured successfully.
    echo Device localhost:8000 now forwards to PC localhost:8000
    echo.
    echo IMPORTANT: Keep this window open while using the app!
    echo If you close it, the connection may be lost.
    echo.
    echo To verify connection is active, run:
    echo   %ADB% reverse --list
    echo.
) else (
    echo ========================================
    echo   FAILED!
    echo ========================================
    echo.
    echo Could not set up ADB reverse.
    echo.
    echo TROUBLESHOOTING:
    echo 1. Make sure your phone is connected via USB cable
    echo 2. On your phone, enable USB Debugging:
    echo    - Go to Settings ^> About Phone
    echo    - Tap "Build Number" 7 times to enable Developer Options
    echo    - Go to Settings ^> Developer Options
    echo    - Enable "USB Debugging"
    echo 3. When you connect, your phone should show a popup asking
    echo    "Allow USB debugging?" - tap "Allow"
    echo 4. Run this script again
    echo.
)

echo.
echo Press any key to check backend status...
pause >nul

echo.
echo Step 6: Checking if backend is running...
curl -s http://localhost:8000/health
echo.

if %errorLevel% equ 0 (
    echo Backend is running! ✓
) else (
    echo Backend is NOT running! ✗
    echo Please start it with: python app_api_no_auth.py
)

echo.
pause
