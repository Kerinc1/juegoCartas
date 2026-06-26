package com.cincuentazo.cincuentazo.controller;

import com.cincuentazo.cincuentazo.model.Carta;
import com.cincuentazo.cincuentazo.model.CincuentazoGame;
import com.cincuentazo.cincuentazo.model.ConfiguracionJuego;
import com.cincuentazo.cincuentazo.model.MazoAgotadoException;
import com.cincuentazo.cincuentazo.model.Maquina;
import com.cincuentazo.cincuentazo.model.MovimientoInvalidoException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Controlador del tablero de juego — HU-4: Tomar una carta del mazo.
 *
 * <p>Extiende la funcionalidad de HU-3 cerrando el ciclo completo de un turno:
 * <em>jugar una carta → robar del mazo → pasar el turno</em>.</p>
 *
 * <p><strong>Flujo del turno humano (HU-4):</strong></p>
 * <ol>
 *   <li>Click en carta → {@link #manejarClickCarta(Carta)} valida y juega.</li>
 *   <li>Actualización inmediata de mesa y suma.</li>
 *   <li>Robo automático del mazo: si el mazo estaba vacío se muestra el aviso
 *       de reciclaje durante 1,2 s antes de continuar.</li>
 *   <li>Re-render de la mano del humano (4 cartas) y del contador del mazo.</li>
 *   <li>Avance de turno → turno de la máquina.</li>
 * </ol>
 *
 * <p><strong>Flujo del turno máquina (HU-4):</strong></p>
 * <ol>
 *   <li>Task 1 (2–4 s, hilo daemon): selección y jugada de la carta.</li>
 *   <li>{@link Platform#runLater}: actualización de mesa, suma y panel de la máquina.</li>
 *   <li>Task 2 (2–4 s, hilo daemon): simulación del tiempo de robo.</li>
 *   <li>{@link Platform#runLater}: robo del mazo, dorso añadido al panel,
 *       contador actualizado; si hubo reciclaje se muestra aviso 1,5 s.</li>
 *   <li>Avance de turno → siguiente turno.</li>
 * </ol>
 *
 * <p>Toda actualización de la interfaz gráfica se ejecuta obligatoriamente
 * en el <em>JavaFX Application Thread</em> via {@link Platform#runLater}.</p>
 */
public class JuegoController {

    // ── Inyecciones FXML ─────────────────────────────────────────────────

    @FXML private HBox      contenedorMaquinas;
    @FXML private StackPane vistaMazo;
    @FXML private Label     lblCantidadMazo;
    @FXML private StackPane contenedorCartaMesa;
    @FXML private Label     lblSumaActual;
    @FXML private HBox      contenedorManoHumano;
    @FXML private Label     lblEstado;

    // ── Estado del controlador ───────────────────────────────────────────

    private CincuentazoGame juego;

    /**
     * Mapa que asocia cada máquina con el {@link HBox} de dorsos de su panel.
     * Permite agregar o eliminar cartas sin reconstruir el panel completo.
     */
    private final Map<Maquina, HBox> manosPanelesMaquina = new HashMap<>();

    /** Fuente de aleatoriedad para los retardos de simulación (2–4 s). */
    private final Random random = new Random();

    // ── Ciclo de vida FXML ───────────────────────────────────────────────

    @FXML
    public void initialize() {
        // No-op intencional: el estado llega vía inicializarJuego().
    }

    // ── API pública ──────────────────────────────────────────────────────

    /**
     * Inicializa el modelo y renderiza el tablero completo.
     * Debe ser invocado desde {@link InicioController} tras cargar el FXML.
     *
     * @param configuracion configuración con el número de máquinas oponentes
     */
    public void inicializarJuego(ConfiguracionJuego configuracion) {
        this.juego = new CincuentazoGame(configuracion);
        renderizarTablero();
        actualizarEstado("TU TURNO — Selecciona una carta");
    }

    // ── Renderizado del tablero ───────────────────────────────────────────

    private void renderizarTablero() {
        manosPanelesMaquina.clear();
        renderizarMaquinas();
        renderizarMazo();
        renderizarCartaMesa();
        renderizarManoHumano();
        actualizarSuma();
    }

    // ── Zona de máquinas ─────────────────────────────────────────────────

    private void renderizarMaquinas() {
        contenedorMaquinas.getChildren().clear();
        for (Maquina maquina : juego.getMaquinas()) {
            contenedorMaquinas.getChildren().add(crearPanelMaquina(maquina));
        }
    }

    private VBox crearPanelMaquina(Maquina maquina) {
        VBox panel = new VBox(10);
        panel.setAlignment(Pos.CENTER);
        panel.getStyleClass().add("panel-maquina");

        Label nombre = new Label(maquina.getNombre().toUpperCase());
        nombre.getStyleClass().add("lbl-maquina-nombre");

        HBox mano = new HBox(8);
        mano.setAlignment(Pos.CENTER);
        for (int i = 0; i < maquina.getCantidadCartas(); i++) {
            mano.getChildren().add(crearCartaDorso(false));
        }
        manosPanelesMaquina.put(maquina, mano);

        panel.getChildren().addAll(nombre, mano);
        return panel;
    }

    /**
     * Quita el último dorso del panel de una máquina cuando juega una carta.
     *
     * @param maquina máquina que acaba de jugar
     */
    private void quitarDorsoAMaquina(Maquina maquina) {
        HBox mano = manosPanelesMaquina.get(maquina);
        if (mano != null && !mano.getChildren().isEmpty()) {
            mano.getChildren().remove(mano.getChildren().size() - 1);
        }
    }

    /**
     * Añade un dorso al panel de una máquina cuando roba una carta del mazo.
     *
     * @param maquina máquina que acaba de robar
     */
    private void agregarDorsoAMaquina(Maquina maquina) {
        HBox mano = manosPanelesMaquina.get(maquina);
        if (mano != null) {
            mano.getChildren().add(crearCartaDorso(false));
        }
    }

    // ── Mazo ─────────────────────────────────────────────────────────────

    /**
     * Reconstruye la representación visual del mazo (hasta 3 capas) y
     * actualiza de forma reactiva la etiqueta del contador de cartas.
     */
    private void renderizarMazo() {
        vistaMazo.getChildren().clear();
        int tamano = juego.getMazo().getTamanio();
        int capas  = Math.min(tamano, 3);
        for (int i = capas - 1; i >= 0; i--) {
            StackPane capa = new StackPane();
            capa.getStyleClass().add("card-back-mazo");
            capa.setTranslateX(i * 2.5);
            capa.setTranslateY(-i * 2.5);
            vistaMazo.getChildren().add(capa);
        }
        lblCantidadMazo.setText(tamano + " cartas");
    }

    // ── Carta de la mesa ─────────────────────────────────────────────────

    private void renderizarCartaMesa() {
        contenedorCartaMesa.getChildren().clear();
        contenedorCartaMesa.getChildren().add(
            crearCartaFrontal(juego.getCartaMesa(), true));
    }

    // ── Mano del jugador humano ───────────────────────────────────────────

    /**
     * Reconstruye la mano del humano con interactividad: cartas jugables
     * reciben un listener de clic; las no jugables aparecen atenuadas.
     */
    private void renderizarManoHumano() {
        contenedorManoHumano.setDisable(false);
        contenedorManoHumano.getChildren().clear();

        for (Carta carta : juego.getJugadorHumano().getMano()) {
            StackPane nodo = crearCartaFrontal(carta, false);
            if (juego.esJugable(carta)) {
                nodo.setOnMouseClicked(e -> manejarClickCarta(carta));
            } else {
                nodo.getStyleClass().add("carta-no-jugable");
                nodo.setDisable(true);
            }
            contenedorManoHumano.getChildren().add(nodo);
        }
    }

    // ── Manejador de clic — humano (HU-3 + HU-4) ─────────────────────────

    /**
     * Gestiona el clic sobre una carta del humano:
     * <ol>
     *   <li>Juega la carta en el modelo (HU-3).</li>
     *   <li>Roba automáticamente una carta del mazo (HU-4).</li>
     *   <li>Si el robo provocó un reciclaje del descarte, muestra el aviso
     *       durante 1,2 s antes de ceder el turno.</li>
     * </ol>
     *
     * @param carta carta sobre la que el usuario hizo clic
     */
    private void manejarClickCarta(Carta carta) {
        if (!juego.esTurnoHumano()) return;

        // ── Paso 1: jugar la carta ──────────────────────────────────────
        try {
            juego.jugarCarta(juego.getJugadorHumano(), carta);
        } catch (MovimientoInvalidoException e) {
            actualizarEstado("⚠  " + e.getMessage());
            return;
        }

        renderizarCartaMesa();
        actualizarSuma();

        // ── Paso 2: robar del mazo ──────────────────────────────────────
        boolean reciclado;
        try {
            reciclado = juego.robarCartaParaJugador(juego.getJugadorHumano());
        } catch (MazoAgotadoException e) {
            renderizarManoHumano();
            renderizarMazo();
            actualizarEstado("⛔  " + e.getMessage());
            return; // fin de partida gestionado en HU futura
        }

        renderizarManoHumano();
        renderizarMazo();

        // ── Paso 3: avanzar turno con o sin pausa de reciclaje ──────────
        if (reciclado) {
            actualizarEstado("♻  Rebarajando la mesa en el mazo…");
            ejecutarDespuesDePausa(1200, () -> {
                juego.avanzarTurno();
                procesarSiguienteTurno();
            });
        } else {
            juego.avanzarTurno();
            procesarSiguienteTurno();
        }
    }

    // ── Gestión de turnos ─────────────────────────────────────────────────

    /**
     * Evalúa el turno activo:
     * <ul>
     *   <li>Humano → reconstruye la mano con interactividad.</li>
     *   <li>Máquina → deshabilita la mano y lanza la simulación asíncrona.</li>
     * </ul>
     */
    private void procesarSiguienteTurno() {
        if (juego.esTurnoHumano()) {
            renderizarManoHumano();
            actualizarEstado("TU TURNO — Selecciona una carta");
        } else {
            contenedorManoHumano.setDisable(true);
            procesarTurnoMaquina(juego.getMaquinaActual());
        }
    }

    // ── Turno de la máquina — jugar + robar asíncronos ───────────────────

    /**
     * Ejecuta el turno completo de una máquina en dos fases asíncronas:
     *
     * <p><strong>Fase 1 — Jugar (Task en hilo daemon, 2–4 s):</strong>
     * selecciona la primera carta jugable y llama a {@code jugarCarta}.</p>
     *
     * <p><strong>Fase 2 — Robar (Task en hilo daemon, 2–4 s):</strong>
     * llama a {@code robarCartaParaJugador} y actualiza el panel de dorsos
     * y el contador del mazo. Si hubo reciclaje, muestra el aviso 1,5 s
     * antes de avanzar.</p>
     *
     * <p>Toda actualización de la UI se ejecuta dentro de
     * {@link Platform#runLater} para respetar el <em>JavaFX Application Thread</em>.</p>
     *
     * @param maquina máquina cuyo turno se debe simular
     */
    private void procesarTurnoMaquina(Maquina maquina) {
        actualizarEstado("⏳  " + maquina.getNombre() + " pensando…");

        // ─── Fase 1: selección y jugada ───────────────────────────────────
        Task<Carta> tareaJugar = new Task<>() {
            @Override
            protected Carta call() throws InterruptedException {
                Thread.sleep(2000L + random.nextInt(2001)); // 2–4 s
                for (Carta c : maquina.getMano()) {
                    if (juego.esJugable(c)) return c;
                }
                return null; // sin cartas jugables
            }
        };

        tareaJugar.setOnSucceeded(ev1 -> Platform.runLater(() -> {
            Carta cartaElegida = tareaJugar.getValue();

            if (cartaElegida == null) {
                // Sin cartas jugables: sólo avanza el turno, no se roba
                actualizarEstado(maquina.getNombre() + " no tiene cartas jugables — pasa turno.");
                juego.avanzarTurno();
                ejecutarDespuesDePausa(700, JuegoController.this::procesarSiguienteTurno);
                return;
            }

            try {
                juego.jugarCarta(maquina, cartaElegida);
            } catch (MovimientoInvalidoException ex) {
                manejarErrorTurnoMaquina(maquina);
                return;
            }

            quitarDorsoAMaquina(maquina);
            renderizarCartaMesa();
            actualizarSuma();
            actualizarEstado(maquina.getNombre() + " jugó: " + cartaElegida + "  —  Robando carta…");

            // ─── Fase 2: robo del mazo ────────────────────────────────────
            Task<Boolean> tareaRobar = new Task<>() {
                @Override
                protected Boolean call() throws InterruptedException {
                    Thread.sleep(2000L + random.nextInt(2001)); // 2–4 s
                    // El robo se hace aquí para no bloquear el JAT
                    return juego.robarCartaParaJugador(maquina);
                }
            };

            tareaRobar.setOnSucceeded(ev2 -> Platform.runLater(() -> {
                boolean reciclado = tareaRobar.getValue();
                agregarDorsoAMaquina(maquina);
                renderizarMazo();

                juego.avanzarTurno();

                if (reciclado) {
                    actualizarEstado("♻  Rebarajando la mesa en el mazo…");
                    ejecutarDespuesDePausa(1500, JuegoController.this::procesarSiguienteTurno);
                } else {
                    ejecutarDespuesDePausa(700, JuegoController.this::procesarSiguienteTurno);
                }
            }));

            tareaRobar.setOnFailed(ev2 -> Platform.runLater(() -> {
                Throwable causa = tareaRobar.getException();
                if (causa instanceof MazoAgotadoException) {
                    actualizarEstado("⛔  " + causa.getMessage());
                } else {
                    manejarErrorTurnoMaquina(maquina);
                }
            }));

            iniciarHiloDaemon(tareaRobar);
        }));

        tareaJugar.setOnFailed(ev1 -> Platform.runLater(() ->
            manejarErrorTurnoMaquina(maquina)));

        iniciarHiloDaemon(tareaJugar);
    }

    // ── Helpers de concurrencia ───────────────────────────────────────────

    /**
     * Ejecuta {@code accion} en el JavaFX Application Thread tras una pausa de
     * {@code ms} milisegundos en un hilo daemon independiente.
     *
     * <p>Centraliza el patrón {@code Task + Thread.sleep + Platform.runLater}
     * para evitar duplicación en los flujos de turno.</p>
     *
     * @param ms      milisegundos de espera antes de ejecutar la acción
     * @param accion  código a ejecutar en el JAT tras la pausa
     */
    private void ejecutarDespuesDePausa(long ms, Runnable accion) {
        Task<Void> pausa = new Task<>() {
            @Override
            protected Void call() throws InterruptedException {
                Thread.sleep(ms);
                return null;
            }
        };
        pausa.setOnSucceeded(e -> Platform.runLater(accion));
        iniciarHiloDaemon(pausa);
    }

    /**
     * Inicia un {@link Task} en un hilo daemon para que no impida el cierre
     * de la aplicación JavaFX.
     *
     * @param tarea tarea a ejecutar en segundo plano
     */
    private void iniciarHiloDaemon(Task<?> tarea) {
        Thread hilo = new Thread(tarea);
        hilo.setDaemon(true);
        hilo.start();
    }

    /**
     * Gestiona un error inesperado durante el turno de una máquina:
     * muestra un mensaje, avanza el turno y continúa la partida.
     *
     * @param maquina máquina cuyo turno falló
     */
    private void manejarErrorTurnoMaquina(Maquina maquina) {
        actualizarEstado("Error inesperado en el turno de " + maquina.getNombre() + ".");
        juego.avanzarTurno();
        procesarSiguienteTurno();
    }

    // ── Helpers de UX ────────────────────────────────────────────────────

    /**
     * Actualiza la etiqueta de estado dinámica del tablero.
     * Debe llamarse siempre desde el JavaFX Application Thread.
     *
     * @param mensaje texto a mostrar al usuario
     */
    private void actualizarEstado(String mensaje) {
        if (lblEstado != null) {
            lblEstado.setText(mensaje);
        }
    }

    /** Sincroniza la etiqueta de suma con el valor del modelo. */
    private void actualizarSuma() {
        lblSumaActual.setText(String.valueOf(juego.getSumaActual()));
    }

    // ── Fábricas de nodos de carta ────────────────────────────────────────

    /**
     * Construye un {@link StackPane} que representa una carta boca arriba.
     *
     * <p>Distribución con {@link AnchorPane} para posicionamiento pixel-exacto:</p>
     * <pre>
     *  StackPane [card-face | card-face-mesa]
     *    └── AnchorPane
     *          ├── Label palo — esquina sup-izq   (pequeño)
     *          ├── Label palo — esquina sup-der   (balance)
     *          ├── Label palo — centro             (grande)
     *          └── Label rango — esquina inf-izq  (bold)
     * </pre>
     *
     * @param carta  carta del modelo a representar
     * @param esMesa {@code true} para la carta central de la mesa
     * @return StackPane estilizado listo para insertar en el árbol de escena
     */
    private StackPane crearCartaFrontal(Carta carta, boolean esMesa) {
        StackPane card = new StackPane();
        card.getStyleClass().add(esMesa ? "card-face-mesa" : "card-face");

        String colorClass = carta.getPalo().esRojo() ? "carta-roja" : "carta-negra";
        AnchorPane layout = new AnchorPane();
        layout.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        Label suitTL = new Label(carta.getPalo().getSimbolo());
        suitTL.getStyleClass().addAll("card-suit-corner", colorClass);
        AnchorPane.setTopAnchor(suitTL,  esMesa ? 9.0 : 7.0);
        AnchorPane.setLeftAnchor(suitTL, esMesa ? 10.0 : 8.0);

        Label suitTR = new Label(carta.getPalo().getSimbolo());
        suitTR.getStyleClass().addAll("card-suit-corner", colorClass);
        AnchorPane.setTopAnchor(suitTR,   esMesa ? 9.0 : 7.0);
        AnchorPane.setRightAnchor(suitTR, esMesa ? 10.0 : 8.0);

        Label suitCenter = new Label(carta.getPalo().getSimbolo());
        suitCenter.getStyleClass().addAll(
            esMesa ? "card-suit-center-mesa" : "card-suit-center", colorClass);
        suitCenter.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        suitCenter.setAlignment(Pos.CENTER);
        AnchorPane.setTopAnchor(suitCenter,    0.0);
        AnchorPane.setBottomAnchor(suitCenter, 0.0);
        AnchorPane.setLeftAnchor(suitCenter,   0.0);
        AnchorPane.setRightAnchor(suitCenter,  0.0);

        Label rankBL = new Label(carta.getRango().getEtiqueta());
        rankBL.getStyleClass().addAll(
            esMesa ? "card-rank-bottom-mesa" : "card-rank-bottom", colorClass);
        AnchorPane.setBottomAnchor(rankBL, esMesa ? 9.0 : 7.0);
        AnchorPane.setLeftAnchor(rankBL,   esMesa ? 10.0 : 8.0);

        layout.getChildren().addAll(suitCenter, suitTL, suitTR, rankBL);
        card.getChildren().add(layout);
        return card;
    }

    /**
     * Construye un {@link StackPane} que representa una carta boca abajo (dorso).
     *
     * @param esMazo {@code true} para el estilo del mazo apilado (mayor);
     *               {@code false} para el dorso pequeño de la mano de la máquina
     * @return StackPane con fondo degradado y patrón interior
     */
    private StackPane crearCartaDorso(boolean esMazo) {
        StackPane card = new StackPane();
        card.getStyleClass().add(esMazo ? "card-back-mazo" : "card-back-small");
        StackPane inner = new StackPane();
        inner.getStyleClass().add("card-back-inner");
        StackPane.setMargin(inner, new Insets(7));
        card.getChildren().add(inner);
        return card;
    }
}
