package com.cincuentazo.cincuentazo.model;

/**
 * Representa la configuración inicial seleccionada por el jugador humano
 * antes de comenzar una partida de Cincuentazo.
 *
 * <p>Esta clase pertenece a la capa <b>Modelo</b> del patrón MVC y es la
 * única responsable de validar que el número de oponentes máquina esté
 * en el rango permitido ({@value #MIN_JUGADORES}–{@value #MAX_JUGADORES}).</p>
 */
public class ConfiguracionJuego {

    /**
     * Número mínimo de jugadores máquina permitidos en una partida.
     */
    public static final int MIN_JUGADORES = 1;

    /**
     * Número máximo de jugadores máquina permitidos en una partida.
     */
    public static final int MAX_JUGADORES = 3;

    /**
     * Cantidad de jugadores máquina configurada para la partida.
     * El valor {@code 0} indica que aún no se ha realizado ninguna selección.
     */
    private int cantidadJugadoresMaquina;

    /**
     * Crea una configuración vacía sin jugadores seleccionados.
     * El valor inicial es {@code 0}, indicando que el usuario aún no ha elegido.
     */
    public ConfiguracionJuego() {
        this.cantidadJugadoresMaquina = 0;
    }

    /**
     * Crea una configuración con la cantidad de jugadores máquina especificada.
     *
     * @param cantidadJugadoresMaquina número de oponentes máquina (entre
     *                                 {@value #MIN_JUGADORES} y {@value #MAX_JUGADORES})
     * @throws IllegalArgumentException si el valor está fuera del rango permitido
     */
    public ConfiguracionJuego(int cantidadJugadoresMaquina) {
        setCantidadJugadoresMaquina(cantidadJugadoresMaquina);
    }

    /**
     * Retorna la cantidad de jugadores máquina actualmente configurada.
     *
     * @return número de oponentes máquina; {@code 0} si aún no se ha seleccionado
     */
    public int getCantidadJugadoresMaquina() {
        return cantidadJugadoresMaquina;
    }

    /**
     * Establece la cantidad de jugadores máquina para la partida y valida
     * que el valor esté en el rango permitido.
     *
     * @param cantidadJugadoresMaquina número de oponentes máquina a configurar
     * @throws IllegalArgumentException si {@code cantidadJugadoresMaquina} es
     *                                  menor que {@value #MIN_JUGADORES} o
     *                                  mayor que {@value #MAX_JUGADORES}
     */
    public void setCantidadJugadoresMaquina(int cantidadJugadoresMaquina) {
        if (cantidadJugadoresMaquina < MIN_JUGADORES || cantidadJugadoresMaquina > MAX_JUGADORES) {
            throw new IllegalArgumentException(
                "El número de jugadores máquina debe estar entre "
                + MIN_JUGADORES + " y " + MAX_JUGADORES
                + ". Valor recibido: " + cantidadJugadoresMaquina
            );
        }
        this.cantidadJugadoresMaquina = cantidadJugadoresMaquina;
    }

    /**
     * Indica si el usuario ya eligió una cantidad válida de oponentes máquina.
     *
     * @return {@code true} si se ha seleccionado al menos un jugador máquina;
     *         {@code false} en caso contrario
     */
    public boolean isConfigurado() {
        return cantidadJugadoresMaquina >= MIN_JUGADORES;
    }

    /**
     * Retorna una representación legible de la configuración actual.
     *
     * @return cadena con la cantidad de jugadores máquina configurada
     */
    @Override
    public String toString() {
        return "ConfiguracionJuego{cantidadJugadoresMaquina=" + cantidadJugadoresMaquina + "}";
    }
}
