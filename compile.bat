@echo off
setlocal
echo ===================================================
echo [PPOO] Compilare Aplicatie E-Banking...
echo ===================================================

where javac >nul 2>nul
if %ERRORLEVEL% neq 0 (
    if exist "C:\Program Files\Android\openjdk\jdk-21.0.8\bin\javac.exe" (
        set "PATH=C:\Program Files\Android\openjdk\jdk-21.0.8\bin;%PATH%"
    ) else if exist "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\jbr\bin\javac.exe" (
        set "PATH=C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\jbr\bin;%PATH%"
    )
)

if not exist "bin" mkdir "bin"

dir /s /B src\*.java > sources.txt
javac -encoding UTF-8 -d bin @sources.txt
del sources.txt

if %ERRORLEVEL% equ 0 (
    echo [SUCCES] Compilarea s-a incheiat cu succes in directorul 'bin'!
) else (
    echo [EROARE] A aparut o eroare la compilare.
)
endlocal
