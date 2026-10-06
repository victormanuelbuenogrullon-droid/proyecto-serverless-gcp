

set ADB="%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"

echo Dispositivos detectados:
%ADB% devices
echo.

for /f "skip=1 tokens=1" %%d in ('%ADB% devices') do (
    if not "%%d"=="" (
        if not "%%d"=="offline" (
            echo Configurando puerto 8000 en dispositivo: %%d
            %ADB% -s %%d reverse tcp:8000 tcp:8000
        )
    )
)

