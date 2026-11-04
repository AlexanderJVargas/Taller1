public class DetalleAbastecimientoBebida {
    private Bebida bebida;
    private int cantidad;

    public DetalleAbastecimientoBebida(Bebida bebida, int cantidad) {
        this.bebida = bebida;
        this.cantidad = cantidad;
    }

    public Bebida getBebida() { return bebida; }
    public int getCantidad() { return cantidad; }
}
