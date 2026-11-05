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

/**
 * Conexion a la base de datos y creacion del esquema.
 *
 * Por defecto usa H2: una base guardada como archivo en la carpeta basedatos/ del proyecto, que
 * funciona en cualquier computadora con Java sin instalar ni configurar nada.
 * Si existe db.properties (o la variable BIBLIO_DB_URL), se conecta a ese servidor PostgreSQL.
 */
public class ConexionBD {

    /** Variable de entorno que, si existe, reemplaza a db.properties. */
    public static final String VARIABLE_ENTORNO = "BIBLIO_DB_URL";

    private final String url;
    private final String usuario;
    private final String contrasena;
    private final boolean h2;

    /** @param base carpeta del proyecto, donde estan db.properties y basedatos/ */
    public ConexionBD(Path base) throws IOException {
        Properties p = new Properties();
        String variable = System.getenv(VARIABLE_ENTORNO);
        Path archivo = base.resolve("db.properties");
        if (variable != null && !variable.isBlank()) {
            p.setProperty("url", variable.trim());
        } else if (Files.exists(archivo)) {
            try (Reader r = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
                p.load(r);
            }
        } else {
            Files.createDirectories(base.resolve("basedatos"));
            p.setProperty("url", "jdbc:h2:" + base.resolve("basedatos").resolve("biblio").toAbsolutePath());
            p.setProperty("usuario", "sa");
        }

        String texto = p.getProperty("url", "").trim();
        if (texto.startsWith("postgres://") || texto.startsWith("postgresql://")) {
            // Cadena de servicios en la nube: postgresql://usuario:contrasena@host/base?sslmode=require
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
        this.h2 = url.startsWith("jdbc:h2:");
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

    /** true si la base es el archivo H2 local; false si es un servidor PostgreSQL. */
    public boolean esH2() { return h2; }

    /** Base a la que se conecta, sin usuario ni contrasena (para mostrarla al iniciar). */
    public String descripcion() {
        return h2 ? "base local H2 (" + url.substring("jdbc:h2:".length()) + ".mv.db)" : url.replaceFirst("\\?.*$", "");
    }

    public Connection abrir() throws SQLException {
        return DriverManager.getConnection(url, usuario, contrasena);
    }

    /** Crea las tablas si no existen. */
    public void crearEsquema() throws SQLException {
        String cedulaValida = h2 ? "REGEXP_LIKE(cedula, '^[0-9]{10}$')" : "cedula ~ '^[0-9]{10}$'";
        String[] esquema = {
            "CREATE TABLE IF NOT EXISTS usuario ("
                + " cedula CHAR(10) PRIMARY KEY CHECK (" + cedulaValida + "),"
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
        try (Connection c = abrir(); Statement st = c.createStatement()) {
            for (String sql : esquema) st.execute(sql);
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
