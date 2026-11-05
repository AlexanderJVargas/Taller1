package biblioteca.persistencia;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/** Datos de conexion a PostgreSQL leidos de db.properties y creacion del esquema. */
public class ConexionBD {

    private static final String[] ESQUEMA = {
        "CREATE TABLE IF NOT EXISTS usuario ("
            + " cedula CHAR(10) PRIMARY KEY CHECK (cedula ~ '^[0-9]{10}$'),"
            + " nombre VARCHAR(100) NOT NULL)",
        "CREATE TABLE IF NOT EXISTS material ("
            + " codigo VARCHAR(20) PRIMARY KEY,"
            + " tipo VARCHAR(10) NOT NULL CHECK (tipo IN ('LIBRO', 'REVISTA')),"
            + " titulo VARCHAR(200) NOT NULL,"
            + " ejemplares INTEGER NOT NULL CHECK (ejemplares >= 1),"
            + " especifico VARCHAR(150) NOT NULL)",
        "CREATE TABLE IF NOT EXISTS prestamo ("
            + " numero BIGINT PRIMARY KEY,"
            + " cedula CHAR(10) NOT NULL REFERENCES usuario (cedula),"
            + " codigo_material VARCHAR(20) NOT NULL REFERENCES material (codigo),"
            + " fecha_prestamo DATE NOT NULL,"
            + " fecha_vencimiento DATE NOT NULL,"
            + " fecha_devolucion DATE,"
            + " multa NUMERIC(8,2) NOT NULL DEFAULT 0 CHECK (multa >= 0),"
            + " multa_pagada BOOLEAN NOT NULL DEFAULT FALSE,"
            + " CHECK (fecha_vencimiento >= fecha_prestamo),"
            + " CHECK (fecha_devolucion IS NULL OR fecha_devolucion >= fecha_prestamo))"
    };

    private final String url;
    private final String usuario;
    private final String contrasena;

    /** Variable de entorno que, si existe, reemplaza a db.properties. */
    public static final String VARIABLE_ENTORNO = "BIBLIO_DB_URL";

    /**
     * Lee la conexion de la variable de entorno BIBLIO_DB_URL o, si no existe, de db.properties.
     * La url puede ser JDBC (jdbc:postgresql://host:5432/base) o la cadena que entregan los servicios
     * en la nube como Neon (postgresql://usuario:contrasena@host/base?sslmode=require).
     */
    public ConexionBD(Path archivoPropiedades) throws IOException {
        Properties p = new Properties();
        String variable = System.getenv(VARIABLE_ENTORNO);
        if (variable != null && !variable.isBlank()) {
            p.setProperty("url", variable.trim());
        } else {
            try (Reader r = Files.newBufferedReader(archivoPropiedades, StandardCharsets.UTF_8)) {
                p.load(r);
            }
        }
        String texto = p.getProperty("url", "jdbc:postgresql://localhost:5432/Biblio").trim();
        if (texto.startsWith("postgres://") || texto.startsWith("postgresql://")) {
            URI uri = URI.create(texto);
            String[] credenciales = uri.getRawUserInfo() == null ? new String[0] : uri.getRawUserInfo().split(":", 2);
            this.usuario = credenciales.length > 0 ? decodificar(credenciales[0]) : p.getProperty("usuario", "");
            this.contrasena = credenciales.length > 1 ? decodificar(credenciales[1]) : p.getProperty("contrasena", "");
            this.url = "jdbc:postgresql://" + uri.getHost() + (uri.getPort() > 0 ? ":" + uri.getPort() : "")
                    + uri.getRawPath() + parametros(uri.getRawQuery());
        } else {
            this.url = texto;
            this.usuario = p.getProperty("usuario", "postgres");
            this.contrasena = p.getProperty("contrasena", "");
        }
    }

    /** Conserva los parametros de la cadena (sslmode) menos channel_binding, que el driver JDBC no usa. */
    private static String parametros(String consulta) {
        if (consulta == null || consulta.isBlank()) return "";
        StringBuilder sb = new StringBuilder();
        for (String par : consulta.split("&")) {
            if (par.isBlank() || par.startsWith("channel_binding=")) continue;
            sb.append(sb.length() == 0 ? "?" : "&").append(par);
        }
        return sb.toString();
    }

    private static String decodificar(String texto) {
        return URLDecoder.decode(texto, StandardCharsets.UTF_8);
    }

    /** Servidor al que se conecta, sin usuario ni contrasena (para mostrarlo al iniciar). */
    public String descripcion() {
        return url.replaceFirst("\\?.*$", "");
    }

    public Connection abrir() throws SQLException {
        return DriverManager.getConnection(url, usuario, contrasena);
    }

    /** Crea las tablas si no existen. */
    public void crearEsquema() throws SQLException {
        try (Connection c = abrir(); Statement st = c.createStatement()) {
            for (String sql : ESQUEMA) st.execute(sql);
        }
    }

    /** true si las tablas de la biblioteca no tienen ningun registro. */
    public boolean estaVacia() throws SQLException {
        try (Connection c = abrir(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT (SELECT COUNT(*) FROM material) + (SELECT COUNT(*) FROM usuario)"
                     + " + (SELECT COUNT(*) FROM prestamo)")) {
            rs.next();
            return rs.getLong(1) == 0;
        }
    }

    static IOException error(String accion, SQLException e) {
        return new IOException("Error de base de datos al " + accion + ": " + e.getMessage(), e);
    }
}
