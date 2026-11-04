import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

// Guarda en memoria todas las listas y se encarga de leerlas y escribirlas
// en archivos de texto (un archivo por tabla, campos separados por ";").
public class RepositorioArchivos {
    private String carpeta;
    private ArrayList<Ingrediente> ingredientes;
    private ArrayList<Bebida> bebidas;
    private ArrayList<Plato> platos;
    private ArrayList<AbastecimientoIngrediente> abastecimientosIngredientes;
    private ArrayList<AbastecimientoBebida> abastecimientosBebidas;
    private ArrayList<Venta> ventas;
    private ArrayList<AuditoriaStock> auditorias;

    public RepositorioArchivos(String carpeta) {
        this.carpeta = carpeta;
        this.ingredientes = new ArrayList<Ingrediente>();
        this.bebidas = new ArrayList<Bebida>();
        this.platos = new ArrayList<Plato>();
        this.abastecimientosIngredientes = new ArrayList<AbastecimientoIngrediente>();
        this.abastecimientosBebidas = new ArrayList<AbastecimientoBebida>();
        this.ventas = new ArrayList<Venta>();
        this.auditorias = new ArrayList<AuditoriaStock>();
    }

    public ArrayList<Ingrediente> getIngredientes() { return ingredientes; }
    public ArrayList<Bebida> getBebidas() { return bebidas; }
    public ArrayList<Plato> getPlatos() { return platos; }
    public ArrayList<AbastecimientoIngrediente> getAbastecimientosIngredientes() { return abastecimientosIngredientes; }
    public ArrayList<AbastecimientoBebida> getAbastecimientosBebidas() { return abastecimientosBebidas; }
    public ArrayList<Venta> getVentas() { return ventas; }
    public ArrayList<AuditoriaStock> getAuditorias() { return auditorias; }

    // ---------- Busquedas por id (devuelven null si no existe) ----------

    public Ingrediente buscarIngrediente(String id) {
        for (Ingrediente i : ingredientes) {
            if (i.getIdIngrediente().equals(id)) {
                return i;
            }
        }
        return null;
    }

    public Bebida buscarBebida(String id) {
        for (Bebida b : bebidas) {
            if (b.getIdBebida().equals(id)) {
                return b;
            }
        }
        return null;
    }

    public Plato buscarPlato(String id) {
        for (Plato p : platos) {
            if (p.getIdPlato().equals(id)) {
                return p;
            }
        }
        return null;
    }

    public AbastecimientoIngrediente buscarAbastIngrediente(String id) {
        for (AbastecimientoIngrediente a : abastecimientosIngredientes) {
            if (a.getIdAbastIngrediente().equals(id)) {
                return a;
            }
        }
        return null;
    }

    public AbastecimientoBebida buscarAbastBebida(String id) {
        for (AbastecimientoBebida a : abastecimientosBebidas) {
            if (a.getIdAbastBebida().equals(id)) {
                return a;
            }
        }
        return null;
    }

    public Venta buscarVenta(String id) {
        for (Venta v : ventas) {
            if (v.getIdVenta().equals(id)) {
                return v;
            }
        }
        return null;
    }

    // ---------- Ids automaticos y auditoria ----------

    // Genera ids como VEN001, ABI002, etc. a partir de cuantos registros hay
    public String siguienteId(String prefijo, int cantidadActual) {
        String numero = String.valueOf(cantidadActual + 1);
        while (numero.length() < 3) {
            numero = "0" + numero;
        }
        return prefijo + numero;
    }

    // Guarda un movimiento de stock de un ingrediente
    public void agregarAuditoria(Ingrediente ingrediente, String fecha, String tipoMovimiento,
                                 String idReferencia, double cantidad, double stockAnterior, double stockNuevo) {
        String id = siguienteId("AUD", auditorias.size());
        auditorias.add(new AuditoriaStock(id, ingrediente, fecha, tipoMovimiento,
                idReferencia, cantidad, stockAnterior, stockNuevo));
    }

    // ---------- Lectura y escritura de un archivo ----------

    // Lee el archivo y devuelve una fila por linea, ya separada por ";"
    private ArrayList<String[]> leerArchivo(String nombreArchivo) {
        ArrayList<String[]> filas = new ArrayList<String[]>();
        File archivo = new File(carpeta, nombreArchivo);
        if (!archivo.exists()) {
            return filas;  // si no existe, se trata como archivo vacio
        }
        try {
            BufferedReader lector = new BufferedReader(new FileReader(archivo));
            String linea;
            while ((linea = lector.readLine()) != null) {
                linea = linea.trim();
                if (!linea.equals("")) {
                    filas.add(linea.split(";"));
                }
            }
            lector.close();
        } catch (IOException e) {
            System.out.println("Error al leer " + nombreArchivo + ": " + e.getMessage());
        }
        return filas;
    }

