@echo off
color 0B
title ManoDost AI - Complete System

echo ========================================
echo   ManoDost AI - Complete System
echo ========================================
echo.
echo Starting both servers:
echo   1. Chat API (port 8000)
echo   2. Voice API (port 8001)
echo.
echo Press any key to start...
pause >nul
cls

REM Start Chat API in new window
start "ManoDost Chat API" cmd /k "python app_api_no_auth.py"
timeout /t 2 /nobreak >nul

REM Start Voice API in new window
start "ManoDost Voice API" cmd /k "python voice_api.py"
timeout /t 2 /nobreak >nul

echo.
echo ========================================
echo   ✓ Both servers started!
echo ========================================
echo.
echo   Chat API:  http://localhost:8000
echo   Voice API: http://localhost:8001
echo.
echo   Check the new windows for server logs.
echo   Close those windows to stop the servers.
echo.
echo ========================================
echo.

pause
