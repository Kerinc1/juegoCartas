package com.cincuentazo.cincuentazo.model;

/**
 * Excepción de negocio <em>no marcada</em> (unchecked) que se lanza cuando no
 * es posible robar ninguna carta porque tanto el mazo como la pila de descarte
 * (cartas jugadas a la mesa) se han agotado completamente.
 *
 * <p>Este es un caso extremo de la regla de reciclaje definida en HU-4:
 * si el mazo se vacía, el sistema intenta reciclar las cartas acumuladas en la
 * mesa; si tampoco las hay disponibles, la partida ya no puede continuar y se
 * lanza esta excepción para evitar un bloqueo silencioso.</p>
 *
 * <p>Al extender {@link RuntimeException} no obliga a declarar {@code throws}
 * en los métodos llamadores; el controlador la captura para mostrar un mensaje
 * de error al usuario.</p>
 *
 * @see CincuentazoGame#robarCartaParaJugador(Jugador)
 */
public class MazoAgotadoException extends RuntimeException {

    /**
     * Construye la excepción con el mensaje descriptivo del estado de error.
     *
     * @param message descripción del motivo por el que no fue posible robar
     *                ninguna carta (ej. ambas fuentes agotadas)
     */
    public MazoAgotadoException(String message) {
        super(message);
    }
}
