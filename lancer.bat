@echo off
REM ---------------------------------------------------------------------------
REM  Presence Numerique - script d'execution (Windows)
REM
REM  Usage :  lancer.bat          compile puis lance l'application
REM           lancer.bat test     lance les tests unitaires
REM
REM  Prerequis : Java 17+ et PostgreSQL (base "attendance", user admin/admin).
REM  Pour une autre base, definir avant le lancement :
REM    SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD
REM  Maven n'est pas necessaire : le Maven Wrapper (mvnw.cmd) le telecharge.
REM ---------------------------------------------------------------------------
setlocal
cd /d "%~dp0app_gestion_presences"

REM 1. Verification de Java
where java >nul 2>&1
if errorlevel 1 (
    echo [ERREUR] Java est introuvable. Installez un JDK 17 ou superieur.
    goto :fin_erreur
)
for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do set "JAVA_VERSION=%%~v"
for /f "delims=." %%m in ("%JAVA_VERSION%") do set "JAVA_MAJOR=%%m"
if %JAVA_MAJOR% LSS 17 (
    echo [ERREUR] Java %JAVA_MAJOR% detecte : Java 17 ou superieur est requis.
    goto :fin_erreur
)
echo [OK] Java %JAVA_MAJOR%

REM 2. Mode test
if /i "%~1"=="test" (
    echo [..] Lancement des tests unitaires
    call .\mvnw.cmd test
    goto :fin
)

REM 3. Compilation
echo [..] Compilation du projet (premier lancement : quelques minutes)
call .\mvnw.cmd -q package -DskipTests
if errorlevel 1 (
    echo [ERREUR] La compilation a echoue.
    goto :fin_erreur
)
echo [OK] Compilation terminee

REM 4. Lancement
set "PORT_AFFICHE=%PORT%"
if "%PORT_AFFICHE%"=="" set "PORT_AFFICHE=8080"
echo.
echo   Application : http://localhost:%PORT_AFFICHE%
echo   Comptes     : admin@test.com / admin123      ab@test.com / secretary123
echo                 jp@test.com / responsable123   cd@test.com / teacher123
echo                 alice@test.com / password123
echo   Arret       : Ctrl+C
echo.
java -jar target\app_gestion_presences-0.0.1-SNAPSHOT.jar
goto :fin

:fin_erreur
pause
exit /b 1

:fin
endlocal
