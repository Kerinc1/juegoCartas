package com.cincuentazo.cincuentazo.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Baraja estándar francesa de 52 cartas (sin comodines).
 *
 * <p>Al construirse genera todas las combinaciones posibles de
 * {@link Palo} × {@link Rango} (4 × 13 = 52 cartas). Las operaciones
 * principales son mezclar y robar la carta superior.</p>
 *
 * <p>Internamente las cartas se almacenan en un {@link ArrayList}; robar
 * una carta elimina el último elemento, operación O(1) en un ArrayList.</p>
 */
public class Mazo {

    /** Número total de cartas de una baraja completa. */
    public static final int CARTAS_TOTALES = 52;

    /** Colección interna de cartas (el último elemento es la "cima" del mazo). */
    private final List<Carta> cartas;

    // ── constructor ──────────────────────────────────────────────────────

    /**
     * Construye un mazo nuevo con las 52 cartas en orden canónico
     * (sin barajar). Llama a {@link #barajar()} para obtener un orden aleatorio.
     */
    public Mazo() {
        cartas = new ArrayList<>(CARTAS_TOTALES);
        for (Palo palo : Palo.values()) {
            for (Rango rango : Rango.values()) {
                cartas.add(new Carta(rango, palo));
            }
        }
    }

    // ── operaciones ──────────────────────────────────────────────────────

    /**
     * Mezcla las cartas restantes en el mazo usando un algoritmo de
     * Fisher-Yates con fuente de entropía del sistema
     * ({@link Collections#shuffle(List)}).
     */
    public void barajar() {
        Collections.shuffle(cartas);
    }

    /**
     * Retira y retorna la carta en la cima del mazo.
     *
     * @return la carta robada
     * @throws IllegalStateException si el mazo está vacío
     */
    public Carta robarCarta() {
        if (cartas.isEmpty()) {
            throw new IllegalStateException("No se puede robar: el mazo está vacío.");
        }
        return cartas.remove(cartas.size() - 1);
    }

    // ── consultas ────────────────────────────────────────────────────────

    /**
     * Retorna el número de cartas que quedan en el mazo.
     *
     * @return cantidad de cartas restantes (0 a 52)
     */
    public int getTamanio() {
        return cartas.size();
    }

    /**
     * Indica si el mazo ha sido agotado por completo.
     *
     * @return {@code true} si no quedan cartas
     */
    public boolean estaVacio() {
        return cartas.isEmpty();
    }
}
