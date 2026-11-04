package negocio;

import datos.ArchivoManager;
import modelo.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class TiendaOtavalo {
    
    // ========================================================
    // NOTA: El módulo de reabastecimiento y control de stock 
    // adicional fue generado con asistencia de IA.
    // ========================================================

    private List<Artesano> artesanos = new ArrayList<>();
    private List<Artesania> artesanias = new ArrayList<>();
    private List<Movimiento> movimientos = new ArrayList<>();

    public TiendaOtavalo() {
        ArchivoManager.inicializarDirectorio();
        cargarDatos();
    }

    public void cargarDatos() {
        artesanos = ArchivoManager.leerLista("artesanos.csv").stream().map(Artesano::desdeCsv).collect(Collectors.toList());
        artesanias = ArchivoManager.leerLista("artesanias.csv").stream().map(Artesania::desdeCsv).collect(Collectors.toList());
        movimientos = ArchivoManager.leerLista("movimientos.csv").stream().map(Movimiento::desdeCsv).collect(Collectors.toList());
    }

    public void guardarDatos() {
        ArchivoManager.guardarLista("artesanos.csv", artesanos.stream().map(Artesano::aCsv).collect(Collectors.toList()));
        ArchivoManager.guardarLista("artesanias.csv", artesanias.stream().map(Artesania::aCsv).collect(Collectors.toList()));
        ArchivoManager.guardarLista("movimientos.csv", movimientos.stream().map(Movimiento::aCsv).collect(Collectors.toList()));
    }

    public boolean registrarArtesano(Artesano a) {
        if(artesanos.stream().anyMatch(art -> art.getId().equals(a.getId()))) return false;
        artesanos.add(a);
        guardarDatos();
        return true;
    }

    public boolean eliminarArtesano(String id) {
        boolean tieneArtesanias = artesanias.stream().anyMatch(art -> art.getArtesanoId().equals(id));
        if (tieneArtesanias) return false; // Restricción: No eliminar artesano con piezas
        
        boolean eliminado = artesanos.removeIf(a -> a.getId().equals(id));
        if(eliminado) guardarDatos();
        return eliminado;
    }

    public boolean registrarRecepcion(Artesania a) {
        if(artesanias.stream().anyMatch(art -> art.getCodigo().equals(a.getCodigo()))) return false;
        artesanias.add(a);
        movimientos.add(new Movimiento("M" + (movimientos.size() + 1), a.getCodigo(), "RECEPCION", a.getStockActual(), LocalDate.now()));
        guardarDatos();
        return true;
    }

    // --- NUEVO MÉTODO DE REABASTECIMIENTO (Generado con IA) ---
    public boolean reabastecerArtesania(String codigoArtesania, int cantidadAdicional) {
        Optional<Artesania> opt = artesanias.stream().filter(a -> a.getCodigo().equals(codigoArtesania)).findFirst();
        if (!opt.isPresent()) return false; // Si no existe el código, no se puede reabastecer

        Artesania a = opt.get();
        a.setStockActual(a.getStockActual() + cantidadAdicional); // Suma al stock existente
        
        // Registra el movimiento de recepción por reabastecimiento
        movimientos.add(new Movimiento("M" + (movimientos.size() + 1), codigoArtesania, "RECEPCION", cantidadAdicional, LocalDate.now()));
        guardarDatos();
        return true;
    }

    public boolean registrarVenta(String codigoArtesania, int cantidad) {
        Optional<Artesania> opt = artesanias.stream().filter(a -> a.getCodigo().equals(codigoArtesania)).findFirst();
        if (!opt.isPresent()) return false;
        Artesania a = opt.get();

        if (cantidad > a.getStockActual()) return false; // Restricción: No vender más del stock

        a.setStockActual(a.getStockActual() - cantidad);
        movimientos.add(new Movimiento("M" + (movimientos.size() + 1), codigoArtesania, "VENTA", cantidad, LocalDate.now()));
        guardarDatos();
        return true;
    }

    public boolean registrarDevolucion(String codigoArtesania, int cantidad, String artesanoId) {
        Optional<Artesania> opt = artesanias.stream().filter(a -> a.getCodigo().equals(codigoArtesania)).findFirst();
        if (!opt.isPresent()) return false;
        Artesania a = opt.get();

        if (cantidad > a.getStockActual() || !a.getArtesanoId().equals(artesanoId)) return false; // Restricción de dueño y stock

        a.setStockActual(a.getStockActual() - cantidad);
        movimientos.add(new Movimiento("M" + (movimientos.size() + 1), codigoArtesania, "DEVOLUCION", cantidad, LocalDate.now()));
        guardarDatos();
        return true;
    }

    // --- REPORTES ---

    public void reporteLiquidacionArtesano(String artesanoId) {
        System.out.println("\n--- REPORTE DE LIQUIDACIÓN ---");
        Optional<Artesano> artOpt = artesanos.stream().filter(a -> a.getId().equals(artesanoId)).findFirst();
        if(!artOpt.isPresent()) { System.out.println("Artesano no encontrado."); return; }
        
        System.out.println("Artesano: " + artOpt.get().getNombre() + " | Comunidad: " + artOpt.get().getComunidad());
        
        List<Artesania> piezas = artesanias.stream().filter(a -> a.getArtesanoId().equals(artesanoId)).collect(Collectors.toList());
        double totalVendido = 0;
        double totalComision = 0;
        double totalPagar = 0;

        for(Artesania p : piezas) {
            int vendidas = movimientos.stream()
                .filter(m -> m.getCodigoArtesania().equals(p.getCodigo()) && m.getTipoMovimiento().equals("VENTA"))
                .mapToInt(Movimiento::getCantidad).sum();
            
            double subtotalVenta = vendidas * p.getPrecioVenta();
            double comisionUni = (p.getPrecioVenta() * (p.getTipo().equalsIgnoreCase("TEXTIL") ? 0.20 : 0.25)) * vendidas;
            double pagarUni = subtotalVenta - comisionUni;

            totalVendido += subtotalVenta;
            totalComision += comisionUni;
            totalPagar += pagarUni;
        }

        System.out.println("Total Vendido: $" + totalVendido);
        System.out.println("Comisión de la Tienda: $" + totalComision);
        System.out.println("Monto a Pagar al Artesano: $" + totalPagar);
    }

    public void reporteDevolucionesPendientes() {
        System.out.println("\n--- ARTESANÍAS QUE DEBEN DEVOLVERSE (>90 DÍAS) ---");
        boolean hay = false;
        for(Artesania a : artesanias) {
            if(a.debeDevolverse()) {
                hay = true;
                System.out.println("Código: " + a.getCodigo() + " | Nombre: " + a.getNombre() + " | Stock: " + a.getStockActual() + " | Recibido: " + a.getFechaRecepcion());
            }
        }
        if(!hay) System.out.println("No hay artesanías pendientes de devolución por tiempo.");
    }

    public void reporteArtesaniasMasVendidas() {
        System.out.println("\n--- ARTESANÍAS MÁS VENDIDAS ---");
        Map<String, Integer> ventas = new HashMap<>();
        for(Movimiento m : movimientos) {
            if(m.getTipoMovimiento().equals("VENTA")) {
                ventas.put(m.getCodigoArtesania(), ventas.getOrDefault(m.getCodigoArtesania(), 0) + m.getCantidad());
            }
        }
        ventas.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .forEach(e -> System.out.println("Código Artesanía: " + e.getKey() + " | Unidades Vendidas: " + e.getValue()));
    }

    public void reporteComunidadesConMasVentas() {
        System.out.println("\n--- COMUNIDADES CON MÁS VENTAS ---");
        Map<String, Integer> ventasComunidad = new HashMap<>();
        for(Movimiento m : movimientos) {
            if(m.getTipoMovimiento().equals("VENTA")) {
                artesanias.stream().filter(a -> a.getCodigo().equals(m.getCodigoArtesania())).findFirst().ifPresent(art -> {
                    artesanos.stream().filter(artesano -> artesano.getId().equals(art.getArtesanoId())).findFirst().ifPresent(artesano -> {
                        ventasComunidad.put(artesano.getComunidad(), ventasComunidad.getOrDefault(artesano.getComunidad(), 0) + m.getCantidad());
                    });
                });
            }
        }
        ventasComunidad.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .forEach(e -> System.out.println("Comunidad: " + e.getKey() + " | Unidades Vendidas: " + e.getValue()));
    }

    public void reporteAuditoriaStock() {
        System.out.println("\n--- AUDITORÍA DE UNIDADES DISPONIBLES ---");
        for(Artesania a : artesanias) {
            System.out.println("Código: " + a.getCodigo() + " | " + a.getNombre() + " | Tipo: " + a.getTipo() + " | Stock Disponible: " + a.getStockActual());
        }
    }
}