@echo off
color 0A
echo ========================================
echo   ManoDost AI - System Check
echo ========================================
echo.

set ADB="%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
set PASS=0
set FAIL=0

REM Check 1: ADB exists
echo [1/5] Checking if ADB is installed...
if exist %ADB% (
    echo       ✓ ADB found
    set /a PASS+=1
) else (
    echo       ✗ ADB not found at %ADB%
    set /a FAIL+=1
)
echo.

REM Check 2: Device connected
echo [2/5] Checking if device is connected...
%ADB% devices | findstr /R "device$" >nul
if %errorLevel% equ 0 (
    echo       ✓ Device connected
    set /a PASS+=1
    
    REM Show device info
    for /f "tokens=1" %%i in ('%ADB% devices ^| findstr /R "device$"') do (
        echo       Device ID: %%i
    )
) else (
    echo       ✗ No device connected
    echo       Please connect your phone via USB and enable USB debugging
    set /a FAIL+=1
)
echo.

REM Check 3: ADB reverse active
echo [3/5] Checking if ADB reverse is active...
%ADB% reverse --list | findstr "tcp:8000" >nul
if %errorLevel% equ 0 (
    echo       ✓ ADB reverse is active for port 8000
    set /a PASS+=1
) else (
    echo       ✗ ADB reverse is NOT active
    echo       Run: reconnect_device.bat
    set /a FAIL+=1
)
echo.

REM Check 4: Backend running
echo [4/5] Checking if backend is running...
curl -s http://localhost:8000/health >nul 2>&1
if %errorLevel% equ 0 (
    echo       ✓ Backend is running on port 8000
    set /a PASS+=1
) else (
    echo       ✗ Backend is NOT running
    echo       Run: python app_api_no_auth.py
    set /a FAIL+=1
)
echo.

REM Check 5: Backend health
echo [5/5] Checking backend health...
curl -s http://localhost:8000/health | findstr "healthy" >nul 2>&1
if %errorLevel% equ 0 (
    echo       ✓ Backend is healthy
    set /a PASS+=1
    curl -s http://localhost:8000/health
) else (
    echo       ✗ Backend health check failed
    set /a FAIL+=1
)
echo.

REM Summary
echo ========================================
echo   SUMMARY
echo ========================================
echo   Passed: %PASS%/5
echo   Failed: %FAIL%/5
echo.

if %FAIL% equ 0 (
    color 0A
    echo   🎉 ALL CHECKS PASSED!
    echo   You're ready to run the app on your phone!
    echo.
    echo   Next steps:
    echo   1. Open Android Studio
    echo   2. Select your physical device
    echo   3. Click Run
    echo   4. Start chatting!
) else (
    color 0C
    echo   ⚠️  SOME CHECKS FAILED
    echo.
    echo   Quick fixes:
    if %FAIL% gtr 2 (
        echo   - Run: reconnect_device.bat
        echo   - Make sure phone is connected via USB
        echo   - Enable USB Debugging on phone
    )
    echo   - If backend not running: python app_api_no_auth.py
    echo   - If ADB reverse not active: reconnect_device.bat
)

echo.
echo ========================================
pause
