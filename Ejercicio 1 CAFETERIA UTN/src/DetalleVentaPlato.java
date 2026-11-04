public class DetalleVentaPlato {
    private Plato plato;
    private int cantidad;
    private double precioUnitario;  // precio al momento de la venta

    public DetalleVentaPlato(Plato plato, int cantidad, double precioUnitario) {
        this.plato = plato;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public Plato getPlato() { return plato; }
    public int getCantidad() { return cantidad; }
    public double getPrecioUnitario() { return precioUnitario; }

    public double getSubtotal() {
        return cantidad * precioUnitario;
    }
}
