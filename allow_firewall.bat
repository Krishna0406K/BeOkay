@echo off
echo ========================================
echo   Adding Firewall Rule for Port 8000
echo ========================================
echo.

REM Check if running as administrator
net session >nul 2>&1
if %errorLevel% neq 0 (
    echo ERROR: This script must be run as Administrator
    echo.
    echo Right-click this file and select "Run as administrator"
    echo.
    pause
    exit /b 1
)

echo Adding firewall rule to allow incoming connections on port 8000...
echo.

REM Delete existing rule if it exists
netsh advfirewall firewall delete rule name="ManoDost Backend Port 8000" >nul 2>&1

REM Add new rule
netsh advfirewall firewall add rule name="ManoDost Backend Port 8000" dir=in action=allow protocol=TCP localport=8000

if %errorLevel% equ 0 (
    echo.
    echo ========================================
    echo   SUCCESS!
    echo ========================================
    echo.
    echo Firewall rule added successfully.
    echo Your Android device can now connect to port 8000.
    echo.
) else (
    echo.
    echo ========================================
    echo   FAILED!
    echo ========================================
    echo.
    echo Could not add firewall rule.
    echo Please check Windows Firewall settings manually.
    echo.
)

pause
