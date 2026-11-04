using System;
using System.Collections.Generic;
using System.Linq;
using System.Text.Json.Serialization;

namespace CooperativaTransporte.Models
{
    public class Viaje
    {
        public static readonly List<string> RutasValidas = new()
        {
            "Ibarra-Quito",
            "Ibarra-Tulcan",
            "Ibarra-Otavalo"
        };

        public string IdViaje { get; set; } = string.Empty;
        public string Ruta { get; set; } = string.Empty;
        public DateTime FechaHora { get; set; }
        public int Capacidad { get; set; }
        public decimal TarifaBase { get; set; }
        public bool EsDirecto { get; set; }

        // Mapa de Asiento (número) -> Cédula del Pasajero
        public Dictionary<int, string> AsientosOcupados { get; set; } = new();

        public Viaje() { }

        public Viaje(string idViaje, string ruta, DateTime fechaHora, int capacidad, decimal tarifaBase, bool esDirecto)
        {
            if (!RutasValidas.Contains(ruta))
                throw new ArgumentException($"Ruta no válida. Permitidas: {string.Join(", ", RutasValidas)}");

            IdViaje = idViaje;
            Ruta = ruta;
            FechaHora = fechaHora;
            Capacidad = capacidad;
            TarifaBase = tarifaBase;
            EsDirecto = esDirecto;
        }

        [JsonIgnore]
        public decimal TarifaServicio => EsDirecto ? TarifaBase : Math.Round(TarifaBase * 0.85m, 2);

        [JsonIgnore]
        public List<int> AsientosLibres =>
            Enumerable.Range(1, Capacidad).Where(num => !AsientosOcupados.ContainsKey(num)).ToList();

        [JsonIgnore]
        public double PorcentajeOcupacion =>
            Capacidad == 0 ? 0.0 : Math.Round(((double)AsientosOcupados.Count / Capacidad) * 100, 2);

        public bool YaSalio(DateTime? momentoActual = null)
        {
            DateTime ahora = momentoActual ?? DateTime.Now;
            return ahora >= FechaHora;
        }

        public void AsignarAsiento(int numeroAsiento, string cedula)
        {
            if (numeroAsiento < 1 || numeroAsiento > Capacidad)
                throw new ArgumentOutOfRangeException(nameof(numeroAsiento), $"Asiento {numeroAsiento} fuera de rango (1-{Capacidad}).");

            if (AsientosOcupados.ContainsKey(numeroAsiento))
                throw new InvalidOperationException($"El asiento {numeroAsiento} ya está ocupado.");

            AsientosOcupados[numeroAsiento] = cedula;
        }

        public void LiberarAsiento(int numeroAsiento)
        {
            if (AsientosOcupados.ContainsKey(numeroAsiento))
            {
                AsientosOcupados.Remove(numeroAsiento);
            }
        }
    }
}