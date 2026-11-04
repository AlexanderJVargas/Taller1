using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text.Json;
using CooperativaTransporte.Models;

namespace CooperativaTransporte.Services
{
    public class CooperativaManager
    {
        private readonly string _pathViajes;
        private readonly string _pathBoletos;

        public Dictionary<string, Viaje> Viajes { get; set; } = new();
        public Dictionary<string, Boleto> Boletos { get; set; } = new();

        public CooperativaManager(string pathViajes = "Data/viajes.json", string pathBoletos = "Data/boletos.json")
        {
            _pathViajes = pathViajes;
            _pathBoletos = pathBoletos;
            CargarDatos();
        }

        public void GuardarDatos()
        {
            Directory.CreateDirectory("Data");
            var options = new JsonSerializerOptions { WriteIndented = true };

            File.WriteAllText(_pathViajes, JsonSerializer.Serialize(Viajes.Values.ToList(), options));
            File.WriteAllText(_pathBoletos, JsonSerializer.Serialize(Boletos.Values.ToList(), options));
        }

        public void CargarDatos()
        {
            if (File.Exists(_pathViajes))
            {
                string json = File.ReadAllText(_pathViajes);
                var lista = JsonSerializer.Deserialize<List<Viaje>>(json);
                if (lista != null)
                {
                    Viajes = lista.ToDictionary(v => v.IdViaje, v => v);
                }
            }

            if (File.Exists(_pathBoletos))
            {
                string json = File.ReadAllText(_pathBoletos);
                var lista = JsonSerializer.Deserialize<List<Boleto>>(json);
                if (lista != null)
                {
                    Boletos = lista.ToDictionary(b => b.IdBoleto, b => b);
                }
            }
        }

        public Viaje RegistrarViaje(string idViaje, string ruta, DateTime fechaHora, int capacidad, decimal tarifaBase, bool esDirecto)
        {
            if (Viajes.ContainsKey(idViaje))
                throw new ArgumentException($"Ya existe un viaje con ID {idViaje}.");

            var viaje = new Viaje(idViaje, ruta, fechaHora, capacidad, tarifaBase, esDirecto);
            Viajes[idViaje] = viaje;
            GuardarDatos();
            return viaje;
        }

        public void EliminarViaje(string idViaje)
        {
            if (!Viajes.ContainsKey(idViaje))
                throw new KeyNotFoundException("Viaje no encontrado.");

            bool tieneBoletosActivos = Boletos.Values.Any(b => b.IdViaje == idViaje && b.Estado == Boleto.EstadoActivo);
            if (tieneBoletosActivos)
                throw new InvalidOperationException("No se puede eliminar el viaje: posee boletos vendidos activos.");

            Viajes.Remove(idViaje);
            GuardarDatos();
        }

        public Boleto VenderBoleto(string idBoleto, string idViaje, Pasajero pasajero, int numeroAsiento)
        {
            if (Boletos.ContainsKey(idBoleto))
                throw new ArgumentException($"Ya existe un boleto con ID {idBoleto}.");

            if (!Viajes.TryGetValue(idViaje, out var viaje))
                throw new KeyNotFoundException("Viaje no encontrado.");

            if (viaje.YaSalio())
                throw new InvalidOperationException("No se pueden vender boletos: el viaje ya partió.");

            viaje.AsignarAsiento(numeroAsiento, pasajero.Cedula);
            decimal precio = Boleto.CalcularTarifa(viaje, pasajero);

            var boleto = new Boleto(idBoleto, idViaje, pasajero, numeroAsiento, precio);
            Boletos[idBoleto] = boleto;
            GuardarDatos();
            return boleto;
        }

        public void AnularBoleto(string idBoleto)
        {
            if (!Boletos.TryGetValue(idBoleto, out var boleto))
                throw new KeyNotFoundException("Boleto no encontrado.");

            if (!Viajes.TryGetValue(boleto.IdViaje, out var viaje))
                throw new KeyNotFoundException("El viaje asociado no existe.");

            boleto.Anular(viaje.FechaHora);
            viaje.LiberarAsiento(boleto.NumeroAsiento);
            GuardarDatos();
        }

