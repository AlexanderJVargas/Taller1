import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    private static Scanner teclado = new Scanner(System.in);
    private static RepositorioArchivos repo;
    private static GestorInventario inventario;
    private static GestorVentas gestorVentas;
    private static GestorReportes reportes;

    public static void main(String[] args) {
        repo = new RepositorioArchivos("datos");
        repo.cargarTodo();
        inventario = new GestorInventario(repo);
        gestorVentas = new GestorVentas(repo);
        reportes = new GestorReportes(repo);

        int opcion = -1;
        while (opcion != 0) {
            mostrarMenu();
            opcion = leerEntero("Opcion: ");
            System.out.println();
            switch (opcion) {
                case 1: registrarIngrediente(); break;
                case 2: registrarBebida(); break;
                case 3: registrarPlato(); break;
                case 4: agregarRecetaAPlato(); break;
                case 5: eliminarIngrediente(); break;
                case 6: abastecerIngredientes(); break;
                case 7: abastecerBebidas(); break;
                case 8: registrarVenta(); break;
                case 9: menuReportes(); break;
                case 10: mostrarCatalogo(); break;
                case 0: break;
                default: System.out.println("Opcion invalida.");
            }
        }
        repo.guardarTodo();
        System.out.println("Datos guardados. Hasta luego.");
    }

    private static void mostrarMenu() {
        System.out.println();
        System.out.println("===== CAFETERIA UTN =====");
        System.out.println("1. Registrar ingrediente");
        System.out.println("2. Registrar bebida");
        System.out.println("3. Registrar plato (con su receta)");
        System.out.println("4. Agregar ingredientes a la receta de un plato");
        System.out.println("5. Eliminar ingrediente");
        System.out.println("6. Abastecer ingredientes");
        System.out.println("7. Abastecer bebidas");
        System.out.println("8. Registrar venta");
        System.out.println("9. Reportes del administrador");
        System.out.println("10. Ver catalogo y stock");
        System.out.println("0. Salir");
    }

    // ---------- Lectura por teclado ----------

    private static String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return teclado.nextLine().trim();
    }

    private static int leerEntero(String mensaje) {
        while (true) {
            String texto = leerTexto(mensaje);
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("Ingrese un numero entero valido.");
            }
        }
    }

    // Acepta coma o punto como separador decimal
    private static double leerDecimal(String mensaje) {
        while (true) {
            String texto = leerTexto(mensaje).replace(',', '.');
            try {
                return Double.parseDouble(texto);
            } catch (NumberFormatException e) {
                System.out.println("Ingrese un numero valido.");
            }
        }
    }

    // ---------- Catalogo ----------

    private static void registrarIngrediente() {
        System.out.println("--- Registrar ingrediente ---");
        String id = leerTexto("Id (ej: ING001): ");
        String nombre = leerTexto("Nombre: ");
        String unidad = leerTexto("Unidad (kg o l): ").toLowerCase();
        double costo = leerDecimal("Costo por unidad: ");
        double minimo = leerDecimal("Stock minimo: ");
        if (inventario.agregarIngrediente(id, nombre, unidad, costo, minimo)) {
            repo.guardarTodo();
        }
    }

    private static void registrarBebida() {
        System.out.println("--- Registrar bebida ---");
        String id = leerTexto("Id (ej: BEB001): ");
        String nombre = leerTexto("Nombre: ");
        double precio = leerDecimal("Precio fijo (sin IVA): ");
        if (inventario.agregarBebida(id, nombre, precio)) {
            repo.guardarTodo();
        }
    }

    private static void registrarPlato() {
        System.out.println("--- Registrar plato ---");
        String id = leerTexto("Id (ej: PLA001): ");
        String nombre = leerTexto("Nombre: ");
        if (inventario.agregarPlato(id, nombre)) {
            repo.guardarTodo();
            agregarIngredientesAReceta(id);
        }
    }

    private static void agregarRecetaAPlato() {
        System.out.println("--- Agregar ingredientes a una receta ---");
        mostrarPlatos();
        String idPlato = leerTexto("Id del plato: ");
        agregarIngredientesAReceta(idPlato);
    }

    private static void agregarIngredientesAReceta(String idPlato) {
        if (repo.buscarPlato(idPlato) == null) {
            System.out.println("ERROR: no existe el plato " + idPlato);
            return;
        }
        mostrarIngredientes();
        while (true) {
            String idIngrediente = leerTexto("Id del ingrediente (vacio para terminar): ");
            if (idIngrediente.equals("")) {
                break;
            }
            double cantidad = leerDecimal("Cantidad por plato (kg o l): ");
            if (inventario.agregarIngredienteAReceta(idPlato, idIngrediente, cantidad)) {
                repo.guardarTodo();
            }
        }
    }

    private static void eliminarIngrediente() {
        System.out.println("--- Eliminar ingrediente ---");
        mostrarIngredientes();
        String id = leerTexto("Id del ingrediente a eliminar: ");
        if (inventario.eliminarIngrediente(id)) {
            repo.guardarTodo();
        }
    }

    // ---------- Abastecimientos ----------

    private static void abastecerIngredientes() {
        System.out.println("--- Abastecer ingredientes ---");
        String fecha = leerTexto("Fecha (aaaa-mm-dd): ");
        if (!Validador.fechaValida(fecha)) {
            System.out.println("ERROR: la fecha debe tener formato aaaa-mm-dd");
            return;
        }
        mostrarIngredientes();
        AbastecimientoIngrediente abast = inventario.crearAbastecimientoIngrediente(fecha);
        while (true) {
            String id = leerTexto("Id del ingrediente (vacio para terminar): ");
            if (id.equals("")) {
                break;
            }
            Ingrediente ingrediente = repo.buscarIngrediente(id);
            if (ingrediente == null) {
                System.out.println("No existe ese ingrediente.");
                continue;
            }
            if (yaEstaEnAbastecimiento(abast, id)) {
                System.out.println("Ese ingrediente ya esta en este abastecimiento.");
                continue;
            }
            double cantidad = leerDecimal("Cantidad (" + ingrediente.getUnidadMedida() + "): ");
            double costo = leerDecimal("Costo por " + ingrediente.getUnidadMedida() + ": ");
            if (cantidad <= 0 || costo < 0) {
                System.out.println("Cantidad o costo invalido.");
                continue;
            }
            abast.agregarDetalle(ingrediente, cantidad, costo);
        }
        if (abast.getDetalles().size() == 0) {
            System.out.println("No se agrego ningun ingrediente.");
            return;
        }
        if (inventario.registrarAbastecimientoIngrediente(abast)) {
            repo.guardarTodo();
        }
    }

    private static boolean yaEstaEnAbastecimiento(AbastecimientoIngrediente abast, String idIngrediente) {
        for (DetalleAbastecimientoIngrediente d : abast.getDetalles()) {
            if (d.getIngrediente().getIdIngrediente().equals(idIngrediente)) {
                return true;
            }
        }
        return false;
    }

    private static void abastecerBebidas() {
        System.out.println("--- Abastecer bebidas ---");
        String fecha = leerTexto("Fecha (aaaa-mm-dd): ");
        if (!Validador.fechaValida(fecha)) {
            System.out.println("ERROR: la fecha debe tener formato aaaa-mm-dd");
            return;
        }
        mostrarBebidas();
        AbastecimientoBebida abast = inventario.crearAbastecimientoBebida(fecha);
        while (true) {
            String id = leerTexto("Id de la bebida (vacio para terminar): ");
            if (id.equals("")) {
                break;
            }
            Bebida bebida = repo.buscarBebida(id);
            if (bebida == null) {
                System.out.println("No existe esa bebida.");
                continue;
            }
            if (yaEstaEnAbastecimientoBebida(abast, id)) {
                System.out.println("Esa bebida ya esta en este abastecimiento.");
                continue;
            }
            int cantidad = leerEntero("Cantidad (unidades): ");
            if (cantidad <= 0) {
                System.out.println("Cantidad invalida.");
                continue;
            }
            abast.agregarDetalle(bebida, cantidad);
        }
        if (abast.getDetalles().size() == 0) {
            System.out.println("No se agrego ninguna bebida.");
            return;
        }
        if (inventario.registrarAbastecimientoBebida(abast)) {
            repo.guardarTodo();
        }
    }

    private static boolean yaEstaEnAbastecimientoBebida(AbastecimientoBebida abast, String idBebida) {
        for (DetalleAbastecimientoBebida d : abast.getDetalles()) {
            if (d.getBebida().getIdBebida().equals(idBebida)) {
                return true;
            }
        }
        return false;
    }

    // ---------- Venta ----------

    private static void registrarVenta() {
        System.out.println("--- Registrar venta ---");
        String fecha = leerTexto("Fecha (aaaa-mm-dd): ");
        if (!Validador.fechaValida(fecha)) {
            System.out.println("ERROR: la fecha debe tener formato aaaa-mm-dd");
            return;
        }
        mostrarPlatos();
        mostrarBebidas();

        // Se acumulan los productos; si se repite uno, se suman las cantidades
        ArrayList<Plato> platos = new ArrayList<Plato>();
        ArrayList<Integer> cantidadesPlatos = new ArrayList<Integer>();
        ArrayList<Bebida> bebidas = new ArrayList<Bebida>();
        ArrayList<Integer> cantidadesBebidas = new ArrayList<Integer>();

        int tipo = -1;
        while (tipo != 0) {
            System.out.println("1. Agregar plato   2. Agregar bebida   0. Terminar y registrar");
            tipo = leerEntero("Opcion: ");
            if (tipo == 1) {
                String id = leerTexto("Id del plato: ");
                Plato plato = repo.buscarPlato(id);
                if (plato == null) {
                    System.out.println("No existe ese plato.");
                } else {
                    int cantidad = leerEntero("Cantidad: ");
                    if (cantidad <= 0) {
                        System.out.println("La cantidad debe ser mayor a 0.");
                    } else {
                        int posicion = platos.indexOf(plato);
                        if (posicion == -1) {
                            platos.add(plato);
                            cantidadesPlatos.add(cantidad);
                        } else {
                            cantidadesPlatos.set(posicion, cantidadesPlatos.get(posicion) + cantidad);
                        }
                    }
                }
            } else if (tipo == 2) {
                String id = leerTexto("Id de la bebida: ");
                Bebida bebida = repo.buscarBebida(id);
                if (bebida == null) {
                    System.out.println("No existe esa bebida.");
                } else {
                    int cantidad = leerEntero("Cantidad: ");
                    if (cantidad <= 0) {
                        System.out.println("La cantidad debe ser mayor a 0.");
                    } else {
                        int posicion = bebidas.indexOf(bebida);
                        if (posicion == -1) {
                            bebidas.add(bebida);
                            cantidadesBebidas.add(cantidad);
                        } else {
                            cantidadesBebidas.set(posicion, cantidadesBebidas.get(posicion) + cantidad);
                        }
                    }
                }
            } else if (tipo != 0) {
                System.out.println("Opcion invalida.");
            }
        }

        Venta venta = gestorVentas.crearVenta(fecha);
        for (int i = 0; i < platos.size(); i++) {
            venta.agregarPlato(platos.get(i), cantidadesPlatos.get(i));
        }
        for (int i = 0; i < bebidas.size(); i++) {
            venta.agregarBebida(bebidas.get(i), cantidadesBebidas.get(i));
        }
        if (gestorVentas.registrarVenta(venta)) {
            repo.guardarTodo();
        }
    }

    // ---------- Reportes ----------

    private static void menuReportes() {
        int opcion = -1;
        while (opcion != 0) {
            System.out.println();
            System.out.println("--- REPORTES DEL ADMINISTRADOR ---");
            System.out.println("1. Ingredientes bajo el stock minimo (lista de compras)");
            System.out.println("2. Costo, precio y margen de los platos");
            System.out.println("3. Platos que se pueden preparar con el stock actual");
            System.out.println("4. Platos mas vendidos en un periodo");
            System.out.println("5. Auditoria del stock de ingredientes");
            System.out.println("0. Volver");
            opcion = leerEntero("Opcion: ");
            System.out.println();
            switch (opcion) {
                case 1:
                    reportes.ingredientesBajoMinimo();
                    break;
                case 2:
                    reportes.preciosYMargenes();
                    break;
                case 3:
                    reportes.platosPreparables();
                    break;
                case 4: {
                    String desde = leerTexto("Desde (aaaa-mm-dd): ");
                    String hasta = leerTexto("Hasta (aaaa-mm-dd): ");
                    reportes.platosMasVendidos(desde, hasta);
                    break;
                }
                case 5: {
                    String id = leerTexto("Id del ingrediente (vacio para todos): ");
                    reportes.auditoriaStock(id);
                    break;
                }
                case 0:
                    break;
                default:
                    System.out.println("Opcion invalida.");
            }
        }
    }

    // ---------- Listados ----------

    private static void mostrarCatalogo() {
        mostrarIngredientes();
        mostrarBebidas();
        mostrarPlatos();
    }

    private static void mostrarIngredientes() {
        System.out.println("INGREDIENTES (id | nombre | stock | minimo | costo por unidad)");
        if (repo.getIngredientes().size() == 0) {
            System.out.println("  (no hay ingredientes)");
        }
        for (Ingrediente i : repo.getIngredientes()) {
            System.out.println("  " + i.getIdIngrediente() + " | " + i.getNombre()
                    + " | " + Formato.cantidad(i.getStockActual()) + " " + i.getUnidadMedida()
                    + " | " + Formato.cantidad(i.getStockMinimo()) + " " + i.getUnidadMedida()
                    + " | $" + Formato.dinero(i.getCostoUnitario()));
        }
    }

    private static void mostrarBebidas() {
        System.out.println("BEBIDAS (id | nombre | precio sin IVA | precio con IVA | stock)");
        if (repo.getBebidas().size() == 0) {
            System.out.println("  (no hay bebidas)");
        }
        for (Bebida b : repo.getBebidas()) {
            System.out.println("  " + b.getIdBebida() + " | " + b.getNombre()
                    + " | $" + Formato.dinero(b.getPrecioFijo())
                    + " | $" + Formato.dinero(b.getPrecioFijo() + b.calcularIva())
                    + " | " + b.getStock() + " u");
        }
    }

    private static void mostrarPlatos() {
        System.out.println("PLATOS (id | nombre | precio de venta)");
        if (repo.getPlatos().size() == 0) {
            System.out.println("  (no hay platos)");
        }
        for (Plato p : repo.getPlatos()) {
            System.out.println("  " + p.getIdPlato() + " | " + p.getNombre()
                    + " | $" + Formato.dinero(p.calcularPrecioVenta()));
            for (DetalleReceta d : p.getReceta()) {
                System.out.println("      - " + d.getIngrediente().getNombre() + ": "
                        + Formato.cantidad(d.getCantidad()) + " " + d.getIngrediente().getUnidadMedida());
            }
        }
    }
}
