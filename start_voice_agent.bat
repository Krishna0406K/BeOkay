@echo off
color 0B
title ManoDost AI - Voice Agent

echo ========================================
echo   ManoDost AI - Voice Agent Starting
echo ========================================
echo.

REM Check if Python is installed
python --version >nul 2>&1
if %errorLevel% neq 0 (
    color 0C
    echo ✗ Python is not installed!
    echo.
    echo Please install Python 3.8+ from:
    echo https://www.python.org/downloads/
    echo.
    pause
    exit /b 1
)

echo ✓ Python found
echo.

REM Check if virtual environment exists
if not exist "venv" (
    echo Creating virtual environment...
    python -m venv venv
    echo ✓ Virtual environment created
    echo.
)

REM Activate virtual environment
echo Activating virtual environment...
call venv\Scripts\activate.bat
echo.

REM Install/upgrade requirements
echo Installing voice agent requirements...
pip install -r requirements_voice.txt --quiet
echo ✓ Requirements installed
echo.

REM Check .env file
if not exist ".env" (
    color 0C
    echo ✗ .env file not found!
    echo.
    echo Please create .env file with:
    echo   GROQ_API_KEY=your_groq_api_key
    echo   SUPABASE_URL=your_supabase_url
    echo   SUPABASE_KEY=your_supabase_key
    echo.
    pause
    exit /b 1
)

echo ✓ .env file found
echo.

echo ========================================
echo   🎙️  Starting Voice Agent API
echo ========================================
echo.
echo   Voice API: http://localhost:8001
echo   WebSocket: ws://localhost:8001
echo.
echo   Press Ctrl+C to stop
echo.
echo ========================================
echo.

REM Start voice agent
python voice_api.py

pause
