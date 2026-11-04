import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

/**
 * ESCAPE: LA HABITACION - Motor del juego (codigo base del profesor).
 *
 * <p>El juego completo son TRES habitaciones. Despiertas encerrado y debes
 * escapar de cada una resolviendo su puzzle. La habitacion 1 ya funciona de
 * punta a punta; las habitaciones 2 y 3 incluyen acertijos y mecanicas
 * implementadas para completar la aventura.</p>
 *
 * <p>Fuentes de verdad de cada habitacion N:</p>
 * <ul>
 *   <li><code>habitacionN.txt</code> - la matriz VISUAL (el dibujo).</li>
 *   <li><code>zonasN.txt</code> - la matriz de MOVIMIENTO (que ES cada casilla).</li>
 *   <li><code>objetosN.csv</code> - nombre, arte y detalle de cada objeto.</li>
 *   <li><code>arte/*.txt</code> y <code>musica/*.mid</code> - compartidos.</li>
 * </ul>
 *
 * <p>La configuracion inicial permite elegir modo, nombre y dificultad.
 * Las pruebas automaticas se ejecutan con <code>java PruebasRegresion</code>.</p>
 *
 * <p><b>Importante:</b> cada equipo construye SU propia version: claves,
 * habitaciones, arte, textos y melodias distintas. El trabajo se rige por el
 * Codigo de Etica del curso (en el sitio): entreguen solo lo que puedan
 * explicar.</p>
 *
 * <p>Compilar y ejecutar: <code>javac *.java</code> y <code>java Main</code>.
 * Documentacion: <code>javadoc -d docs -charset UTF-8 *.java</code>.</p>
 */
public class Main {

    /** El lector de teclado unico de todo el juego. */
    public static Scanner lector = new Scanner(System.in);

    /** La matriz VISUAL de la habitacion actual: lo que se VE (habitacionN.txt). */
    public static char[][] visual;

    /**
     * La matriz de MOVIMIENTO (zonasN.txt), del mismo tamano que la visual:
     * que ES cada casilla ('.' piso, '#' solido, letra = objeto).
     */
    public static char[][] zonas;

    /** La tabla de objetosN.csv: [0] simbolo | [1] nombre | [2] archivo | [3] detalle. */
    public static String[][] objetos;

    /** En que habitacion esta el jugador: el progreso del juego. */
    public static int habitacionActual = 1;

    /** Cuantas habitaciones tiene el juego: la victoria esta tras la ultima. */
    public static final int TOTAL_HABITACIONES = 3;

    /** El nombre elegido por el jugador al iniciar la partida. */
    public static String nombreJugador = "aventurero";

    /** La dificultad elegida: 1 = facil, 2 = normal, 3 = dificil. */
    public static int dificultad = 1;

    /** El modo elegido: 1 = historia, 2 = practica, 3 = contrarreloj. */
    public static int tipoJuego = 1;
    public static int habitacionFinal = TOTAL_HABITACIONES;
    public static int pistasRestantes = Integer.MAX_VALUE;
    public static int pistasUsadas = 0;
    public static int movimientosTotales = 0;
    public static int intentosMaximosPuerta3 = 3;

    /** La fila de la matriz donde esta parado el jugador. */
    public static int fila;

    /** La columna de la matriz donde esta parado el jugador. */
    public static int columna;
    public static int filaObjeto;
    public static int columnaObjeto;

    /** El mensaje que se muestra bajo el mapa en cada turno. */
    public static String mensaje = "Despiertas encerrado y no recuerdas nada. Explora.";

    /** Los simbolos de los objetos ya mirados de cerca (sin repetir). */
    public static String mirados = "";

    /** El inventario del jugador: un arreglo de tamano fijo. */
    public static String[] inventario = new String[10];

    /** Cuantas casillas del inventario estan ocupadas. */
    public static int totalInventario = 0;

    /** El ancho del mapa en caracteres; se calcula de la matriz visual. */
    public static int anchoMapa;

    /** El ancho interior del panel derecho, en caracteres. */
    public static final int ANCHO_PANEL = 26;

    /** El ancho de las escenas y pantallas grandes, en caracteres. */
    public static final int ANCHO_ESCENA = 64;

    /**
     * El numero grabado en el cofre. Se sortea al cargar cada habitacion
     * (dos digitos, par y sin terminar en cero) para que la clave cambie
     * en cada partida.
     */
    public static int numeroCofre;

    /** Las 5 operaciones posibles del puzzle; la nota revela la sorteada. */
    public static String[] operaciones = {
        "el DOBLE del numero",
        "la MITAD del numero",
        "el numero MAS 10",
        "el numero MENOS 10",
        "el numero escrito AL REVES"
    };

    /** El indice (0 a 4) de la operacion sorteada para esta habitacion. */
    public static int operacionElegida;

    /** La clave de la puerta: la operacion aplicada al numero del cofre. */
    public static String clave;
    public static boolean tetano = false;
    public static boolean tetanoYaContrajo = false;
    public static int pasosRestantes = 0;
    public static boolean derrota = false;
    public static boolean cofreAbierto2 = false;
    public static boolean estanteBloqueado2 = false;
    public static boolean curado2 = false;
    public static boolean puertaInteriorAbierta2 = false;
    public static int entradasPinchos2 = 0;
    public static boolean estrellaSuperior3 = false;
    public static boolean estrellaInferior3 = false;
    public static boolean llave3 = false;
    public static boolean puertaInteriorAbierta3 = false;
    public static boolean numeroUno3 = false;
    public static boolean numeroDos3 = false;
    public static int intentosPuerta3 = 0;
    public static String codigoMedicina2, codigoFalso2, codigoSalida2;
    public static int[] numeros3 = new int[3];
    public static String clave3;
    public static final int PASOS_INICIALES_H2 = 180;
    public static final int PENALIZACION_TETANO = 25;
    public static final int PASOS_INICIALES_H3 = 165;
    public static final int PASOS_POR_ESTRELLA = 20;

