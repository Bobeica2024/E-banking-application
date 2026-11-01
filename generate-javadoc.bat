@echo off
setlocal
echo ===================================================
echo [PPOO] Generare Documentatie JavaDoc E-Banking...
echo ===================================================

where javadoc >nul 2>nul
if %ERRORLEVEL% neq 0 (
    if exist "C:\Program Files\Android\openjdk\jdk-21.0.8\bin\javadoc.exe" (
        set "PATH=C:\Program Files\Android\openjdk\jdk-21.0.8\bin;%PATH%"
    ) else if exist "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\jbr\bin\javadoc.exe" (
        set "PATH=C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\jbr\bin;%PATH%"
    )
)

if exist "docs\javadoc" rmdir /s /q "docs\javadoc"

javadoc -Xdoclint:none -encoding UTF-8 -docencoding UTF-8 -charset UTF-8 -d docs\javadoc -sourcepath src -subpackages ro.ase.ppoo.ebanking

if %ERRORLEVEL% equ 0 (
    echo [SUCCES] JavaDoc a fost generat in folderul 'docs\javadoc'!
) else (
    echo [EROARE] A aparut o eroare la generarea JavaDoc.
)
endlocal
