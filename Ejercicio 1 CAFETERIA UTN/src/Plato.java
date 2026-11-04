import java.util.ArrayList;

public class Plato {
    private String idPlato;
    private String nombre;
    private ArrayList<DetalleReceta> receta;

    public Plato(String idPlato, String nombre) {
        this.idPlato = idPlato;
        this.nombre = nombre;
        this.receta = new ArrayList<DetalleReceta>();
    }

    public String getIdPlato() { return idPlato; }
    public String getNombre() { return nombre; }
    public ArrayList<DetalleReceta> getReceta() { return receta; }

    public void agregarIngrediente(Ingrediente ingrediente, double cantidad) {
        receta.add(new DetalleReceta(ingrediente, cantidad));
    }

    // Revisa si algun renglon de la receta usa ese ingrediente
    public boolean usaIngrediente(String idIngrediente) {
        for (DetalleReceta d : receta) {
            if (d.getIngrediente().getIdIngrediente().equals(idIngrediente)) {
                return true;
            }
        }
        return false;
    }

    // Costo = suma de (cantidad x costo unitario) de cada ingrediente
    public double calcularCosto() {
        double costo = 0;
        for (DetalleReceta d : receta) {
            costo = costo + d.getCantidad() * d.getIngrediente().getCostoUnitario();
        }
        return costo;
    }

    // Precio de venta = el doble del costo
    public double calcularPrecioVenta() {
        return calcularCosto() * 2;
    }
}
