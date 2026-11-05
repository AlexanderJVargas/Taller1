# Biblioteca UTN - Prestamos y multas

Aplicacion de consola en Java (17 o superior) para controlar los prestamos de libros y revistas.

## Como iniciar el programa
El programa arranca desde la clase **`src/biblioteca/App.java`** (metodo `main`).

Requisito: solo Java 17 o superior (JDK). No hay que instalar ni configurar ninguna base de datos:
la base se crea sola la primera vez que se ejecuta el programa (ver "Base de datos").

### Opcion 1: desde App.java en VS Code (recomendado)
1. Abra en VS Code la carpeta `BibliotecaUTN` (la que contiene `src`, `lib` y `datos`).
   Necesita la extension "Extension Pack for Java".
2. Abra `src/biblioteca/App.java`.
3. Pulse **Run** sobre el metodo `main` (o F5 y elija "Biblioteca UTN").
4. El menu aparece en la **terminal** de VS Code; escriba ahi las opciones.

La carpeta `.vscode` ya configura los drivers de la carpeta `lib` y la terminal para el menu.
En IntelliJ IDEA o NetBeans: agregue los `.jar` de la carpeta `lib` como librerias del proyecto,
marque `src` como carpeta de fuentes, use la carpeta `BibliotecaUTN` como directorio de trabajo
y ejecute `biblioteca.App`.

### Opcion 2: ejecutar.bat (Windows)
Doble clic en `ejecutar.bat`: compila el codigo y abre el menu. Avisa si falta Java.

### Opcion 3: desde la consola
Dentro de la carpeta `BibliotecaUTN`:
```
javac --release 17 -encoding UTF-8 -d out src/biblioteca/*.java src/biblioteca/modelo/*.java src/biblioteca/persistencia/*.java src/biblioteca/servicio/*.java src/biblioteca/ui/*.java
java -cp "out;lib/h2-2.3.232.jar;lib/postgresql-42.7.12.jar" biblioteca.App
```
En Linux o macOS use `:` en lugar de `;` en el `-cp`.

## Base de datos (H2, incluida en el proyecto)
La base de datos es H2, un motor SQL escrito en Java (`lib/h2-2.3.232.jar`). No necesita servidor:
se guarda como el archivo `basedatos/biblio.mv.db` dentro de la carpeta del proyecto.
- La primera vez que se ejecuta, el programa crea la carpeta `basedatos/`, las tablas `usuario`,
  `material` y `prestamo` (con sus claves primarias, foraneas y restricciones CHECK) e importa los
  datos iniciales de la carpeta `datos/` (`materiales.csv`, `usuarios.csv`, `prestamos.csv`).
- Cada computadora tiene su propia base. `basedatos/` no se sube a GitHub, asi que los datos de un
  compañero no se mezclan con los de otro.
- Para empezar de cero con los datos iniciales, cierre el programa y borre la carpeta `basedatos/`.
- `sql/biblio.sql`: el esquema de las tablas, como referencia.

Opcional: para usar un servidor PostgreSQL (por ejemplo, con pgAdmin) cree un archivo `db.properties`
con `url=jdbc:postgresql://localhost:5432/Biblio`, `usuario=postgres` y `contrasena=...`. Si ese archivo
existe, el programa se conecta a PostgreSQL en lugar de H2. No suba `db.properties` a GitHub.

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
  (JDBC: H2 local o PostgreSQL) y `MaterialCsv`, `UsuarioCsv`, `PrestamoCsv` (importacion inicial).
- `servicio`: `Biblioteca` (reglas de negocio y reportes), `MaterialPrestado`, `LineaAuditoria`.
- `ui`: `Consola` (lectura validada) y `MenuPrincipal`.