    /**
     * El ciclo de vida del juego: configuracion inicial (los placeholders),
     * carga de la habitacion 1 y el ciclo principal de comandos.
     */
    public static void main(String[] args) {
        Sonido.iniciar();
        generarSecretosPartida();

        // ---- Configuracion inicial: aqui viven 3 de los placeholders ----
        configurarArte();
        tipoJuego = seleccionarTipoJuego();
        configurarJugador();

        if (!cargarMundo(habitacionActual)) {
            return; // sin datos no hay juego
        }
        Sonido.musicaFondo("musica/ambiente.mid");

        boolean escapo = false;
        boolean jugando = true;
        while (jugando) {
            dibujarPantalla(panelEstado());
            System.out.print(" > ");
            String comando = lector.nextLine().trim().toUpperCase();
            if (comando.equals("Q")) {
                jugando = false;
            } else if (comando.equals("H")) {
                mensaje = darPista();
            } else if (comando.equals("M")) {
                Sonido.alternarMusica();
                mensaje = "Musica de fondo: " + (Sonido.musicaActiva ? "ACTIVADA" : "APAGADA");
            } else if (comando.equals("E")) {
                Sonido.alternarEfectos();
                mensaje = "Efectos de sonido: " + (Sonido.efectosActivos ? "ACTIVADOS" : "APAGADOS");
            } else if (comando.equals("X")) {
                easterEgg(); // la tecla secreta (no aparece en el panel)
            } else {
                escapo = mover(comando);
                if (escapo) {
                    jugando = false;
                }
            }
        }

        if (derrota) {
            System.out.println();
            System.out.println(" GAME OVER. Se agotaron tus pasos o intentos.");
        } else if (!escapo) {
            System.out.println();
            System.out.println(" Te rindes. La habitacion gana esta vez...");
        }
        Sonido.apagar(); // sin esto el programa no termina
    }

    /* ====================================================================
     *                 CONFIGURACION Y NAVEGACION DEL JUEGO
     * ==================================================================== */

    /**
     * MENU INICIAL - EL TIPO DE JUEGO.
     *
     * <p>Debe mostrar un menu inicial y leer la opcion del jugador (validada,
     * como leerOpcion()). Modos sugeridos: 1 = HISTORIA (las 3 habitaciones),
     * 2 = HABITACION UNICA (elegir cual y escapar solo de esa),
     * 3 = CONTRARRELOJ (historia con movimientos limitados). El modo elegido
     * se guarda en tipoJuego y el resto del motor debe respetarlo.</p>
     *
     * @return el modo elegido por el jugador.
     */
    public static int seleccionarTipoJuego() {
        System.out.println();
        System.out.println(bordeEscena());
        System.out.println(centrar("ESCAPE: LA HABITACION", ANCHO_ESCENA));
        System.out.println(centrar("1. Historia completa (3 habitaciones)", ANCHO_ESCENA));
        System.out.println(centrar("2. Practicar una habitacion", ANCHO_ESCENA));
        System.out.println(centrar("3. Contrarreloj (historia con menos pasos)", ANCHO_ESCENA));
        System.out.println(bordeEscena());
        int opcion = leerNumeroEntre("Selecciona el modo (1-3): ", 1, 3);
        if (opcion == 2) {
            habitacionActual = leerNumeroEntre("Que habitacion quieres jugar (1-3): ", 1, 3);
            habitacionFinal = habitacionActual;
        } else {
            habitacionActual = 1;
            habitacionFinal = TOTAL_HABITACIONES;
        }
        tipoJuego = opcion;
        return opcion;
    }

    /**
     * CONFIGURACION DEL JUGADOR.
     *
     * <p>Debe configurar la partida conversando con el jugador: preguntar su
     * nombre (guardarlo en nombreJugador y usarlo en los mensajes del juego),
     * elegir la dificultad 1-3 (guardarla en dificultad) y aplicarla: por
     * ejemplo en FACIL hay pistas ilimitadas, en NORMAL cuestan y en DIFICIL
     * la puerta admite maximo 3 intentos de clave. Tambien pueden ofrecer
     * aqui el estado inicial del sonido (M/E).</p>
     */
    public static void configurarJugador() {
        System.out.print("Como te llamas? ");
        String nombre = lector.nextLine().trim();
        if (!nombre.isEmpty()) nombreJugador = nombre.length() > 24 ? nombre.substring(0, 24) : nombre;
        System.out.println("Dificultad:");
        System.out.println("1. Facil: mas pasos y pistas ilimitadas");
        System.out.println("2. Normal: pasos normales y 5 pistas");
        System.out.println("3. Dificil: menos pasos y 2 pistas");
        dificultad = leerNumeroEntre("Elige dificultad (1-3): ", 1, 3);
        pistasRestantes = dificultad == 1 ? Integer.MAX_VALUE : (dificultad == 2 ? 5 : 2);
        intentosMaximosPuerta3 = dificultad == 3 ? 2 : 3;
        System.out.println("Bienvenido, " + nombreJugador + ". La aventura comienza.");
        System.out.println();
    }

    /**
     * IDENTIDAD VISUAL DEL JUEGO.
     *
     * <p>Debe configurar la identidad visual: un titulo propio del equipo
     * (reemplacen arte/titulo.txt o elijan el archivo segun el tipo de juego
     * o la dificultad), y la decoracion de cada habitacion (por ejemplo un
     * arte distinto al entrar a la 2 y a la 3). Hoy solo imprime el titulo
     * generico del profesor.</p>
     */
    public static void configurarArte() {
        System.out.println();
        System.out.println(bordeEscena());
        System.out.println(centrar("E S C A P E", ANCHO_ESCENA));
        System.out.println(centrar("LA HABITACION", ANCHO_ESCENA));
        System.out.println(centrar("Tres pruebas. Una salida. Ningun camino sencillo.", ANCHO_ESCENA));
        System.out.println(bordeEscena());
        imprimirArte("arte/titulo.txt");
        System.out.println();
    }

