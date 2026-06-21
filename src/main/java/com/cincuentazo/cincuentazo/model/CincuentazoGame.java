package com.cincuentazo.cincuentazo.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Orquestador principal de la partida de Cincuentazo.
 *
 * <p>Responsabilidades en HU-2:</p>
 * <ol>
 *   <li>Crear y mezclar el {@link Mazo}.</li>
 *   <li>Repartir exactamente {@value #CARTAS_POR_JUGADOR} cartas a cada
 *       participante (humano y máquinas).</li>
 *   <li>Colocar la carta inicial boca arriba en la mesa y calcular la
 *       suma inicial del juego.</li>
 * </ol>
 *
 * <p>El reparto sigue el orden intercalado estándar: una carta al humano,
 * una carta a cada máquina, y se repite hasta completar las cuatro manos.</p>
 */
public class CincuentazoGame {

    /** Número fijo de cartas repartidas a cada jugador al iniciar la partida. */
    public static final int CARTAS_POR_JUGADOR = 4;

    // ── estado ───────────────────────────────────────────────────────────

    /** Mazo de juego; se reduce a medida que se roban cartas. */
    private final Mazo mazo;

    /** Jugador humano. */
    private final Humano jugadorHumano;

    /** Lista de oponentes máquina (1 a 3 según {@link ConfiguracionJuego}). */
    private final List<Maquina> maquinas;

    /** Carta boca arriba que inicializa la mesa. */
    private Carta cartaMesa;

    /** Suma acumulada actual del juego (se actualiza al jugar cada carta). */
    private int sumaActual;

    // ── constructor ──────────────────────────────────────────────────────

    /**
     * Construye e inicializa completamente una partida nueva.
     *
     * <p>El constructor mezcla el mazo, reparte las cartas y establece
     * la carta inicial de la mesa. Al retornar, el estado del juego está
     * listo para que el jugador humano realice su primer turno (HU-3).</p>
     *
     * @param configuracion configuración con el número de máquinas oponentes;
     *                      debe estar configurada ({@link ConfiguracionJuego#isConfigurado()})
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

        for (int i = 1; i <= configuracion.getCantidadJugadoresMaquina(); i++) {
            maquinas.add(new Maquina(i));
        }

        repartirCartas();
        colocarCartaMesa();
    }

    // ── inicialización privada ───────────────────────────────────────────

    /**
     * Reparte {@value #CARTAS_POR_JUGADOR} cartas a cada jugador de forma
     * intercalada (primero el humano, luego cada máquina en orden).
     * Las cartas de las máquinas se marcan boca abajo.
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
     * La suma inicial del juego se establece con su impacto.
     */
    private void colocarCartaMesa() {
        cartaMesa  = mazo.robarCarta();
        sumaActual = cartaMesa.getImpactoSuma();
    }

    // ── accesores ────────────────────────────────────────────────────────

    /**
     * Retorna el mazo restante de la partida.
     *
     * @return referencia al {@link Mazo} en curso
     */
    public Mazo getMazo() {
        return mazo;
    }

    /**
     * Retorna el jugador humano.
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
     * Retorna la carta boca arriba que se encuentra actualmente en la mesa.
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

    // ── mutaciones (HU-3 las expandirá) ─────────────────────────────────

    /**
     * Actualiza la carta de la mesa y recalcula la suma tras jugar una carta.
     * Este método será invocado por el controlador en HU-3.
     *
     * @param cartaJugada carta que el jugador deposita en la mesa
     */
    public void jugarCarta(Carta cartaJugada) {
        cartaMesa   = cartaJugada;
        sumaActual += cartaJugada.getImpactoSuma();
    }
}
