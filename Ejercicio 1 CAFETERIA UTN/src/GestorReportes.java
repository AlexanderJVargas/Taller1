import java.util.ArrayList;

// Reportes que pide el administrador de la cafeteria
public class GestorReportes {
    private RepositorioArchivos repo;

    public GestorReportes(RepositorioArchivos repo) {
        this.repo = repo;
    }

    // 1. Ingredientes por debajo del stock minimo, como lista de compras
    public void ingredientesBajoMinimo() {
        System.out.println("--- Ingredientes bajo el stock minimo (lista de compras) ---");
        boolean hay = false;
        for (Ingrediente i : repo.getIngredientes()) {
            if (i.estaBajoMinimo()) {
                double faltante = i.getStockMinimo() - i.getStockActual();
                System.out.println(i.getIdIngrediente() + " | " + i.getNombre()
                        + " | stock: " + Formato.cantidad(i.getStockActual()) + " " + i.getUnidadMedida()
                        + " | minimo: " + Formato.cantidad(i.getStockMinimo()) + " " + i.getUnidadMedida()
                        + " | comprar al menos: " + Formato.cantidad(faltante) + " " + i.getUnidadMedida());
                hay = true;
            }
        }
        if (!hay) {
            System.out.println("Todos los ingredientes estan sobre su stock minimo.");
        }
    }

    // 2. Costo, precio de venta y margen de cada plato
    public void preciosYMargenes() {
        System.out.println("--- Costo, precio y margen de los platos ---");
        if (repo.getPlatos().size() == 0) {
            System.out.println("No hay platos registrados.");
            return;
        }
        for (Plato p : repo.getPlatos()) {
            double costo = p.calcularCosto();
            double precio = p.calcularPrecioVenta();
            double margen = precio - costo;
            String aviso = "";
            if (p.getReceta().size() == 0) {
                aviso = " (sin receta)";
            }
            System.out.println(p.getIdPlato() + " | " + p.getNombre()
                    + " | costo: $" + Formato.dinero(costo)
                    + " | precio: $" + Formato.dinero(precio)
                    + " | margen: $" + Formato.dinero(margen) + aviso);
        }
    }

    // 3. Platos que se pueden preparar con el stock actual y cuantas unidades de cada uno
    public void platosPreparables() {
        System.out.println("--- Platos que se pueden preparar con el stock actual ---");
        boolean hay = false;
        for (Plato p : repo.getPlatos()) {
            int unidades = calcularUnidadesPreparables(p);
            if (unidades > 0) {
                System.out.println(p.getIdPlato() + " | " + p.getNombre() + " | unidades: " + unidades);
                hay = true;
            }
        }
        if (!hay) {
            System.out.println("Con el stock actual no se puede preparar ningun plato.");
        } else {
            System.out.println("Nota: cada cifra se calcula por separado; los platos comparten ingredientes,");
            System.out.println("por eso no se pueden preparar todos a la vez.");
        }
    }

    // Se limita por el ingrediente que se acaba primero
    private int calcularUnidadesPreparables(Plato plato) {
        if (plato.getReceta().size() == 0) {
            return 0;
        }
        int minimo = -1;
        for (DetalleReceta r : plato.getReceta()) {
            int alcanza = (int) ((r.getIngrediente().getStockActual() + 0.000001) / r.getCantidad());
            if (minimo == -1 || alcanza < minimo) {
                minimo = alcanza;
            }
        }
        return minimo;
    }

