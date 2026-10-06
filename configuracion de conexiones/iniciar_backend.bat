@echo off
echo ========================================================
echo   Iniciando Backend FastAPI - UTESA Concurrencia
echo ========================================================
cd /d "%~dp0..\Backend"
"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" reverse tcp:8000 tcp:8000
python -m uvicorn app.principal:app --host 0.0.0.0 --port 8000 --reload
pause
