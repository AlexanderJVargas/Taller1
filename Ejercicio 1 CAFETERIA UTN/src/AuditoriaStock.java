public class AuditoriaStock {
    private String idAuditoria;
    private Ingrediente ingrediente;
    private String fecha;
    private String tipoMovimiento;  // "ABASTECIMIENTO" o "VENTA"
    private String idReferencia;    // id de la venta o del abastecimiento
    private double cantidad;
    private double stockAnterior;
    private double stockNuevo;

    public AuditoriaStock(String idAuditoria, Ingrediente ingrediente, String fecha,
                          String tipoMovimiento, String idReferencia, double cantidad,
                          double stockAnterior, double stockNuevo) {
        this.idAuditoria = idAuditoria;
        this.ingrediente = ingrediente;
        this.fecha = fecha;
        this.tipoMovimiento = tipoMovimiento;
        this.idReferencia = idReferencia;
        this.cantidad = cantidad;
        this.stockAnterior = stockAnterior;
        this.stockNuevo = stockNuevo;
    }

    public String getIdAuditoria() { return idAuditoria; }
    public Ingrediente getIngrediente() { return ingrediente; }
    public String getFecha() { return fecha; }
    public String getTipoMovimiento() { return tipoMovimiento; }
    public String getIdReferencia() { return idReferencia; }
    public double getCantidad() { return cantidad; }
    public double getStockAnterior() { return stockAnterior; }
    public double getStockNuevo() { return stockNuevo; }
}
