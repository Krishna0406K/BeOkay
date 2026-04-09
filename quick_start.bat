@echo off
REM ManoDost AI - Quick Start Script for Windows
REM This script helps you set up the development environment quickly

echo ==================================
echo ManoDost AI - Quick Start Setup
echo ==================================
echo.

REM Check Python version
echo Checking Python version...
python --version
if errorlevel 1 (
    echo X Python is not installed. Please install Python 3.8 or higher.
    pause
    exit /b 1
)
echo √ Python is installed
echo.

REM Create virtual environment
echo Creating virtual environment...
if not exist "venv" (
    python -m venv venv
    echo √ Virtual environment created
) else (
    echo i Virtual environment already exists
)
echo.

REM Activate virtual environment
echo Activating virtual environment...
call venv\Scripts\activate.bat
echo √ Virtual environment activated
echo.

REM Install dependencies
echo Installing Python dependencies...
python -m pip install --upgrade pip
pip install -r requirements.txt
echo √ Dependencies installed
echo.

REM Check for .env file
echo Checking environment configuration...
if not exist ".env" (
    echo ! .env file not found
    echo Creating .env from template...
    copy .env.example .env
    echo √ .env file created
    echo.
    echo ! IMPORTANT: Please edit .env file and add your API keys:
    echo    - GROQ_API_KEY
    echo    - SUPABASE_URL
    echo    - SUPABASE_KEY
    echo.
    pause
) else (
    echo √ .env file exists
)
echo.

REM Test Supabase connection
echo Testing Supabase connection...
python -c "from supabase_backend import SupabaseBackend; SupabaseBackend(); print('√ Supabase connection successful')" 2>nul
if errorlevel 1 (
    echo X Supabase connection failed. Please check your credentials in .env
    echo.
) else (
    echo √ Supabase connection successful
    echo.
)

REM Ask if user wants to start the API
echo ==================================
echo Setup Complete!
echo ==================================
echo.
echo What would you like to do?
echo 1. Start Flask API server
echo 2. Start terminal chat agent
echo 3. Exit
echo.
set /p choice="Enter your choice (1-3): "

if "%choice%"=="1" (
    echo.
    echo Starting Flask API server...
    echo API will be available at: http://localhost:8000
    echo Press Ctrl+C to stop
    echo.
    python app_api.py
) else if "%choice%"=="2" (
    echo.
    echo Starting terminal chat agent...
    echo Press Ctrl+C to exit
    echo.
    python agent.py
) else if "%choice%"=="3" (
    echo Goodbye!
    exit /b 0
) else (
    echo Invalid choice. Exiting.
    exit /b 1
)