    /**
     * NAVEGACION ENTRE HABITACIONES.
     *
     * <p>Se llama cuando el jugador abre la puerta de una habitacion que NO
     * es la ultima. Debe llevarlo a la siguiente: sumar 1 a habitacionActual,
     * llamar cargarMundo(habitacionActual) con los archivos que ustedes crean
     * (habitacion2.txt, zonas2.txt, objetos2.csv... y los de la 3), mostrar
     * una transicion (arte + mensaje) y retornar false para que el ciclo siga
     * jugando. El inventario se conserva entre habitaciones: usenlo para que
     * un objeto de la habitacion 1 sirva en la 3.</p>
     *
     * <p>Hoy el pasillo esta "en obra": el juego termina aqui. Por eso, sin
     * programar este metodo, ES IMPOSIBLE llegar al final del juego.</p>
     *
     * @return true si el juego debe terminar; false para seguir jugando en la
     *         nueva habitacion.
     */
    public static boolean siguienteHabitacion() {

        if (habitacionActual >= habitacionFinal) { mostrarVictoria(); return true; }
        habitacionActual++;
        if (!cargarMundo(habitacionActual)) { derrota = true; return true; }
        System.out.println(bordeEscena());
        System.out.println(centrar("HABITACION " + habitacionActual, ANCHO_ESCENA));
        System.out.println(centrar("La siguiente prueba comienza...", ANCHO_ESCENA));
        System.out.println(bordeEscena());
        esperarEnter();
        return false;
    }

    /**
     * PLACEHOLDER BONUS - HABITACIONES DE OTROS EQUIPOS (+10%).
     *
     * <p>Como todas las habitaciones del salon usan el mismo formato
     * (habitacionN.txt + zonasN.txt + objetosN.csv + su arte), es posible
     * armar un juego GIGANTE conectando habitaciones de varios equipos.
     * Solo queda indicado: si lo intentan, el plan es:</p>
     * <ol>
     *   <li>Acordar con otro equipo el intercambio de la carpeta de una
     *       habitacion (sus 3 archivos + los arte/ que use).</li>
     *   <li>Copiar los archivos con un numero nuevo (ej. habitacion4.txt) y
     *       sus artes a arte/ sin pisar los propios.</li>
     *   <li>Llamar este metodo desde siguienteHabitacion() cuando el jugador
     *       cruce la ultima puerta propia, y subir TOTAL_HABITACIONES.</li>
     *   <li>Verificar que los simbolos y claves del otro equipo no choquen
     *       con los suyos (cada CSV trae los suyos, asi que basta cargarlo).</li>
     * </ol>
     *
     * @param numero el numero con el que se copiaron los archivos del otro equipo.
     * @return true si la habitacion externa quedo cargada y lista para jugar.
     */
    public static boolean conectarHabitacionExterna(int numero) {
        // Bonus opcional: requiere copiar primero los tres archivos de una
        // habitacion externa y ampliar el flujo de navegacion.
        return false;
    }

    /* ==================== CARGA DEL MUNDO ==================== */

    /**
     * Carga todos los datos de una habitacion y sortea su puzzle.
     * Se usa para la habitacion 1 y debe reutilizarse para la 2 y la 3.
     *
     * @param numero el numero de la habitacion (sus archivos llevan el sufijo).
     * @return true si todos los archivos cargaron bien.
     */
    public static boolean cargarMundo(int numero) {

        visual = cargarMatriz("habitacion" + numero + ".txt");
        zonas = cargarMatriz("zonas" + numero + ".txt");
        objetos = cargarObjetos("objetos" + numero + ".csv");
        if (visual == null || zonas == null || objetos == null) return false;
        if (visual.length != zonas.length) return false;
        for (int i = 0; i < visual.length; i++) if (visual[i].length != zonas[i].length) return false;
        calcularAnchoMapa();
        buscarInicio();
        mirados = "";
        mensaje = "Explora la habitacion y encuentra la salida.";
        if (numero == 1) {
            int decena = 1 + (int)(Math.random() * 9);
            int[] pares = {2, 4, 6, 8};
            numeroCofre = decena * 10 + pares[(int)(Math.random() * pares.length)];
            operacionElegida = (int)(Math.random() * operaciones.length);
            clave = calcularClave(operacionElegida, numeroCofre);
        } else {
            inventario = new String[10]; totalInventario = 0;
            if (numero == 2) {
                pasosRestantes = (int)(PASOS_INICIALES_H2 * factorDificultad()) - (tetano ? PENALIZACION_TETANO : 0) - (tipoJuego == 3 ? 35 : 0);
                cofreAbierto2 = false; estanteBloqueado2 = false; curado2 = false;
                puertaInteriorAbierta2 = false; entradasPinchos2 = 0;
            } else {
                pasosRestantes = (int)(PASOS_INICIALES_H3 * factorDificultad()) - (tetano ? PENALIZACION_TETANO : 0) - (tipoJuego == 3 ? 35 : 0);
                estrellaSuperior3 = false; estrellaInferior3 = false; llave3 = false;
                puertaInteriorAbierta3 = false; numeroUno3 = false; numeroDos3 = false;
                intentosPuerta3 = 0;
            }
        }
        return true;
    }

    /** El ancho del mapa es el de la fila mas larga de la matriz visual. */
    public static void calcularAnchoMapa() {
        anchoMapa = 0;
        for (int i = 0; i < visual.length; i++) {
            if (visual[i].length > anchoMapa) {
                anchoMapa = visual[i].length;
            }
        }
    }

    /**
     * Encuentra la @ en la matriz visual: ahi arranca el jugador.
     * Se borra del dibujo porque el @ se superpone al dibujar.
     */
    public static void buscarInicio() {
        fila = 1;
        columna = 1; // posicion de emergencia si olvidan la @
        for (int i = 0; i < visual.length; i++) {
            for (int j = 0; j < visual[i].length; j++) {
                if (visual[i][j] == '@') {
                    fila = i;
                    columna = j;
                    visual[i][j] = ' ';
                }
            }
        }
    }

    /* ==================== PANTALLA 1: LA HABITACION ==================== */

