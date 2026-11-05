package biblioteca.persistencia;

import biblioteca.modelo.Usuario;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Usuarios guardados en la tabla usuario (H2 local o PostgreSQL). */
public class UsuarioRepositorio {

    private static final String SELECT = "SELECT cedula, nombre FROM usuario ORDER BY nombre";
    private static final String UPSERT = "INSERT INTO usuario (cedula, nombre) VALUES (?, ?)"
            + " ON CONFLICT (cedula) DO UPDATE SET nombre = EXCLUDED.nombre";

    /** Equivalente de UPSERT para la base local H2. */
    private static final String MERGE_H2 = "MERGE INTO usuario (cedula, nombre) KEY (cedula) VALUES (?, ?)";

    private final ConexionBD bd;

    public UsuarioRepositorio(ConexionBD bd) {
        this.bd = bd;
    }

    public List<Usuario> cargar() throws IOException {
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection c = bd.abrir(); PreparedStatement ps = c.prepareStatement(SELECT);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String cedula = rs.getString("cedula");
                try {
                    usuarios.add(new Usuario(cedula, rs.getString("nombre")));
                } catch (RuntimeException e) {
                    throw new IOException("Usuario invalido en la base (" + cedula + "): " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            throw ConexionBD.error("leer usuarios", e);
        }
        return usuarios;
    }

    /** Inserta o actualiza todos los usuarios en una sola transaccion. */
    public void guardarTodos(Collection<Usuario> usuarios) throws IOException {
        try (Connection c = bd.abrir()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(bd.esH2() ? MERGE_H2 : UPSERT)) {
                for (Usuario u : usuarios) {
                    ps.setString(1, u.getCedula());
                    ps.setString(2, u.getNombre());
                    ps.addBatch();
                }
                ps.executeBatch();
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw ConexionBD.error("guardar usuarios", e);
        }
    }
}
