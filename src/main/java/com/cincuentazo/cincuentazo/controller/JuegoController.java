package com.cincuentazo.cincuentazo.controller;

import com.cincuentazo.cincuentazo.model.Carta;
import com.cincuentazo.cincuentazo.model.CincuentazoGame;
import com.cincuentazo.cincuentazo.model.ConfiguracionJuego;
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
 * Controlador del tablero de juego — HU-3: Jugar una carta.
 *
 * <p>Extiende la funcionalidad de HU-2 añadiendo interactividad completa:</p>
 * <ul>
 *   <li>Manejadores de clic sobre las cartas del jugador humano.</li>
 *   <li>Validación de jugadas contra la regla del límite de 50 puntos.</li>
 *   <li>Simulación asíncrona del turno de las máquinas (2–4 s) mediante
 *       {@link Task} en un hilo de fondo; nunca bloquea el
 *       <em>JavaFX Application Thread</em>.</li>
 *   <li>Actualización de UI (suma, carta de la mesa, manos, estado) siempre
 *       dentro de {@link Platform#runLater(Runnable)} para garantizar
 *       la seguridad de hilos.</li>
 *   <li>Indicador de estado dinámico ("Máquina X pensando…" / "TU TURNO").</li>
 * </ul>
 *
 * <p>El método {@link #inicializarJuego(ConfiguracionJuego)} debe invocarse
 * desde {@link InicioController} tras cargar el FXML y antes de mostrar
 * el {@code Stage}.</p>
 */
public class JuegoController {

    // ── Inyecciones FXML ─────────────────────────────────────────────────

    /** Contenedor horizontal de los paneles de cada máquina oponente. */
    @FXML private HBox      contenedorMaquinas;

    /** StackPane del mazo apilado (efecto de profundidad). */
    @FXML private StackPane vistaMazo;

    /** Etiqueta con la cantidad de cartas restantes en el mazo. */
    @FXML private Label     lblCantidadMazo;

    /** Contenedor de la carta central de la mesa (boca arriba). */
    @FXML private StackPane contenedorCartaMesa;

    /** Etiqueta numérica grande con la suma acumulada actual. */
    @FXML private Label     lblSumaActual;

    /** Contenedor horizontal de las cartas del jugador humano. */
    @FXML private HBox      contenedorManoHumano;

    /**
     * Etiqueta de estado dinámica: muestra "TU TURNO", "Máquina X pensando…"
     * o el nombre de la carta que acaba de jugar la máquina.
     */
    @FXML private Label     lblEstado;

    // ── Estado del controlador ───────────────────────────────────────────

    /** Modelo central de la partida. */
    private CincuentazoGame juego;

    /**
     * Mapa que asocia cada máquina con el {@link HBox} interior de su panel
     * de cartas, para poder actualizar el número de dorsos sin reconstruir
     * todo el contenedor de máquinas.
     */
    private final Map<Maquina, HBox> manosPanelesMaquina = new HashMap<>();

    /** Fuente de aleatoriedad para los retardos de las máquinas (2–4 s). */
    private final Random random = new Random();

    // ── Ciclo de vida FXML ───────────────────────────────────────────────

    /**
     * Invocado automáticamente por JavaFX tras cargar el FXML.
     * El juego se configura en {@link #inicializarJuego(ConfiguracionJuego)};
     * en este punto la {@link ConfiguracionJuego} aún no ha llegado.
     */
    @FXML
    public void initialize() {
        // No-op intencional: el estado llega vía inicializarJuego().
    }

    // ── API pública ──────────────────────────────────────────────────────

    /**
     * Inicializa el modelo de la partida, renderiza el tablero completo y
     * muestra el mensaje inicial de estado para el turno del jugador humano.
     *
     * <p>Debe ser invocado por {@link InicioController} inmediatamente después
     * de obtener esta instancia mediante {@code FXMLLoader.getController()}.</p>
     *
     * @param configuracion número de máquinas oponentes seleccionado
     */
    public void inicializarJuego(ConfiguracionJuego configuracion) {
        this.juego = new CincuentazoGame(configuracion);
        renderizarTablero();
        actualizarEstado("TU TURNO — Selecciona una carta");
    }

    // ── Renderizado completo ──────────────────────────────────────────────

    /**
     * Coordina el renderizado inicial de todos los elementos del tablero.
     * Limpia los mapas de referencias y reconstruye el árbol de escena completo.
     */
    private void renderizarTablero() {
        manosPanelesMaquina.clear();
        renderizarMaquinas();
        renderizarMazo();
        renderizarCartaMesa();
        renderizarManoHumano();
        actualizarSuma();
    }

    // ── Zona de máquinas ─────────────────────────────────────────────────

    /**
     * Construye un panel visual por cada máquina oponente y guarda la
     * referencia a su HBox de cartas para actualizaciones parciales.
     */
    private void renderizarMaquinas() {
        contenedorMaquinas.getChildren().clear();
        for (Maquina maquina : juego.getMaquinas()) {
            contenedorMaquinas.getChildren().add(crearPanelMaquina(maquina));
        }
    }

    /**
     * Construye el panel visual de una máquina: etiqueta de nombre y fila de
     * cartas boca abajo. Almacena el {@link HBox} interior en
     * {@link #manosPanelesMaquina} para actualizarlo cuando la máquina juegue.
     *
     * @param maquina máquina oponente a representar
     * @return VBox listo para insertar en {@link #contenedorMaquinas}
     */
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

        // Guardar referencia para actualizar tras la jugada de la máquina
        manosPanelesMaquina.put(maquina, mano);

        panel.getChildren().addAll(nombre, mano);
        return panel;
    }

    /**
     * Elimina un dorso del panel de mano de la máquina para reflejar que
     * acaba de jugar una carta. No reconstruye el panel completo; solo
     * elimina el último hijo del HBox.
     *
     * @param maquina máquina que acaba de jugar
     */
    private void actualizarManoMaquina(Maquina maquina) {
        HBox mano = manosPanelesMaquina.get(maquina);
        if (mano != null && !mano.getChildren().isEmpty()) {
            mano.getChildren().remove(mano.getChildren().size() - 1);
        }
    }

    // ── Mazo ─────────────────────────────────────────────────────────────

    /**
     * Renderiza el mazo con hasta 3 capas desplazadas diagonalmente para
     * simular profundidad. Actualiza también la etiqueta de cantidad.
     */
    private void renderizarMazo() {
        vistaMazo.getChildren().clear();
        int capas = Math.min(juego.getMazo().getTamanio(), 3);
        for (int i = capas - 1; i >= 0; i--) {
            StackPane capa = new StackPane();
            capa.getStyleClass().add("card-back-mazo");
            capa.setTranslateX(i * 2.5);
            capa.setTranslateY(-i * 2.5);
            vistaMazo.getChildren().add(capa);
        }
        lblCantidadMazo.setText(juego.getMazo().getTamanio() + " cartas");
    }

    // ── Carta de la mesa ─────────────────────────────────────────────────

    /**
     * Reconstruye el contenedor de la carta de la mesa con la carta actual
     * del modelo, mostrándola boca arriba con el estilo diferenciado.
     */
    private void renderizarCartaMesa() {
        contenedorCartaMesa.getChildren().clear();
        contenedorCartaMesa.getChildren().add(
            crearCartaFrontal(juego.getCartaMesa(), true)
        );
    }

    // ── Mano del jugador humano ───────────────────────────────────────────

    /**
     * Reconstruye la mano del jugador humano habilitando el contenedor y
     * añadiendo un manejador de clic solo a las cartas jugables según el
     * estado actual de la suma. Las cartas no jugables se muestran atenuadas.
     *
     * <p>Este método también se usa para restaurar el estado interactivo de
     * la mano después de que la(s) máquina(s) terminen su turno.</p>
     */
    private void renderizarManoHumano() {
        contenedorManoHumano.setDisable(false);
        contenedorManoHumano.getChildren().clear();

        for (Carta carta : juego.getJugadorHumano().getMano()) {
            StackPane nodo = crearCartaFrontal(carta, false);
            boolean jugable = juego.esJugable(carta);

            if (jugable) {
                nodo.setOnMouseClicked(e -> manejarClickCarta(carta));
            } else {
                nodo.getStyleClass().add("carta-no-jugable");
                nodo.setDisable(true);
            }
            contenedorManoHumano.getChildren().add(nodo);
        }
    }

    // ── Manejador de clic — humano ────────────────────────────────────────

    /**
     * Gestiona el clic sobre una carta del jugador humano.
     *
     * <p>Delegación de responsabilidades:</p>
     * <ol>
     *   <li>Ignora el evento si el turno no es del humano (protección extra).</li>
     *   <li>Intenta ejecutar la jugada en el modelo; captura
     *       {@link MovimientoInvalidoException} si la carta es inválida.</li>
     *   <li>Si la jugada es válida, actualiza todos los componentes visuales
     *       afectados y cede el turno a la siguiente máquina.</li>
     * </ol>
     *
     * @param carta carta sobre la que el usuario hizo clic
     */
    private void manejarClickCarta(Carta carta) {
        if (!juego.esTurnoHumano()) return;

        try {
            juego.jugarCarta(juego.getJugadorHumano(), carta);
        } catch (MovimientoInvalidoException e) {
            // Retroalimentación visual sin interrumpir el flujo
            actualizarEstado("⚠  " + e.getMessage());
            return;
        }

        // Refrescar todos los componentes afectados por la jugada
        renderizarCartaMesa();
        renderizarManoHumano();
        actualizarSuma();
        renderizarMazo();

        juego.avanzarTurno();
        procesarSiguienteTurno();
    }

    // ── Gestión de turnos ─────────────────────────────────────────────────

    /**
     * Evalúa el turno activo y actúa en consecuencia:
     * <ul>
     *   <li>Si es el turno del humano: rehabilita la mano, actualiza el estado.</li>
     *   <li>Si es el turno de una máquina: deshabilita la mano y lanza la
     *       simulación asíncrona de la máquina correspondiente.</li>
     * </ul>
     */
    private void procesarSiguienteTurno() {
        if (juego.esTurnoHumano()) {
            renderizarManoHumano(); // reconstruye con estado de jugabilidad actualizado
            actualizarEstado("TU TURNO — Selecciona una carta");
        } else {
            contenedorManoHumano.setDisable(true); // bloquea el HBox completo
            procesarTurnoMaquina(juego.getMaquinaActual());
        }
    }

    // ── Turno de la máquina — lógica asíncrona ────────────────────────────

    /**
     * Simula el "pensamiento" de una máquina mediante un {@link Task} que
     * se ejecuta en un hilo de fondo, respetando estrictamente la regla de
     * no bloquear el <em>JavaFX Application Thread</em>.
     *
     * <p><strong>Flujo detallado:</strong></p>
     * <ol>
     *   <li>Muestra el indicador "⏳ Máquina X pensando…" en el hilo de UI.</li>
     *   <li>El {@link Task} duerme entre 2 000 y 4 000 ms en el hilo de fondo.</li>
     *   <li>Selecciona la primera carta jugable de la mano de la máquina.</li>
     *   <li>Al completarse, {@link Platform#runLater(Runnable)} actualiza la UI:
     *       carta de la mesa, panel de la máquina, suma y mazo.</li>
     *   <li>Lanza una breve pausa de 700 ms (también en hilo de fondo) para
     *       dar tiempo a leer el estado antes del próximo turno.</li>
     *   <li>Llama recursivamente a {@link #procesarSiguienteTurno()} en el JAT.</li>
     * </ol>
     *
     * @param maquina máquina cuyo turno se debe simular
     */
    private void procesarTurnoMaquina(Maquina maquina) {
        actualizarEstado("⏳  " + maquina.getNombre() + " pensando…");

        Task<Carta> tareaIA = new Task<>() {
            @Override
            protected Carta call() throws InterruptedException {
                // Retardo aleatorio 2 000–4 000 ms en hilo de fondo (NO el JAT)
                long retardo = 2000L + random.nextInt(2001);
                Thread.sleep(retardo);

                // Seleccionar la primera carta jugable de la mano de la máquina
                for (Carta c : maquina.getMano()) {
                    if (juego.esJugable(c)) {
                        return c;
                    }
                }
                return null; // sin cartas jugables (borde: será gestionado en HU futura)
            }
        };

        tareaIA.setOnSucceeded(evento -> {
            // OBLIGATORIO: toda modificación de UI en el JavaFX Application Thread
            Platform.runLater(() -> {
                Carta cartaElegida = tareaIA.getValue();

                if (cartaElegida != null) {
                    try {
                        juego.jugarCarta(maquina, cartaElegida);
                    } catch (MovimientoInvalidoException ex) {
                        // No debería ocurrir: ya validamos con esJugable()
                        actualizarEstado("Error interno en el turno de " + maquina.getNombre());
                        juego.avanzarTurno();
                        procesarSiguienteTurno();
                        return;
                    }
                    actualizarManoMaquina(maquina);
                    renderizarCartaMesa();
                    actualizarSuma();
                    renderizarMazo();
                    actualizarEstado(maquina.getNombre() + " jugó: " + cartaElegida);
                } else {
                    actualizarEstado(maquina.getNombre() + " no tiene cartas jugables — pasa turno.");
                }

                juego.avanzarTurno();

                // Pausa visual de 700 ms antes del siguiente turno para legibilidad
                Task<Void> pausa = new Task<>() {
                    @Override
                    protected Void call() throws InterruptedException {
                        Thread.sleep(700);
                        return null;
                    }
                };
                pausa.setOnSucceeded(e2 ->
                    Platform.runLater(JuegoController.this::procesarSiguienteTurno));
                Thread hiloPausa = new Thread(pausa);
                hiloPausa.setDaemon(true);
                hiloPausa.start();
            });
        });

        tareaIA.setOnFailed(evento -> {
            Platform.runLater(() -> {
                actualizarEstado("Error inesperado en el turno de " + maquina.getNombre() + ".");
                juego.avanzarTurno();
                procesarSiguienteTurno();
            });
        });

        Thread hiloIA = new Thread(tareaIA);
        hiloIA.setDaemon(true); // el hilo no impide cerrar la aplicación
        hiloIA.start();
    }

    // ── Helpers de UX ────────────────────────────────────────────────────

    /**
     * Actualiza la etiqueta de estado dinámica del tablero.
     * La llamada es segura desde cualquier hilo si se asegura el JAT por contexto;
     * desde hilos de fondo siempre se envuelve en {@link Platform#runLater(Runnable)}.
     *
     * @param mensaje texto informativo a mostrar al usuario
     */
    private void actualizarEstado(String mensaje) {
        if (lblEstado != null) {
            lblEstado.setText(mensaje);
        }
    }

    /** Sincroniza la etiqueta numérica de suma con el valor actual del modelo. */
    private void actualizarSuma() {
        lblSumaActual.setText(String.valueOf(juego.getSumaActual()));
    }

    // ── Fábricas de nodos de carta ────────────────────────────────────────

    /**
     * Construye un {@link StackPane} que representa una carta boca arriba.
     *
     * <p>Distribución interna con {@link AnchorPane} para posicionamiento
     * pixel-exacto de los cuatro elementos visuales de la carta:</p>
     * <pre>
     *  StackPane [card-face | card-face-mesa]
     *    └── AnchorPane
     *          ├── Label palo  — esquina superior-izquierda (pequeño)
     *          ├── Label palo  — esquina superior-derecha   (balance visual)
     *          ├── Label palo  — centro                     (grande, protagonista)
     *          └── Label rango — esquina inferior-izquierda (bold)
     * </pre>
     *
     * @param carta  carta del modelo a representar
     * @param esMesa {@code true} para la carta central de la mesa (dimensiones y
     *               bordes diferenciados)
     * @return StackPane estilizado listo para añadir al árbol de escena
     */
    private StackPane crearCartaFrontal(Carta carta, boolean esMesa) {
        StackPane card = new StackPane();
        card.getStyleClass().add(esMesa ? "card-face-mesa" : "card-face");

        String colorClass = carta.getPalo().esRojo() ? "carta-roja" : "carta-negra";

        AnchorPane layout = new AnchorPane();
        layout.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // Palo pequeño — esquina superior-izquierda
        Label suitTL = new Label(carta.getPalo().getSimbolo());
        suitTL.getStyleClass().addAll("card-suit-corner", colorClass);
        AnchorPane.setTopAnchor(suitTL,  esMesa ? 9.0 : 7.0);
        AnchorPane.setLeftAnchor(suitTL, esMesa ? 10.0 : 8.0);

        // Palo pequeño — esquina superior-derecha (balance visual)
        Label suitTR = new Label(carta.getPalo().getSimbolo());
        suitTR.getStyleClass().addAll("card-suit-corner", colorClass);
        AnchorPane.setTopAnchor(suitTR,   esMesa ? 9.0 : 7.0);
        AnchorPane.setRightAnchor(suitTR, esMesa ? 10.0 : 8.0);

        // Palo grande — centrado (protagonista de la carta)
        Label suitCenter = new Label(carta.getPalo().getSimbolo());
        suitCenter.getStyleClass().addAll(
            esMesa ? "card-suit-center-mesa" : "card-suit-center", colorClass);
        suitCenter.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        suitCenter.setAlignment(Pos.CENTER);
        AnchorPane.setTopAnchor(suitCenter,    0.0);
        AnchorPane.setBottomAnchor(suitCenter, 0.0);
        AnchorPane.setLeftAnchor(suitCenter,   0.0);
        AnchorPane.setRightAnchor(suitCenter,  0.0);

        // Rango — esquina inferior-izquierda (sin solapamiento con el palo)
        Label rankBL = new Label(carta.getRango().getEtiqueta());
        rankBL.getStyleClass().addAll(
            esMesa ? "card-rank-bottom-mesa" : "card-rank-bottom", colorClass);
        AnchorPane.setBottomAnchor(rankBL, esMesa ? 9.0 : 7.0);
        AnchorPane.setLeftAnchor(rankBL,   esMesa ? 10.0 : 8.0);

        // suitCenter al fondo; esquinas y rango encima
        layout.getChildren().addAll(suitCenter, suitTL, suitTR, rankBL);
        card.getChildren().add(layout);
        return card;
    }

    /**
     * Construye un {@link StackPane} que representa una carta boca abajo (dorso).
     *
     * @param esMazo {@code true} para usar el estilo del mazo apilado (más grande);
     *               {@code false} para el dorso pequeño de las cartas de la máquina
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