    private void escribirArchivo(String nombreArchivo, ArrayList<String> lineas) {
        try {
            PrintWriter escritor = new PrintWriter(new FileWriter(new File(carpeta, nombreArchivo)));
            for (String linea : lineas) {
                escritor.println(linea);
            }
            escritor.close();
        } catch (IOException e) {
            System.out.println("Error al guardar " + nombreArchivo + ": " + e.getMessage());
        }
    }

    // ---------- Cargar ----------

    // El orden importa: primero lo que no depende de nada, luego los detalles
    public void cargarTodo() {
        ingredientes.clear();
        bebidas.clear();
        platos.clear();
        abastecimientosIngredientes.clear();
        abastecimientosBebidas.clear();
        ventas.clear();
        auditorias.clear();

        cargarIngredientes();
        cargarBebidas();
        cargarPlatos();
        cargarRecetas();
        cargarAbastecimientosIngredientes();
        cargarAbastecimientosBebidas();
        cargarVentas();
        cargarAuditoria();
    }

    private void cargarIngredientes() {
        ArrayList<String[]> filas = leerArchivo("ingredientes.txt");
        for (int i = 0; i < filas.size(); i++) {
            String[] f = filas.get(i);
            try {
                if (buscarIngrediente(f[0]) != null) {
                    System.out.println("Ingrediente repetido en ingredientes.txt: " + f[0]);
                } else {
                    ingredientes.add(new Ingrediente(f[0], f[1], f[2],
                            Double.parseDouble(f[3]), Double.parseDouble(f[4]), Double.parseDouble(f[5])));
                }
            } catch (Exception e) {
                System.out.println("Linea invalida en ingredientes.txt (registro " + (i + 1) + ")");
            }
        }
    }

    private void cargarBebidas() {
        ArrayList<String[]> filas = leerArchivo("bebidas.txt");
        for (int i = 0; i < filas.size(); i++) {
            String[] f = filas.get(i);
            try {
                if (buscarBebida(f[0]) != null) {
                    System.out.println("Bebida repetida en bebidas.txt: " + f[0]);
                } else {
                    bebidas.add(new Bebida(f[0], f[1], Double.parseDouble(f[2]), Integer.parseInt(f[3])));
                }
            } catch (Exception e) {
                System.out.println("Linea invalida en bebidas.txt (registro " + (i + 1) + ")");
            }
        }
    }

    private void cargarPlatos() {
        ArrayList<String[]> filas = leerArchivo("platos.txt");
        for (int i = 0; i < filas.size(); i++) {
            String[] f = filas.get(i);
            try {
                if (buscarPlato(f[0]) != null) {
                    System.out.println("Plato repetido en platos.txt: " + f[0]);
                } else {
                    platos.add(new Plato(f[0], f[1]));
                }
            } catch (Exception e) {
                System.out.println("Linea invalida en platos.txt (registro " + (i + 1) + ")");
            }
        }
    }

    private void cargarRecetas() {
        ArrayList<String[]> filas = leerArchivo("recetas.txt");
        for (int i = 0; i < filas.size(); i++) {
            String[] f = filas.get(i);
            try {
                Plato plato = buscarPlato(f[0]);
                Ingrediente ingrediente = buscarIngrediente(f[1]);
                if (plato == null || ingrediente == null) {
                    System.out.println("Receta con referencia inexistente en recetas.txt (registro " + (i + 1) + ")");
                } else {
                    plato.agregarIngrediente(ingrediente, Double.parseDouble(f[2]));
                }
            } catch (Exception e) {
                System.out.println("Linea invalida en recetas.txt (registro " + (i + 1) + ")");
            }
        }
    }

    private void cargarAbastecimientosIngredientes() {
        ArrayList<String[]> filas = leerArchivo("abastecimientos_ingredientes.txt");
        for (int i = 0; i < filas.size(); i++) {
            String[] f = filas.get(i);
            try {
                abastecimientosIngredientes.add(new AbastecimientoIngrediente(f[0], f[1]));
            } catch (Exception e) {
                System.out.println("Linea invalida en abastecimientos_ingredientes.txt (registro " + (i + 1) + ")");
            }
        }

        ArrayList<String[]> detalles = leerArchivo("detalle_abast_ingredientes.txt");
        for (int i = 0; i < detalles.size(); i++) {
            String[] f = detalles.get(i);
            try {
                AbastecimientoIngrediente abast = buscarAbastIngrediente(f[0]);
                Ingrediente ingrediente = buscarIngrediente(f[1]);
                if (abast == null || ingrediente == null) {
                    System.out.println("Detalle con referencia inexistente en detalle_abast_ingredientes.txt (registro " + (i + 1) + ")");
                } else {
                    abast.agregarDetalle(ingrediente, Double.parseDouble(f[2]), Double.parseDouble(f[3]));
                }
            } catch (Exception e) {
                System.out.println("Linea invalida en detalle_abast_ingredientes.txt (registro " + (i + 1) + ")");
            }
        }
    }

