package com.cincuentazo.cincuentazo.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para {@link ConfiguracionJuego}.
 *
 * <p>Verifica que el modelo almacene correctamente el número de jugadores
 * máquina seleccionado y que rechace valores fuera del rango permitido
 * ({@value ConfiguracionJuego#MIN_JUGADORES}–{@value ConfiguracionJuego#MAX_JUGADORES}).</p>
 */
@DisplayName("ConfiguracionJuego — pruebas de validación y almacenamiento")
class ConfiguracionJuegoTest {

    /** Instancia fresca creada antes de cada prueba. */
    private ConfiguracionJuego configuracion;

    /**
     * Crea una nueva instancia de {@link ConfiguracionJuego} antes de cada test.
     */
    @BeforeEach
    void setUp() {
        configuracion = new ConfiguracionJuego();
    }

    // =========================================================
    // Estado inicial
    // =========================================================

    @Test
    @DisplayName("Constructor por defecto: el valor inicial debe ser 0 (sin configurar)")
    void testConstructorPorDefecto_valorInicialCero() {
        assertEquals(0, configuracion.getCantidadJugadoresMaquina(),
                "El valor inicial sin selección debe ser 0");
    }

    @Test
    @DisplayName("Constructor por defecto: isConfigurado() debe retornar false")
    void testConstructorPorDefecto_noEstaConfigurado() {
        assertFalse(configuracion.isConfigurado(),
                "Una instancia recién creada no debe estar configurada");
    }

    // =========================================================
    // Valores válidos (1, 2, 3)
    // =========================================================

    @Test
    @DisplayName("1 jugador máquina: debe almacenarse y marcarse como configurado")
    void testSetCantidad_unJugador_almacenaCorrectamente() {
        configuracion.setCantidadJugadoresMaquina(1);

        assertEquals(1, configuracion.getCantidadJugadoresMaquina());
        assertTrue(configuracion.isConfigurado());
    }

    @Test
    @DisplayName("2 jugadores máquina: debe almacenarse correctamente")
    void testSetCantidad_dosJugadores_almacenaCorrectamente() {
        configuracion.setCantidadJugadoresMaquina(2);

        assertEquals(2, configuracion.getCantidadJugadoresMaquina());
        assertTrue(configuracion.isConfigurado());
    }

    @Test
    @DisplayName("3 jugadores máquina: debe almacenarse correctamente")
    void testSetCantidad_tresJugadores_almacenaCorrectamente() {
        configuracion.setCantidadJugadoresMaquina(3);

        assertEquals(3, configuracion.getCantidadJugadoresMaquina());
        assertTrue(configuracion.isConfigurado());
    }

    @Test
    @DisplayName("Constructor con valor 2: debe configurarse directamente")
    void testConstructorConValor_dosJugadores_configuraCorrectamente() {
        ConfiguracionJuego cfg = new ConfiguracionJuego(2);

        assertEquals(2, cfg.getCantidadJugadoresMaquina());
        assertTrue(cfg.isConfigurado());
    }

    @Test
    @DisplayName("La configuración puede cambiarse de 1 a 3 sin errores")
    void testSetCantidad_cambioDeValorValido_funciona() {
        configuracion.setCantidadJugadoresMaquina(1);
        configuracion.setCantidadJugadoresMaquina(3);

        assertEquals(3, configuracion.getCantidadJugadoresMaquina());
    }

    // =========================================================
    // Valores inválidos — menores al mínimo
    // =========================================================

    @Test
    @DisplayName("Valor 0: debe lanzar IllegalArgumentException")
    void testSetCantidad_cero_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> configuracion.setCantidadJugadoresMaquina(0),
                "El valor 0 debe ser rechazado");
    }

    @Test
    @DisplayName("Valor -1: debe lanzar IllegalArgumentException")
    void testSetCantidad_negativo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> configuracion.setCantidadJugadoresMaquina(-1),
                "Los valores negativos deben ser rechazados");
    }

    @Test
    @DisplayName("Valor muy negativo: debe lanzar IllegalArgumentException")
    void testSetCantidad_muyNegativo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> configuracion.setCantidadJugadoresMaquina(-100));
    }

    // =========================================================
    // Valores inválidos — mayores al máximo
    // =========================================================

    @Test
    @DisplayName("Valor 4: debe lanzar IllegalArgumentException")
    void testSetCantidad_cuatro_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> configuracion.setCantidadJugadoresMaquina(4),
                "El valor 4 supera el máximo permitido");
    }

    @Test
    @DisplayName("Valor 100: debe lanzar IllegalArgumentException")
    void testSetCantidad_cien_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> configuracion.setCantidadJugadoresMaquina(100));
    }

    // =========================================================
    // Mensaje de la excepción
    // =========================================================

    @Test
    @DisplayName("La excepción debe incluir el valor inválido recibido en su mensaje")
    void testExcepcion_mensajeContieneValorInvalido() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> configuracion.setCantidadJugadoresMaquina(5));

        assertTrue(ex.getMessage().contains("5"),
                "El mensaje de la excepción debe mencionar el valor recibido");
    }

    @Test
    @DisplayName("La excepción del constructor también debe contener el valor inválido")
    void testConstructorConValorInvalido_mensajeContieneCero() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new ConfiguracionJuego(0));

        assertTrue(ex.getMessage().contains("0"));
    }

    // =========================================================
    // Invariante: el estado no cambia tras una excepción
    // =========================================================

    @Test
    @DisplayName("Tras una excepción, el valor previo válido debe conservarse")
    void testSetCantidad_excepcionNoAlteraEstadoPrevio() {
        configuracion.setCantidadJugadoresMaquina(2);

        assertThrows(IllegalArgumentException.class,
                () -> configuracion.setCantidadJugadoresMaquina(5));

        assertEquals(2, configuracion.getCantidadJugadoresMaquina(),
                "El valor válido previo no debe cambiar después de una excepción");
    }

    // =========================================================
    // toString
    // =========================================================

    @Test
    @DisplayName("toString() debe contener la cantidad de jugadores configurada")
    void testToString_contieneValor() {
        configuracion.setCantidadJugadoresMaquina(3);

        assertTrue(configuracion.toString().contains("3"),
                "toString() debe reflejar el valor actual");
    }
}
