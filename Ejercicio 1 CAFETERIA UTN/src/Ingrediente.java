public class Ingrediente {
    // Los double acumulan errores de redondeo (0.1 + 0.2 no da exactamente 0.3)
    private static final double TOLERANCIA = 0.000001;

    private String idIngrediente;
    private String nombre;
    private String unidadMedida;   // "kg" o "l"
    private double costoUnitario;  // costo por kg o por litro
    private double stockActual;
    private double stockMinimo;

    public Ingrediente(String idIngrediente, String nombre, String unidadMedida,
                       double costoUnitario, double stockActual, double stockMinimo) {
        this.idIngrediente = idIngrediente;
        this.nombre = nombre;
        this.unidadMedida = unidadMedida;
        this.costoUnitario = costoUnitario;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
    }

    public String getIdIngrediente() { return idIngrediente; }
    public String getNombre() { return nombre; }
    public String getUnidadMedida() { return unidadMedida; }
    public double getCostoUnitario() { return costoUnitario; }
    public double getStockActual() { return stockActual; }
    public double getStockMinimo() { return stockMinimo; }

    public void setCostoUnitario(double costoUnitario) { this.costoUnitario = costoUnitario; }

    // Indica si alcanza el stock para la cantidad pedida
    public boolean hayStock(double cantidad) {
        return stockActual + TOLERANCIA >= cantidad;
    }

    public void descontar(double cantidad) {
        stockActual = stockActual - cantidad;
        if (stockActual < TOLERANCIA) {  // evita valores como -0.0000000001
            stockActual = 0;
        }
    }

    public void aumentar(double cantidad) {
        stockActual = stockActual + cantidad;
    }

    // Esta por debajo del minimo cuando el stock es menor al minimo
    public boolean estaBajoMinimo() {
        return stockActual + TOLERANCIA < stockMinimo;
    }
}
