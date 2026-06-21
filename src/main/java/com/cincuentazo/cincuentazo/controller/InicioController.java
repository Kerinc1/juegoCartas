package com.cincuentazo.cincuentazo.controller;

import com.cincuentazo.cincuentazo.model.ConfiguracionJuego;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controlador de la pantalla de inicio del juego Cincuentazo.
 *
 * <p>Pertenece a la capa <b>Controlador</b> del patrón MVC. Gestiona los
 * eventos generados por {@code inicio-view.fxml}: la selección del número
 * de oponentes máquina y el inicio de la partida.</p>
 *
 * <p>El botón "Iniciar Juego" permanece deshabilitado hasta que el usuario
 * selecciona una opción válida (1, 2 o 3 jugadores máquina).</p>
 */
public class InicioController {

    /** Grupo de selección exclusiva que agrupa los tres ToggleButtons de oponentes. */
    @FXML
    private ToggleGroup grupoJugadores;

    /** Botón de selección para enfrentar 1 jugador máquina. */
    @FXML
    private ToggleButton btnUnoJugador;

    /** Botón de selección para enfrentar 2 jugadores máquina. */
    @FXML
    private ToggleButton btnDosJugadores;

    /** Botón de selección para enfrentar 3 jugadores máquina. */
    @FXML
    private ToggleButton btnTresJugadores;

    /** Botón para confirmar la selección e iniciar la partida. */
    @FXML
    private Button btnIniciar;

    /** Etiqueta informativa que describe la selección actual del usuario. */
    @FXML
    private Label lblSeleccion;

    /** Modelo que almacena la configuración elegida por el jugador humano. */
    private ConfiguracionJuego configuracion;

    /**
     * Inicializa el controlador inmediatamente después de que el FXML ha sido cargado.
     *
     * <p>Asigna los valores de datos a cada ToggleButton, deshabilita el botón de
     * inicio y registra un listener en el grupo de selección para habilitar el
     * botón en cuanto se realice una elección.</p>
     */
    @FXML
    public void initialize() {
        configuracion = new ConfiguracionJuego();

        btnUnoJugador.setUserData(1);
        btnDosJugadores.setUserData(2);
        btnTresJugadores.setUserData(3);

        btnIniciar.setDisable(true);

        grupoJugadores.selectedToggleProperty().addListener((obs, anterior, nuevo) -> {
            boolean haySeleccion = (nuevo != null);
            btnIniciar.setDisable(!haySeleccion);

            if (haySeleccion) {
                int cantidad = (int) nuevo.getUserData();
                String descripcion = (cantidad == 1)
                        ? "Jugarás contra 1 jugador máquina"
                        : "Jugarás contra " + cantidad + " jugadores máquina";
                lblSeleccion.setText(descripcion);
            } else {
                lblSeleccion.setText("");
            }
        });
    }

    /**
     * Maneja el clic en el botón "Iniciar Juego".
     *
     * <p>Persiste la selección en el modelo, carga {@code juego-view.fxml},
     * transfiere la configuración al {@link JuegoController} e intercambia
     * la escena activa del {@link Stage} por el tablero de juego.</p>
     *
     * @param event evento de acción disparado por el botón
     */
    @FXML
    public void onIniciarJuego(ActionEvent event) {
        Toggle seleccion = grupoJugadores.getSelectedToggle();
        if (seleccion == null) {
            return;
        }

        int cantidad = (int) seleccion.getUserData();
        configuracion.setCantidadJugadoresMaquina(cantidad);

        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/cincuentazo/cincuentazo/juego-view.fxml")
            );
            Parent root = loader.load();

            JuegoController juegoController = loader.getController();
            juegoController.inicializarJuego(configuracion);

            Stage stage = (Stage) btnIniciar.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setResizable(true);
            stage.centerOnScreen();

        } catch (IOException e) {
            throw new RuntimeException("Error al cargar el tablero de juego.", e);
        }
    }

    /**
     * Retorna la configuración de juego actualmente almacenada en el modelo.
     * Útil para transferir el estado hacia la siguiente pantalla (HU-2).
     *
     * @return objeto {@link ConfiguracionJuego} con la selección del usuario
     */
    public ConfiguracionJuego getConfiguracion() {
        return configuracion;
    }
}
