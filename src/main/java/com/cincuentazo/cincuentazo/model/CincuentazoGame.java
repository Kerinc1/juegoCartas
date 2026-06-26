package com.cincuentazo.cincuentazo.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Orquestador principal de la partida de Cincuentazo.
 *
 * <p><strong>Responsabilidades ampliadas en HU-3:</strong></p>
 * <ol>
 *   <li>Control del turno actual (humano → máquinas, orden cíclico).</li>
 *   <li>Cálculo dinámico del impacto del As (10 si no supera el límite; 1 en caso contrario).</li>
 *   <li>Validación de jugadas contra la regla del límite de
 *       {@value #LIMITE_SUMA} puntos mediante {@link #esJugable(Carta)}.</li>
 *   <li>Ejecución de la jugada con {@link #jugarCarta(Jugador, Carta)}, que lanza
 *       {@link MovimientoInvalidoException} si la carta supera el límite.</li>
 * </ol>
 *
 * <p><strong>Reglas de impacto por rango:</strong></p>
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

    /** Mazo de juego; se reduce a medida que se roban cartas durante el reparto inicial. */
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
     * Índice del turno actual en el ciclo de participantes.
     * <ul>
     *   <li>{@code 0} → turno del jugador humano.</li>
     *   <li>{@code 1..N} → turno de {@code maquinas.get(turnoActual - 1)}.</li>
     * </ul>
     * Avanza de forma circular mediante {@link #avanzarTurno()}.
     */
    private int turnoActual;

    // ── Constructor ──────────────────────────────────────────────────────

    /**
     * Construye e inicializa completamente una partida nueva.
     *
     * <p>Mezcla el mazo, reparte {@value #CARTAS_POR_JUGADOR} cartas a cada
     * participante de forma intercalada, coloca la carta inicial en la mesa
     * y posiciona el turno en el jugador humano (índice 0).</p>
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
        this.turnoActual   = 0; // comienza con el jugador humano

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
     * Roba la primera carta del mazo y la coloca boca arriba en la mesa.
     * La suma inicial usa el impacto base de la carta (la regla dinámica del
     * As aplica únicamente durante las jugadas de los participantes).
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
     * <p>Este método es <strong>puro</strong>: no modifica el estado del juego
     * y puede invocarse múltiples veces para prevalidar antes de llamar a
     * {@link #jugarCarta(Jugador, Carta)}.</p>
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
     *   <li>Valida que la carta sea jugable contra la regla del límite.</li>
     *   <li>Verifica que la carta pertenezca a la mano del jugador indicado.</li>
     *   <li>Calcula el impacto efectivo (incluyendo la regla dinámica del As).</li>
     *   <li>Retira la carta de la mano del jugador.</li>
     *   <li>Actualiza {@link #cartaMesa} y {@link #sumaActual}.</li>
     * </ol>
     *
     * <p>La carta queda siempre orientada boca arriba en la mesa.</p>
     *
     * @param jugador jugador que realiza la jugada (humano o máquina)
     * @param carta   carta que el jugador desea colocar en la mesa;
     *                debe pertenecer a la mano del jugador
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

        jugador.jugarCarta(indice); // retira la carta de la mano
        cartaMesa = carta;
        cartaMesa.setBocaAbajo(false); // siempre boca arriba en la mesa
        sumaActual += impacto;
    }

    // ── Gestión de turnos ────────────────────────────────────────────────

    /**
     * Avanza al siguiente turno de forma circular:
     * humano (0) → máquina 1 (1) → … → máquina N (N) → humano (0).
     */
    public void avanzarTurno() {
        turnoActual = (turnoActual + 1) % (1 + maquinas.size());
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
     * <p>Solo debe invocarse cuando {@link #esTurnoHumano()} es {@code false}.</p>
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

    /**
     * Retorna el mazo restante de la partida.
     *
     * @return referencia al {@link Mazo} en curso
     */
    public Mazo getMazo() {
        return mazo;
    }

    /**
     * Retorna el jugador humano de la partida.
     *
     * @return instancia de {@link Humano}
     */
    public Humano getJugadorHumano() {
        return jugadorHumano;
    }

    /**
     * Retorna la lista no modificable de máquinas oponentes.
     *
     * @return lista de {@link Maquina} (1 a 3 elementos)
     */
    public List<Maquina> getMaquinas() {
        return Collections.unmodifiableList(maquinas);
    }

    /**
     * Retorna la carta boca arriba actualmente en la mesa.
     *
     * @return última carta jugada (o carta inicial al comenzar la partida)
     */
    public Carta getCartaMesa() {
        return cartaMesa;
    }

    /**
     * Retorna la suma acumulada actual del juego.
     *
     * @return valor entero de la suma (puede ser negativo si J/Q/K fue la carta inicial)
     */
    public int getSumaActual() {
        return sumaActual;
    }

    // ── API de pruebas (package-private) ─────────────────────────────────

    /**
     * Fuerza un valor específico de la suma acumulada.
     *
     * <p><strong>Exclusivo para pruebas unitarias:</strong> permite llevar el
     * juego a un estado conocido (por ejemplo {@code sumaActual = 45}) sin
     * necesidad de jugar una secuencia real de cartas previas. No debe
     * utilizarse en código de producción.</p>
     *
     * @param suma nuevo valor de la suma acumulada
     */
    void setSumaActual(int suma) {
        this.sumaActual = suma;
    }
}