    /**
     * La pantalla principal: la habitacion (matriz visual con el @ del
     * jugador superpuesto) a la izquierda y el panel a la derecha.
     *
     * @param panel las lineas del panel derecho (estado o preview).
     */
    public static void dibujarPantalla(String[] panel) {
        System.out.println();
        System.out.println(" +============ ESCAPE: LA HABITACION ============+");
        System.out.println();
        int total = Math.max(visual.length, panel.length);
        for (int i = 0; i < total; i++) {
            String izquierda = (i < visual.length) ? filaVisual(i) : repetir(' ', anchoMapa);
            String derecha = (i < panel.length) ? panel[i] : "";
            System.out.println(" " + izquierda + "   " + derecha);
        }
        System.out.println();
        System.out.println(" " + mensaje);
    }

    /** Una fila de la matriz visual como texto, con el jugador (@) encima. */
    public static String filaVisual(int i) {
        String linea = "";
        for (int j = 0; j < anchoMapa; j++) {
            if (i == fila && j == columna) {
                linea = linea + '@';
            } else if (j < visual[i].length) {
                linea = linea + visual[i][j];
            } else {
                linea = linea + ' '; // relleno si la linea del txt quedo corta
            }
        }
        return linea;
    }

    /** El panel cuando no hay objeto cerca: progreso, inventario y comandos. */
    public static String[] panelEstado() {

        int lineasInventario = Math.max(1, totalInventario);
        int extras = habitacionActual >= 2 ? 2 : 0;
        String[] contenido = new String[4 + lineasInventario + extras];
        contenido[0] = "Habitacion " + habitacionActual + " de " + TOTAL_HABITACIONES;
        contenido[1] = "Objetos mirados: " + mirados.length() + " de " + objetos.length;
        contenido[2] = "";
        contenido[3] = "Inventario:";
        if (totalInventario == 0) contenido[4] = "  (vacio)";
        else for (int i = 0; i < totalInventario; i++) contenido[4+i] = "  - " + inventario[i];
        if (habitacionActual >= 2) {
            contenido[4+lineasInventario] = "Pasos: " + pasosRestantes;
            contenido[5+lineasInventario] = tetano ? "TETANOSIS: activa" : "Estado: estable";
        }
        String[] caja = enmarcar(contenido);
        String[] panel = new String[caja.length + 4];
        for (int i = 0; i < caja.length; i++) panel[i] = caja[i];
        panel[caja.length] = "";
        panel[caja.length+1] = " Moverse: W A S D";
        panel[caja.length+2] = " Pista: H | Rendirse: Q";
        panel[caja.length+3] = " Musica: M | Efectos: E";
        return panel;
    }

    /**
     * Intenta mover al jugador. La matriz de ZONAS decide que pasa:
     * '.' piso (avanza), letra A-Z objeto (pregunta), otro solido (bloquea).
     *
     * @param comando W, A, S o D en mayuscula.
     * @return true solo si el jugador escapo y el juego termina.
     */
    public static boolean mover(String comando) {

        int nf = fila, nc = columna;
        if (comando.equals("W")) nf--;
        else if (comando.equals("S")) nf++;
        else if (comando.equals("D")) nc++;
        else if (comando.equals("A")) nc--;
        else { mensaje = "Usa W, A, S o D."; return false; }
        char casilla = casillaZona(nf, nc);
        if (habitacionActual >= 2 && pasosRestantes <= 0) { derrota = true; return true; }
        // En la habitacion 2, la zona derecha solo se alcanza por la puerta interior.
        if (habitacionActual == 2 && !puertaInteriorAbierta2 && nc >= 13) {
            mensaje = "La puerta interior sigue cerrada. Necesitas la llave del lado de los pinchos.";
            Sonido.golpe();
            return false;
        }
        if (casilla == 'D' && ((habitacionActual == 2 && puertaInteriorAbierta2) ||
                (habitacionActual == 3 && puertaInteriorAbierta3))) casilla = '.';
        if (casilla == '.' || (casilla == 'S' && habitacionActual == 2)) {
            boolean entraPinchos = casilla == 'S' && casillaZona(fila, columna) != 'S';
            fila = nf; columna = nc;
            if (habitacionActual >= 2) pasosRestantes--;
            movimientosTotales++;
            if (entraPinchos) {
                entradasPinchos2++;
                if (!tetanoYaContrajo) { tetanoYaContrajo = true; tetano = true; pasosRestantes -= PENALIZACION_TETANO; mensaje = "Contrajiste tetanos: pierdes 25 pasos adicionales."; }
                else { pasosRestantes -= 40; mensaje = tetano ? "Volviste a cruzar los pinchos: pierdes 40 pasos adicionales." : "Cruzas de nuevo los pinchos sin contraer tetanos, pero pierdes 40 pasos."; }
            } else mensaje = "Caminas hacia " + nombreDireccion(comando) + ".";
            if (habitacionActual >= 2 && pasosRestantes <= 0) { pasosRestantes = 0; derrota = true; return true; }
        } else if ((casilla >= 'A' && casilla <= 'Z') || casilla == '?' || casilla == '*') { filaObjeto = nf; columnaObjeto = nc; return preguntarObjeto(casilla); }
        else { Sonido.golpe(); mensaje = "Algo solido te bloquea el paso."; }
        return false;
    }

    /** La zona de una casilla; fuera de la matriz todo es solido. */
    public static char casillaZona(int f, int c) {
        if (f < 0 || f >= zonas.length) {
            return '#';
        }
        if (c < 0 || c >= zonas[f].length) {
            return '#';
        }
        return zonas[f][c];
    }

    /** El nombre en palabras de una tecla de movimiento. */
    public static String nombreDireccion(String comando) {
        if (comando.equals("W")) return "arriba";
        if (comando.equals("S")) return "abajo";
        if (comando.equals("D")) return "la derecha";
        return "la izquierda";
    }

    /* ==================== PANTALLA 2: LA PREGUNTA ==================== */

