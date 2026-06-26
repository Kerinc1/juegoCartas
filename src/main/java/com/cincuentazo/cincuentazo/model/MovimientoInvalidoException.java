package com.cincuentazo.cincuentazo.model;

/**
 * Excepción de negocio <em>no marcada</em> (unchecked) que se lanza cuando un
 * jugador intenta colocar en la mesa una carta cuyo impacto haría que la suma
 * acumulada supere el límite de {@value CincuentazoGame#LIMITE_SUMA} puntos
 * establecido por las reglas del Cincuentazo.
 *
 * <p>Al extender {@link RuntimeException} no obliga a los métodos llamadores
 * a declarar {@code throws}, pero el controlador ({@code JuegoController})
 * la captura explícitamente para mostrar retroalimentación visual al usuario.</p>
 *
 * <p><strong>Ejemplo de lanzamiento:</strong></p>
 * <pre>
 *   // sumaActual = 48, carta = DOS (impacto +2) → 50 total, válido
 *   // sumaActual = 49, carta = DOS (impacto +2) → 51 total, INVÁLIDO
 *   if (!esJugable(carta)) throw new MovimientoInvalidoException("...");
 * </pre>
 *
 * @see CincuentazoGame#jugarCarta(Jugador, Carta)
 * @see CincuentazoGame#esJugable(Carta)
 */
public class MovimientoInvalidoException extends RuntimeException {

    /**
     * Construye la excepción con el mensaje descriptivo del movimiento rechazado.
     *
     * @param message descripción del motivo del rechazo; debe incluir la carta
     *                intentada y la suma resultante que se habría alcanzado
     */
    public MovimientoInvalidoException(String message) {
        super(message);
    }
}
