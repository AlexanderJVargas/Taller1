using System;

namespace CooperativaTransporte.Models
{
    public class Boleto
    {
        public const string EstadoActivo = "ACTIVO";
        public const string EstadoAnulado = "ANULADO";

        public string IdBoleto { get; set; } = string.Empty;
        public string IdViaje { get; set; } = string.Empty;
        public Pasajero Pasajero { get; set; } = new();
        public int NumeroAsiento { get; set; }
        public decimal PrecioFinal { get; set; }
        public DateTime FechaEmision { get; set; }
        public string Estado { get; set; } = EstadoActivo;
        public DateTime? FechaAnulacion { get; set; }

        public Boleto() { }

        public Boleto(string idBoleto, string idViaje, Pasajero pasajero, int numeroAsiento, decimal precioFinal)
        {
            IdBoleto = idBoleto;
            IdViaje = idViaje;
            Pasajero = pasajero;
            NumeroAsiento = numeroAsiento;
            PrecioFinal = precioFinal;
            FechaEmision = DateTime.Now;
            Estado = EstadoActivo;
        }

        public static decimal CalcularTarifa(Viaje viaje, Pasajero pasajero)
        {
            decimal tarifa = viaje.TarifaServicio;
            if (pasajero.TipoDescuento == "TERCERA_EDAD" || pasajero.TipoDescuento == "DISCAPACIDAD")
            {
                tarifa *= 0.50m;
            }
            return Math.Round(tarifa, 2);
        }

        public void Anular(DateTime fechaHoraViaje, DateTime? momentoActual = null)
        {
            DateTime ahora = momentoActual ?? DateTime.Now;

            if (Estado == EstadoAnulado)
                throw new InvalidOperationException("El boleto ya ha sido anulado previamente.");

            if (ahora > fechaHoraViaje.AddHours(-2))
                throw new InvalidOperationException("No se puede anular: faltan menos de 2 horas para la salida o el viaje ya partió.");

            Estado = EstadoAnulado;
            FechaAnulacion = ahora;
        }
    }
}