    /**
     * El jugador choco con un objeto: se redibuja la pantalla con el preview
     * en el panel y se pregunta que hacer. Aqui viven los comportamientos
     * especiales (la alfombra, el cofre y la nota son los moldes a imitar).
     *
     * @param simbolo la letra del objeto en la matriz de zonas.
     * @return true si el objeto era la puerta final y el jugador escapo.
     */
    public static boolean preguntarObjeto(char simbolo) {

        String[] objeto = buscarObjeto(simbolo);
        if (objeto == null) { mensaje = "Objeto sin registrar: " + simbolo; return false; }
        dibujarPantalla(panelObjeto(objeto));
        if (leerOpcion() == 2) { mensaje = "Decides seguir explorando."; return false; }

        if (habitacionActual == 1) {
            if (simbolo == 'A') {
                abrirEscena(objeto); recordarMirado(simbolo);
                if (!tieneObjeto("llave oxidada")) { guardarObjeto("llave oxidada"); Sonido.recoger(); mensaje = "Guardaste la llave oxidada."; }
                return false;
            }
            if (simbolo == 'C') {
                if (!tieneObjeto("llave oxidada")) { abrirEscena(objeto); mensaje = "Necesitas una llave para abrir el cofre."; }
                else { mostrarEscena("Cofre abierto","cofre_abierto","El numero grabado es "+numeroCofre+"."); esperarEnter(); recordarMirado(simbolo); }
                return false;
            }
            if (simbolo == 'N') { mostrarEscena(objeto[1],objeto[2],objeto[3]+" La operacion es: "+operaciones[operacionElegida]+"."); esperarEnter(); recordarMirado(simbolo); return false; }
            if (simbolo == 'P') return abrirPuerta(objeto);
        }

        if (habitacionActual == 2) {
            if (simbolo == 'L') {
                if (!tieneObjeto("llave pequeña")) { guardarObjeto("llave pequeña"); Sonido.recoger(); }
                recordarMirado(simbolo); mensaje = "Recogiste la llave pequeña."; return false;
            }
            if (simbolo == 'I') {
                mostrarEscena(objeto[1],objeto[2],"El código de esta inscripción es "+codigoFalso2+". Parece una pista, pero no es el tratamiento.");
                esperarEnter(); recordarMirado(simbolo); return false;
            }
            if (simbolo == 'C') {
                if (!tieneObjeto("llave pequeña")) { mensaje = "El cofre está cerrado. Necesitas la llave pequeña."; Sonido.error(); }
                else if (!cofreAbierto2) {
                    cofreAbierto2 = true;
                    mostrarEscena("Cofre abierto","cofre_abierto","Código del estante: "+codigoMedicina2+". Código de salida: "+codigoSalida2+".");
                    esperarEnter(); mensaje = "Ya conoces los dos códigos.";
                } else mensaje = "El cofre ya está abierto.";
                recordarMirado(simbolo); return false;
            }
            if (simbolo == 'E') {
                if (estanteBloqueado2) { mensaje = "El estante quedó bloqueado permanentemente."; return false; }
                System.out.print("Código del estante (ENTER cancela): ");
                String intento = lector.nextLine().trim();
                if (intento.isEmpty()) return false;
                if (intento.equals(codigoFalso2)) { estanteBloqueado2 = true; mensaje = "Usaste el código falso. El estante quedó bloqueado."; Sonido.error(); }
                else if (intento.equals(codigoMedicina2)) { curado2 = true; tetano = false; mensaje = "Encontraste el tratamiento. El tetanos fue curado."; Sonido.exito(); }
                else { mensaje = "Código incorrecto. Puedes volver a intentarlo."; Sonido.error(); }
                recordarMirado(simbolo); return false;
            }
            if (simbolo == 'D') {
                if (!tieneObjeto("llave pequeña")) { mensaje = "La puerta interior solo puede abrirse desde el lado de la llave."; Sonido.error(); return false; }
                puertaInteriorAbierta2 = true; mensaje = "La puerta interior quedó abierta. Ya puedes atravesarla."; return false;
            }
            if (simbolo == 'P') {
                if (!cofreAbierto2) { mensaje = "Primero debes abrir el cofre y descubrir el código de salida."; return false; }
                System.out.print("Código de salida (ENTER cancela): ");
                String intento = lector.nextLine().trim();
                if (intento.equals(codigoSalida2)) {
                    if (habitacionActual >= habitacionFinal) { mostrarVictoria(); return true; }
                    return siguienteHabitacion();
                }
                if (!intento.isEmpty()) { mensaje = "Código incorrecto. Inténtalo otra vez."; Sonido.error(); }
                return false;
            }
        }

        if (habitacionActual == 3) {
            if (simbolo == 'K') {
                llave3 = true; puertaInteriorAbierta3 = true;
                mostrarEscena("La llave antigua","alfombra","El tercer número es "+numeros3[2]+". La llave abre la puerta interior y no se consume.");
                esperarEnter(); recordarMirado(simbolo); mensaje = "Encontraste la llave y el tercer número."; return false;
            }
            if (simbolo == '?') {
                int numero;
                if (filaObjeto == 1 && !numeroUno3) { numeroUno3 = true; numero = numeros3[0]; }
                else if (filaObjeto == 9 && !numeroDos3) { numeroDos3 = true; numero = numeros3[1]; }
                else { mensaje = "Esta casilla ya fue revisada."; return false; }
                mostrarEscena("Número oculto","nota","Encontraste el número "+numero+".");
                esperarEnter(); recordarMirado(simbolo); return false;
            }
            if (simbolo == '*') {
                if (filaObjeto == 1 && !estrellaSuperior3) { estrellaSuperior3 = true; pasosRestantes += PASOS_POR_ESTRELLA; mensaje = "La estrella te concede 20 pasos."; }
                else if (filaObjeto == 11 && !estrellaInferior3) { estrellaInferior3 = true; pasosRestantes += PASOS_POR_ESTRELLA; mensaje = "La estrella te concede 20 pasos."; }
                else { mensaje = "Ya recogiste esta estrella."; return false; }
                recordarMirado(simbolo); Sonido.recoger(); return false;
            }
            if (simbolo == 'D') {
                if (!llave3) { mensaje = "La puerta interior está cerrada. Necesitas la llave K."; return false; }
                puertaInteriorAbierta3 = true; mensaje = "La puerta interior quedó abierta."; return false;
            }
            if (simbolo == 'P') return abrirPuerta(objeto);
        }
        abrirEscena(objeto); recordarMirado(simbolo); mensaje = "Has examinado "+objeto[1]+"."; return false;
    }

