public class Bebida {
    public static final double IVA = 0.15;

    private String idBebida;
    private String nombre;
    private double precioFijo;  // sin IVA
    private int stock;          // en unidades

    public Bebida(String idBebida, String nombre, double precioFijo, int stock) {
        this.idBebida = idBebida;
        this.nombre = nombre;
        this.precioFijo = precioFijo;
        this.stock = stock;
    }

    public String getIdBebida() { return idBebida; }
    public String getNombre() { return nombre; }
    public double getPrecioFijo() { return precioFijo; }
    public int getStock() { return stock; }

    public boolean hayStock(int cantidad) {
        return stock >= cantidad;
    }

    public void descontar(int cantidad) {
        stock = stock - cantidad;
    }

    public void aumentar(int cantidad) {
        stock = stock + cantidad;
    }

    // IVA de una unidad
    public double calcularIva() {
        return precioFijo * IVA;
    }
}
