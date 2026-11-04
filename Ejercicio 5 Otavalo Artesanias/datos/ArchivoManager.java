package datos;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class ArchivoManager {
    private static final String DIR = "datos_tienda/";

    public static void inicializarDirectorio() {
        try {
            Files.createDirectories(Paths.get(DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void guardarLista(String archivo, List<String> lineas) {
        try {
            Files.write(Paths.get(DIR + archivo), lineas);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> leerLista(String archivo) {
        Path ruta = Paths.get(DIR + archivo);
        if (!Files.exists(ruta)) return new ArrayList<>();
        try {
            return Files.readAllLines(ruta);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }
}