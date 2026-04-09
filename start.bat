@echo off
echo ========================================
echo   ManoDost AI - Backend Startup
echo ========================================
echo.

REM Check if Python is installed
python --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Python is not installed or not in PATH
    echo Please install Python 3.x and try again
    pause
    exit /b 1
)

echo Starting backend server...
echo.
echo Backend will be accessible at:
echo   - From this computer: http://localhost:8000
echo   - From Android emulator: http://10.0.2.2:8000
echo.
echo Press Ctrl+C to stop the server
echo.

REM Start the backend
python start_backend.py

pause
