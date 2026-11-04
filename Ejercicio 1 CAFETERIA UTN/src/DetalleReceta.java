// Una linea de la receta: que ingrediente y cuanto se usa por plato
public class DetalleReceta {
    private Ingrediente ingrediente;
    private double cantidad;  // kg o litros

    public DetalleReceta(Ingrediente ingrediente, double cantidad) {
        this.ingrediente = ingrediente;
        this.cantidad = cantidad;
    }

    public Ingrediente getIngrediente() { return ingrediente; }
    public double getCantidad() { return cantidad; }
}