    private void cargarAbastecimientosBebidas() {
        ArrayList<String[]> filas = leerArchivo("abastecimientos_bebidas.txt");
        for (int i = 0; i < filas.size(); i++) {
            String[] f = filas.get(i);
            try {
                abastecimientosBebidas.add(new AbastecimientoBebida(f[0], f[1]));
            } catch (Exception e) {
                System.out.println("Linea invalida en abastecimientos_bebidas.txt (registro " + (i + 1) + ")");
            }
        }

        ArrayList<String[]> detalles = leerArchivo("detalle_abast_bebidas.txt");
        for (int i = 0; i < detalles.size(); i++) {
            String[] f = detalles.get(i);
            try {
                AbastecimientoBebida abast = buscarAbastBebida(f[0]);
                Bebida bebida = buscarBebida(f[1]);
                if (abast == null || bebida == null) {
                    System.out.println("Detalle con referencia inexistente en detalle_abast_bebidas.txt (registro " + (i + 1) + ")");
                } else {
                    abast.agregarDetalle(bebida, Integer.parseInt(f[2]));
                }
            } catch (Exception e) {
                System.out.println("Linea invalida en detalle_abast_bebidas.txt (registro " + (i + 1) + ")");
            }
        }
    }

    private void cargarVentas() {
        ArrayList<String[]> filas = leerArchivo("ventas.txt");
        for (int i = 0; i < filas.size(); i++) {
            String[] f = filas.get(i);
            try {
                ventas.add(new Venta(f[0], f[1]));
            } catch (Exception e) {
                System.out.println("Linea invalida en ventas.txt (registro " + (i + 1) + ")");
            }
        }

        // Los detalles se cargan con el precio que tenian al momento de la venta
        ArrayList<String[]> detPlatos = leerArchivo("detalle_venta_platos.txt");
        for (int i = 0; i < detPlatos.size(); i++) {
            String[] f = detPlatos.get(i);
            try {
                Venta venta = buscarVenta(f[0]);
                Plato plato = buscarPlato(f[1]);
                if (venta == null || plato == null) {
                    System.out.println("Detalle con referencia inexistente en detalle_venta_platos.txt (registro " + (i + 1) + ")");
                } else {
                    venta.getPlatos().add(new DetalleVentaPlato(plato, Integer.parseInt(f[2]), Double.parseDouble(f[3])));
                }
            } catch (Exception e) {
                System.out.println("Linea invalida en detalle_venta_platos.txt (registro " + (i + 1) + ")");
            }
        }

        ArrayList<String[]> detBebidas = leerArchivo("detalle_venta_bebidas.txt");
        for (int i = 0; i < detBebidas.size(); i++) {
            String[] f = detBebidas.get(i);
            try {
                Venta venta = buscarVenta(f[0]);
                Bebida bebida = buscarBebida(f[1]);
                if (venta == null || bebida == null) {
                    System.out.println("Detalle con referencia inexistente en detalle_venta_bebidas.txt (registro " + (i + 1) + ")");
                } else {
                    venta.getBebidas().add(new DetalleVentaBebida(bebida, Integer.parseInt(f[2]), Double.parseDouble(f[3])));
                }
            } catch (Exception e) {
                System.out.println("Linea invalida en detalle_venta_bebidas.txt (registro " + (i + 1) + ")");
            }
        }

        // Los totales se recalculan a partir de los detalles para que siempre coincidan
        for (Venta v : ventas) {
            v.calcularTotales();
        }
    }

    private void cargarAuditoria() {
        ArrayList<String[]> filas = leerArchivo("auditoria_stock.txt");
        for (int i = 0; i < filas.size(); i++) {
            String[] f = filas.get(i);
            try {
                Ingrediente ingrediente = buscarIngrediente(f[1]);
                if (ingrediente == null) {
                    System.out.println("Auditoria con ingrediente inexistente (registro " + (i + 1) + ")");
                } else {
                    auditorias.add(new AuditoriaStock(f[0], ingrediente, f[2], f[3], f[4],
                            Double.parseDouble(f[5]), Double.parseDouble(f[6]), Double.parseDouble(f[7])));
                }
            } catch (Exception e) {
                System.out.println("Linea invalida en auditoria_stock.txt (registro " + (i + 1) + ")");
            }
        }
    }

