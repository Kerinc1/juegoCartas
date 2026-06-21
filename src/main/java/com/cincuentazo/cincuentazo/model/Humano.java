package com.cincuentazo.cincuentazo.model;

/**
 * Representa al jugador humano que controla la partida.
 *
 * <p>Extiende {@link Jugador} sin añadir lógica adicional en HU-2;
 * la interacción con la interfaz (seleccionar y jugar cartas) se
 * implementa en el controlador durante HU-3.</p>
 */
public class Humano extends Jugador {

    /**
     * Crea el jugador humano con el nombre por defecto {@code "Jugador"}.
     */
    public Humano() {
        super("Jugador");
    }

    /**
     * Crea el jugador humano con un nombre personalizado.
     *
     * @param nombre nombre visible en la interfaz
     */
    public Humano(String nombre) {
        super(nombre);
    }
}
