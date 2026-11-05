
import java.util.ArrayList;

public class AbastecimientoBebida {
    private String idAbastBebida;
    private String fecha;  // formato aaaa-mm-dd
    private ArrayList<DetalleAbastecimientoBebida> detalles;

    public AbastecimientoBebida(String idAbastBebida, String fecha) {
        this.idAbastBebida = idAbastBebida;
        this.fecha = fecha;
        this.detalles = new ArrayList<DetalleAbastecimientoBebida>();
    }

    public String getIdAbastBebida() { return idAbastBebida; }
    public String getFecha() { return fecha; }
    public ArrayList<DetalleAbastecimientoBebida> getDetalles() { return detalles; }

    public void agregarDetalle(Bebida bebida, int cantidad) {
        detalles.add(new DetalleAbastecimientoBebida(bebida, cantidad));
    }
}
