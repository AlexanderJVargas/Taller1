import java.util.ArrayList;

// Altas del catalogo, eliminacion de ingredientes y abastecimientos
public class GestorInventario {
    private RepositorioArchivos repo;

    public GestorInventario(RepositorioArchivos repo) {
        this.repo = repo;
    }

    // ---------- Altas del catalogo ----------

    public boolean agregarIngrediente(String id, String nombre, String unidad, double costoUnitario, double stockMinimo) {
        if (!Validador.textoValido(id, 10) || !Validador.textoValido(nombre, 60)) {
            System.out.println("ERROR: el id (max 10) o el nombre (max 60) es invalido");
            return false;
        }
        if (!unidad.equals("kg") && !unidad.equals("l")) {
            System.out.println("ERROR: la unidad debe ser kg o l");
            return false;
        }
        if (costoUnitario < 0 || stockMinimo < 0) {
            System.out.println("ERROR: el costo y el stock minimo no pueden ser negativos");
            return false;
        }
        if (repo.buscarIngrediente(id) != null) {
            System.out.println("ERROR: ya existe un ingrediente con id " + id);
            return false;
        }
        for (Ingrediente i : repo.getIngredientes()) {
            if (i.getNombre().equalsIgnoreCase(nombre)) {
                System.out.println("ERROR: ya existe un ingrediente llamado " + nombre);
                return false;
            }
        }
        // El stock empieza en 0: entra al sistema con un abastecimiento
        repo.getIngredientes().add(new Ingrediente(id, nombre, unidad, costoUnitario, 0, stockMinimo));
        System.out.println("Ingrediente registrado: " + nombre + " (stock inicial 0)");
        return true;
    }

    public boolean agregarBebida(String id, String nombre, double precioFijo) {
        if (!Validador.textoValido(id, 10) || !Validador.textoValido(nombre, 60)) {
            System.out.println("ERROR: el id (max 10) o el nombre (max 60) es invalido");
            return false;
        }
        if (precioFijo < 0) {
            System.out.println("ERROR: el precio no puede ser negativo");
            return false;
        }
        if (repo.buscarBebida(id) != null) {
            System.out.println("ERROR: ya existe una bebida con id " + id);
            return false;
        }
        for (Bebida b : repo.getBebidas()) {
            if (b.getNombre().equalsIgnoreCase(nombre)) {
                System.out.println("ERROR: ya existe una bebida llamada " + nombre);
                return false;
            }
        }
        repo.getBebidas().add(new Bebida(id, nombre, precioFijo, 0));
        System.out.println("Bebida registrada: " + nombre + " (stock inicial 0)");
        return true;
    }

    public boolean agregarPlato(String id, String nombre) {
        if (!Validador.textoValido(id, 10) || !Validador.textoValido(nombre, 60)) {
            System.out.println("ERROR: el id (max 10) o el nombre (max 60) es invalido");
            return false;
        }
        if (repo.buscarPlato(id) != null) {
            System.out.println("ERROR: ya existe un plato con id " + id);
            return false;
        }
        for (Plato p : repo.getPlatos()) {
            if (p.getNombre().equalsIgnoreCase(nombre)) {
                System.out.println("ERROR: ya existe un plato llamado " + nombre);
                return false;
            }
        }
        repo.getPlatos().add(new Plato(id, nombre));
        System.out.println("Plato registrado: " + nombre + " (falta agregarle ingredientes)");
        return true;
    }

    public boolean agregarIngredienteAReceta(String idPlato, String idIngrediente, double cantidad) {
        Plato plato = repo.buscarPlato(idPlato);
        Ingrediente ingrediente = repo.buscarIngrediente(idIngrediente);
        if (plato == null) {
            System.out.println("ERROR: no existe el plato " + idPlato);
            return false;
        }
        if (ingrediente == null) {
            System.out.println("ERROR: no existe el ingrediente " + idIngrediente);
            return false;
        }
        if (cantidad <= 0) {
            System.out.println("ERROR: la cantidad debe ser mayor a 0");
            return false;
        }
        if (plato.usaIngrediente(idIngrediente)) {
            System.out.println("ERROR: " + ingrediente.getNombre() + " ya esta en la receta de " + plato.getNombre());
            return false;
        }
        plato.agregarIngrediente(ingrediente, cantidad);
        System.out.println("Receta actualizada: " + plato.getNombre() + " usa " + cantidad + " "
                + ingrediente.getUnidadMedida() + " de " + ingrediente.getNombre());
        return true;
    }

    // ---------- Eliminar ingrediente ----------

    public boolean eliminarIngrediente(String id) {
        Ingrediente ingrediente = repo.buscarIngrediente(id);
        if (ingrediente == null) {
            System.out.println("ERROR: no existe el ingrediente " + id);
            return false;
        }
        // Regla del enunciado: no se elimina si forma parte de alguna receta
        for (Plato p : repo.getPlatos()) {
            if (p.usaIngrediente(id)) {
                System.out.println("ERROR: no se puede eliminar " + ingrediente.getNombre()
                        + ", forma parte de la receta de " + p.getNombre());
                return false;
            }
        }
        // Igual que el RESTRICT de la base: tampoco si ya tiene historial
        if (tieneMovimientos(id)) {
            System.out.println("ERROR: no se puede eliminar " + ingrediente.getNombre()
                    + ", tiene abastecimientos o auditoria registrados");
            return false;
        }
        repo.getIngredientes().remove(ingrediente);
        System.out.println("Ingrediente eliminado: " + ingrediente.getNombre());
        return true;
    }

