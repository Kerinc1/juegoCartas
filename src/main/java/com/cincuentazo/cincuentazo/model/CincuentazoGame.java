package com.cincuentazo.cincuentazo.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Orquestador principal de la partida de Cincuentazo.
 *
 * <p><strong>Responsabilidades ampliadas en HU-4:</strong></p>
 * <ol>
 *   <li>Mantenimiento de la <em>pila de descarte</em>: cada carta jugada a la mesa
 *       (excepto la que está encima) se acumula en {@link #pilaDescarte}.</li>
 *   <li>Robo de una carta tras cada jugada: {@link #robarCartaParaJugador(Jugador)}
 *       devuelve la carta al jugador y garantiza que su mano vuelva a 4 cartas.</li>
 *   <li>Reciclaje automático del mazo: si el mazo se agota,
 *       {@link #reciclarMesaEnMazo()} barajea la pila de descarte y la
 *       incorpora al mazo sin alterar la suma actual.</li>
 *   <li>Excepción {@link MazoAgotadoException}: se lanza si tanto el mazo como
 *       la pila de descarte están vacíos y no es posible robar ninguna carta.</li>
 * </ol>
 *
 * <p><strong>Reglas de impacto por rango (invariante desde HU-3):</strong></p>
 * <ul>
 *   <li>2–8 y 10 → suman su valor nominal.</li>
 *   <li>9         → neutro (0 puntos).</li>
 *   <li>J, Q, K   → restan 10 puntos.</li>
 *   <li>A (As)    → 10 si {@code sumaActual + 10 ≤ 50}; 1 en caso contrario.</li>
 * </ul>
 */
public class CincuentazoGame {

    // ── Constantes ───────────────────────────────────────────────────────

    /** Número fijo de cartas repartidas a cada jugador al iniciar la partida. */
    public static final int CARTAS_POR_JUGADOR = 4;

    /** Suma máxima permitida; superarla hace que una jugada sea inválida. */
    public static final int LIMITE_SUMA = 50;

    /** Valor alto del As cuando la suma actual lo permite. */
    private static final int AS_VALOR_ALTO = 10;

    /** Valor bajo del As cuando el valor alto excedería el límite. */
    private static final int AS_VALOR_BAJO  = 1;

    // ── Estado ───────────────────────────────────────────────────────────

    /** Mazo de juego; se reduce a medida que se roban cartas. */
    private final Mazo mazo;

    /** Jugador humano de la partida. */
    private final Humano jugadorHumano;

    /** Lista de oponentes máquina (1 a 3 según la configuración). */
    private final List<Maquina> maquinas;

    /** Carta boca arriba visible actualmente en el centro de la mesa. */
    private Carta cartaMesa;

    /** Suma acumulada del juego; se actualiza tras cada carta jugada. */
    private int sumaActual;

    /**
     * Pila de descarte: contiene todas las cartas que alguna vez estuvieron en
     * la mesa, excepto la {@link #cartaMesa} actual (la más reciente).
     *
     * <p>Cuando el mazo se agota, estas cartas se barajan y se devuelven al
     * mazo sin modificar la suma acumulada.</p>
     */
    private final List<Carta> pilaDescarte;

    /**
     * Índice del turno actual en el ciclo de participantes.
     * <ul>
     *   <li>{@code 0} → turno del jugador humano.</li>
     *   <li>{@code 1..N} → turno de {@code maquinas.get(turnoActual - 1)}.</li>
     * </ul>
     */
    private int turnoActual;

    // ── Constructor ──────────────────────────────────────────────────────

    /**
     * Construye e inicializa completamente una partida nueva.
     *
     * <p>Mezcla el mazo, reparte {@value #CARTAS_POR_JUGADOR} cartas a cada
     * participante de forma intercalada, coloca la carta inicial en la mesa e
     * inicializa la pila de descarte vacía.</p>
     *
     * @param configuracion configuración con el número de máquinas oponentes;
     *                      debe estar validada ({@link ConfiguracionJuego#isConfigurado()})
     * @throws IllegalArgumentException si la configuración no está lista
     */
    public CincuentazoGame(ConfiguracionJuego configuracion) {
        if (!configuracion.isConfigurado()) {
            throw new IllegalArgumentException(
                "La configuración del juego no está lista (0 jugadores máquina).");
        }

        this.mazo          = new Mazo();
        this.mazo.barajar();
        this.jugadorHumano = new Humano();
        this.maquinas      = new ArrayList<>(configuracion.getCantidadJugadoresMaquina());
        this.pilaDescarte  = new ArrayList<>();
        this.turnoActual   = 0;

        for (int i = 1; i <= configuracion.getCantidadJugadoresMaquina(); i++) {
            maquinas.add(new Maquina(i));
        }

        repartirCartas();
        colocarCartaMesa();
    }

    // ── Inicialización privada ───────────────────────────────────────────

    /**
     * Reparte {@value #CARTAS_POR_JUGADOR} cartas a cada jugador de forma
     * intercalada (humano primero, luego cada máquina en orden).
     * Las cartas de las máquinas se orientan boca abajo.
     */
    private void repartirCartas() {
        for (int ronda = 0; ronda < CARTAS_POR_JUGADOR; ronda++) {
            jugadorHumano.recibirCarta(mazo.robarCarta());
            for (Maquina maquina : maquinas) {
                Carta carta = mazo.robarCarta();
                carta.setBocaAbajo(true);
                maquina.recibirCarta(carta);
            }
        }
    }

    /**
     * Roba la primera carta del mazo, la orienta boca arriba y la coloca
     * en la mesa. La suma inicial usa el impacto base de la carta
     * (la regla dinámica del As aplica únicamente en jugadas de los participantes).
     */
    private void colocarCartaMesa() {
        cartaMesa  = mazo.robarCarta();
        sumaActual = cartaMesa.getImpactoSuma();
    }

    // ── Lógica de negocio — HU-3 ─────────────────────────────────────────

    /**
     * Calcula el impacto real que tendrá la carta sobre {@link #sumaActual}
     * si fuera jugada en este instante.
     *
     * <p>Regla dinámica exclusiva del As ({@link Rango#AS}):</p>
     * <ul>
     *   <li>Si {@code sumaActual + 10 ≤ LIMITE_SUMA} → el As vale {@value #AS_VALOR_ALTO}.</li>
     *   <li>De lo contrario → el As vale {@value #AS_VALOR_BAJO}.</li>
     * </ul>
     * <p>Para las demás cartas retorna directamente {@link Carta#getImpactoSuma()}.</p>
     *
     * @param carta carta a evaluar; no puede ser {@code null}
     * @return impacto efectivo (positivo, negativo o cero)
     */
    public int calcularImpacto(Carta carta) {
        if (carta.getRango() == Rango.AS) {
            return (sumaActual + AS_VALOR_ALTO <= LIMITE_SUMA) ? AS_VALOR_ALTO : AS_VALOR_BAJO;
        }
        return carta.getImpactoSuma();
    }

    /**
     * Determina si jugar la carta indicada es un movimiento válido según
     * la regla del límite de {@value #LIMITE_SUMA} puntos.
     *
     * <p>Una carta es jugable si y solo si:
     * {@code sumaActual + calcularImpacto(carta) ≤ LIMITE_SUMA}.</p>
     *
     * @param carta carta a evaluar; no puede ser {@code null}
     * @return {@code true} si la carta puede colocarse en la mesa sin violar la regla
     */
    public boolean esJugable(Carta carta) {
        return sumaActual + calcularImpacto(carta) <= LIMITE_SUMA;
    }

    /**
     * Ejecuta la jugada completa de una carta:
     * <ol>
     *   <li>Valida que la carta sea jugable.</li>
     *   <li>Verifica que pertenezca a la mano del jugador.</li>
     *   <li>Mueve la {@link #cartaMesa} anterior a la {@link #pilaDescarte}.</li>
     *   <li>Calcula el impacto efectivo (incluyendo la regla dinámica del As).</li>
     *   <li>Retira la carta de la mano y actualiza {@link #cartaMesa} y
     *       {@link #sumaActual}.</li>
     * </ol>
     *
     * @param jugador jugador que realiza la jugada (humano o máquina)
     * @param carta   carta a colocar en la mesa; debe pertenecer a la mano del jugador
     * @throws MovimientoInvalidoException si la carta haría que la suma superara
     *                                      los {@value #LIMITE_SUMA} puntos
     * @throws IllegalArgumentException    si la carta no pertenece a la mano del jugador
     */
    public void jugarCarta(Jugador jugador, Carta carta) {
        if (!esJugable(carta)) {
            int sumaResultante = sumaActual + calcularImpacto(carta);
            throw new MovimientoInvalidoException(
                "Movimiento inválido: jugar " + carta + " llevaría la suma a "
                + sumaResultante + ", superando el límite de " + LIMITE_SUMA + " puntos."
            );
        }

        int indice = jugador.getMano().indexOf(carta);
        if (indice == -1) {
            throw new IllegalArgumentException(
                "La carta " + carta + " no pertenece a la mano de " + jugador.getNombre() + "."
            );
        }

        // Capturar el impacto ANTES de modificar sumaActual (crítico para el As)
        int impacto = calcularImpacto(carta);

        jugador.jugarCarta(indice);       // retira la carta de la mano
        pilaDescarte.add(cartaMesa);      // la carta anterior pasa al descarte (HU-4)
        cartaMesa = carta;
        cartaMesa.setBocaAbajo(false);    // la carta en mesa siempre boca arriba
        sumaActual += impacto;
    }

    // ── Robo de carta — HU-4 ─────────────────────────────────────────────

    /**
     * Roba una carta del mazo y la añade a la mano del jugador indicado.
     *
     * <p>Si el mazo está vacío antes de robar, invoca
     * {@link #reciclarMesaEnMazo()} para reincorporar la {@link #pilaDescarte}
     * al mazo (barajada), respetando la regla crítica de que la suma actual
     * no se ve modificada.</p>
     *
     * <p>La orientación de la carta robada se establece automáticamente:</p>
     * <ul>
     *   <li>{@link Humano}: boca arriba ({@code bocaAbajo = false}).</li>
     *   <li>{@link Maquina}: boca abajo ({@code bocaAbajo = true}).</li>
     * </ul>
     *
     * @param jugador jugador que roba la carta; no puede ser {@code null}
     * @return {@code true} si fue necesario reciclar la pila de descarte antes
     *         de robar; {@code false} si el mazo tenía cartas disponibles
     * @throws MazoAgotadoException si tanto el mazo como la pila de descarte
     *                               están vacíos y no existe ninguna carta disponible
     */
    public boolean robarCartaParaJugador(Jugador jugador) {
        boolean fueReciclado = false;

        if (mazo.estaVacio()) {
            reciclarMesaEnMazo(); // puede lanzar MazoAgotadoException
            fueReciclado = true;
        }

        Carta carta = mazo.robarCarta();
        carta.setBocaAbajo(jugador instanceof Maquina);
        jugador.recibirCarta(carta);

        return fueReciclado;
    }

    /**
     * Recicla la pila de descarte incorporándola al mazo y la baraja.
     *
     * <p>La {@link #cartaMesa} actual (la última carta jugada) <strong>no</strong>
     * se incluye en el reciclaje, preservando así la continuidad visual de la
     * mesa y la integridad de la suma acumulada.</p>
     *
     * @throws MazoAgotadoException si la pila de descarte también está vacía y
     *                               no hay ninguna carta disponible para reciclar
     */
    private void reciclarMesaEnMazo() {
        if (pilaDescarte.isEmpty()) {
            throw new MazoAgotadoException(
                "Partida sin cartas disponibles: el mazo y la pila de descarte "
                + "están vacíos. La partida no puede continuar."
            );
        }
        mazo.recargar(new ArrayList<>(pilaDescarte));
        pilaDescarte.clear();
    }

    // ── Lógica de eliminación — HU-5 ────────────────────────────────────

    /**
     * Determina si el jugador dado tiene al menos un movimiento válido
     * con la suma actual de la mesa.
     *
     * <p>Itera sobre cada carta de la mano y delega en {@link #esJugable(Carta)}.
     * Si la mano está vacía retorna {@code false}: sin cartas, no hay movimientos.</p>
     *
     * @param jugador jugador a evaluar; no puede ser {@code null}
     * @return {@code true} si al menos una carta de la mano puede jugarse
     */
    public boolean tieneMovimientosValidos(Jugador jugador) {
        return jugador.getMano().stream().anyMatch(this::esJugable);
    }

    /**
     * Elimina a un jugador de la partida:
     * <ol>
     *   <li>Lo marca como inactivo ({@link Jugador#setActivo(boolean) setActivo(false)}).</li>
     *   <li>Retira todas sus cartas de la mano mediante
     *       {@link Jugador#devolverTodasLasCartas()}.</li>
     *   <li>Inserta esas cartas en {@link #pilaDescarte} para que queden disponibles
     *       en el reciclaje automático.</li>
     * </ol>
     *
     * @param jugador jugador a eliminar; no puede ser {@code null}
     */
    public void eliminarJugador(Jugador jugador) {
        jugador.setActivo(false);
        List<Carta> devueltas = jugador.devolverTodasLasCartas();
        pilaDescarte.addAll(devueltas);
    }

    /**
     * Retorna cuántos participantes (humano y/o máquinas) siguen activos en la partida.
     *
     * @return número de jugadores activos (≥ 0)
     */
    public int contarJugadoresActivos() {
        int total = jugadorHumano.isActivo() ? 1 : 0;
        for (Maquina m : maquinas) {
            if (m.isActivo()) total++;
        }
        return total;
    }

    // ── Gestión de turnos ────────────────────────────────────────────────

    /**
     * Avanza al siguiente turno de forma circular, saltando automáticamente
     * los jugadores que hayan sido eliminados (HU-5).
     *
     * <p>El ciclo natural es: humano (0) → máquina 1 (1) → … → máquina N (N) → humano (0).
     * Si el candidato al siguiente turno está inactivo, se sigue avanzando hasta
     * encontrar un jugador activo. El bucle se detiene como máximo tras {@code total}
     * intentos para evitar un ciclo infinito cuando no queda ningún jugador activo.</p>
     */
    public void avanzarTurno() {
        int total    = 1 + maquinas.size();
        int intentos = 0;
        do {
            turnoActual = (turnoActual + 1) % total;
            intentos++;
        } while (!obtenerJugadorEnTurnoActual().isActivo() && intentos < total);
    }

    /**
     * Retorna el {@link Jugador} al que corresponde el {@link #turnoActual}.
     *
     * @return {@link #jugadorHumano} si {@code turnoActual == 0};
     *         la {@link Maquina} de índice {@code turnoActual - 1} en otro caso
     */
    private Jugador obtenerJugadorEnTurnoActual() {
        return turnoActual == 0 ? jugadorHumano : maquinas.get(turnoActual - 1);
    }

    /**
     * Indica si el turno actual corresponde al jugador humano.
     *
     * @return {@code true} si es el turno del humano (índice 0)
     */
    public boolean esTurnoHumano() {
        return turnoActual == 0;
    }

    /**
     * Retorna la máquina que tiene el turno actual.
     *
     * @return la {@link Maquina} que debe jugar ahora
     * @throws IllegalStateException si el turno actual pertenece al humano
     */
    public Maquina getMaquinaActual() {
        if (esTurnoHumano()) {
            throw new IllegalStateException(
                "El turno actual es del jugador humano, no de una máquina.");
        }
        return maquinas.get(turnoActual - 1);
    }

    /**
     * Retorna el índice numérico del turno actual (0 = humano, 1..N = máquinas).
     *
     * @return índice del turno activo
     */
    public int getTurnoActual() {
        return turnoActual;
    }

    // ── Accesores ────────────────────────────────────────────────────────

    /** @return referencia al {@link Mazo} en curso */
    public Mazo getMazo() { return mazo; }

    /** @return instancia de {@link Humano} */
    public Humano getJugadorHumano() { return jugadorHumano; }

    /** @return lista no modificable de {@link Maquina} (1 a 3 elementos) */
    public List<Maquina> getMaquinas() {
        return Collections.unmodifiableList(maquinas);
    }

    /** @return última carta jugada (o carta inicial al comenzar la partida) */
    public Carta getCartaMesa() { return cartaMesa; }

    /** @return valor entero de la suma acumulada */
    public int getSumaActual() { return sumaActual; }

    /**
     * Retorna una vista no modificable de la pila de descarte.
     *
     * <p>Contiene todas las cartas que estuvieron en la mesa excepto la actual.
     * Útil para inspección y pruebas unitarias.</p>
     *
     * @return lista inmutable de {@link Carta} en el descarte
     */
    public List<Carta> getPilaDescarte() {
        return Collections.unmodifiableList(pilaDescarte);
    }

    // ── API de pruebas (package-private) ─────────────────────────────────

    /**
     * Fuerza un valor específico de la suma acumulada.
     * <p><strong>Exclusivo para pruebas unitarias.</strong></p>
     *
     * @param suma nuevo valor de la suma acumulada
     */
    void setSumaActual(int suma) {
        this.sumaActual = suma;
    }
}
