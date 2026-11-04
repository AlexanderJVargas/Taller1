public class DetalleAbastecimientoIngrediente {
    private Ingrediente ingrediente;
    private double cantidad;
    private double costoUnitario;  // costo pagado en esa compra

    public DetalleAbastecimientoIngrediente(Ingrediente ingrediente, double cantidad, double costoUnitario) {
        this.ingrediente = ingrediente;
        this.cantidad = cantidad;
        this.costoUnitario = costoUnitario;
    }

    public Ingrediente getIngrediente() { return ingrediente; }
    public double getCantidad() { return cantidad; }
    public double getCostoUnitario() { return costoUnitario; }
}
