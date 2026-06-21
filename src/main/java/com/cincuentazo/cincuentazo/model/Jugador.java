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

    // ── constructor ──────────────────────────────────────────────────────

    /**
     * Crea un jugador con el nombre indicado y la mano vacía.
     *
     * @param nombre nombre del jugador; no puede ser {@code null} ni vacío
     */
    protected Jugador(String nombre) {
        this.nombre = nombre;
        this.mano   = new ArrayList<>(4);
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
