using System;
using CooperativaTransporte.Models;
using CooperativaTransporte.Services;

namespace CooperativaTransporte
{
    internal class Program
    {
        static void Main(string[] args)
        {
            var manager = new CooperativaManager();

            while (true)
            {
                Console.WriteLine("\n=============================================");
                Console.WriteLine("    COOPERATIVA DE TRANSPORTE DE IBARRA      ");
                Console.WriteLine("=============================================");
                Console.WriteLine("1. Registrar nuevo viaje");
                Console.WriteLine("2. Vender boleto");
                Console.WriteLine("3. Anular boleto");
                Console.WriteLine("4. Reporte: Mapa de asientos de un viaje");
                Console.WriteLine("5. Reporte: Recaudación por ruta en un período");
                Console.WriteLine("6. Reporte: Porcentaje de ocupación");
                Console.WriteLine("7. Reporte: Boletos anulados en un período");
                Console.WriteLine("8. Auditoría: Asientos libres");
                Console.WriteLine("9. Salir");
                Console.Write("Seleccione una opción: ");

                string? opcion = Console.ReadLine();

                try
                {
                    switch (opcion)
                    {
                        case "1":
                            Console.Write("ID Viaje (ej. V-01): ");
                            string idV = Console.ReadLine()!;
                            Console.WriteLine("Rutas: Ibarra-Quito, Ibarra-Tulcan, Ibarra-Otavalo");
                            Console.Write("Ruta: ");
                            string ruta = Console.ReadLine()!;
                            Console.Write("Horas para la salida desde ahora: ");
                            int horas = int.Parse(Console.ReadLine()!);
                            Console.Write("Capacidad de asientos: ");
                            int cap = int.Parse(Console.ReadLine()!);
                            Console.Write("Tarifa base ($): ");
                            decimal tarifa = decimal.Parse(Console.ReadLine()!);
                            Console.Write("¿Es viaje directo? (s/n): ");
                            bool directo = Console.ReadLine()!.Trim().ToLower() == "s";

                            manager.RegistrarViaje(idV, ruta, DateTime.Now.AddHours(horas), cap, tarifa, directo);
                            Console.WriteLine(" Viaje registrado correctamente.");
                            break;

                        case "2":
                            Console.Write("ID Boleto (ej. B-10): ");
                            string idB = Console.ReadLine()!;
                            Console.Write("ID Viaje: ");
                            string idViajeB = Console.ReadLine()!;
                            Console.Write("Cédula del pasajero: ");
                            string ced = Console.ReadLine()!;
                            Console.Write("Nombre del pasajero: ");
                            string nom = Console.ReadLine()!;
                            Console.WriteLine("Tipo descuento: 1. Ninguno | 2. Tercera Edad (50%) | 3. Discapacidad (50%)");
                            Console.Write("Seleccione tipo: ");
                            string tOp = Console.ReadLine()!;
                            string desc = tOp switch
                            {
                                "2" => "TERCERA_EDAD",
                                "3" => "DISCAPACIDAD",
                                _ => "NINGUNO"
                            };
                            Console.Write("Número de asiento: ");
                            int asiento = int.Parse(Console.ReadLine()!);

                            var pas = new Pasajero(ced, nom, desc);
                            var bVendido = manager.VenderBoleto(idB, idViajeB, pas, asiento);
                            Console.WriteLine($" Venta exitosa. Total a cobrar: ${bVendido.PrecioFinal:F2}");
                            break;

                        case "3":
                            Console.Write("ID Boleto a anular: ");
                            string idAnular = Console.ReadLine()!;
                            manager.AnularBoleto(idAnular);
                            Console.WriteLine(" Boleto anulado y asiento liberado correctamente.");
                            break;

                        case "4":
                            Console.Write("ID Viaje a consultar: ");
                            string idConsulta = Console.ReadLine()!;
                            manager.ReporteMapaAsientos(idConsulta);
                            break;

                        case "5":
                            manager.ReporteRecaudacionPorRuta(DateTime.Now.AddDays(-30), DateTime.Now.AddDays(30));
                            break;

                        case "6":
                            manager.ReportePorcentajeOcupacion();
                            break;

                        case "7":
                            manager.ReporteBoletosAnulados(DateTime.Now.AddDays(-30), DateTime.Now.AddDays(30));
                            break;

                        case "8":
                            manager.AuditoriaAsientosLibres();
                            break;

                        case "9":
                            Console.WriteLine("Cerrando el sistema...");
                            return;

                        default:
                            Console.WriteLine("Opción no válida.");
                            break;
                    }
                }
                catch (Exception ex)
                {
                    Console.WriteLine($"\n[ERROR]: {ex.Message}");
                }
            }
        }
    }
}