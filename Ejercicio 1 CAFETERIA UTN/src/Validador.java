// Validaciones sencillas que se usan en varias clases
public class Validador {

    // Texto no vacio, no mas largo que el maximo y sin ";" (porque rompe los archivos)
    public static boolean textoValido(String texto, int largoMaximo) {
        if (texto == null) {
            return false;
        }
        texto = texto.trim();
        return !texto.equals("") && texto.length() <= largoMaximo && texto.indexOf(';') == -1;
    }

    // Formato aaaa-mm-dd
    public static boolean fechaValida(String fecha) {
        if (fecha == null || fecha.length() != 10) {
            return false;
        }
        if (fecha.charAt(4) != '-' || fecha.charAt(7) != '-') {
            return false;
        }
        try {
            Integer.parseInt(fecha.substring(0, 4));
            int mes = Integer.parseInt(fecha.substring(5, 7));
            int dia = Integer.parseInt(fecha.substring(8, 10));
            return mes >= 1 && mes <= 12 && dia >= 1 && dia <= 31;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