    private boolean tieneMovimientos(String idIngrediente) {
        for (AbastecimientoIngrediente a : repo.getAbastecimientosIngredientes()) {
            for (DetalleAbastecimientoIngrediente d : a.getDetalles()) {
                if (d.getIngrediente().getIdIngrediente().equals(idIngrediente)) {
                    return true;
                }
            }
        }
        for (AuditoriaStock a : repo.getAuditorias()) {
            if (a.getIngrediente().getIdIngrediente().equals(idIngrediente)) {
                return true;
            }
        }
        return false;
    }

    // ---------- Abastecimiento de ingredientes ----------

    // Crea el abastecimiento con su id automatico; todavia no se registra
    public AbastecimientoIngrediente crearAbastecimientoIngrediente(String fecha) {
        String id = repo.siguienteId("ABI", repo.getAbastecimientosIngredientes().size());
        return new AbastecimientoIngrediente(id, fecha);
    }

    public boolean registrarAbastecimientoIngrediente(AbastecimientoIngrediente abast) {
        if (!Validador.fechaValida(abast.getFecha())) {
            System.out.println("ERROR: la fecha debe tener formato aaaa-mm-dd");
            return false;
        }
        ArrayList<DetalleAbastecimientoIngrediente> detalles = abast.getDetalles();
        if (detalles.size() == 0) {
            System.out.println("ERROR: el abastecimiento no tiene ingredientes");
            return false;
        }
        // Se valida todo antes de tocar el stock
        for (int i = 0; i < detalles.size(); i++) {
            DetalleAbastecimientoIngrediente d = detalles.get(i);
            if (d.getCantidad() <= 0 || d.getCostoUnitario() < 0) {
                System.out.println("ERROR: cantidad o costo invalido para " + d.getIngrediente().getNombre());
                return false;
            }
            for (int j = i + 1; j < detalles.size(); j++) {
                if (detalles.get(j).getIngrediente().getIdIngrediente().equals(d.getIngrediente().getIdIngrediente())) {
                    System.out.println("ERROR: " + d.getIngrediente().getNombre() + " esta repetido en el abastecimiento");
                    return false;
                }
            }
        }
        // Ahora si se actualiza el stock y se deja la auditoria
        for (DetalleAbastecimientoIngrediente d : detalles) {
            Ingrediente ingrediente = d.getIngrediente();
            double anterior = ingrediente.getStockActual();
            ingrediente.aumentar(d.getCantidad());
            ingrediente.setCostoUnitario(d.getCostoUnitario());  // queda el ultimo costo pagado
            repo.agregarAuditoria(ingrediente, abast.getFecha(), "ABASTECIMIENTO",
                    abast.getIdAbastIngrediente(), d.getCantidad(), anterior, ingrediente.getStockActual());
        }
        repo.getAbastecimientosIngredientes().add(abast);
        System.out.println("Abastecimiento " + abast.getIdAbastIngrediente() + " registrado");
        return true;
    }

    // ---------- Abastecimiento de bebidas ----------

    public AbastecimientoBebida crearAbastecimientoBebida(String fecha) {
        String id = repo.siguienteId("ABB", repo.getAbastecimientosBebidas().size());
        return new AbastecimientoBebida(id, fecha);
    }

    public boolean registrarAbastecimientoBebida(AbastecimientoBebida abast) {
        if (!Validador.fechaValida(abast.getFecha())) {
            System.out.println("ERROR: la fecha debe tener formato aaaa-mm-dd");
            return false;
        }
        ArrayList<DetalleAbastecimientoBebida> detalles = abast.getDetalles();
        if (detalles.size() == 0) {
            System.out.println("ERROR: el abastecimiento no tiene bebidas");
            return false;
        }
        for (int i = 0; i < detalles.size(); i++) {
            DetalleAbastecimientoBebida d = detalles.get(i);
            if (d.getCantidad() <= 0) {
                System.out.println("ERROR: cantidad invalida para " + d.getBebida().getNombre());
                return false;
            }
            for (int j = i + 1; j < detalles.size(); j++) {
                if (detalles.get(j).getBebida().getIdBebida().equals(d.getBebida().getIdBebida())) {
                    System.out.println("ERROR: " + d.getBebida().getNombre() + " esta repetida en el abastecimiento");
                    return false;
                }
            }
        }
        for (DetalleAbastecimientoBebida d : detalles) {
            d.getBebida().aumentar(d.getCantidad());
        }
        repo.getAbastecimientosBebidas().add(abast);
        System.out.println("Abastecimiento " + abast.getIdAbastBebida() + " registrado");
        return true;
    }
}
