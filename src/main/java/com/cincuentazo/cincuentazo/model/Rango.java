package com.cincuentazo.cincuentazo.model;

/**
 * Enumeración de los trece rangos de la baraja estándar francesa,
 * con su etiqueta visual y su impacto sobre la suma del Cincuentazo.
 *
 * <p>Reglas de impacto:</p>
 * <ul>
 *   <li>2–8 y 10 → suman su valor nominal.</li>
 *   <li>9        → neutro (impacto = 0).</li>
 *   <li>J, Q, K  → restan 10.</li>
 *   <li>A        → suma 1 (la elección 1 / 10 se gestiona en HU-3).</li>
 * </ul>
 */
public enum Rango {

    DOS   ("2",  2),
    TRES  ("3",  3),
    CUATRO("4",  4),
    CINCO ("5",  5),
    SEIS  ("6",  6),
    SIETE ("7",  7),
    OCHO  ("8",  8),

    /** 9 es neutro: no modifica la suma del juego. */
    NUEVE ("9",  0),

    DIEZ  ("10", 10),

    /** Jota resta 10 a la suma actual. */
    JOTA  ("J", -10),

    /** Reina resta 10 a la suma actual. */
    REINA ("Q", -10),

    /** Rey resta 10 a la suma actual. */
    REY   ("K", -10),

    /** As suma 1 por defecto; la elección 1 / 10 se implementa en HU-3. */
    AS    ("A",  1);

    // ── campos ──────────────────────────────────────────────────────────

    /** Etiqueta abreviada mostrada en la carta (ej. {@code "J"}, {@code "10"}). */
    private final String etiqueta;

    /** Impacto numérico sobre la suma del juego al jugar la carta. */
    private final int impacto;

    // ── constructor ──────────────────────────────────────────────────────

    /**
     * @param etiqueta texto corto visible en la carta
     * @param impacto  puntos que la carta suma (positivo) o resta (negativo)
     */
    Rango(String etiqueta, int impacto) {
        this.etiqueta = etiqueta;
        this.impacto  = impacto;
    }

    // ── accesores ────────────────────────────────────────────────────────

    /**
     * Retorna la etiqueta abreviada del rango.
     *
     * @return cadena como {@code "A"}, {@code "10"}, {@code "K"}, etc.
     */
    public String getEtiqueta() {
        return etiqueta;
    }

    /**
     * Retorna el impacto de esta carta sobre la suma del juego.
     *
     * @return entero positivo, negativo o cero
     */
    public int getImpacto() {
        return impacto;
    }
}
