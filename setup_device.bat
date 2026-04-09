@echo off
echo ========================================
echo   ManoDost AI - Physical Device Setup
echo ========================================
echo.

echo Setting up ADB reverse for port 8000...
"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" reverse tcp:8000 tcp:8000

if %errorLevel% equ 0 (
    echo.
    echo ========================================
    echo   SUCCESS!
    echo ========================================
    echo.
    echo ADB reverse configured successfully.
    echo Your physical device can now access the backend at localhost:8000
    echo.
    echo Next steps:
    echo 1. Make sure backend is running: python app_api_no_auth.py
    echo 2. Rebuild the app in Android Studio
    echo 3. Run on your physical device
    echo 4. Chat should work!
    echo.
) else (
    echo.
    echo ========================================
    echo   FAILED!
    echo ========================================
    echo.
    echo Could not set up ADB reverse.
    echo Please check:
    echo 1. Device is connected via USB
    echo 2. USB debugging is enabled
    echo 3. Device is authorized
    echo.
)

pause
