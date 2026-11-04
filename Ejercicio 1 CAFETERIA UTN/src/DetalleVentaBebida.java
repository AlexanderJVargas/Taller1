public class DetalleVentaBebida {
    private Bebida bebida;
    private int cantidad;
    private double precioUnitario;  // sin IVA, al momento de la venta

    public DetalleVentaBebida(Bebida bebida, int cantidad, double precioUnitario) {
        this.bebida = bebida;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public Bebida getBebida() { return bebida; }
    public int getCantidad() { return cantidad; }
    public double getPrecioUnitario() { return precioUnitario; }

    public double getSubtotal() {
        return cantidad * precioUnitario;
    }
}
