@echo off
echo ========================================================
echo   Iniciando Backend con Docker Compose (PostgreSQL + FastAPI)
echo ========================================================
cd /d "%~dp0..\Backend"
"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" reverse tcp:8000 tcp:8000
echo.
echo Construyendo y levantando contenedores Docker...
docker compose up --build
pause
