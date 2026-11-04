import java.util.ArrayList;

public class Venta {
    private String idVenta;
    private String fecha;  // formato aaaa-mm-dd
    private ArrayList<DetalleVentaPlato> platos;
    private ArrayList<DetalleVentaBebida> bebidas;
    private double subtotal;
    private double iva;
    private double total;

    public Venta(String idVenta, String fecha) {
        this.idVenta = idVenta;
        this.fecha = fecha;
        this.platos = new ArrayList<DetalleVentaPlato>();
        this.bebidas = new ArrayList<DetalleVentaBebida>();
    }

    public String getIdVenta() { return idVenta; }
    public String getFecha() { return fecha; }
    public ArrayList<DetalleVentaPlato> getPlatos() { return platos; }
    public ArrayList<DetalleVentaBebida> getBebidas() { return bebidas; }
    public double getSubtotal() { return subtotal; }
    public double getIva() { return iva; }
    public double getTotal() { return total; }

    public void agregarPlato(Plato plato, int cantidad) {
        platos.add(new DetalleVentaPlato(plato, cantidad, plato.calcularPrecioVenta()));
    }

    public void agregarBebida(Bebida bebida, int cantidad) {
        bebidas.add(new DetalleVentaBebida(bebida, cantidad, bebida.getPrecioFijo()));
    }

    // El IVA (15 %) se suma solo a las bebidas
    public void calcularTotales() {
        double totalPlatos = 0;
        for (DetalleVentaPlato d : platos) {
            totalPlatos = totalPlatos + d.getSubtotal();
        }
        double totalBebidas = 0;
        for (DetalleVentaBebida d : bebidas) {
            totalBebidas = totalBebidas + d.getSubtotal();
        }
        subtotal = totalPlatos + totalBebidas;
        iva = totalBebidas * Bebida.IVA;
        total = subtotal + iva;
    }
}
