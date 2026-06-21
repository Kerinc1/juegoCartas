package com.cincuentazo.cincuentazo.model;

/**
 * Representa una carta individual de la baraja estándar francesa de 52 naipes.
 *
 * <p>Una carta queda identificada de forma única por la combinación de su
 * {@link Rango} y su {@link Palo}. También mantiene un indicador de orientación
 * ({@code bocaAbajo}) para que la vista pueda decidir si mostrar el frente
 * o el dorso sin acceder a los datos del modelo.</p>
 */
public class Carta {

    /** Rango de la carta (2–10, J, Q, K, A). */
    private final Rango rango;

    /** Palo de la carta (corazones, diamantes, picas, tréboles). */
    private final Palo palo;

    /**
     * {@code true} si la carta está orientada boca abajo (dorso visible).
     * Las cartas de las máquinas arrancan en {@code true}; la del humano y
     * la carta de la mesa arrancan en {@code false}.
     */
    private boolean bocaAbajo;

    // ── constructor ──────────────────────────────────────────────────────

    /**
     * Crea una carta con el rango y el palo indicados, orientada boca arriba.
     *
     * @param rango rango de la carta; no puede ser {@code null}
     * @param palo  palo de la carta; no puede ser {@code null}
     */
    public Carta(Rango rango, Palo palo) {
        this.rango     = rango;
        this.palo      = palo;
        this.bocaAbajo = false;
    }

    // ── lógica de negocio ────────────────────────────────────────────────

    /**
     * Retorna el impacto que esta carta tiene sobre la suma del juego cuando
     * es jugada.
     *
     * <p>El valor proviene directamente del {@link Rango} de la carta:</p>
     * <ul>
     *   <li>2–8 y 10 → su valor nominal.</li>
     *   <li>9        → 0 (neutro).</li>
     *   <li>J, Q, K  → −10.</li>
     *   <li>A        → 1 (la elección 1 / 10 se gestiona en HU-3).</li>
     * </ul>
     *
     * @return entero que representa el cambio en la suma
     */
    public int getImpactoSuma() {
        return rango.getImpacto();
    }

    // ── accesores ────────────────────────────────────────────────────────

    /**
     * Retorna el rango de la carta.
     *
     * @return {@link Rango} de la carta
     */
    public Rango getRango() {
        return rango;
    }

    /**
     * Retorna el palo de la carta.
     *
     * @return {@link Palo} de la carta
     */
    public Palo getPalo() {
        return palo;
    }

    /**
     * Indica si la carta está orientada boca abajo.
     *
     * @return {@code true} si el dorso es visible
     */
    public boolean isBocaAbajo() {
        return bocaAbajo;
    }

    /**
     * Establece la orientación de la carta.
     *
     * @param bocaAbajo {@code true} para mostrar el dorso; {@code false} para el frente
     */
    public void setBocaAbajo(boolean bocaAbajo) {
        this.bocaAbajo = bocaAbajo;
    }

    // ── Object ───────────────────────────────────────────────────────────

    /**
     * Representación textual de la carta en formato {@code RangoPalo}
     * (ej. {@code "A♥"}, {@code "10♠"}).
     *
     * @return cadena que identifica la carta
     */
    @Override
    public String toString() {
        return rango.getEtiqueta() + palo.getSimbolo();
    }
}
