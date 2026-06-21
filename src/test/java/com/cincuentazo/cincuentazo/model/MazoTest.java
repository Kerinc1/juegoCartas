package com.cincuentazo.cincuentazo.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para {@link Mazo}.
 *
 * <p>Cubre la creación canónica, unicidad de cartas, reducción del tamaño
 * al robar y el comportamiento de borde cuando el mazo está vacío.</p>
 */
@DisplayName("Mazo — pruebas de integridad y operaciones")
class MazoTest {

    /** Instancia fresca para cada prueba. */
    private Mazo mazo;

    @BeforeEach
    void setUp() {
        mazo = new Mazo();
    }

    // =========================================================
    // Creación
    // =========================================================

    @Test
    @DisplayName("Un mazo recién creado debe tener exactamente 52 cartas")
    void testCreacion_tieneCincuentaYDosCartas() {
        assertEquals(Mazo.CARTAS_TOTALES, mazo.getTamanio(),
                "La baraja completa debe contener 52 cartas");
    }

    @Test
    @DisplayName("Un mazo recién creado no debe estar vacío")
    void testCreacion_noEstaVacio() {
        assertFalse(mazo.estaVacio());
    }

    // =========================================================
    // Unicidad (sin duplicados)
    // =========================================================

    @Test
    @DisplayName("Las 52 cartas del mazo deben ser únicas (sin repetición)")
    void testUnicidad_sinDuplicados() {
        Set<String> vistas = new HashSet<>();
        int totalRobadas = 0;

        while (!mazo.estaVacio()) {
            Carta c = mazo.robarCarta();
            String clave = c.getRango().name() + "_" + c.getPalo().name();
            assertTrue(vistas.add(clave),
                    "Carta duplicada detectada: " + clave);
            totalRobadas++;
        }

        assertEquals(Mazo.CARTAS_TOTALES, totalRobadas,
                "Deben haberse robado exactamente 52 cartas únicas");
    }

    @Test
    @DisplayName("El mazo cubre todas las combinaciones de Rango × Palo")
    void testCobertura_todasLasCombinaciones() {
        int combinacionesEsperadas = Rango.values().length * Palo.values().length;
        assertEquals(combinacionesEsperadas, mazo.getTamanio(),
                "Deben existir " + combinacionesEsperadas + " combinaciones");
    }

    // =========================================================
    // robarCarta — reducción del tamaño
    // =========================================================

    @Test
    @DisplayName("Robar una carta reduce el tamaño del mazo en exactamente uno")
    void testRobar_reduceEnUno() {
        mazo.robarCarta();
        assertEquals(Mazo.CARTAS_TOTALES - 1, mazo.getTamanio());
    }

    @Test
    @DisplayName("Robar N cartas reduce el tamaño en exactamente N")
    void testRobar_reduceEnN() {
        int n = 10;
        for (int i = 0; i < n; i++) {
            mazo.robarCarta();
        }
        assertEquals(Mazo.CARTAS_TOTALES - n, mazo.getTamanio());
    }

    @Test
    @DisplayName("Robar todas las cartas agota el mazo (estaVacio = true)")
    void testRobar_agotaElMazo() {
        while (!mazo.estaVacio()) {
            mazo.robarCarta();
        }
        assertTrue(mazo.estaVacio());
        assertEquals(0, mazo.getTamanio());
    }

    // =========================================================
    // robarCarta — borde: mazo vacío
    // =========================================================

    @Test
    @DisplayName("Robar de un mazo vacío lanza IllegalStateException")
    void testRobar_mazoVacio_lanzaExcepcion() {
        while (!mazo.estaVacio()) {
            mazo.robarCarta();
        }
        assertThrows(IllegalStateException.class,
                () -> mazo.robarCarta(),
                "Robar de un mazo vacío debe lanzar IllegalStateException");
    }

    // =========================================================
    // barajar — integridad
    // =========================================================

    @Test
    @DisplayName("Barajar no altera el número de cartas del mazo")
    void testBarajar_mantieneTamanio() {
        mazo.barajar();
        assertEquals(Mazo.CARTAS_TOTALES, mazo.getTamanio());
    }

    @Test
    @DisplayName("Barajar y robar todas las cartas sigue produciendo exactamente 52 únicas")
    void testBarajar_unicidadPreservada() {
        mazo.barajar();

        Set<String> vistas = new HashSet<>();
        while (!mazo.estaVacio()) {
            Carta c = mazo.robarCarta();
            vistas.add(c.getRango().name() + "_" + c.getPalo().name());
        }

        assertEquals(Mazo.CARTAS_TOTALES, vistas.size(),
                "Después de barajar deben seguir existiendo 52 cartas únicas");
    }
}
