import modelo.*;
import negocio.TiendaOtavalo;
import java.time.LocalDate;
import java.util.Scanner;

public class Main {
    
    // ========================================================
    // NOTA: La gestión de excepciones y validaciones de menú
    // en este código fueron generadas con IA.
    // ========================================================

    public static void main(String[] args) {
        TiendaOtavalo tienda = new TiendaOtavalo();
        Scanner scanner = new Scanner(System.in);
        int opcion = 0;

        do {
            System.out.println("\n==================================================");
            System.out.println("   SISTEMA DE GESTIÓN DE ARTESANÍAS - OTAVALO");
            System.out.println("==================================================");
            System.out.println("1. Registrar Artesano");
            System.out.println("2. Eliminar Artesano");
            System.out.println("3. Registrar Recepción de Artesanía");
            System.out.println("4. Registrar Venta");
            System.out.println("5. Registrar Devolución");
            System.out.println("6. Ver Reportes");
            System.out.println("7. Reabastecer Artesanía Existente");
            System.out.println("8. Salir");
            System.out.println("==================================================");
            System.out.print("Seleccione una opción: ");
            
            // Validación básica para evitar errores si ingresan texto en lugar de números
            if (!scanner.hasNextInt()) {
                System.out.println("\n[ERROR] Por favor, ingrese un número válido del menú.");
                scanner.next(); // Limpiar entrada incorrecta
                continue;
            }

            opcion = scanner.nextInt();
            scanner.nextLine(); // Limpiar buffer

            switch (opcion) {
                case 1:
                    System.out.println("\n--- REGISTRO DE NUEVO ARTESANO ---");
                    System.out.print("Ingrese Cédula / ID (10 dígitos): "); String id = scanner.nextLine();
                    System.out.print("Ingrese Nombre y Apellido (solo letras): "); String nom = scanner.nextLine();
                    System.out.print("Ingrese Comunidad (solo letras): "); String com = scanner.nextLine();
                    System.out.print("Ingrese Teléfono (10 dígitos): "); String telf = scanner.nextLine();
                    
                    // Bloque try-catch para manejar las excepciones de validación generadas con IA
                    try {
                        Artesano nuevoArtesano = new Artesano(id, nom, com, telf);
                        boolean registrado = tienda.registrarArtesano(nuevoArtesano);
                        
                        if(registrado) {
                            System.out.println("\n[¡ÉXITO!] El artesano " + nom + " de la comunidad " + com + " fue registrado correctamente.");
                        } else {
                            System.out.println("\n[AVISO] No se pudo registrar. Ya existe un artesano registrado con el ID: " + id);
                        }
                    } catch (IllegalArgumentException e) {
                        System.out.println("\n[DENEGADO] Error de validación: " + e.getMessage());
                    }
                    break;

                case 2:
                    System.out.println("\n--- ELIMINACIÓN DE ARTESANO ---");
                    System.out.print("Ingrese el ID del Artesano a eliminar: "); String idDel = scanner.nextLine();
                    
                    boolean eliminado = tienda.eliminarArtesano(idDel);
                    if(eliminado) {
                        System.out.println("\n[¡ÉXITO!] El artesano con ID " + idDel + " ha sido eliminado del sistema.");
                    } else {
                        System.out.println("\n[DENEGADO] No se puede eliminar a este artesano. Motivo: Tiene artesanías registradas en la tienda o el ID no existe.");
                    }
                    break;

                case 3:
                    System.out.println("\n--- RECEPCIÓN DE NUEVA ARTESANÍA EN CONSIGNACIÓN ---");
                    System.out.print("Ingrese Código único de artesanía: "); String cod = scanner.nextLine();
                    System.out.print("Nombre de la pieza: "); String nomP = scanner.nextLine();
                    System.out.print("Tipo de artesanía (TEXTIL / MADERA): "); String tipo = scanner.nextLine();
                    System.out.print("Precio de venta ($): "); double precio = scanner.nextDouble();
                    System.out.print("Cantidad inicial recibida (Stock): "); int stock = scanner.nextInt();
                    scanner.nextLine();
                    System.out.print("ID del Artesano dueño de la pieza: "); String idDueño = scanner.nextLine();
                    
                    LocalDate fechaRec = LocalDate.now(); 
                    boolean recepcionOk = tienda.registrarRecepcion(new Artesania(cod, nomP, tipo, precio, stock, fechaRec, idDueño));
                    
                    if(recepcionOk) {
                        System.out.println("\n[¡ÉXITO!] Se han recibido " + stock + " unidades de '" + nomP + "' (Código: " + cod + ").");
                    } else {
                        System.out.println("\n[AVISO] No se pudo registrar la recepción. El código de artesanía '" + cod + "' ya se encuentra registrado.");
                    }
                    break;

                case 4:
                    System.out.println("\n--- REGISTRO DE VENTA DE ARTESANÍA ---");
                    System.out.print("Ingrese el Código de la artesanía a vender: "); String codV = scanner.nextLine();
                    System.out.print("Ingrese la cantidad que desea vender: "); int cantV = scanner.nextInt();
                    scanner.nextLine();
                    
                    boolean ventaOk = tienda.registrarVenta(codV, cantV);
                    if(ventaOk) {
                        System.out.println("\n[¡ÉXITO!] Venta procesada con éxito. Se descontaron " + cantV + " unidades del inventario.");
                    } else {
                        System.out.println("\n[DENEGADO] No se pudo realizar la venta. Motivo: La artesanía no existe o estás intentando vender una cantidad mayor a las unidades disponibles en stock.");
                    }
                    break;

                case 5:
                    System.out.println("\n--- REGISTRO DE DEVOLUCIÓN A ARTESANO ---");
                    System.out.print("Ingrese el Código de la artesanía a devolver: "); String codD = scanner.nextLine();
                    System.out.print("Ingrese la cantidad a devolver: "); int cantD = scanner.nextInt();
                    scanner.nextLine();
                    System.out.print("Ingrese el ID del artesano dueño: "); String dueñoD = scanner.nextLine();
                    
                    boolean devOk = tienda.registrarDevolucion(codD, cantD, dueñoD);
                    if(devOk) {
                        System.out.println("\n[¡ÉXITO!] Devolución realizada correctamente al artesano dueño.");
                    } else {
                        System.out.println("\n[DENEGADO] No se pudo procesar la devolución. Verifique que el código exista, que la cantidad no supere el stock actual, o que el ID corresponda estrictamente al artesano dueño de la pieza.");
                    }
                    break;

                case 6:
                    System.out.println("\n==================================================");
                    System.out.println("                 MENÚ DE REPORTES");
                    System.out.println("==================================================");
                    System.out.println("1. Liquidación de Artesano (Ventas, Comisión y Pago)");
                    System.out.println("2. Artesanías que deben devolverse (>90 días)");
                    System.out.println("3. Artesanías más vendidas");
                    System.out.println("4. Comunidades con más ventas");
                    System.out.println("5. Auditoría de unidades disponibles (Stock)");
                    System.out.print("Elija una opción de reporte: ");
                    
                    if (!scanner.hasNextInt()) {
                        System.out.println("[AVISO] Opción de reporte no válida.");
                        scanner.next();
                        break;
                    }
                    
                    int rep = scanner.nextInt();
                    scanner.nextLine();
                    
                    if(rep == 1) {
                        System.out.print("Ingrese el ID del Artesano para la liquidación: ");
                        tienda.reporteLiquidacionArtesano(scanner.nextLine());
                    } else if(rep == 2) {
                        tienda.reporteDevolucionesPendientes();
                    } else if(rep == 3) {
                        tienda.reporteArtesaniasMasVendidas();
                    } else if(rep == 4) {
                        tienda.reporteComunidadesConMasVentas();
                    } else if(rep == 5) {
                        tienda.reporteAuditoriaStock();
                    } else {
                        System.out.println("[AVISO] Opción de reporte no válida.");
                    }
                    break;

                case 7:
                    System.out.println("\n--- REABASTECER ARTESANÍA EXISTENTE ---");
                    System.out.print("Ingrese el Código de la artesanía existente: ");
                    String codReab = scanner.nextLine();
                    System.out.print("Ingrese la cantidad de unidades que desea agregar: ");
                    int cantAdicional = scanner.nextInt();
                    scanner.nextLine(); // Limpiar buffer
                    
                    boolean reabastecido = tienda.reabastecerArtesania(codReab, cantAdicional);
                    if(reabastecido) {
                        System.out.println("\n[¡ÉXITO!] Se han agregado " + cantAdicional + " unidades al stock de la artesanía con código: " + codReab);
                    } else {
                        System.out.println("\n[DENEGADO] El código de artesanía '" + codReab + "' no existe en el sistema. Debe registrarla primero como nueva (Opción 3).");
                    }
                    break;

                case 8:
                    System.out.println("\nSaliendo del sistema de la tienda. ¡Hasta luego!");
                    break;

                default:
                    System.out.println("\n[AVISO] Opción no válida. Por favor, elija un número entre 1 y 8.");
            }
            
            if (opcion != 8) {
                System.out.println("\nPresione ENTER para continuar...");
                scanner.nextLine();
            }
        } while (opcion != 8);
        scanner.close();
    }
}