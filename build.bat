@echo off
color 0A

set SCRIPT_VERSION=2.0
set PROJECT_NAME=EnchAdd
set PROJECT_VERSION=1.0.0
set GRADLE_VERSION=8.5
set JAVA_VERSION=21
set PAPER_VERSION=1.21.1-R0.1-SNAPSHOT

echo.
echo ============================================================
echo  EnchAdd Build Script
echo ============================================================
echo.

if "%1"=="--help" goto SHOW_HELP
if "%1"=="-h" goto SHOW_HELP
if "%1"=="--clean" goto CLEAN_BUILD
if "%1"=="--check" goto CHECK_ENVIRONMENT

:MENU
cls
echo.
echo ============================================================
echo  Build Options
echo ============================================================
echo.
echo  [1] Standard Build (Recommended)
echo  [2] Full Build with Verification
echo  [3] Clean Only
echo  [4] Check Environment
echo  [5] Fix Dependencies
echo.
echo  [0] Exit
echo.
echo ============================================================
echo.

set /p choice="Select option (0-5): "

if "%choice%"=="1" goto STANDARD_BUILD
if "%choice%"=="2" goto FULL_BUILD
if "%choice%"=="3" goto CLEAN_ONLY
if "%choice%"=="4" goto CHECK_ENVIRONMENT
if "%choice%"=="5" goto FIX_WRAPPER
if "%choice%"=="0" goto EXIT
goto MENU

:STANDARD_BUILD
echo.
echo ============================================================
echo  Starting Standard Build...
echo ============================================================
echo.

call :CHECK_JAVA
if errorlevel 1 (
    echo [ERROR] Java environment check failed
    goto FAILED
)

call :CHECK_WRAPPER
if errorlevel 1 (
    echo [WARNING] Gradle Wrapper missing, attempting to fix...
    call :FIX_WRAPPER_SILENT
    if errorlevel 1 (
        echo [ERROR] Gradle Wrapper fix failed
        goto FAILED
    )
)

echo [1/3] Cleaning old build artifacts...
gradlew.bat clean --quiet --no-daemon
if errorlevel 1 (
    echo [WARNING] Clean process had warnings, continuing...
)

echo [2/3] Compiling source code...
gradlew.bat compileJava --no-daemon
if errorlevel 1 (
    echo [ERROR] Compilation failed!
    goto FAILED
)
echo [OK] Compilation successful

echo [3/3] Building JAR file...
gradlew.bat build --no-daemon --x test
if errorlevel 1 (
    echo [ERROR] Build failed!
    goto FAILED
)
echo [OK] Build successful

echo.
echo ============================================================
echo  Build Complete!
echo ============================================================
echo.
call :SHOW_OUTPUT_FILES
goto SUCCESS

:FULL_BUILD
echo.
echo ============================================================
echo  Starting Full Build with Verification...
echo ============================================================
echo.

call :CHECK_JAVA
if errorlevel 1 (
    echo [ERROR] Java environment check failed
    goto FAILED
)

call :CHECK_WRAPPER
if errorlevel 1 (
    call :FIX_WRAPPER_SILENT
    if errorlevel 1 (
        echo [ERROR] Gradle Wrapper fix failed
        goto FAILED
    )
)

echo [1/6] Cleaning...
gradlew.bat clean --quiet --no-daemon
echo [OK] Clean complete

echo [2/6] Compiling...
gradlew.bat compileJava --no-daemon
if errorlevel 1 (
    echo [ERROR] Compilation failed!
    goto FAILED
)
echo [OK] Compilation successful

echo [3/6] Verifying class files...
gradlew.bat classes --quiet --no-daemon
if errorlevel 1 (
    echo [ERROR] Verification failed!
    goto FAILED
)
echo [OK] Verification passed

echo [4/6] Building JAR...
gradlew.bat build --no-daemon --x test
if errorlevel 1 (
    echo [ERROR] Build failed!
    goto FAILED
)
echo [OK] Build successful

echo [5/6] Checking output integrity...
if not exist "build\libs\enchadd-%PROJECT_VERSION%.jar" (
    echo [ERROR] JAR file not generated!
    goto FAILED
)
echo [OK] JAR file complete

echo [6/6] Verifying JAR contents...
call :VERIFY_JAR
if errorlevel 1 (
    echo [WARNING] JAR verification found issues
)
echo [OK] Verification complete

echo.
echo ============================================================
echo  Full Build + Verification Complete!
echo ============================================================
echo.
call :SHOW_OUTPUT_FILES
goto SUCCESS

:CLEAN_ONLY
echo.
echo ============================================================
echo  Cleaning build artifacts...
echo ============================================================
echo.

if exist "build" (
    echo Deleting build directory...
    rmdir /s /q build 2>nul
    echo [OK] build directory deleted
)

if exist ".gradle\buildOutputCleanup" (
    rmdir /s /q ".gradle\buildOutputCleanup" 2>nul
)

echo.
echo [OK] Clean complete
goto SUCCESS

:CHECK_ENVIRONMENT
cls
echo.
echo ============================================================
echo  Environment Check Report
echo ============================================================
echo.

echo [1/4] Checking Java environment...
call :CHECK_JAVA
if errorlevel 1 (
    echo   X Java not installed or incompatible version
) else (
    echo   OK Java environment normal
)

echo.
echo [2/4] Checking Gradle Wrapper...
call :CHECK_WRAPPER
if errorlevel 1 (
    echo   X Gradle Wrapper missing or corrupted
) else (
    echo   OK Gradle Wrapper normal
)

