@echo off
cd /d "%~dp0"
echo ===== Cafeteria UTN =====
echo Compilando...
if not exist bin mkdir bin
javac -d bin -sourcepath src src\Main.java
if errorlevel 1 (
    echo.
    echo Hubo errores de compilacion. Revise los mensajes de arriba.
    echo Verifique que tiene instalado el JDK: javac -version
    pause
    exit /b 1
)
echo Compilacion correcta. Iniciando el sistema...
echo.
java -cp bin Main
pause
