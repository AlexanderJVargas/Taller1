#!/bin/sh
cd "$(dirname "$0")" || exit 1
echo "===== Cafeteria UTN ====="
echo "Compilando..."
mkdir -p bin
if javac -d bin -sourcepath src src/Main.java; then
    echo "Compilacion correcta. Iniciando el sistema..."
    java -cp bin Main
else
    echo "Hubo errores de compilacion. Verifique que tiene instalado el JDK: javac -version"
    exit 1
fi
