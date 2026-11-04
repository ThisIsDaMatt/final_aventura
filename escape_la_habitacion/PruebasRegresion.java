/**
 * Pruebas automaticas basicas del proyecto ESCAPE: LA HABITACION.
 * Se ejecutan desde la carpeta escape_la_habitacion.
 */
public class PruebasRegresion {

    public static void main(String[] args) {
        probarCalculosHabitacion1();
        probarSecretosHabitacion2();
        probarAcertijoHabitacion3();
        probarArchivosYMapas();
        probarDificultades();
        System.out.println("PRUEBAS CORRECTAS: calculos, claves, mapas y dificultades.");
    }

    private static void probarCalculosHabitacion1() {
        exigir(Main.calcularClave(0, 24).equals("48"), "doble");
        exigir(Main.calcularClave(1, 24).equals("12"), "mitad");
        exigir(Main.calcularClave(2, 24).equals("34"), "suma");
        exigir(Main.calcularClave(3, 24).equals("14"), "resta");
        exigir(Main.calcularClave(4, 24).equals("42"), "numero invertido");
    }

    private static void probarSecretosHabitacion2() {
        for (int vuelta = 0; vuelta < 25; vuelta++) {
            Main.generarSecretosPartida();
            exigir(!Main.codigoMedicina2.equals(Main.codigoFalso2), "codigo medicina y falso distintos");
            exigir(!Main.codigoMedicina2.equals(Main.codigoSalida2), "codigo medicina y salida distintos");
            exigir(!Main.codigoFalso2.equals(Main.codigoSalida2), "codigo falso y salida distintos");
            exigir(Main.codigoMedicina2.length() == 4, "codigo medicina de cuatro cifras");
            exigir(Main.codigoFalso2.length() == 4, "codigo falso de cuatro cifras");
            exigir(Main.codigoSalida2.length() == 4, "codigo salida de cuatro cifras");
            probarAcertijoHabitacion3();
        }
    }

    private static void probarAcertijoHabitacion3() {
        int[] n = Main.numeros3.clone();
        exigir(n[0] != n[1] && n[0] != n[2] && n[1] != n[2], "tres numeros diferentes");
        for (int i = 0; i < 2; i++) {
            for (int j = i + 1; j < 3; j++) {
                if (n[i] > n[j]) {
                    int temporal = n[i];
                    n[i] = n[j];
                    n[j] = temporal;
                }
            }
        }
        String esperado = Main.dosCifras(n[0] * n[1] % 100)
                + Main.dosCifras(n[0] * n[2] % 100)
                + Main.dosCifras(n[1] * n[2] % 100);
        exigir(Main.clave3.equals(esperado), "codigo de seis cifras de habitacion 3");
        exigir(Main.clave3.length() == 6, "longitud del codigo de habitacion 3");
        exigir(Main.dosCifras(4).equals("04"), "cero a la izquierda");
    }

    private static void probarArchivosYMapas() {
        for (int numero = 1; numero <= 3; numero++) {
            exigir(Main.cargarMundo(numero), "carga de habitacion " + numero);
            exigir(Main.visual.length == 13, "13 filas en habitacion " + numero);
            exigir(Main.zonas.length == 13, "13 filas de zonas en habitacion " + numero);
            for (int fila = 0; fila < 13; fila++) {
                exigir(Main.visual[fila].length == 37, "37 columnas visuales en habitacion " + numero);
                exigir(Main.zonas[fila].length == 37, "37 columnas de zonas en habitacion " + numero);
            }
            exigir(Main.objetos != null && Main.objetos.length > 0, "CSV de objetos de habitacion " + numero);
        }
    }

    private static void probarDificultades() {
        Main.dificultad = 1;
        exigir(Main.factorDificultad() == 1.20, "multiplicador facil");
        Main.dificultad = 2;
        exigir(Main.factorDificultad() == 1.0, "multiplicador normal");
        Main.dificultad = 3;
        exigir(Main.factorDificultad() == 0.80, "multiplicador dificil");
    }

    private static void exigir(boolean condicion, String prueba) {
        if (!condicion) {
            throw new IllegalStateException("FALLO: " + prueba);
        }
    }
}