    /** El panel con el preview (mini arte) del objeto y la pregunta. */
    public static String[] panelObjeto(String[] objeto) {
        String[] mini = leerLineas("arte/mini_" + objeto[2] + ".txt");
        if (mini == null) {
            mini = new String[] { "( sin dibujo )" };
        }

        String[] contenido = new String[mini.length + 2];
        for (int i = 0; i < mini.length; i++) {
            contenido[i] = mini[i];
        }
        contenido[mini.length] = "";
        contenido[mini.length + 1] = centrar(objeto[1].toUpperCase(), ANCHO_PANEL - 2);

        String[] caja = enmarcar(contenido);
        String[] panel = new String[caja.length + 4];
        for (int i = 0; i < caja.length; i++) {
            panel[i] = caja[i];
        }
        panel[caja.length] = "";
        panel[caja.length + 1] = " Que haces?";
        panel[caja.length + 2] = "   1. Mirar de cerca";
        panel[caja.length + 3] = "   2. Seguir explorando";
        return panel;
    }

    /** Busca la fila del CSV cuyo simbolo coincide, o null si no existe. */
    public static String[] buscarObjeto(char simbolo) {
        for (int i = 0; i < objetos.length; i++) {
            if (objetos[i][0].charAt(0) == simbolo) {
                return objetos[i];
            }
        }
        return null;
    }

    /* ==================== PANTALLA 3: LA ESCENA ==================== */

    /** Abre la escena de un objeto del CSV y espera el ENTER. */
    public static void abrirEscena(String[] objeto) {
        mostrarEscena(objeto[1], objeto[2], objeto[3]);
        esperarEnter();
    }

    /** Pausa la escena hasta que el jugador presione ENTER. */
    public static void esperarEnter() {
        System.out.println(centrar("[ ENTER para volver a la habitacion ]", ANCHO_ESCENA));
        System.out.println(bordeEscena());
        lector.nextLine();
    }

    /**
     * Imprime una escena generica: titulo, arte grande y texto.
     * Los comportamientos especiales la reutilizan con sus propios textos.
     *
     * @param titulo  el nombre que encabeza la escena.
     * @param archivo el nombre base del arte (arte/archivo.txt).
     * @param texto   el detalle, partido automaticamente en lineas.
     */
    public static void mostrarEscena(String titulo, String archivo, String texto) {
        System.out.println();
        System.out.println(bordeEscena());
        System.out.println();
        System.out.println(centrar("~ " + titulo.toUpperCase() + " ~", ANCHO_ESCENA));
        System.out.println();
        imprimirArte("arte/" + archivo + ".txt");
        System.out.println();
        String[] lineas = partirTexto(texto, 48);
        for (int i = 0; i < lineas.length; i++) {
            System.out.println(centrar(lineas[i], ANCHO_ESCENA));
        }
        System.out.println();
    }

    /**
     * LA PUERTA pide la clave del puzzle. Si es la ultima habitacion el
     * jugador gana; si no, el viaje continua en siguienteHabitacion().
     *
     * @param objeto la fila del CSV de la puerta.
     * @return true si el juego termina.
     */
    public static boolean abrirPuerta(String[] objeto) {

        if (habitacionActual == 3) {
            mostrarEscena(objeto[1],objeto[2],"Ordena los tres números de menor a mayor. Multiplica primero por segundo, primero por tercero y segundo por tercero. Conserva las dos últimas cifras de cada producto; si queda una, agrega un cero a la izquierda. Une los resultados.");
            System.out.print("Clave de seis cifras (ENTER cancela): ");
            String intento = lector.nextLine().trim();
            if (intento.isEmpty()) { mensaje = "Te alejas de la puerta."; return false; }
            if (!numeroUno3 || !numeroDos3 || !llave3) { mensaje = "Todavía no has encontrado los tres números."; return false; }
            if (intento.equals(clave3)) { mostrarVictoria(); return true; }
            intentosPuerta3++; Sonido.error();
            if (intentosPuerta3 >= intentosMaximosPuerta3) { derrota = true; mensaje = "Agotaste los intentos disponibles."; return true; }
            mensaje = "Clave incorrecta. Te quedan "+(intentosMaximosPuerta3-intentosPuerta3)+" intentos.";
            return false;
        }
        if (habitacionActual == 2) { mensaje = "Necesitas el código de salida que estaba dentro del cofre."; return false; }
        mostrarEscena(objeto[1],objeto[2],objeto[3]); recordarMirado('P');
        System.out.print("Clave (solo ENTER para alejarte): ");
        String intento = lector.nextLine().trim();
        if (intento.equals(clave)) {
            Sonido.exito();
            if (habitacionActual >= habitacionFinal) { mostrarVictoria(); return true; }
            return siguienteHabitacion();
        }
        if (intento.isEmpty()) mensaje = "Te alejas de la puerta sin intentar nada.";
        else { Sonido.error(); mensaje = "CLAVE INCORRECTA."; }
        return false;
    }

