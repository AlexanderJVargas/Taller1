package modelo;

public class Artesano {
    
    // ========================================================
    // NOTA: Las validaciones de esta clase fueron generadas con IA
    // ========================================================
    
    private String id;
    private String nombre;
    private String comunidad;
    private String telefono;

    public Artesano(String id, String nombre, String comunidad, String telefono) {
        
        // 1. Validación de ID: Exactamente 10 dígitos numéricos
        if (id == null || !id.matches("^[0-9]{10}$")) {
            throw new IllegalArgumentException("Error de validación: El ID debe contener exactamente 10 dígitos numéricos.");
        }
        this.id = id;

        // 2. Validación de Nombre: Solo caracteres (letras y espacios, soportando tildes y ñ)
        if (nombre == null || !nombre.matches("^[a-zA-ZÁÉÍÓÚáéíóúÑñ\\s]+$")) {
            throw new IllegalArgumentException("Error de validación: El nombre solo debe contener letras y espacios.");
        }
        this.nombre = nombre;

        // 3. Validación de Comunidad: Solo caracteres (letras y espacios)
        if (comunidad == null || !comunidad.matches("^[a-zA-ZÁÉÍÓÚáéíóúÑñ\\s]+$")) {
            throw new IllegalArgumentException("Error de validación: La comunidad solo debe contener letras y espacios.");
        }
        this.comunidad = comunidad;

        // 4. Validación de Teléfono: Exactamente 10 dígitos numéricos
        if (telefono == null || !telefono.matches("^[0-9]{10}$")) {
            throw new IllegalArgumentException("Error de validación: El teléfono debe contener exactamente 10 dígitos numéricos.");
        }
        this.telefono = telefono;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getComunidad() { return comunidad; }
    public String getTelefono() { return telefono; }

    public String aCsv() {
        return id + "," + nombre + "," + comunidad + "," + telefono;
    }

    public static Artesano desdeCsv(String linea) {
        String[] p = linea.split(",");
        return new Artesano(p[0], p[1], p[2], p[3]);
    }
}