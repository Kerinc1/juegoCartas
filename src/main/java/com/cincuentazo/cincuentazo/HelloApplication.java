package com.cincuentazo.cincuentazo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Punto de entrada principal de la aplicación Cincuentazo.
 *
 * <p>Extiende {@link javafx.application.Application} y carga la pantalla
 * de inicio ({@code inicio-view.fxml}) como escena inicial del {@link Stage}.</p>
 */
public class HelloApplication extends Application {

    /**
     * Inicializa y muestra la ventana principal de la aplicación.
     *
     * @param stage el contenedor raíz de JavaFX proporcionado por el entorno de ejecución
     * @throws IOException si el archivo FXML de inicio no puede ser cargado
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                HelloApplication.class.getResource("inicio-view.fxml")
        );
        Scene scene = new Scene(fxmlLoader.load(), 680, 520);
        stage.setTitle("Cincuentazo");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }
}
