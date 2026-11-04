import java.util.ArrayList;

public class AbastecimientoIngrediente {
    private String idAbastIngrediente;
    private String fecha;  // formato aaaa-mm-dd
    private ArrayList<DetalleAbastecimientoIngrediente> detalles;

    public AbastecimientoIngrediente(String idAbastIngrediente, String fecha) {
        this.idAbastIngrediente = idAbastIngrediente;
        this.fecha = fecha;
        this.detalles = new ArrayList<DetalleAbastecimientoIngrediente>();
    }

    public String getIdAbastIngrediente() { return idAbastIngrediente; }
    public String getFecha() { return fecha; }
    public ArrayList<DetalleAbastecimientoIngrediente> getDetalles() { return detalles; }

    public void agregarDetalle(Ingrediente ingrediente, double cantidad, double costoUnitario) {
        detalles.add(new DetalleAbastecimientoIngrediente(ingrediente, cantidad, costoUnitario));
    }
}
