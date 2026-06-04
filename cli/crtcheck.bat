@echo off
setlocal enabledelayedexpansion

set PROJECT_DIR=%~dp0..
set CLI_DIR=%~dp0
set JAVA_HOME=C:\Program Files\Zulu\zulu-17

:: Build jar if not exists
if not exist "%CLI_DIR%build\libs\crtcheck.jar" (
    echo Building CLI tool...
    cd "%PROJECT_DIR%"
    call "%PROJECT_DIR%\gradlew.bat" -p "%CLI_DIR%" jar -q --no-daemon
    if errorlevel 1 (
        echo Build failed
        exit /b 1
    )
)

:: Run syntax check
java -jar "%CLI_DIR%build\libs\crtcheck.jar" %*
