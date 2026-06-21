package com.cincuentazo.cincuentazo.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para {@link CincuentazoGame}.
 *
 * <p>Verifica que el reparto inicial sea correcto: cada jugador recibe
 * exactamente 4 cartas, el mazo se reduce proporcionalmente y la suma
 * inicial corresponde al impacto de la carta de la mesa.</p>
 */
@DisplayName("CincuentazoGame — pruebas de inicialización y estado")
class CincuentazoGameTest {

    // ── helpers ──────────────────────────────────────────────────────────

    /** Número de cartas repartidas a cada jugador al iniciar. */
    private static final int CARTAS_POR_JUGADOR = CincuentazoGame.CARTAS_POR_JUGADOR;

    /**
     * Cartas retiradas del mazo en la fase de inicio:
     * 4 × (1 humano + N máquinas) + 1 carta de la mesa.
     */
    private static int cartasEsperadasRestantes(int numMaquinas) {
        int totalJugadores = 1 + numMaquinas;
        return Mazo.CARTAS_TOTALES - (CARTAS_POR_JUGADOR * totalJugadores) - 1;
    }

    private static CincuentazoGame crearJuego(int numMaquinas) {
        return new CincuentazoGame(new ConfiguracionJuego(numMaquinas));
    }

    // =========================================================
    // Mano del jugador humano
    // =========================================================

    @ParameterizedTest(name = "Con {0} máquina(s): humano recibe 4 cartas")
    @ValueSource(ints = {1, 2, 3})
    @DisplayName("El jugador humano debe iniciar con exactamente 4 cartas")
    void testHumano_iniciaConCuatroCartas(int numMaquinas) {
        CincuentazoGame juego = crearJuego(numMaquinas);
        assertEquals(CARTAS_POR_JUGADOR, juego.getJugadorHumano().getCantidadCartas(),
                "El humano debe tener " + CARTAS_POR_JUGADOR + " cartas");
    }

    // =========================================================
    // Mano de las máquinas
    // =========================================================

    @ParameterizedTest(name = "Con {0} máquina(s): cada máquina recibe 4 cartas")
    @ValueSource(ints = {1, 2, 3})
    @DisplayName("Cada máquina debe iniciar con exactamente 4 cartas")
    void testMaquinas_cadaUnaConCuatroCartas(int numMaquinas) {
        CincuentazoGame juego = crearJuego(numMaquinas);
        for (Maquina maquina : juego.getMaquinas()) {
            assertEquals(CARTAS_POR_JUGADOR, maquina.getCantidadCartas(),
                    maquina.getNombre() + " debe tener " + CARTAS_POR_JUGADOR + " cartas");
        }
    }

    @ParameterizedTest(name = "Con {0} máquina(s): número correcto de oponentes")
    @ValueSource(ints = {1, 2, 3})
    @DisplayName("La partida debe contener exactamente el número de máquinas configurado")
    void testMaquinas_cantidadCorrecta(int numMaquinas) {
        CincuentazoGame juego = crearJuego(numMaquinas);
        assertEquals(numMaquinas, juego.getMaquinas().size());
    }

    @Test
    @DisplayName("Las cartas de las máquinas deben estar boca abajo al iniciar")
    void testMaquinas_cartasBocaAbajo() {
        CincuentazoGame juego = crearJuego(2);
        for (Maquina maquina : juego.getMaquinas()) {
            for (Carta carta : maquina.getMano()) {
                assertTrue(carta.isBocaAbajo(),
                        "Carta de máquina debe estar boca abajo: " + carta);
            }
        }
    }

    @Test
    @DisplayName("Las cartas del humano deben estar boca arriba al iniciar")
    void testHumano_cartasBocaArriba() {
        CincuentazoGame juego = crearJuego(1);
        for (Carta carta : juego.getJugadorHumano().getMano()) {
            assertFalse(carta.isBocaAbajo(),
                    "Carta del humano debe estar boca arriba: " + carta);
        }
    }

    // =========================================================
    // Tamaño del mazo tras el reparto
    // =========================================================

    @ParameterizedTest(name = "Con {0} máquina(s): mazo reduce a {0}·4+5 cartas menos")
    @ValueSource(ints = {1, 2, 3})
    @DisplayName("El mazo debe reducirse proporcionalmente según el número de jugadores")
    void testMazo_tamañoPostReparto(int numMaquinas) {
        CincuentazoGame juego = crearJuego(numMaquinas);
        int esperado = cartasEsperadasRestantes(numMaquinas);
        assertEquals(esperado, juego.getMazo().getTamanio(),
                "Con " + numMaquinas + " máquina(s) deben quedar " + esperado + " cartas");
    }

    // =========================================================
    // Carta y suma de la mesa
    // =========================================================

    @Test
    @DisplayName("La carta de la mesa no debe ser nula al iniciar la partida")
    void testMesa_cartaNoNula() {
        CincuentazoGame juego = crearJuego(1);
        assertNotNull(juego.getCartaMesa(), "Debe existir una carta inicial en la mesa");
    }

    @ParameterizedTest(name = "Con {0} máquina(s): suma = impacto carta mesa")
    @ValueSource(ints = {1, 2, 3})
    @DisplayName("La suma inicial debe coincidir con el impacto de la carta de la mesa")
    void testSuma_igualAlImpactoCartaMesa(int numMaquinas) {
        CincuentazoGame juego = crearJuego(numMaquinas);
        int impactoEsperado = juego.getCartaMesa().getImpactoSuma();
        assertEquals(impactoEsperado, juego.getSumaActual(),
                "sumaActual debe ser igual a getImpactoSuma() de la carta mesa");
    }

    @Test
    @DisplayName("La carta de la mesa debe estar boca arriba")
    void testMesa_cartaBocaArriba() {
        CincuentazoGame juego = crearJuego(1);
        assertFalse(juego.getCartaMesa().isBocaAbajo(),
                "La carta de la mesa debe estar boca arriba");
    }

    // =========================================================
    // Configuración inválida
    // =========================================================

    @Test
    @DisplayName("Configuración sin selección lanza IllegalArgumentException")
    void testConstructor_configuracionSinSeleccion_lanzaExcepcion() {
        ConfiguracionJuego cfg = new ConfiguracionJuego(); // cantidad = 0
        assertThrows(IllegalArgumentException.class,
                () -> new CincuentazoGame(cfg),
                "Una configuración con 0 máquinas debe ser rechazada");
    }

    // =========================================================
    // jugarCarta (contrato básico de HU-3)
    // =========================================================

    @Test
    @DisplayName("jugarCarta actualiza la carta de la mesa y la suma acumulada")
    void testJugarCarta_actualizaMesaYSuma() {
        CincuentazoGame juego = crearJuego(1);
        int sumaAntes = juego.getSumaActual();

        Carta cartaJugada = new Carta(Rango.CINCO, Palo.CORAZONES);
        juego.jugarCarta(cartaJugada);

        assertEquals(cartaJugada, juego.getCartaMesa(),
                "La carta mesa debe actualizarse");
        assertEquals(sumaAntes + 5, juego.getSumaActual(),
                "La suma debe incrementarse en el impacto de la carta jugada");
    }
}
