package modelo;

import java.time.LocalDate;

public class Movimiento {
    private String idMovimiento;
    private String codigoArtesania;
    private String tipoMovimiento; // "RECEPCION", "VENTA", "DEVOLUCION"
    private int cantidad;
    private LocalDate fecha;

    public Movimiento(String idMovimiento, String codigoArtesania, String tipoMovimiento, int cantidad, LocalDate fecha) {
        this.idMovimiento = idMovimiento;
        this.codigoArtesania = codigoArtesania;
        this.tipoMovimiento = tipoMovimiento;
        this.cantidad = cantidad;
        this.fecha = fecha;
    }

    public String getIdMovimiento() { return idMovimiento; }
    public String getCodigoArtesania() { return codigoArtesania; }
    public String getTipoMovimiento() { return tipoMovimiento; }
    public int getCantidad() { return cantidad; }
    public LocalDate getFecha() { return fecha; }

    public String aCsv() {
        return idMovimiento + "," + codigoArtesania + "," + tipoMovimiento + "," + cantidad + "," + fecha;
    }

    public static Movimiento desdeCsv(String linea) {
        String[] p = linea.split(",");
        return new Movimiento(p[0], p[1], p[2], Integer.parseInt(p[3]), LocalDate.parse(p[4]));
    }
}