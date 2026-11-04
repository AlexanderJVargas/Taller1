namespace CooperativaTransporte.Models
{
    public class Pasajero
    {
        public string Cedula { get; set; } = string.Empty;
        public string Nombre { get; set; } = string.Empty;
        // Valores válidos: "NINGUNO", "TERCERA_EDAD", "DISCAPACIDAD"
        public string TipoDescuento { get; set; } = "NINGUNO";

        public Pasajero() { }

        public Pasajero(string cedula, string nombre, string tipoDescuento)
        {
            Cedula = cedula;
            Nombre = nombre;
            TipoDescuento = tipoDescuento.ToUpper();
        }
    }
}