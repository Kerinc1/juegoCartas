package com.cincuentazo.cincuentazo.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Clase base abstracta que representa a un participante de la partida,
 * ya sea el jugador humano o una máquina.
 *
 * <p>Gestiona la mano de cartas ({@link Carta}) del jugador y expone
 * las operaciones comunes de recibir y jugar cartas.</p>
 */
public abstract class Jugador {

    /** Nombre identificador del jugador (ej. {@code "Jugador"}, {@code "Máquina 1"}). */
    private final String nombre;

    /**
     * Cartas actualmente en la mano del jugador.
     * La capacidad inicial coincide con el número de cartas repartidas al inicio.
     */
    private final List<Carta> mano;

    /**
     * Indica si el jugador sigue participando en la partida.
     * Se vuelve {@code false} cuando es eliminado por no tener movimientos válidos (HU-5).
     */
    private boolean activo;

    // ── constructor ──────────────────────────────────────────────────────

    /**
     * Crea un jugador con el nombre indicado, la mano vacía y en estado activo.
     *
     * @param nombre nombre del jugador; no puede ser {@code null} ni vacío
     */
    protected Jugador(String nombre) {
        this.nombre = nombre;
        this.mano   = new ArrayList<>(4);
        this.activo = true;
    }

    // ── operaciones ──────────────────────────────────────────────────────

    /**
     * Añade una carta al final de la mano del jugador.
     *
     * @param carta carta recibida del mazo; no puede ser {@code null}
     */
    public void recibirCarta(Carta carta) {
        mano.add(carta);
    }

    /**
     * Retira la carta en la posición indicada de la mano y la retorna.
     *
     * @param indice índice (base 0) de la carta a jugar
     * @return la carta retirada de la mano
     * @throws IndexOutOfBoundsException si el índice está fuera del rango de la mano
     */
    public Carta jugarCarta(int indice) {
        return mano.remove(indice);
    }

    // ── consultas ────────────────────────────────────────────────────────

    /**
     * Retorna una vista no modificable de las cartas en la mano del jugador.
     *
     * @return lista inmutable de {@link Carta}
     */
    public List<Carta> getMano() {
        return Collections.unmodifiableList(mano);
    }

    /**
     * Retorna el nombre del jugador.
     *
     * @return cadena con el nombre identificador
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Retorna cuántas cartas tiene actualmente el jugador en la mano.
     *
     * @return número de cartas (0 a N)
     */
    public int getCantidadCartas() {
        return mano.size();
    }

    /**
     * Indica si el jugador sigue activo en la partida.
     *
     * @return {@code true} si el jugador no ha sido eliminado
     */
    public boolean isActivo() {
        return activo;
    }

    // ── mutadores (package-private, solo el modelo los usa) ─────────────

    /**
     * Cambia el estado de actividad del jugador.
     * Exclusivo para {@link CincuentazoGame#eliminarJugador(Jugador)}.
     *
     * @param activo {@code false} para marcar al jugador como eliminado
     */
    void setActivo(boolean activo) {
        this.activo = activo;
    }

    /**
     * Retira todas las cartas de la mano, las devuelve en una lista nueva y
     * deja la mano vacía.
     *
     * <p>Invocado por {@link CincuentazoGame#eliminarJugador(Jugador)} para
     * reinsertar las cartas en la pila de descarte.</p>
     *
     * @return lista con todas las cartas que tenía el jugador
     */
    List<Carta> devolverTodasLasCartas() {
        List<Carta> devueltas = new ArrayList<>(mano);
        mano.clear();
        return devueltas;
    }

    // ── Object ───────────────────────────────────────────────────────────

    /**
     * Representación textual con el nombre y las cartas en mano.
     *
     * @return cadena descriptiva del jugador
     */
    @Override
    public String toString() {
        return nombre + " " + mano;
    }
}
