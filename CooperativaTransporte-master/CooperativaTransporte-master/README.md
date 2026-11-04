# Sistema de Gestión de Boletos - Cooperativa de Transporte Ibarra

Sistema de consola en **C# (.NET Core)** bajo Programación Orientada a Objetos (POO) con persistencia en archivos JSON.

## Integrantes
* Melody Criollo
* Steven Reyna
* Villy Reyes
* Alexis Pozo
* Alexander Vargas

## Requisitos y Entorno
* **IDE:** Microsoft Visual Studio 2022 o Visual Studio Code.
* **SDK:** .NET 7.0 o .NET 8.0.

## Instrucciones para Compilar y Ejecutar

### Desde Visual Studio:
1. Abrir la solución `CooperativaTransporte.slnx` o `CooperativaTransporte.sln`.
2. Verificar que los archivos dentro de la carpeta `Data/` tengan en sus propiedades la opción **Copiar en el directorio de salida: Copiar si es posterior**.
3. Presionar `F5` o el botón **Iniciar** para ejecutar en la consola interactiva.

### Desde la Terminal (.NET CLI):
```bash
dotnet build
dotnet run --project CooperativaTransporte
```

---

## Diagrama de Clases

```mermaid
classDiagram
    class Pasajero {
        +string Cedula
        +string Nombre
        +string TipoDescuento
        +Pasajero(string, string, string)
    }

    class Viaje {
        +List~string~ RutasValidas$
        +string IdViaje
        +string Ruta
        +DateTime FechaHora
        +int Capacidad
        +decimal TarifaBase
        +bool EsDirecto
        +Dictionary~int, string~ AsientosOcupados
        +decimal TarifaServicio
        +List~int~ AsientosLibres
        +double PorcentajeOcupacion
        +YaSalio(DateTime?) bool
        +AsignarAsiento(int, string) void
        +LiberarAsiento(int) void
    }

    class Boleto {
        +string EstadoActivo$+string EstadoAnulado$
        +string IdBoleto
        +string IdViaje
        +Pasajero Pasajero
        +int NumeroAsiento
        +decimal PrecioFinal
        +DateTime FechaEmision
        +string Estado
        +DateTime? FechaAnulacion
        +CalcularTarifa(Viaje, Pasajero)$ decimal
        +Anular(DateTime, DateTime?) void
    }

    class CooperativaManager {
        -string _pathViajes
        -string _pathBoletos
        +Dictionary~string, Viaje~ Viajes
        +Dictionary~string, Boleto~ Boletos
        +GuardarDatos() void
        +CargarDatos() void
        +RegistrarViaje(string, string, DateTime, int, decimal, bool) Viaje
        +EliminarViaje(string) void
        +VenderBoleto(string, string, Pasajero, int) Boleto
        +AnularBoleto(string) void
        +ReporteMapaAsientos(string) void
        +ReporteRecaudacionPorRuta(DateTime, DateTime) void
        +ReportePorcentajeOcupacion() void
        +ReporteBoletosAnulados(DateTime, DateTime) void
        +AuditoriaAsientosLibres() void
    }

    CooperativaManager "1" o-- "*" Viaje : gestiona
    CooperativaManager "1" o-- "*" Boleto : gestiona
    Boleto "1" *-- "1" Pasajero : contiene
    Boleto "*" ..> "1" Viaje : consulta
```