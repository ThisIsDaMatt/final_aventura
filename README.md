# Escape: La Habitación 🔐

Proyecto final del curso de **Lógica de Programación** (UPB). Es un juego de
"salir de la habitación" en consola, hecho en Java, con **tres habitaciones**.
La habitación 1 viene del código base del profesor; las habitaciones 2 y 3, los
cuatro placeholders del motor, el Easter Egg y la documentación JavaDoc los
construimos nosotros.

## Equipo

| Integrante         | Aportes                                                     |
| ------------------ | ----------------------------------------------------------- |
| Matías Múnera Mazo | Trabajo compartido: código, habitaciones y documentación    |
| Emanuel Zuluaga    | Trabajo compartido: código, habitaciones y documentación    |
| Esteban González   | Trabajo compartido: código, habitaciones y documentación    |

Repartimos el trabajo de forma equitativa: los tres participamos en el código,
el diseño de las habitaciones, la documentación y la presentación. El historial
de commits del repositorio evidencia el aporte de cada integrante.

## Contenido

1. [Cómo ejecutarlo](#cómo-ejecutarlo)
2. [Estructura del repositorio](#estructura-del-repositorio)
3. [Cómo se juega](#cómo-se-juega)
4. [Modos de juego y dificultad](#modos-de-juego-y-dificultad)
5. [Las tres habitaciones](#las-tres-habitaciones)
6. [Las dos matrices de cada habitación](#las-dos-matrices-de-cada-habitación)
7. [Formato de `objetosN.csv`](#formato-de-objetosncsv)
8. [Lo que programamos en el motor](#lo-que-programamos-en-el-motor)
9. [Sonido](#sonido)
10. [Documentación JavaDoc](#documentación-javadoc)
11. [Pruebas automáticas](#pruebas-automáticas)
12. [Presentación y evidencias](#presentación-y-evidencias)
13. [Créditos](#créditos)

## Cómo ejecutarlo

Se necesita el JDK. Desde la carpeta del juego:

```bash
cd escape_la_habitacion
javac -encoding UTF-8 *.java
java Main
```

## Estructura del repositorio

```text
final_aventura/
├── README.md                                  Este archivo
├── Mácula & Laberintos — Mecánicas de juego.pdf   Presentación de la sustentación
├── .gitignore
└── escape_la_habitacion/
    ├── Main.java              Motor del juego (con nuestros métodos)
    ├── Mapa.java              Demo en miniatura del movimiento en una matriz
    ├── Sonido.java            Efectos y música MIDI
    ├── PruebasRegresion.java  Pruebas automáticas
    ├── habitacion1.txt        Matriz VISUAL de cada habitación (1, 2 y 3)
    ├── habitacion2.txt
    ├── habitacion3.txt
    ├── zonas1.txt             Matriz de MOVIMIENTO de cada habitación
    ├── zonas2.txt
    ├── zonas3.txt
    ├── objetos1.csv           Objetos de cada habitación (nombre, arte, detalle)
    ├── objetos2.csv
    ├── objetos3.csv
    ├── Habitacion 2.png       Diagramas de diseño de las habitaciones 2 y 3
    ├── Habitacion 3.png
    ├── arte/                  ASCII art: título, victoria, previews y escenas
    ├── musica/ambiente.mid    Música de fondo (MIDI)
    └── docs/                  Documentación JavaDoc (abrir index.html)
```

## Cómo se juega

Todo se maneja por teclado:

| Tecla        | Acción                                              |
| ------------ | --------------------------------------------------- |
| `W A S D`    | Moverse (arriba, izquierda, abajo, derecha)         |
| `H`          | Pedir una pista (según la habitación y el progreso) |
| `Q`          | Rendirse                                            |
| `M` / `E`    | Activar o desactivar música / efectos               |
| `X`          | Tecla secreta (Easter Egg, no aparece en el panel)  |

Hay tres pantallas:

1. **La habitación:** el mapa a la izquierda y el panel de progreso e
   inventario a la derecha.
2. **La pregunta:** los objetos son sólidos; al chocar con uno aparece su
   preview y el menú *1. Mirar de cerca / 2. Seguir explorando*.
3. **La escena:** el objeto a pantalla completa con su pista. ENTER vuelve al
   mapa.

Todas las entradas están validadas: el juego no se rompe por un dato mal
ingresado.

## Modos de juego y dificultad

Al iniciar, el juego pide el nombre del jugador y ofrece:

**Modos** (`seleccionarTipoJuego()`)

1. **Historia completa:** las 3 habitaciones seguidas.
2. **Practicar una habitación:** se elige cuál (1 a 3) y se juega solo esa.
3. **Contrarreloj:** la historia con 35 pasos menos en las habitaciones 2 y 3.

**Dificultad** (`configurarJugador()`)

| Nivel   | Pasos (habitaciones 2 y 3) | Pistas      | Intentos en la puerta 3 |
| ------- | -------------------------- | ----------- | ----------------------- |
| Fácil   | +20 %                      | Ilimitadas  | 3                       |
| Normal  | Base (180 y 165)           | 5           | 3                       |
| Difícil | −20 %                      | 2           | 2                       |

## Las tres habitaciones

### Habitación 1 (código base)

Cuatro objetos: nota, cofre, alfombra y puerta. La **alfombra** esconde la
llave → la llave abre el **cofre**, que revela un número sorteado → la **nota**
dice qué operación aplicarle (doble, mitad, +10, −10 o al revés, sorteada) → la
**puerta** pide el resultado. Número y operación cambian en cada partida.

### Habitación 2 (nuestra): los pinchos

Se juega con un límite de **pasos** (180 base). La habitación se divide en dos
zonas por una puerta interior (`D`), y la llave pequeña (`L`) está del lado de
los pinchos (`S`).

- Cruzar los pinchos por primera vez provoca **tétanos** y cuesta 25 pasos
  extra; cada cruce posterior cuesta 40 pasos.
- La **llave pequeña** abre la puerta interior y el **cofre** (`C`), que revela
  dos códigos de cuatro cifras: el del **estante** y el de **salida**.
- La **inscripción** (`I`) muestra un código **falso**: usarlo en el estante lo
  bloquea para siempre.
- El **estante** (`E`) con el código correcto cura el tétanos.
- La **puerta de salida** (`P`) pide el código de salida del cofre.

Los códigos se sortean en cada partida.

### Habitación 3 (nuestra): el laberinto

También con pasos limitados (165 base). Es un laberinto con estas piezas:

- **Estrellas** (`*`): cada una suma 20 pasos (hay dos).
- **Casillas con número** (`?`): dos de los tres números de dos cifras que se
  sortean por partida.
- **Llave antigua** (`K`): revela el tercer número y abre la puerta interior
  (`D`); no se consume.
- **Puerta de salida** (`P`): la clave tiene seis cifras. Se **ordenan los tres
  números de menor a mayor** (a, b, c) y se forma con las últimas dos cifras de
  `a·b`, `a·c` y `b·c`. Los intentos son limitados según la dificultad.

## Las dos matrices de cada habitación

Cada habitación se describe con dos matrices del mismo tamaño:

- `habitacionN.txt` (**visual**): lo que el jugador **ve**. La `@` marca dónde
  empieza.
- `zonasN.txt` (**movimiento**): lo que cada casilla **es**. `.` es piso, `#`
  es sólido y una letra es la zona de un objeto. El motor consulta esta matriz
  para mover al jugador.

Ejemplo (fragmento de la habitación 2):

```text
habitacion2.txt           zonas2.txt
#####################     #####################
#     L     ]       ...   #.....L.....D.......
#          <#             #..........S#.......
#<<<<<<<<<<<#             #SSSSSSSSSSS#.......
```

## Formato de `objetosN.csv`

```text
simbolo,nombre,archivo,detalle
```

- `simbolo`: la letra usada en la matriz de zonas.
- `nombre`: cómo se presenta el objeto al jugador.
- `archivo`: nombre base del arte → `arte/mini_<archivo>.txt` (preview) y
  `arte/<archivo>.txt` (escena).
- `detalle`: el texto de la escena, donde van las pistas.

Los textos no llevan comas, porque se separan con `split(",")`.

## Lo que programamos en el motor

**Los 4 placeholders obligatorios** (`Main.java`)

| Método                  | Qué hace en nuestra versión                                    |
| ----------------------- | -------------------------------------------------------------- |
| `seleccionarTipoJuego()`| Menú de modos (historia, practicar, contrarreloj)              |
| `configurarJugador()`   | Nombre del jugador, dificultad 1-3 y su aplicación             |
| `configurarArte()`      | Título y encabezado ASCII del juego                            |
| `siguienteHabitacion()` | Navegación de la 1 a la 3 con transición, hasta la victoria    |

**Otros métodos nuestros**

- `leerNumeroEntre()`: lee un entero en un rango y repite si es inválido.
- `generarSecretosPartida()`, `codigoCuatroCifras()`, `dosCifras()`: sortean los
  códigos y números de las habitaciones 2 y 3.
- `factorDificultad()`: multiplicador de pasos según la dificultad.
- Lógica propia de las habitaciones 2 y 3 en `mover()`, `preguntarObjeto()`,
  `abrirPuerta()` y `darPista()`.
- `easterEgg()` (tecla `X`): muestra un mensaje oculto del equipo.

El proyecto usa lo visto en el curso: métodos `static`, arreglos, matrices,
ciclos, cadenas y `Scanner`.

## Sonido

`Sonido.java` usa MIDI (incluido en el JDK, sin instalar nada). Los efectos son
arreglos de notas (`exito()`, `error()`, `recoger()`, `golpe()`, `victoria()`) y
la música de fondo es `musica/ambiente.mid`, que suena en bucle. Si no hay
audio, falla en silencio y el juego sigue.

## Documentación JavaDoc

La documentación está en `escape_la_habitacion/docs/index.html` e incluye
nuestros métodos nuevos. Para regenerarla, desde `escape_la_habitacion`:

```bash
javadoc -d docs -charset UTF-8 -encoding UTF-8 *.java
```

## Pruebas automáticas

`PruebasRegresion.java` verifica los cálculos de la habitación 1, los códigos
de la 2, el acertijo de la 3, la carga de archivos y las dificultades:

```bash
javac -encoding UTF-8 *.java
java PruebasRegresion
```

Si todo está bien, imprime `PRUEBAS CORRECTAS`.

## Presentación y evidencias

- **Presentación de la sustentación:** `Mácula & Laberintos — Mecánicas de
  juego.pdf` (raíz del repositorio).
- **Diagramas de diseño:** `Habitacion 2.png` y `Habitacion 3.png` en
  `escape_la_habitacion/`.
- **Trabajo colaborativo:** commits de los tres integrantes y una integración
  mediante pull request en el historial.

## Créditos

El motor base, la habitación 1, `Mapa.java`, `Sonido.java` y el material
inicial son del profesor (curso de Lógica, UPB,
[xacarana.com/cursos/logica](https://xacarana.com/cursos/logica/)). Todo lo
descrito en "Lo que programamos en el motor" y las habitaciones 2 y 3 es del
equipo.
