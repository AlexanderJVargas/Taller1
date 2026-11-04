// Para mostrar numeros en pantalla (en los archivos se guardan sin formato)
public class Formato {

    // Dinero con 2 decimales
    public static String dinero(double valor) {
        return String.format("%.2f", valor);
    }

    // Cantidades de ingredientes con 3 decimales
    public static String cantidad(double valor) {
        return String.format("%.3f", valor);
    }
}