    /** La pantalla final del juego, con fanfarria y pausa para disfrutarla. */
    public static void mostrarVictoria() {
        System.out.println();
        System.out.println(bordeEscena());
        System.out.println();
        imprimirArte("arte/victoria.txt");
        System.out.println();
        System.out.println(centrar("Escapaste de las " + TOTAL_HABITACIONES
                + " habitaciones. Eres libre, " + nombreJugador + ".", ANCHO_ESCENA));
        System.out.println();
        System.out.println(bordeEscena());
        Sonido.detenerMusica();
        Sonido.victoria();
        System.out.println(centrar("[ ENTER para salir ]", ANCHO_ESCENA));
        lector.nextLine();
        System.out.println();
        System.out.println("RESUMEN DE LA PARTIDA");
        System.out.println("Jugador: " + nombreJugador);
        System.out.println("Habitaciones completadas: " + (tipoJuego == 2 ? 1 : habitacionFinal));
        System.out.println("Movimientos realizados: " + movimientosTotales);
        System.out.println("Pistas utilizadas: " + pistasUsadas);
        System.out.println("Objetos examinados en la ultima habitacion: " + mirados.length());
    }

    /* ==================== PUZZLE, PISTAS Y SECRETO ==================== */

    /**
     * Aplica la operacion sorteada al numero del cofre.
     *
     * @param operacion indice en el arreglo operaciones (0 a 4).
     * @param numero    el numero grabado en el cofre.
     * @return la clave resultante, como texto.
     */
    public static String calcularClave(int operacion, int numero) {
        if (operacion == 0) {
            return "" + (numero * 2);
        }
        if (operacion == 1) {
            return "" + (numero / 2);
        }
        if (operacion == 2) {
            return "" + (numero + 10);
        }
        if (operacion == 3) {
            return "" + (numero - 10);
        }
        // operacion 4: el numero escrito al reves (como cadena)
        String texto = "" + numero;
        String alReves = "";
        for (int i = texto.length() - 1; i >= 0; i--) {
            alReves = alReves + texto.charAt(i);
        }
        return alReves;
    }

    /**
     * La pista que toca segun el estado del juego (tecla H).
     * Las pistas cambian segun la habitacion y respetan el limite de dificultad.
     */
    public static String darPista() {
        if (pistasRestantes <= 0) return "Ya no te quedan pistas. Examina los objetos y usa las notas.";
        if (pistasRestantes != Integer.MAX_VALUE) pistasRestantes--;
        pistasUsadas++;

        if (habitacionActual == 2) {
            if (!tieneObjeto("llave pequeña")) return "Busca la llave pequeña. Tendrás que cruzar los pinchos.";
            if (!cofreAbierto2) return "La llave abre el cofre. Dentro hay un código para el estante y otro para la salida.";
            if (!curado2 && !estanteBloqueado2) return "El estante cura el tetanos con su código verdadero. La inscripción I es falsa.";
            return "Busca la puerta interior desde el lado donde encontraste la llave.";
        }
        if (habitacionActual == 3) {
            if (!llave3) return "Busca la llave K. Abre la puerta interior y revela un número.";
            if (!numeroUno3 || !numeroDos3) return "Encuentra las dos casillas con signo de interrogación.";
            return "Ordena los números y realiza las tres multiplicaciones indicadas en la puerta.";
        }
        if (mirados.length() == 0) return "Los objetos guardan secretos. Camina hacia uno y míralo de cerca.";
        if (!tieneObjeto("llave oxidada")) return "Hay algo escondido debajo de algo.";
        if (mirados.indexOf('C') < 0) return "La llave abre algo que guarda un número.";
        if (mirados.indexOf('N') < 0) return "La nota revela qué operación aplicar.";
        return "Aplica la operación de la nota al número del cofre.";
    }

    /**
     * EL EASTER EGG (tecla secreta X): muestra un mensaje oculto del equipo.
     */
    public static void easterEgg() {
        mensaje = "SECRETO: La paciencia tambien es una herramienta. Observa los simbolos y revisa cada pista.";
        System.out.println();
        System.out.println(bordeEscena());
        System.out.println(centrar("MENSAJE OCULTO DEL EQUIPO", ANCHO_ESCENA));
        System.out.println(centrar("La paciencia tambien es una herramienta.", ANCHO_ESCENA));
        System.out.println(bordeEscena());
    }

    /* ==================== ESTADO DEL JUGADOR ==================== */

    /** Guarda algo en el inventario si hay espacio y no esta repetido. */
    public static void guardarObjeto(String nombre) {
        if (totalInventario < inventario.length && !tieneObjeto(nombre)) {
            inventario[totalInventario] = nombre;
            totalInventario++;
        }
    }

    /**
     * Recorre el inventario buscando un objeto por su nombre.
     *
     * @param nombre el nombre exacto guardado.
     * @return true si el jugador lo tiene.
     */
    public static boolean tieneObjeto(String nombre) {
        for (int i = 0; i < totalInventario; i++) {
            if (inventario[i].equals(nombre)) {
                return true;
            }
        }
        return false;
    }

    /** Recuerda los objetos ya mirados (sin repetir) en una cadena. */
    public static void recordarMirado(char simbolo) {
        if (mirados.indexOf(simbolo) < 0) {
            mirados = mirados + simbolo;
        }
    }

    /* ==================== UTILIDADES DE TEXTO ==================== */

    /** El borde superior e inferior de las escenas. */
    public static String bordeEscena() {
        return "+" + repetir('=', ANCHO_ESCENA - 2) + "+";
    }

    /** Imprime un archivo de arte centrado como bloque (sin deformarlo). */
    public static void imprimirArte(String ruta) {
        String[] arte = leerLineas(ruta);
        if (arte == null) {
            System.out.println(centrar("( falta el dibujo: " + ruta + " )", ANCHO_ESCENA));
            return;
        }
        int masAncha = 0;
        for (int i = 0; i < arte.length; i++) {
            if (arte[i].length() > masAncha) {
                masAncha = arte[i].length();
            }
        }
        String margen = repetir(' ', (ANCHO_ESCENA - masAncha) / 2);
        for (int i = 0; i < arte.length; i++) {
            System.out.println(margen + arte[i]);
        }
    }

