package com.cincuentazo.cincuentazo.model;

/**
 * Enumeración de los cuatro palos de la baraja estándar francesa.
 *
 * <p>Cada palo almacena su símbolo Unicode y si su color es rojo,
 * dato usado por la capa de vista para aplicar el estilo correcto.</p>
 */
public enum Palo {

    /** Corazones — palo rojo (♥). */
    CORAZONES("♥", true),

    /** Diamantes — palo rojo (♦). */
    DIAMANTES("♦", true),

    /** Picas — palo negro (♠). */
    PICAS("♠", false),

    /** Tréboles — palo negro (♣). */
    TREBOLES("♣", false);

    /** Símbolo Unicode del palo. */
    private final String simbolo;

    /** {@code true} si el palo se imprime en rojo (corazones y diamantes). */
    private final boolean esRojo;

    /**
     * Constructor del enum.
     *
     * @param simbolo símbolo Unicode del palo
     * @param esRojo  {@code true} para palos rojos
     */
    Palo(String simbolo, boolean esRojo) {
        this.simbolo = simbolo;
        this.esRojo  = esRojo;
    }

    /**
     * Retorna el símbolo Unicode del palo.
     *
     * @return cadena de un carácter (ej. {@code "♥"})
     */
    public String getSimbolo() {
        return simbolo;
    }

    /**
     * Indica si el palo se representa en color rojo.
     *
     * @return {@code true} para corazones y diamantes
     */
    public boolean esRojo() {
        return esRojo;
    }
}
