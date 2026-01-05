@echo off
REM Build script for LegendasBUDA Windows EXE installer
echo ======================================
echo LegendasBUDA Windows Build Script
echo ======================================

REM Step 1: Clean and build JAR
echo.
echo [1/3] Building JAR with dependencies...
call mvn clean package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven build failed!
    pause
    exit /b 1
)
echo JAR created: target\LegendasBUDA-standalone.jar

REM Step 2: Create custom JRE
echo.
echo [2/3] Creating custom Java runtime...
if exist target\java-runtime rmdir /s /q target\java-runtime
jlink --add-modules java.base,java.desktop,java.logging,java.xml ^
      --output target\java-runtime ^
      --strip-debug ^
      --no-header-files ^
      --no-man-pages ^
      --compress=2
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: jlink failed!
    pause
    exit /b 1
)
echo Custom JRE created at target\java-runtime

REM Step 3: Prepare jpackage input folder
echo.
echo [3/3] Preparing jpackage input folder...
set INPUT_FOLDER=target\jpackage-input
if exist "%INPUT_FOLDER%" rmdir /s /q "%INPUT_FOLDER%"
mkdir "%INPUT_FOLDER%"
copy target\LegendasBUDA-standalone.jar "%INPUT_FOLDER%\"

REM Step 4: Create installer with jpackage
echo.
echo [4/4] Creating Windows EXE installer...
if exist target\dist rmdir /s /q target\dist

jpackage --type exe ^
    --name "LegendasBUDA" ^
    --app-version "1.0.0" ^
    --vendor "Associacao BUDA" ^
    --icon src\main\resources\icons\BUDA.ico ^
    --input %INPUT_FOLDER% ^
    --main-jar LegendasBUDA-standalone.jar ^
    --main-class com.budaassociacao.legendas.LegendasApp ^
    --runtime-image target\java-runtime ^
    --dest target\dist ^
    --win-menu ^
    --win-dir-chooser ^
    --win-shortcut ^
    --win-upgrade-uuid "b2c3d4e5-f6a7-8901-bcde-f12345678901" ^
    --java-options "-Dfile.encoding=UTF-8" ^
    --verbose

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ======================================
    echo ERROR: jpackage failed!
    echo ======================================
    pause
    exit /b 1
)

echo.
echo ======================================
echo Build Complete!
echo ======================================
echo.
echo Installer created: target\dist\LegendasBUDA-1.0.0.exe
echo.

REM Cleanup input folder
if exist "%INPUT_FOLDER%" rmdir /s /q "%INPUT_FOLDER%"

pause