echo.
echo [3/4] Checking build configuration files...
if exist "build.gradle.kts" (
    echo   OK build.gradle.kts exists
) else (
    echo   X build.gradle.kts missing
)

if exist "settings.gradle.kts" (
    echo   OK settings.gradle.kts exists
) else (
    echo   X settings.gradle.kts missing
)

echo.
echo [4/4] Checking dependencies configuration...
if exist "gradle.properties" (
    echo   OK gradle.properties exists
) else (
    echo   ! gradle.properties missing
)

echo.
echo ============================================================
echo  Environment Check Complete
echo ============================================================
goto SUCCESS

:FIX_WRAPPER
cls
echo.
echo ============================================================
echo  Fixing Gradle Wrapper...
echo ============================================================
echo.

call :FIX_WRAPPER_SILENT
if errorlevel 1 (
    echo [ERROR] Gradle Wrapper fix failed
    echo.
    echo Please download manually:
    echo   1. Visit https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip
    echo   2. Extract and find lib/gradle-wrapper-%GRADLE_VERSION%.jar
    echo   3. Rename to gradle-wrapper.jar
    echo   4. Copy to gradle\wrapper\ directory
    goto FAILED
)

echo [OK] Gradle Wrapper fix successful
goto SUCCESS

:FIX_WRAPPER_SILENT
if exist "gradle\wrapper\gradle-wrapper.jar" exit /b 0

where gradle >nul 2>nul
if %errorlevel% equ 0 (
    echo Generating Wrapper using system Gradle...
    gradle wrapper --gradle-version %GRADLE_VERSION% --no-daemon
    exit /b %errorlevel%
)

echo Attempting to download Gradle Wrapper...
powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; try { Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile 'gradle-temp.zip' -UseBasicParsing -TimeoutSec 60 } catch { exit 1 }}"

if exist "gradle-temp.zip" (
    echo Extracting...
    powershell -Command "& {Expand-Archive -Path 'gradle-temp.zip' -DestinationPath 'gradle-temp' -Force}"
    
    if exist "gradle-temp\gradle-%GRADLE_VERSION%\lib\gradle-wrapper-%GRADLE_VERSION%.jar" (
        copy "gradle-temp\gradle-%GRADLE_VERSION%\lib\gradle-wrapper-%GRADLE_VERSION%.jar" "gradle\wrapper\gradle-wrapper.jar" >nul
        rmdir /s /q gradle-temp
        del gradle-temp.zip
        exit /b 0
    )
    
    rmdir /s /q gradle-temp 2>nul
    del gradle-temp.zip 2>nul
)

powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://github.com/gradle/gradle/raw/v%GRADLE_VERSION%.0/gradle/wrapper/gradle-wrapper.jar' -OutFile 'gradle\wrapper\gradle-wrapper.jar' -UseBasicParsing}"
exit /b 0

:CHECK_JAVA
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java not found!
    echo       Please install Java %JAVA_VERSION% or higher
    echo       Download: https://adoptium.net/
    exit /b 1
)

for /f "tokens=* usebackq" %%v in (`java -version 2^>^&1 ^| findstr /i "version"`) do (
    for /f "tokens=3 delims=." %%a in ("%%v") do (
        if %%a lss %JAVA_VERSION% (
            echo [WARNING] Java version lower than %JAVA_VERSION%
        )
    )
)
exit /b 0

:CHECK_WRAPPER
if not exist "gradle\wrapper\gradle-wrapper.jar" exit /b 1
exit /b 0

:SHOW_OUTPUT_FILES
echo Output files:
echo.
if exist "build\libs\enchadd-%PROJECT_VERSION%.jar" (
    for %%A in (build\libs\enchadd-%PROJECT_VERSION%.jar) do (
        echo   Package: enchadd-%PROJECT_VERSION%.jar
        echo      Size: %%~zA bytes
    )
)
if exist "build\libs\enchadd-%PROJECT_VERSION%-sources.jar" (
    echo   Source: enchadd-%PROJECT_VERSION%-sources.jar
)
echo.
echo Location: %cd%\build\libs\
echo.
exit /b 0

:VERIFY_JAR
if not exist "build\libs\enchadd-%PROJECT_VERSION%.jar" exit /b 1

echo   Verifying Manifest...
powershell -Command "& {Add-Type -AssemblyName System.IO.Compression; $zip = [System.IO.Compression.ZipFile]::OpenRead('%cd%\build\libs\enchadd-%PROJECT_VERSION%.jar'); $zip.Entries.Count | Out-Host; $zip.Dispose()}"

exit /b 0

:SHOW_HELP
cls
echo.
echo ============================================================
echo  EnchAdd Build Script Help
echo ============================================================
echo.
echo Usage:
echo.
echo   build.bat              - Show interactive menu
echo   build.bat --clean      - Clean build artifacts only
echo   build.bat --check      - Check build environment
echo   build.bat --help       - Show this help message
echo.
echo Quick Commands:
echo.
echo   gradlew.bat clean              - Clean
echo   gradlew.bat build              - Standard build
echo   gradlew.bat build --info      - Build with detailed output
echo   gradlew.bat build --refresh-dependencies - Refresh dependencies
echo.
echo Build Output:
echo   build\libs\enchadd-1.0.0.jar
echo.
exit /b 0

:FAILED
echo.
echo ============================================================
echo  X Operation Failed
echo ============================================================
echo.
pause
exit /b 1

:SUCCESS
echo.
pause
exit /b 0

:EXIT
exit /b 0
