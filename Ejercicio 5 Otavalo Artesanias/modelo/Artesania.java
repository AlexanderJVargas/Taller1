package modelo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Artesania {


    private String codigo;
    private String nombre;
    private String tipo; // "TEXTIL" o "MADERA"
    private double precioVenta;
    private int stockActual;
    private LocalDate fechaRecepcion;
    private String artesanoId;

    public Artesania(String codigo, String nombre, String tipo, double precioVenta, int stockActual, LocalDate fechaRecepcion, String artesanoId) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.tipo = tipo;
        this.precioVenta = precioVenta;
        this.stockActual = stockActual;
        this.fechaRecepcion = fechaRecepcion;
        this.artesanoId = artesanoId;
    }

    public double calcularComisionUnitaria() {
        if (tipo.equalsIgnoreCase("TEXTIL")) {
            return precioVenta * 0.20; // 20%
        } else if (tipo.equalsIgnoreCase("MADERA")) {
            return precioVenta * 0.25; // 25%
        }
        return 0.0;
    }

    public boolean debeDevolverse() {
        long dias = ChronoUnit.DAYS.between(fechaRecepcion, LocalDate.now());
        return dias > 90 && stockActual > 0;
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getTipo() { return tipo; }
    public double getPrecioVenta() { return precioVenta; }
    public int getStockActual() { return stockActual; }
    
    public void setStockActual(int stockActual) {
        this.stockActual = stockActual;
    }

    public void setStock(int stockActual) {
        this.stockActual = stockActual;
    }

    public LocalDate getFechaRecepcion() { return fechaRecepcion; }
    public String getArtesanoId() { return artesanoId; }

    public String aCsv() {
        return codigo + "," + nombre + "," + tipo + "," + precioVenta + "," + stockActual + "," + fechaRecepcion + "," + artesanoId;
    }

    public static Artesania desdeCsv(String linea) {
        String[] p = linea.split(",");
        return new Artesania(p[0], p[1], p[2], Double.parseDouble(p[3]), Integer.parseInt(p[4]), LocalDate.parse(p[5]), p[6]);
    }
}