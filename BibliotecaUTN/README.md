# Biblioteca UTN - Prestamos y multas

Aplicacion de consola en Java (17 o superior) para controlar los prestamos de libros y revistas.

## Como iniciar el programa
El programa arranca desde la clase **`src/biblioteca/App.java`** (metodo `main`).

Requisitos: Java 17 o superior (JDK) y una base PostgreSQL, en la nube (Neon) o local.

### Paso previo (una sola vez)
Copie `db.properties.example` como `db.properties` y complete la conexion a la base
(ver la seccion "Base de datos"). Sin ese archivo el programa no puede conectarse.

### Opcion 1: desde App.java en VS Code (recomendado)
1. Abra en VS Code la carpeta `BibliotecaUTN` (la que contiene `src`, `lib` y `db.properties`).
   Necesita la extension "Extension Pack for Java".
2. Abra `src/biblioteca/App.java`.
3. Pulse **Run** sobre el metodo `main` (o F5 y elija "Biblioteca UTN").
4. El menu aparece en la **terminal** de VS Code; escriba ahi las opciones.

La carpeta `.vscode` ya configura el driver `lib/postgresql-42.7.12.jar` y la terminal para el menu.
En IntelliJ IDEA o NetBeans: agregue `lib/postgresql-42.7.12.jar` como libreria del proyecto,
marque `src` como carpeta de fuentes, use la carpeta `BibliotecaUTN` como directorio de trabajo
y ejecute `biblioteca.App`.

### Opcion 2: ejecutar.bat (Windows)
Doble clic en `ejecutar.bat`: compila el codigo y abre el menu. Avisa si falta Java o `db.properties`.

### Opcion 3: desde la consola
Dentro de la carpeta `BibliotecaUTN`:
```
javac --release 17 -encoding UTF-8 -d out src/biblioteca/*.java src/biblioteca/modelo/*.java src/biblioteca/persistencia/*.java src/biblioteca/servicio/*.java src/biblioteca/ui/*.java
java -cp "out;lib/postgresql-42.7.12.jar" biblioteca.App
```
En Linux o macOS use `:` en lugar de `;` en el `-cp`.

## Base de datos (PostgreSQL)
Los datos se guardan en PostgreSQL. La base principal esta en la nube, en Neon (neon.tech), asi
que la aplicacion funciona desde cualquier computadora con internet. Tambien puede usarse una base local.
- `db.properties`: datos de la conexion. No se sube al repositorio: copie `db.properties.example`
  como `db.properties` y pegue la cadena de conexion de Neon
  (`url=postgresql://usuario:contrasena@host/neondb?sslmode=require`).
- Variable de entorno `BIBLIO_DB_URL`: si existe, se usa en lugar de `db.properties`.
- `lib/postgresql-42.7.12.jar`: driver JDBC.
- `sql/biblio.sql`: esquema (tablas `usuario`, `material`, `prestamo`). La aplicacion crea las
  tablas sola al iniciar si no existen.
- Si la base esta vacia, en el primer arranque se importan los archivos de la carpeta `datos/`
  (`materiales.csv`, `usuarios.csv`, `prestamos.csv`). Despues ya no se usan.

Los ejemplares prestados de cada titulo se calculan al iniciar a partir de los prestamos activos,
por eso los disponibles siempre coinciden con los prestamos registrados.

## Reglas de negocio
| Material | Dias de prestamo | Multa por dia de retraso |
|----------|------------------|--------------------------|
| Libro    | 7                | $0.25                    |
| Revista  | 3                | $0.50                    |

- Un usuario puede tener como maximo 3 prestamos activos.
- No se presta si no hay ejemplares disponibles.
- Un prestamo no se puede devolver dos veces.
- Un usuario con multas pendientes no puede solicitar nuevos prestamos.
  Tampoco si tiene un prestamo vencido sin devolver (su multa ya esta corriendo).
- La multa se genera al devolver: dias de retraso x multa por dia del material.
- Al prestar y devolver se puede indicar la fecha (por defecto hoy), util para probar retrasos.

## Reportes del administrador
1. Prestamos vencidos a una fecha de corte (con la multa acumulada).
2. Historial de un usuario.
3. Materiales mas prestados.
4. Total de multas generadas en un periodo (generado, cobrado y por cobrar).
5. Auditoria de ejemplares: total, prestados y disponibles por titulo, verificando que el
   contador coincida con los prestamos activos.

## Estructura (POO)
- `modelo`: `Material` (abstracta) -> `Libro`, `Revista`. Polimorfismo en `diasPrestamo()`
  y `multaPorDia()`. `Usuario`, `Prestamo` (devolucion, calculo de retraso y multa).
- `persistencia`: `ConexionBD`, `MaterialRepositorio`, `UsuarioRepositorio`, `PrestamoRepositorio`
  (PostgreSQL via JDBC) y `MaterialCsv`, `UsuarioCsv`, `PrestamoCsv` (importacion inicial).
- `servicio`: `Biblioteca` (reglas de negocio y reportes), `MaterialPrestado`, `LineaAuditoria`.
- `ui`: `Consola` (lectura validada) y `MenuPrincipal`.
