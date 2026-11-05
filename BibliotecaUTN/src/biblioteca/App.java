package biblioteca;

import biblioteca.persistencia.ConexionBD;
import biblioteca.persistencia.MaterialCsv;
import biblioteca.persistencia.MaterialRepositorio;
import biblioteca.persistencia.PrestamoCsv;
import biblioteca.persistencia.PrestamoRepositorio;
import biblioteca.persistencia.UsuarioCsv;
import biblioteca.persistencia.UsuarioRepositorio;
import biblioteca.servicio.Biblioteca;
import biblioteca.servicio.BibliotecaException;
import biblioteca.ui.Consola;
import biblioteca.ui.MenuPrincipal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Locale;

public class App {

    public static void main(String[] args) {
        Locale.setDefault(Locale.US); // decimales con punto: 0.25
        Path base = carpetaDelProyecto();
        try {
            ConexionBD bd = new ConexionBD(base.resolve("db.properties"));
            System.out.println("Conectando a " + bd.descripcion() + " ...");
            bd.crearEsquema();

            MaterialRepositorio materialRepo = new MaterialRepositorio(bd);
            UsuarioRepositorio usuarioRepo = new UsuarioRepositorio(bd);
            PrestamoRepositorio prestamoRepo = new PrestamoRepositorio(bd);

            if (bd.estaVacia()) importarCsv(base.resolve("datos"), materialRepo, usuarioRepo, prestamoRepo);

            Biblioteca biblioteca = new Biblioteca(materialRepo, usuarioRepo, prestamoRepo);
            new MenuPrincipal(biblioteca, new Consola()).iniciar();
        } catch (IOException e) {
            salir("No se pudo leer la configuracion o importar los datos: " + e.getMessage()
                    + "\nCree db.properties a partir de db.properties.example o defina la variable "
                    + ConexionBD.VARIABLE_ENTORNO + ".");
        } catch (SQLException e) {
            salir("No se pudo conectar a PostgreSQL: " + e.getMessage()
                    + "\nRevise que el servidor este encendido y los datos de db.properties.");
        } catch (BibliotecaException e) {
            salir("No se pudo iniciar la aplicacion: " + e.getMessage());
        }
    }

    /** Primera ejecucion con la base vacia: copia los datos de los archivos CSV a PostgreSQL. */
    private static void importarCsv(Path carpeta, MaterialRepositorio materialRepo, UsuarioRepositorio usuarioRepo,
                                    PrestamoRepositorio prestamoRepo) throws IOException {
        if (!Files.isDirectory(carpeta)) return;
        usuarioRepo.guardarTodos(new UsuarioCsv(carpeta.resolve("usuarios.csv")).cargar());
        materialRepo.guardarTodos(new MaterialCsv(carpeta.resolve("materiales.csv")).cargar());
        prestamoRepo.guardarTodos(new PrestamoCsv(carpeta.resolve("prestamos.csv")).cargar());
        System.out.println("Base de datos vacia: se importaron los datos de la carpeta " + carpeta + ".");
    }

    /**
     * Carpeta donde estan db.properties y datos/. Normalmente es la carpeta actual; si se ejecuta
     * desde un IDE abierto en la carpeta del repositorio del grupo, se usa la subcarpeta BibliotecaUTN.
     */
    private static Path carpetaDelProyecto() {
        Path actual = Path.of("");
        if (Files.exists(actual.resolve("db.properties")) || Files.isDirectory(actual.resolve("src/biblioteca"))) {
            return actual;
        }
        Path subcarpeta = actual.resolve("BibliotecaUTN");
        return Files.isDirectory(subcarpeta) ? subcarpeta : actual;
    }

    private static void salir(String mensaje) {
        System.out.println(mensaje);
        System.exit(1);
    }
}