    // 4. Platos mas vendidos en un periodo (fechas en formato aaaa-mm-dd)
    public void platosMasVendidos(String desde, String hasta) {
        System.out.println("--- Platos mas vendidos del " + desde + " al " + hasta + " ---");
        if (!Validador.fechaValida(desde) || !Validador.fechaValida(hasta)) {
            System.out.println("ERROR: las fechas deben tener formato aaaa-mm-dd");
            return;
        }
        if (desde.compareTo(hasta) > 0) {
            System.out.println("ERROR: la fecha inicial es mayor que la final");
            return;
        }

        // Se suman las cantidades vendidas de cada plato en el periodo
        ArrayList<Plato> platos = new ArrayList<Plato>();
        ArrayList<Integer> cantidades = new ArrayList<Integer>();
        for (Venta v : repo.getVentas()) {
            if (v.getFecha().compareTo(desde) >= 0 && v.getFecha().compareTo(hasta) <= 0) {
                for (DetalleVentaPlato d : v.getPlatos()) {
                    int posicion = platos.indexOf(d.getPlato());
                    if (posicion == -1) {
                        platos.add(d.getPlato());
                        cantidades.add(d.getCantidad());
                    } else {
                        cantidades.set(posicion, cantidades.get(posicion) + d.getCantidad());
                    }
                }
            }
        }

        if (platos.size() == 0) {
            System.out.println("No hubo ventas de platos en ese periodo.");
            return;
        }

        // Se ordena de mayor a menor (burbuja), moviendo las dos listas juntas
        for (int i = 0; i < platos.size() - 1; i++) {
            for (int j = 0; j < platos.size() - 1 - i; j++) {
                if (cantidades.get(j) < cantidades.get(j + 1)) {
                    int auxCantidad = cantidades.get(j);
                    cantidades.set(j, cantidades.get(j + 1));
                    cantidades.set(j + 1, auxCantidad);

                    Plato auxPlato = platos.get(j);
                    platos.set(j, platos.get(j + 1));
                    platos.set(j + 1, auxPlato);
                }
            }
        }

        for (int i = 0; i < platos.size(); i++) {
            System.out.println((i + 1) + ". " + platos.get(i).getNombre() + " | unidades vendidas: " + cantidades.get(i));
        }
    }

    // 5. Auditoria del stock: movimientos y verificacion contra el stock actual
    // Si idIngrediente esta vacio, se muestran todos los ingredientes
    public void auditoriaStock(String idIngrediente) {
        boolean filtrar = !idIngrediente.equals("");
        if (filtrar && repo.buscarIngrediente(idIngrediente) == null) {
            System.out.println("ERROR: no existe el ingrediente " + idIngrediente);
            return;
        }

        System.out.println("--- Auditoria de stock: movimientos ---");
        boolean hay = false;
        for (AuditoriaStock a : repo.getAuditorias()) {
            if (!filtrar || a.getIngrediente().getIdIngrediente().equals(idIngrediente)) {
                System.out.println(a.getIdAuditoria() + " | " + a.getFecha() + " | " + a.getIngrediente().getNombre()
                        + " | " + a.getTipoMovimiento() + " " + a.getIdReferencia()
                        + " | cantidad: " + Formato.cantidad(a.getCantidad())
                        + " | stock: " + Formato.cantidad(a.getStockAnterior())
                        + " -> " + Formato.cantidad(a.getStockNuevo()));
                hay = true;
            }
        }
        if (!hay) {
            System.out.println("No hay movimientos registrados.");
        }

        // El stock actual debe ser igual a lo que entro menos lo que salio
        System.out.println("--- Auditoria de stock: verificacion ---");
        for (Ingrediente i : repo.getIngredientes()) {
            if (!filtrar || i.getIdIngrediente().equals(idIngrediente)) {
                double entradas = 0;
                double salidas = 0;
                for (AuditoriaStock a : repo.getAuditorias()) {
                    if (a.getIngrediente().getIdIngrediente().equals(i.getIdIngrediente())) {
                        if (a.getTipoMovimiento().equals("ABASTECIMIENTO")) {
                            entradas = entradas + a.getCantidad();
                        } else {
                            salidas = salidas + a.getCantidad();
                        }
                    }
                }
                double esperado = entradas - salidas;
                double diferencia = i.getStockActual() - esperado;
                String estado = "OK";
                if (diferencia > 0.001 || diferencia < -0.001) {
                    estado = "DIFERENCIA de " + Formato.cantidad(diferencia);
                }
                System.out.println(i.getNombre() + " | entradas: " + Formato.cantidad(entradas)
                        + " | salidas: " + Formato.cantidad(salidas)
                        + " | esperado: " + Formato.cantidad(esperado)
                        + " | actual: " + Formato.cantidad(i.getStockActual()) + " | " + estado);
            }
        }
    }
}
