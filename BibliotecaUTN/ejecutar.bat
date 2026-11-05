@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0"

where javac >nul 2>nul
if errorlevel 1 (
    echo No se encontro Java. Instale Java 17 o superior ^(JDK^) y vuelva a intentar.
    pause & exit /b 1
)

if not exist out mkdir out
rem Lista de fuentes entre comillas y con "/" para que funcione aunque la ruta tenga espacios
(for /r src %%f in (*.java) do (
    set "archivo=%%f"
    echo "!archivo:\=/!"
)) > fuentes.txt

echo Compilando...
javac --release 17 -encoding UTF-8 -d out @fuentes.txt
if errorlevel 1 (del fuentes.txt & pause & exit /b 1)
del fuentes.txt

java -cp "out;lib\h2-2.3.232.jar;lib\postgresql-42.7.12.jar" biblioteca.App
pause