    // ---------- Guardar ----------

    public void guardarTodo() {
        new File(carpeta).mkdirs();  // crea la carpeta si no existe

        ArrayList<String> lineas = new ArrayList<String>();

        for (Ingrediente i : ingredientes) {
            lineas.add(i.getIdIngrediente() + ";" + i.getNombre() + ";" + i.getUnidadMedida() + ";"
                    + i.getCostoUnitario() + ";" + i.getStockActual() + ";" + i.getStockMinimo());
        }
        escribirArchivo("ingredientes.txt", lineas);

        lineas = new ArrayList<String>();
        for (Bebida b : bebidas) {
            lineas.add(b.getIdBebida() + ";" + b.getNombre() + ";" + b.getPrecioFijo() + ";" + b.getStock());
        }
        escribirArchivo("bebidas.txt", lineas);

        lineas = new ArrayList<String>();
        ArrayList<String> lineasReceta = new ArrayList<String>();
        for (Plato p : platos) {
            lineas.add(p.getIdPlato() + ";" + p.getNombre());
            for (DetalleReceta d : p.getReceta()) {
                lineasReceta.add(p.getIdPlato() + ";" + d.getIngrediente().getIdIngrediente() + ";" + d.getCantidad());
            }
        }
        escribirArchivo("platos.txt", lineas);
        escribirArchivo("recetas.txt", lineasReceta);

        lineas = new ArrayList<String>();
        ArrayList<String> lineasDetalle = new ArrayList<String>();
        for (AbastecimientoIngrediente a : abastecimientosIngredientes) {
            lineas.add(a.getIdAbastIngrediente() + ";" + a.getFecha());
            for (DetalleAbastecimientoIngrediente d : a.getDetalles()) {
                lineasDetalle.add(a.getIdAbastIngrediente() + ";" + d.getIngrediente().getIdIngrediente() + ";"
                        + d.getCantidad() + ";" + d.getCostoUnitario());
            }
        }
        escribirArchivo("abastecimientos_ingredientes.txt", lineas);
        escribirArchivo("detalle_abast_ingredientes.txt", lineasDetalle);

        lineas = new ArrayList<String>();
        lineasDetalle = new ArrayList<String>();
        for (AbastecimientoBebida a : abastecimientosBebidas) {
            lineas.add(a.getIdAbastBebida() + ";" + a.getFecha());
            for (DetalleAbastecimientoBebida d : a.getDetalles()) {
                lineasDetalle.add(a.getIdAbastBebida() + ";" + d.getBebida().getIdBebida() + ";" + d.getCantidad());
            }
        }
        escribirArchivo("abastecimientos_bebidas.txt", lineas);
        escribirArchivo("detalle_abast_bebidas.txt", lineasDetalle);

        lineas = new ArrayList<String>();
        ArrayList<String> lineasPlatos = new ArrayList<String>();
        ArrayList<String> lineasBebidas = new ArrayList<String>();
        for (Venta v : ventas) {
            lineas.add(v.getIdVenta() + ";" + v.getFecha() + ";" + v.getSubtotal() + ";" + v.getIva() + ";" + v.getTotal());
            for (DetalleVentaPlato d : v.getPlatos()) {
                lineasPlatos.add(v.getIdVenta() + ";" + d.getPlato().getIdPlato() + ";"
                        + d.getCantidad() + ";" + d.getPrecioUnitario());
            }
            for (DetalleVentaBebida d : v.getBebidas()) {
                lineasBebidas.add(v.getIdVenta() + ";" + d.getBebida().getIdBebida() + ";"
                        + d.getCantidad() + ";" + d.getPrecioUnitario());
            }
        }
        escribirArchivo("ventas.txt", lineas);
        escribirArchivo("detalle_venta_platos.txt", lineasPlatos);
        escribirArchivo("detalle_venta_bebidas.txt", lineasBebidas);

        lineas = new ArrayList<String>();
        for (AuditoriaStock a : auditorias) {
            lineas.add(a.getIdAuditoria() + ";" + a.getIngrediente().getIdIngrediente() + ";" + a.getFecha() + ";"
                    + a.getTipoMovimiento() + ";" + a.getIdReferencia() + ";" + a.getCantidad() + ";"
                    + a.getStockAnterior() + ";" + a.getStockNuevo());
        }
        escribirArchivo("auditoria_stock.txt", lineas);
    }
}
