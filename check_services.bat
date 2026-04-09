@echo off
color 0A
title ManoDost AI - Service Status Check

echo ========================================
echo   ManoDost AI - Service Status
echo ========================================
echo.

set PASS=0
set FAIL=0

REM Check Chat API
echo [1/2] Checking Chat API (port 8000)...
curl -s http://localhost:8000/health >nul 2>&1
if %errorLevel% equ 0 (
    echo       ✓ Chat API is running
    curl -s http://localhost:8000/health
    set /a PASS+=1
) else (
    echo       ✗ Chat API is NOT running
    echo       Run: start_all_services.bat
    set /a FAIL+=1
)
echo.

REM Check Voice API
echo [2/2] Checking Voice API (port 8001)...
curl -s http://localhost:8001/voice/health >nul 2>&1
if %errorLevel% equ 0 (
    echo       ✓ Voice API is running
    curl -s http://localhost:8001/voice/health
    set /a PASS+=1
) else (
    echo       ✗ Voice API is NOT running
    echo       Run: start_all_services.bat
    set /a FAIL+=1
)
echo.

REM Summary
echo ========================================
echo   SUMMARY
echo ========================================
echo   Running: %PASS%/2
echo   Stopped: %FAIL%/2
echo.

if %FAIL% equ 0 (
    color 0A
    echo   🎉 ALL SERVICES ARE RUNNING!
    echo.
    echo   You can now run the app in Android Studio.
) else (
    color 0C
    echo   ⚠️  SOME SERVICES ARE NOT RUNNING
    echo.
    echo   Please run: start_all_services.bat
)

echo.
echo ========================================
pause
