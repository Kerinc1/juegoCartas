package com.cincuentazo.cincuentazo.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para {@link CincuentazoGame}.
 *
 * <p>Cubre dos grupos principales de comportamiento:</p>
 * <ol>
 *   <li><strong>HU-2 — Inicialización</strong>: reparto de cartas, tamaño del
 *       mazo tras el reparto, carta y suma iniciales de la mesa.</li>
 *   <li><strong>HU-3 — Jugar una carta</strong>: lógica dinámica del As,
 *       resta de figuras, excepción {@link MovimientoInvalidoException},
 *       validación de jugabilidad ({@link CincuentazoGame#esJugable(Carta)})
 *       y ciclo de turnos.</li>
 * </ol>
 *
 * <p>Los tests de HU-3 utilizan {@link CincuentazoGame#setSumaActual(int)}
 * (método package-private) para situar el juego en un estado conocido sin
 * necesidad de jugar una secuencia real de cartas previas.</p>
 */
@DisplayName("CincuentazoGame — inicialización (HU-2) y reglas de juego (HU-3)")
class CincuentazoGameTest {

    // ── Helpers compartidos ───────────────────────────────────────────────

    private static final int CARTAS_POR_JUGADOR = CincuentazoGame.CARTAS_POR_JUGADOR;

    /**
     * Cartas que deben quedar en el mazo tras el reparto inicial:
     * 52 − (4 × (1 humano + N máquinas)) − 1 carta de la mesa.
     */
    private static int cartasEsperadasRestantes(int numMaquinas) {
        return Mazo.CARTAS_TOTALES - (CARTAS_POR_JUGADOR * (1 + numMaquinas)) - 1;
    }

    private static CincuentazoGame crearJuego(int numMaquinas) {
        return new CincuentazoGame(new ConfiguracionJuego(numMaquinas));
    }

    // ═════════════════════════════════════════════════════════════════════
    // HU-2 — Inicialización de la partida
    // ═════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("HU-2 · Inicialización de la partida")
    class InicializacionTests {

        // ── Mano del jugador humano ───────────────────────────────────────

        @ParameterizedTest(name = "Con {0} máquina(s): humano recibe 4 cartas")
        @ValueSource(ints = {1, 2, 3})
        @DisplayName("El jugador humano debe iniciar con exactamente 4 cartas")
        void testHumano_iniciaConCuatroCartas(int numMaquinas) {
            CincuentazoGame juego = crearJuego(numMaquinas);
            assertEquals(CARTAS_POR_JUGADOR, juego.getJugadorHumano().getCantidadCartas(),
                    "El humano debe tener " + CARTAS_POR_JUGADOR + " cartas");
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

        // ── Mano de las máquinas ──────────────────────────────────────────

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

        // ── Tamaño del mazo ───────────────────────────────────────────────

        @ParameterizedTest(name = "Con {0} máquina(s): mazo se reduce proporcionalmente")
        @ValueSource(ints = {1, 2, 3})
        @DisplayName("El mazo debe reducirse en 4×(1+N)+1 cartas tras el reparto")
        void testMazo_tamañoPostReparto(int numMaquinas) {
            CincuentazoGame juego = crearJuego(numMaquinas);
            int esperado = cartasEsperadasRestantes(numMaquinas);
            assertEquals(esperado, juego.getMazo().getTamanio(),
                    "Con " + numMaquinas + " máquina(s) deben quedar " + esperado + " cartas");
        }

        // ── Carta y suma de la mesa ───────────────────────────────────────

        @Test
        @DisplayName("La carta de la mesa no debe ser nula al iniciar la partida")
        void testMesa_cartaNoNula() {
            assertNotNull(crearJuego(1).getCartaMesa(),
                    "Debe existir una carta inicial en la mesa");
        }

        @ParameterizedTest(name = "Con {0} máquina(s): sumaActual = impacto carta mesa")
        @ValueSource(ints = {1, 2, 3})
        @DisplayName("La suma inicial debe coincidir con el impacto base de la carta de la mesa")
        void testSuma_igualAlImpactoCartaMesa(int numMaquinas) {
            CincuentazoGame juego = crearJuego(numMaquinas);
            int impactoEsperado = juego.getCartaMesa().getImpactoSuma();
            assertEquals(impactoEsperado, juego.getSumaActual(),
                    "sumaActual debe ser igual a getImpactoSuma() de la carta mesa");
        }

        @Test
        @DisplayName("La carta de la mesa debe estar boca arriba")
        void testMesa_cartaBocaArriba() {
            assertFalse(crearJuego(1).getCartaMesa().isBocaAbajo(),
                    "La carta de la mesa debe estar boca arriba");
        }

        // ── Configuración inválida ────────────────────────────────────────

        @Test
        @DisplayName("Configuración sin selección (0 máquinas) lanza IllegalArgumentException")
        void testConstructor_configuracionSinSeleccion_lanzaExcepcion() {
            ConfiguracionJuego cfg = new ConfiguracionJuego(); // cantidad = 0
            assertThrows(IllegalArgumentException.class,
                    () -> new CincuentazoGame(cfg),
                    "Una configuración con 0 máquinas debe ser rechazada");
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    // HU-3 — Reglas de juego: jugarCarta, As, Figuras, excepción, turnos
    // ═════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("HU-3 · Reglas de juego — jugarCarta")
    class ReglasDejuegoTests {

        /** Juego fresco con 1 máquina; sumaActual se fuerza en cada test. */
        private CincuentazoGame juego;
        private Humano humano;

        @BeforeEach
        void setUp() {
            juego  = crearJuego(1);
            humano = juego.getJugadorHumano();
        }

        // ── Jugada básica ─────────────────────────────────────────────────

        @Test
        @DisplayName("jugarCarta actualiza la carta de la mesa y la suma acumulada")
        void testJugarCarta_actualizaMesaYSuma() {
            juego.setSumaActual(10); // estado controlado: suma = 10

            Carta cinco = new Carta(Rango.CINCO, Palo.CORAZONES); // impacto = +5
            humano.recibirCarta(cinco);
            juego.jugarCarta(humano, cinco);

            assertAll(
                () -> assertEquals(cinco, juego.getCartaMesa(),
                        "La carta de la mesa debe actualizarse"),
                () -> assertEquals(15, juego.getSumaActual(),
                        "La suma debe pasar de 10 a 15 (10 + 5)")
            );
        }

        @Test
        @DisplayName("jugarCarta retira la carta de la mano del jugador")
        void testJugarCarta_retiraCartaDeMano() {
            juego.setSumaActual(0);
            int manoAntes = humano.getCantidadCartas();

            Carta dos = new Carta(Rango.DOS, Palo.PICAS);
            humano.recibirCarta(dos);
            juego.jugarCarta(humano, dos);

            assertEquals(manoAntes, humano.getCantidadCartas(),
                    "La mano debe volver a la cantidad previa (la carta extra fue retirada)");
        }

        @Test
        @DisplayName("La carta jugada queda boca arriba en la mesa")
        void testJugarCarta_cartaMesaBocaArriba() {
            juego.setSumaActual(5);

            Carta tres = new Carta(Rango.TRES, Palo.TREBOLES);
            humano.recibirCarta(tres);
            juego.jugarCarta(humano, tres);

            assertFalse(juego.getCartaMesa().isBocaAbajo(),
                    "La carta en la mesa siempre debe estar boca arriba");
        }

        // ── MovimientoInvalidoException ───────────────────────────────────

        @Test
        @DisplayName("Jugar una carta que excede 50 pts lanza MovimientoInvalidoException")
        void testJugarCarta_excede50_lanzaMovimientoInvalidoException() {
            juego.setSumaActual(49); // suma = 49; DOS (+2) → 51, inválido

            Carta dos = new Carta(Rango.DOS, Palo.CORAZONES);
            humano.recibirCarta(dos);

            assertThrows(MovimientoInvalidoException.class,
                    () -> juego.jugarCarta(humano, dos),
                    "Jugar DOS con suma 49 debe lanzar MovimientoInvalidoException (49+2=51>50)");
        }

        @Test
        @DisplayName("El mensaje de MovimientoInvalidoException indica la suma resultante")
        void testMovimientoInvalidoException_mensajeContieneInformacion() {
            juego.setSumaActual(48);

            Carta cuatro = new Carta(Rango.CUATRO, Palo.DIAMANTES); // 48+4=52 > 50
            humano.recibirCarta(cuatro);

            MovimientoInvalidoException ex = assertThrows(
                    MovimientoInvalidoException.class,
                    () -> juego.jugarCarta(humano, cuatro));

            assertAll(
                () -> assertTrue(ex.getMessage().contains("52") || ex.getMessage().contains("50"),
                        "El mensaje debe mencionar la suma resultante o el límite"),
                () -> assertNotNull(ex.getMessage(),
                        "El mensaje no debe ser nulo")
            );
        }

        @Test
        @DisplayName("Tras una excepción, el estado del juego no debe cambiar")
        void testJugarCarta_excepcion_noAlteraEstado() {
            juego.setSumaActual(49);
            Carta dos = new Carta(Rango.DOS, Palo.PICAS);
            humano.recibirCarta(dos);
            int cantidadManoAntes = humano.getCantidadCartas();
            Carta mesaAntes = juego.getCartaMesa();

            assertThrows(MovimientoInvalidoException.class,
                    () -> juego.jugarCarta(humano, dos));

            assertAll(
                () -> assertEquals(49, juego.getSumaActual(),
                        "La suma no debe cambiar tras una excepción"),
                () -> assertEquals(mesaAntes, juego.getCartaMesa(),
                        "La carta de la mesa no debe cambiar tras una excepción"),
                () -> assertEquals(cantidadManoAntes, humano.getCantidadCartas(),
                        "La carta no debe retirarse de la mano si la jugada falla")
            );
        }

        // ── Lógica del As ─────────────────────────────────────────────────

        @Test
        @DisplayName("As vale 10 cuando sumaActual + 10 ≤ 50 (mesa en 20 → pasa a 30)")
        void testAs_sumaActualBaja_valeAlto() {
            juego.setSumaActual(20); // 20 + 10 = 30 ≤ 50 → As debe valer 10

            Carta as = new Carta(Rango.AS, Palo.CORAZONES);
            humano.recibirCarta(as);
            juego.jugarCarta(humano, as);

            assertEquals(30, juego.getSumaActual(),
                    "Con mesa en 20, el As debe sumar 10 (20 + 10 = 30)");
        }

        @Test
        @DisplayName("As vale 1 cuando sumaActual + 10 > 50 (mesa en 45 → pasa a 46)")
        void testAs_sumaActualAlta_valeBajo() {
            juego.setSumaActual(45); // 45 + 10 = 55 > 50 → As debe valer 1

            Carta as = new Carta(Rango.AS, Palo.PICAS);
            humano.recibirCarta(as);
            juego.jugarCarta(humano, as);

            assertEquals(46, juego.getSumaActual(),
                    "Con mesa en 45, el As debe sumar 1 (45 + 1 = 46)");
        }

        @Test
        @DisplayName("As vale 10 exactamente en el límite: sumaActual 40 → pasa a 50")
        void testAs_sumaEnLimiteExacto_valeAlto() {
            juego.setSumaActual(40); // 40 + 10 = 50 = límite → As debe valer 10

            Carta as = new Carta(Rango.AS, Palo.DIAMANTES);
            humano.recibirCarta(as);
            juego.jugarCarta(humano, as);

            assertEquals(50, juego.getSumaActual(),
                    "Con mesa en 40, el As debe sumar 10 y alcanzar exactamente el límite (50)");
        }

        @Test
        @DisplayName("As con sumaActual 41 debe valer 1 (41 + 10 = 51 > 50)")
        void testAs_sumaActual41_valeUno() {
            juego.setSumaActual(41); // 41 + 10 = 51 > 50 → As debe valer 1

            Carta as = new Carta(Rango.AS, Palo.TREBOLES);
            humano.recibirCarta(as);
            juego.jugarCarta(humano, as);

            assertEquals(42, juego.getSumaActual(),
                    "Con mesa en 41, el As debe sumar 1 (41 + 1 = 42)");
        }

        // ── calcularImpacto del As (método puro) ──────────────────────────

        @Test
        @DisplayName("calcularImpacto: As devuelve 10 cuando sumaActual = 20")
        void testCalcularImpacto_as_sumaActualBaja_retorna10() {
            juego.setSumaActual(20);
            Carta as = new Carta(Rango.AS, Palo.CORAZONES);
            assertEquals(10, juego.calcularImpacto(as),
                    "calcularImpacto debe retornar 10 para el As con suma 20");
        }

        @Test
        @DisplayName("calcularImpacto: As devuelve 1 cuando sumaActual = 45")
        void testCalcularImpacto_as_sumaActualAlta_retorna1() {
            juego.setSumaActual(45);
            Carta as = new Carta(Rango.AS, Palo.PICAS);
            assertEquals(1, juego.calcularImpacto(as),
                    "calcularImpacto debe retornar 1 para el As con suma 45");
        }

        // ── Figuras (J, Q, K) restan 10 ──────────────────────────────────

        @Test
        @DisplayName("El Rey (K) resta exactamente 10 a la suma acumulada (30 → 20)")
        void testFigura_Rey_resta10() {
            juego.setSumaActual(30);

            Carta rey = new Carta(Rango.REY, Palo.PICAS);
            humano.recibirCarta(rey);
            juego.jugarCarta(humano, rey);

            assertEquals(20, juego.getSumaActual(),
                    "El Rey debe restar 10 a la suma (30 - 10 = 20)");
        }

        @Test
        @DisplayName("La Reina (Q) resta exactamente 10 a la suma acumulada (25 → 15)")
        void testFigura_Reina_resta10() {
            juego.setSumaActual(25);

            Carta reina = new Carta(Rango.REINA, Palo.CORAZONES);
            humano.recibirCarta(reina);
            juego.jugarCarta(humano, reina);

            assertEquals(15, juego.getSumaActual(),
                    "La Reina debe restar 10 a la suma (25 - 10 = 15)");
        }

        @Test
        @DisplayName("La Jota (J) resta exactamente 10 a la suma acumulada (18 → 8)")
        void testFigura_Jota_resta10() {
            juego.setSumaActual(18);

            Carta jota = new Carta(Rango.JOTA, Palo.TREBOLES);
            humano.recibirCarta(jota);
            juego.jugarCarta(humano, jota);

            assertEquals(8, juego.getSumaActual(),
                    "La Jota debe restar 10 a la suma (18 - 10 = 8)");
        }

        // ── 9 neutro ──────────────────────────────────────────────────────

        @Test
        @DisplayName("El 9 es neutro: la suma no cambia tras jugarlo")
        void testNueve_esNeutro_sumaNoVaria() {
            juego.setSumaActual(33);

            Carta nueve = new Carta(Rango.NUEVE, Palo.DIAMANTES);
            humano.recibirCarta(nueve);
            juego.jugarCarta(humano, nueve);

            assertEquals(33, juego.getSumaActual(),
                    "El 9 es neutro y no debe modificar la suma (33 + 0 = 33)");
        }

        // ── esJugable ─────────────────────────────────────────────────────

        @Test
        @DisplayName("esJugable retorna false para carta que excedería el límite")
        void testEsJugable_excede50_retornaFalse() {
            juego.setSumaActual(49);
            Carta dos = new Carta(Rango.DOS, Palo.CORAZONES); // 49 + 2 = 51 > 50
            assertFalse(juego.esJugable(dos),
                    "DOS con suma 49 no debe ser jugable (51 > 50)");
        }

        @Test
        @DisplayName("esJugable retorna true para carta que alcanza exactamente el límite")
        void testEsJugable_alcanzaLimiteExacto_retornaTrue() {
            juego.setSumaActual(48);
            Carta dos = new Carta(Rango.DOS, Palo.CORAZONES); // 48 + 2 = 50 = límite
            assertTrue(juego.esJugable(dos),
                    "DOS con suma 48 debe ser jugable (48 + 2 = 50 = límite)");
        }

        @Test
        @DisplayName("Las figuras siempre son jugables cuando la suma >= 0")
        void testEsJugable_figura_siempreJugable() {
            juego.setSumaActual(50);
            Carta rey = new Carta(Rango.REY, Palo.PICAS); // 50 + (-10) = 40 ≤ 50
            assertTrue(juego.esJugable(rey),
                    "Una figura siempre puede jugarse cuando la suma no supera el límite");
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    // HU-3 — Gestión de turnos
    // ═════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("HU-3 · Gestión de turnos")
    class GestionTurnosTests {

        @Test
        @DisplayName("El turno inicial es siempre del jugador humano (índice 0)")
        void testTurnoInicial_esDelHumano() {
            CincuentazoGame juego = crearJuego(1);
            assertAll(
                () -> assertTrue(juego.esTurnoHumano(), "El turno inicial debe ser del humano"),
                () -> assertEquals(0, juego.getTurnoActual(), "El índice inicial debe ser 0")
            );
        }

        @Test
        @DisplayName("avanzarTurno: humano → máquina 1 con 1 máquina")
        void testAvanzarTurno_unaMaquina_humanoPasaAMaquina() {
            CincuentazoGame juego = crearJuego(1);
            juego.avanzarTurno();

            assertFalse(juego.esTurnoHumano(), "Tras avanzar debe ser turno de la máquina");
            assertEquals(1, juego.getTurnoActual());
        }

        @Test
        @DisplayName("avanzarTurno: máquina → humano (ciclo completo con 1 máquina)")
        void testAvanzarTurno_unaMaquina_cicloCorrecto() {
            CincuentazoGame juego = crearJuego(1);
            juego.avanzarTurno(); // humano → maquina1
            juego.avanzarTurno(); // maquina1 → humano

            assertTrue(juego.esTurnoHumano(), "Tras 2 avances con 1 máquina debe volver al humano");
            assertEquals(0, juego.getTurnoActual());
        }

        @Test
        @DisplayName("getMaquinaActual retorna la máquina correcta en turno 1 con 2 máquinas")
        void testGetMaquinaActual_turno1_retornaMaquina1() {
            CincuentazoGame juego = crearJuego(2);
            juego.avanzarTurno(); // turno 1 → Máquina 1

            Maquina maquinaActual = juego.getMaquinaActual();
            assertEquals("Máquina 1", maquinaActual.getNombre());
        }

        @Test
        @DisplayName("getMaquinaActual lanza IllegalStateException cuando es turno del humano")
        void testGetMaquinaActual_turnoHumano_lanzaExcepcion() {
            CincuentazoGame juego = crearJuego(1);
            // turnoActual = 0 (humano)
            assertThrows(IllegalStateException.class,
                    juego::getMaquinaActual,
                    "Invocar getMaquinaActual en turno del humano debe lanzar excepción");
        }

        @Test
        @DisplayName("Ciclo completo de 3 máquinas regresa al humano en el turno 4")
        void testAvanzarTurno_tresMaquinas_cicloCorrecto() {
            CincuentazoGame juego = crearJuego(3);
            for (int i = 0; i < 3; i++) juego.avanzarTurno(); // 3 máquinas
            juego.avanzarTurno(); // vuelve al humano

            assertAll(
                () -> assertTrue(juego.esTurnoHumano(), "Tras 4 avances con 3 máquinas debe volver al humano"),
                () -> assertEquals(0, juego.getTurnoActual())
            );
        }
    }
}
