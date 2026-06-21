package com.cincuentazo.cincuentazo.model;

/**
 * Representa a un oponente controlado por el sistema (IA).
 *
 * <p>En HU-2 las máquinas reciben sus cartas boca abajo; la lógica de
 * decisión de juego se implementará en HU-3.</p>
 */
public class Maquina extends Jugador {

    /**
     * Número ordinal de la máquina dentro de la partida (1, 2 o 3).
     * Se usa para componer el nombre y para identificarla en la vista.
     */
    private final int indice;

    // ── constructor ──────────────────────────────────────────────────────

    /**
     * Crea una máquina oponente con el índice indicado.
     * El nombre resultante sigue el patrón {@code "Máquina N"}.
     *
     * @param indice número ordinal de esta máquina (1, 2 o 3)
     */
    public Maquina(int indice) {
        super("Máquina " + indice);
        this.indice = indice;
    }

    // ── consultas ────────────────────────────────────────────────────────

    /**
     * Retorna el índice ordinal de la máquina.
     *
     * @return número entero ≥ 1
     */
    public int getIndice() {
        return indice;
    }
}
