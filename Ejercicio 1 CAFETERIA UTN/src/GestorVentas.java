import java.util.ArrayList;

// Registra ventas: valida todo primero y solo despues descuenta el stock
public class GestorVentas {
    private RepositorioArchivos repo;

    public GestorVentas(RepositorioArchivos repo) {
        this.repo = repo;
    }

    // Crea la venta con su id automatico; todavia no se registra
    public Venta crearVenta(String fecha) {
        String id = repo.siguienteId("VEN", repo.getVentas().size());
        return new Venta(id, fecha);
    }

    public boolean registrarVenta(Venta venta) {
        if (!Validador.fechaValida(venta.getFecha())) {
            System.out.println("ERROR: la fecha debe tener formato aaaa-mm-dd");
            return false;
        }
        if (venta.getPlatos().size() == 0 && venta.getBebidas().size() == 0) {
            System.out.println("ERROR: la venta no tiene productos");
            return false;
        }
        if (!validarDetalles(venta)) {
            return false;
        }

        // Cuanto se necesita de cada ingrediente sumando TODOS los platos de la venta
        ArrayList<Ingrediente> ingredientes = new ArrayList<Ingrediente>();
        ArrayList<Double> necesarios = new ArrayList<Double>();
        calcularNecesidades(venta, ingredientes, necesarios);

        // Si falta algo, se rechaza la venta completa y no se toca nada
        if (!validarStockVenta(venta, ingredientes, necesarios)) {
            System.out.println("Venta " + venta.getIdVenta() + " rechazada: el stock no se modifico");
            return false;
        }

        // Desde aqui ya se sabe que todo alcanza: recien ahora se descuenta
        for (int i = 0; i < ingredientes.size(); i++) {
            Ingrediente ingrediente = ingredientes.get(i);
            double cantidad = necesarios.get(i);
            double anterior = ingrediente.getStockActual();
            ingrediente.descontar(cantidad);
            repo.agregarAuditoria(ingrediente, venta.getFecha(), "VENTA", venta.getIdVenta(),
                    cantidad, anterior, ingrediente.getStockActual());
        }
        for (DetalleVentaBebida d : venta.getBebidas()) {
            d.getBebida().descontar(d.getCantidad());
        }

        venta.calcularTotales();
        repo.getVentas().add(venta);
        System.out.println("Venta " + venta.getIdVenta() + " registrada. Subtotal: " + Formato.dinero(venta.getSubtotal())
                + " | IVA: " + Formato.dinero(venta.getIva()) + " | Total: " + Formato.dinero(venta.getTotal()));
        return true;
    }

    // Cantidades positivas, platos con receta y sin productos repetidos en la venta
    private boolean validarDetalles(Venta venta) {
        ArrayList<DetalleVentaPlato> platos = venta.getPlatos();
        for (int i = 0; i < platos.size(); i++) {
            DetalleVentaPlato d = platos.get(i);
            if (d.getCantidad() <= 0) {
                System.out.println("ERROR: cantidad invalida para " + d.getPlato().getNombre());
                return false;
            }
            if (d.getPlato().getReceta().size() == 0) {
                System.out.println("ERROR: el plato " + d.getPlato().getNombre() + " no tiene receta");
                return false;
            }
            for (int j = i + 1; j < platos.size(); j++) {
                if (platos.get(j).getPlato().getIdPlato().equals(d.getPlato().getIdPlato())) {
                    System.out.println("ERROR: " + d.getPlato().getNombre() + " esta repetido en la venta");
                    return false;
                }
            }
        }

        ArrayList<DetalleVentaBebida> bebidas = venta.getBebidas();
        for (int i = 0; i < bebidas.size(); i++) {
            DetalleVentaBebida d = bebidas.get(i);
            if (d.getCantidad() <= 0) {
                System.out.println("ERROR: cantidad invalida para " + d.getBebida().getNombre());
                return false;
            }
            for (int j = i + 1; j < bebidas.size(); j++) {
                if (bebidas.get(j).getBebida().getIdBebida().equals(d.getBebida().getIdBebida())) {
                    System.out.println("ERROR: " + d.getBebida().getNombre() + " esta repetida en la venta");
                    return false;
                }
            }
        }
        return true;
    }

    // Llena dos listas paralelas: el ingrediente y cuanto se necesita en total
    private void calcularNecesidades(Venta venta, ArrayList<Ingrediente> ingredientes, ArrayList<Double> necesarios) {
        for (DetalleVentaPlato d : venta.getPlatos()) {
            for (DetalleReceta r : d.getPlato().getReceta()) {
                double necesidad = r.getCantidad() * d.getCantidad();
                int posicion = ingredientes.indexOf(r.getIngrediente());
                if (posicion == -1) {
                    ingredientes.add(r.getIngrediente());
                    necesarios.add(necesidad);
                } else {
                    necesarios.set(posicion, necesarios.get(posicion) + necesidad);
                }
            }
        }
    }

    // Revisa ingredientes y bebidas; informa todo lo que falta, no solo lo primero
    private boolean validarStockVenta(Venta venta, ArrayList<Ingrediente> ingredientes, ArrayList<Double> necesarios) {
        boolean alcanza = true;
        for (int i = 0; i < ingredientes.size(); i++) {
            Ingrediente ingrediente = ingredientes.get(i);
            double necesario = necesarios.get(i);
            if (!ingrediente.hayStock(necesario)) {
                System.out.println("Falta stock de " + ingrediente.getNombre() + ": necesita " + Formato.cantidad(necesario)
                        + " " + ingrediente.getUnidadMedida() + " y hay " + Formato.cantidad(ingrediente.getStockActual()));
                alcanza = false;
            }
        }
        for (DetalleVentaBebida d : venta.getBebidas()) {
            if (!d.getBebida().hayStock(d.getCantidad())) {
                System.out.println("Falta stock de " + d.getBebida().getNombre() + ": necesita " + d.getCantidad()
                        + " unidades y hay " + d.getBebida().getStock());
                alcanza = false;
            }
        }
        return alcanza;
    }
}
