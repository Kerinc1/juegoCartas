package com.cincuentazo.cincuentazo.controller;

import com.cincuentazo.cincuentazo.model.Carta;
import com.cincuentazo.cincuentazo.model.CincuentazoGame;
import com.cincuentazo.cincuentazo.model.ConfiguracionJuego;
import com.cincuentazo.cincuentazo.model.Maquina;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Controlador de la pantalla principal del tablero de juego (HU-2).
 *
 * <p>Recibe la {@link ConfiguracionJuego} desde {@link InicioController},
 * crea el modelo ({@link CincuentazoGame}) y construye programáticamente
 * los nodos JavaFX que representan las cartas y los paneles de los jugadores.</p>
 *
 * <p>El método {@link #inicializarJuego(ConfiguracionJuego)} debe llamarse
 * desde el exterior <em>después</em> de cargar el FXML pero <em>antes</em>
 * de mostrar el {@code Stage}.</p>
 */
public class JuegoController {

    // ── Inyecciones FXML ─────────────────────────────────────────────────

    /** Contenedor horizontal donde se insertan los paneles de cada máquina. */
    @FXML private HBox contenedorMaquinas;

    /** StackPane que alberga las capas del mazo apilado (efecto de profundidad). */
    @FXML private StackPane vistaMazo;

    /** Etiqueta que muestra cuántas cartas quedan en el mazo. */
    @FXML private Label lblCantidadMazo;

    /** Contenedor de la carta central boca arriba en la mesa. */
    @FXML private StackPane contenedorCartaMesa;

    /** Etiqueta grande con la suma acumulada actual del juego. */
    @FXML private Label lblSumaActual;

    /** Contenedor horizontal de las cartas boca arriba del jugador humano. */
    @FXML private HBox contenedorManoHumano;

    // ── Estado ───────────────────────────────────────────────────────────

    /** Modelo de la partida; inicializado en {@link #inicializarJuego}. */
    private CincuentazoGame juego;

    // ── Ciclo de vida FXML ───────────────────────────────────────────────

    /**
     * Llamado automáticamente por JavaFX tras cargar el FXML.
     * El juego se configura en {@link #inicializarJuego(ConfiguracionJuego)},
     * no aquí, porque en este punto la {@link ConfiguracionJuego} todavía
     * no ha llegado desde la pantalla anterior.
     */
    @FXML
    public void initialize() {
        // No-op intencional: estado llega vía inicializarJuego().
    }

    // ── API pública ──────────────────────────────────────────────────────

    /**
     * Inicializa el modelo del juego y renderiza el tablero completo.
     *
     * <p>Debe ser invocado por {@link InicioController} inmediatamente
     * después de obtener esta instancia con {@code FXMLLoader.getController()}.</p>
     *
     * @param configuracion configuración con el número de oponentes máquina
     */
    public void inicializarJuego(ConfiguracionJuego configuracion) {
        this.juego = new CincuentazoGame(configuracion);
        renderizarTablero();
    }

    // ── Renderizado del tablero ───────────────────────────────────────────

    /** Coordina el renderizado completo de todos los elementos del tablero. */
    private void renderizarTablero() {
        renderizarMaquinas();
        renderizarMazo();
        renderizarCartaMesa();
        renderizarManoHumano();
        actualizarSuma();
    }

    // ── Máquinas ─────────────────────────────────────────────────────────

    /**
     * Crea y añade al contenedor un panel por cada máquina oponente.
     * Los paneles se distribuyen horizontalmente centrados.
     */
    private void renderizarMaquinas() {
        contenedorMaquinas.getChildren().clear();
        for (Maquina maquina : juego.getMaquinas()) {
            contenedorMaquinas.getChildren().add(crearPanelMaquina(maquina));
        }
    }

    /**
     * Construye el panel visual de una máquina: nombre y fila de cartas boca abajo.
     *
     * @param maquina la máquina oponente a representar
     * @return nodo VBox listo para insertar en {@link #contenedorMaquinas}
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

        panel.getChildren().addAll(nombre, mano);
        return panel;
    }

    // ── Mazo ─────────────────────────────────────────────────────────────

    /**
     * Renderiza el mazo con un efecto de profundidad (hasta 3 capas desplazadas).
     * La etiqueta inferior muestra cuántas cartas quedan.
     */
    private void renderizarMazo() {
        vistaMazo.getChildren().clear();
        int capas = Math.min(juego.getMazo().getTamanio(), 3);
        for (int i = capas - 1; i >= 0; i--) {
            StackPane capa = new StackPane();
            capa.getStyleClass().add("card-back-mazo");
            // Desplazamiento diagonal para simular profundidad
            capa.setTranslateX(i * 2.5);
            capa.setTranslateY(-i * 2.5);
            vistaMazo.getChildren().add(capa);
        }
        lblCantidadMazo.setText(juego.getMazo().getTamanio() + " cartas");
    }

    // ── Carta de la mesa ─────────────────────────────────────────────────

    /**
     * Renderiza la carta boca arriba que inicia la suma en la mesa central.
     */
    private void renderizarCartaMesa() {
        contenedorCartaMesa.getChildren().clear();
        contenedorCartaMesa.getChildren().add(
            crearCartaFrontal(juego.getCartaMesa(), true)
        );
    }

    // ── Mano del humano ──────────────────────────────────────────────────

    /**
     * Renderiza las cuatro cartas boca arriba del jugador humano.
     */
    private void renderizarManoHumano() {
        contenedorManoHumano.getChildren().clear();
        for (Carta carta : juego.getJugadorHumano().getMano()) {
            contenedorManoHumano.getChildren().add(crearCartaFrontal(carta, false));
        }
    }

    // ── Suma ─────────────────────────────────────────────────────────────

    /** Actualiza la etiqueta de la suma con el valor actual del modelo. */
    private void actualizarSuma() {
        lblSumaActual.setText(String.valueOf(juego.getSumaActual()));
    }

    // ── Fábricas de nodos de carta ────────────────────────────────────────

    /**
     * Construye un nodo StackPane que representa una carta boca arriba.
     *
     * <p>Distribución con {@link AnchorPane} para posicionamiento pixel-exacto,
     * evitando sobreposiciones causadas por la interacción entre {@code setRotate}
     * y {@code StackPane.setAlignment}:</p>
     * <pre>
     *  StackPane [card-face | card-face-mesa]
     *    └── AnchorPane (fills card)
     *          ├── Label suit  — ARRIBA-IZQUIERDA  (palo en pequeño)
     *          ├── Label suit  — ARRIBA-DERECHA     (palo en pequeño, balance visual)
     *          ├── Label suit  — CENTRO             (palo grande, protagonista)
     *          └── Label rank  — ABAJO-IZQUIERDA    (rango en grande, sin superposición)
     * </pre>
     *
     * @param carta  carta del modelo a representar
     * @param esMesa {@code true} para la carta central de la mesa (estilo diferenciado)
     * @return StackPane estilizado listo para añadir al árbol de escena
     */
    private StackPane crearCartaFrontal(Carta carta, boolean esMesa) {
        StackPane card = new StackPane();
        card.getStyleClass().add(esMesa ? "card-face-mesa" : "card-face");

        String colorClass = carta.getPalo().esRojo() ? "carta-roja" : "carta-negra";

        AnchorPane layout = new AnchorPane();
        layout.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // ── Palo pequeño arriba-izquierda ──────────────────────────────
        Label suitTL = new Label(carta.getPalo().getSimbolo());
        suitTL.getStyleClass().addAll("card-suit-corner", colorClass);
        AnchorPane.setTopAnchor(suitTL, esMesa ? 9.0 : 7.0);
        AnchorPane.setLeftAnchor(suitTL, esMesa ? 10.0 : 8.0);

        // ── Palo pequeño arriba-derecha (balance visual) ───────────────
        Label suitTR = new Label(carta.getPalo().getSimbolo());
        suitTR.getStyleClass().addAll("card-suit-corner", colorClass);
        AnchorPane.setTopAnchor(suitTR, esMesa ? 9.0 : 7.0);
        AnchorPane.setRightAnchor(suitTR, esMesa ? 10.0 : 8.0);

        // ── Símbolo de palo grande centrado (protagonista de la carta) ─
        Label suitCenter = new Label(carta.getPalo().getSimbolo());
        suitCenter.getStyleClass().addAll(
            esMesa ? "card-suit-center-mesa" : "card-suit-center", colorClass);
        suitCenter.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        suitCenter.setAlignment(Pos.CENTER);
        AnchorPane.setTopAnchor(suitCenter, 0.0);
        AnchorPane.setBottomAnchor(suitCenter, 0.0);
        AnchorPane.setLeftAnchor(suitCenter, 0.0);
        AnchorPane.setRightAnchor(suitCenter, 0.0);

        // ── Rango abajo-izquierda (lectura prioritaria sin solapamiento) ──
        Label rankBL = new Label(carta.getRango().getEtiqueta());
        rankBL.getStyleClass().addAll(
            esMesa ? "card-rank-bottom-mesa" : "card-rank-bottom", colorClass);
        AnchorPane.setBottomAnchor(rankBL, esMesa ? 9.0 : 7.0);
        AnchorPane.setLeftAnchor(rankBL, esMesa ? 10.0 : 8.0);

        // suitCenter primero (capa de fondo), esquinas encima
        layout.getChildren().addAll(suitCenter, suitTL, suitTR, rankBL);
        card.getChildren().add(layout);
        return card;
    }

    /**
     * Construye un nodo StackPane que representa una carta boca abajo (dorso).
     *
     * @param esMazo {@code true} usa el estilo del mazo (mayor); {@code false}
     *               usa el estilo pequeño de las cartas de las máquinas
     * @return StackPane con fondo de dorso y patrón interior
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
