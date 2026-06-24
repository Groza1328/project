@echo off
setlocal EnableExtensions
chcp 65001 >nul 2>&1
title SibMobile

cd /d "%~dp0"
set "SCRIPTS=%~dp0scripts"
set "PG_SERVICE=postgresql-x64-17"
set "PG_BIN=C:\Program Files\PostgreSQL\17\bin"
set "PG_CTL=%PG_BIN%\pg_ctl.exe"
set "PG_DATA=C:\Program Files\PostgreSQL\17\data"
set "PGPASSWORD=Groza_13_28"

cls
echo ====================================
echo   SibMobile - PostgreSQL + site
echo ====================================
echo.
echo DB:  PostgreSQL port 5432
echo Web: http://localhost:8080
echo.

if exist "%PG_BIN%\psql.exe" set "PATH=%PG_BIN%;%PATH%"

call :ensure_java
if errorlevel 1 goto :end

if not exist "%~dp0mvnw.cmd" (
    echo [ERROR] mvnw.cmd not found in %~dp0
    goto :end
)

echo [1/3] PostgreSQL...
powershell -NoProfile -ExecutionPolicy Bypass -File "%SCRIPTS%\check-pg.ps1"
if errorlevel 1 (
    echo PostgreSQL is down. Starting service...
    powershell -NoProfile -ExecutionPolicy Bypass -File "%SCRIPTS%\start-pg.ps1"
    timeout /t 3 /nobreak >nul
    powershell -NoProfile -ExecutionPolicy Bypass -File "%SCRIPTS%\check-pg.ps1"
)
if errorlevel 1 (
    net start "%PG_SERVICE%" >nul 2>&1
    timeout /t 3 /nobreak >nul
    powershell -NoProfile -ExecutionPolicy Bypass -File "%SCRIPTS%\check-pg.ps1"
)
if errorlevel 1 (
    if exist "%PG_CTL%" (
        echo Starting via pg_ctl...
        "%PG_CTL%" -D "%PG_DATA%" -w start
        timeout /t 3 /nobreak >nul
        powershell -NoProfile -ExecutionPolicy Bypass -File "%SCRIPTS%\check-pg.ps1"
    )
)
if errorlevel 1 (
    echo.
    echo [ERROR] PostgreSQL is not running on port 5432.
    echo Start service: net start %PG_SERVICE%
    echo Or open pgAdmin and start the server.
    goto :end
)
echo PostgreSQL OK.
echo.

echo [2/3] Database sibmobile...
where psql >nul 2>&1
if not errorlevel 1 (
    psql -U postgres -h localhost -p 5432 -tc "SELECT 1 FROM pg_database WHERE datname='sibmobile'" 2>nul | findstr /I "1" >nul
    if errorlevel 1 (
        echo Creating database sibmobile...
        psql -U postgres -h localhost -p 5432 -c "CREATE DATABASE sibmobile;" 2>nul
    ) else (
        echo Database sibmobile exists.
    )
    echo Updating schema...
    powershell -NoProfile -ExecutionPolicy Bypass -File "%SCRIPTS%\migrate-db.ps1" -PgPassword "%PGPASSWORD%"
    if errorlevel 1 (
        echo.
        echo [ERROR] Database schema migration failed.
        goto :end
    )
) else (
    echo psql not found - skipping DB check.
)
echo.

echo [3/3] Starting Spring Boot...
echo Site:  http://localhost:8080
echo Admin: http://localhost:8080/admin  login Admin777 / Admin123
echo Browser opens when port 8080 is ready.
echo Stop server: Ctrl+C
echo ====================================
echo.

start "SibMobileBrowser" /MIN powershell -NoProfile -ExecutionPolicy Bypass -File "%SCRIPTS%\wait-and-open-site.ps1"

call mvnw.cmd spring-boot:run
set "MVN_ERR=%ERRORLEVEL%"

echo.
if not "%MVN_ERR%"=="0" (
    echo [ERROR] Spring Boot exited with code %MVN_ERR%.
) else (
    echo Server stopped.
)
goto :end

:ensure_java
where java >nul 2>&1
if not errorlevel 1 exit /b 0

if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        set "PATH=%JAVA_HOME%\bin;%PATH%"
        exit /b 0
    )
)

for /f "usebackq delims=" %%P in (`powershell -NoProfile -ExecutionPolicy Bypass -File "%SCRIPTS%\find-java.ps1" 2^>nul`) do (
    set "PATH=%%P;%PATH%"
    exit /b 0
)

echo [ERROR] Java not found. Install JDK 17 and restart PC, or set JAVA_HOME.
exit /b 1

:end
echo.
pause
exit /b 0
