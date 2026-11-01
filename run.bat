@echo off
setlocal
where java >nul 2>nul
if %ERRORLEVEL% neq 0 (
    if exist "C:\Program Files\Android\openjdk\jdk-21.0.8\bin\java.exe" (
        set "PATH=C:\Program Files\Android\openjdk\jdk-21.0.8\bin;%PATH%"
    ) else if exist "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\jbr\bin\java.exe" (
        set "PATH=C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\jbr\bin;%PATH%"
    )
)

java -Dfile.encoding=UTF-8 -cp bin ro.ase.ppoo.ebanking.Main
endlocal