    /** Mete lineas de texto en una caja de ancho fijo. */
    public static String[] enmarcar(String[] lineas) {
        String borde = "+" + repetir('-', ANCHO_PANEL) + "+";
        String[] caja = new String[lineas.length + 2];
        caja[0] = borde;
        for (int i = 0; i < lineas.length; i++) {
            String linea = " " + lineas[i];
            while (linea.length() < ANCHO_PANEL) {
                linea = linea + " ";
            }
            if (linea.length() > ANCHO_PANEL) {
                linea = linea.substring(0, ANCHO_PANEL);
            }
            caja[i + 1] = "|" + linea + "|";
        }
        caja[lineas.length + 1] = borde;
        return caja;
    }

    /** Agrega espacios a la izquierda para centrar un texto en 'ancho'. */
    public static String centrar(String texto, int ancho) {
        int sobrante = ancho - texto.length();
        if (sobrante <= 0) {
            return texto;
        }
        return repetir(' ', sobrante / 2) + texto;
    }

    /** Construye una cadena repitiendo un caracter 'veces' veces. */
    public static String repetir(char c, int veces) {
        String resultado = "";
        for (int i = 0; i < veces; i++) {
            resultado = resultado + c;
        }
        return resultado;
    }

    /**
     * Parte un texto largo en lineas de maximo 'ancho' caracteres,
     * cortando por espacios para no romper las palabras.
     */
    public static String[] partirTexto(String texto, int ancho) {
        String[] palabras = texto.split(" ");
        String[] temporal = new String[palabras.length];
        int total = 0;
        String linea = "";
        for (int i = 0; i < palabras.length; i++) {
            if (linea.isEmpty()) {
                linea = palabras[i];
            } else if (linea.length() + 1 + palabras[i].length() <= ancho) {
                linea = linea + " " + palabras[i];
            } else {
                temporal[total] = linea;
                total++;
                linea = palabras[i];
            }
        }
        temporal[total] = linea;
        total++;

        String[] lineas = new String[total];
        for (int i = 0; i < total; i++) {
            lineas[i] = temporal[i];
        }
        return lineas;
    }

    /* ==================== ENTRADA Y ARCHIVOS ==================== */

    /** Multiplicador de pasos segun la dificultad elegida. */
    public static double factorDificultad() {
        if (dificultad == 1) return 1.20;
        if (dificultad == 3) return 0.80;
        return 1.0;
    }

    /** Lee un entero dentro de un intervalo y vuelve a preguntar si es invalido. */
    public static int leerNumeroEntre(String pregunta, int minimo, int maximo) {
        while (true) {
            System.out.print(pregunta);
            String entrada = lector.nextLine().trim();
            try {
                int numero = Integer.parseInt(entrada);
                if (numero >= minimo && numero <= maximo) return numero;
            } catch (NumberFormatException e) {
                // Se vuelve a solicitar una entrada valida.
            }
            System.out.println("Opcion invalida. Escribe un numero entre " + minimo + " y " + maximo + ".");
        }
    }

    /** Lee 1 o 2 validando la entrada. Nunca rompe el programa. */
    public static int leerOpcion() {
        int opcion = -1;
        do {
            System.out.print(" > Elige (1 o 2): ");
            String entrada = lector.nextLine().trim();
            if (entrada.equals("1") || entrada.equals("2")) {
                opcion = Integer.parseInt(entrada);
            } else {
                System.out.println(" Entrada invalida. Intenta de nuevo.");
            }
        } while (opcion == -1);
        return opcion;
    }

    /** Lee un archivo de texto completo y retorna sus lineas, o null. */
    public static String[] leerLineas(String ruta) {
        String[] temporal = new String[200];
        int total = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(ruta));
            String linea;
            while ((linea = br.readLine()) != null && total < 200) {
                temporal[total] = linea;
                total++;
            }
            br.close();
        } catch (IOException e) {
            System.out.println("No se pudo leer " + ruta + ": " + e.getMessage());
            return null;
        }
        String[] lineas = new String[total];
        for (int i = 0; i < total; i++) {
            lineas[i] = temporal[i];
        }
        return lineas;
    }

    /** Convierte un archivo de texto en una matriz de caracteres. */
    public static char[][] cargarMatriz(String ruta) {
        String[] lineas = leerLineas(ruta);
        if (lineas == null) {
            return null;
        }
        char[][] matriz = new char[lineas.length][];
        for (int i = 0; i < lineas.length; i++) {
            matriz[i] = lineas[i].toCharArray();
        }
        return matriz;
    }


    public static void generarSecretosPartida() {
        codigoMedicina2 = codigoCuatroCifras();
        do { codigoFalso2 = codigoCuatroCifras(); } while (codigoFalso2.equals(codigoMedicina2));
        do { codigoSalida2 = codigoCuatroCifras(); } while (codigoSalida2.equals(codigoMedicina2) || codigoSalida2.equals(codigoFalso2));
        for (int i=0; i<3; i++) {
            boolean repetido;
            do {
                numeros3[i] = 10 + (int)(Math.random()*90);
                repetido = (i>0 && numeros3[i]==numeros3[0]) || (i>1 && numeros3[i]==numeros3[1]);
            } while (repetido);
        }
        int[] n = numeros3.clone();
        for(int i=0;i<2;i++) for(int j=i+1;j<3;j++) if(n[i]>n[j]) { int t=n[i]; n[i]=n[j]; n[j]=t; }
        clave3 = dosCifras(n[0]*n[1]%100) + dosCifras(n[0]*n[2]%100) + dosCifras(n[1]*n[2]%100);
    }
    public static String codigoCuatroCifras() { return ""+(1000+(int)(Math.random()*9000)); }
    public static String dosCifras(int n) { return n<10 ? "0"+n : ""+n; }

    /** Lee un CSV de objetos (saltando el encabezado) a una matriz de String. */
    public static String[][] cargarObjetos(String ruta) {
        String[] lineas = leerLineas(ruta);
        if (lineas == null || lineas.length < 2) {
            System.out.println(ruta + " esta vacio o no existe.");
            return null;
        }
        String[][] tabla = new String[lineas.length - 1][];
        for (int i = 1; i < lineas.length; i++) {
            tabla[i - 1] = lineas[i].split(",");
        }
        return tabla;
    }
}