        // --- REPORTES ---
        public void ReporteMapaAsientos(string idViaje)
        {
            if (!Viajes.TryGetValue(idViaje, out var viaje))
            {
                Console.WriteLine("Viaje no encontrado.");
                return;
            }

            Console.WriteLine($"\n=== MAPA DE ASIENTOS: Viaje {viaje.IdViaje} ({viaje.Ruta}) ===");
            for (int i = 1; i <= viaje.Capacidad; i++)
            {
                if (viaje.AsientosOcupados.TryGetValue(i, out var cedula))
                    Console.WriteLine($"  [Asiento {i:D2}]: OCUPADO (Cédula: {cedula})");
                else
                    Console.WriteLine($"  [Asiento {i:D2}]: LIBRE");
            }
        }

        public void ReporteRecaudacionPorRuta(DateTime inicio, DateTime fin)
        {
            var recaudacion = Viaje.RutasValidas.ToDictionary(r => r, _ => 0.0m);

            foreach (var b in Boletos.Values.Where(b => b.Estado == Boleto.EstadoActivo && b.FechaEmision >= inicio && b.FechaEmision <= fin))
            {
                if (Viajes.TryGetValue(b.IdViaje, out var viaje) && recaudacion.ContainsKey(viaje.Ruta))
                {
                    recaudacion[viaje.Ruta] += b.PrecioFinal;
                }
            }

            Console.WriteLine($"\n=== RECAUDACIÓN POR RUTA ({inicio:yyyy-MM-dd} a {fin:yyyy-MM-dd}) ===");
            foreach (var kvp in recaudacion)
            {
                Console.WriteLine($"  Ruta {kvp.Key,-18}: ${kvp.Value:F2}");
            }
        }

        public void ReportePorcentajeOcupacion()
        {
            Console.WriteLine("\n=== PORCENTAJE DE OCUPACIÓN DE VIAJES ===");
            foreach (var v in Viajes.Values)
            {
                Console.WriteLine($"  Viaje {v.IdViaje} ({v.Ruta}) -> {v.PorcentajeOcupacion}% ({v.AsientosOcupados.Count}/{v.Capacidad} asientos)");
            }
        }

        public void ReporteBoletosAnulados(DateTime inicio, DateTime fin)
        {
            Console.WriteLine($"\n=== BOLETOS ANULADOS ({inicio:yyyy-MM-dd} a {fin:yyyy-MM-dd}) ===");
            var anulados = Boletos.Values
                .Where(b => b.Estado == Boleto.EstadoAnulado && b.FechaAnulacion >= inicio && b.FechaAnulacion <= fin)
                .ToList();

            if (!anuradosAny(anulados))
            {
                Console.WriteLine("  No se registran boletos anulados en este período.");
                return;
            }

            foreach (var b in anulados)
            {
                Console.WriteLine($"  Boleto: {b.IdBoleto} | Asiento: {b.NumeroAsiento} | Pasajero: {b.Pasajero.Nombre} | Anulado: {b.FechaAnulacion:yyyy-MM-dd HH:mm}");
            }
        }

        private static bool anuradosAny(List<Boleto> l) => l.Count > 0;

        public void AuditoriaAsientosLibres()
        {
            Console.WriteLine("\n=== AUDITORÍA DE ASIENTOS LIBRES ===");
            foreach (var v in Viajes.Values)
            {
                string libresStr = v.AsientosLibres.Count > 0 ? string.Join(", ", v.AsientosLibres) : "Ninguno";
                Console.WriteLine($"  Viaje {v.IdViaje} ({v.Ruta}) -> {v.AsientosLibres.Count} libres [{libresStr}]");
            }
        }
    }
}