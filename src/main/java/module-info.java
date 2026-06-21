module com.cincuentazo.cincuentazo {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.kordamp.bootstrapfx.core;

    /* El paquete raíz sigue abierto para la clase Application y el Launcher. */
    opens com.cincuentazo.cincuentazo to javafx.fxml;

    /* El controlador necesita abrirse a javafx.fxml para inyección reflectiva de @FXML. */
    opens com.cincuentazo.cincuentazo.controller to javafx.fxml;

    exports com.cincuentazo.cincuentazo;
    exports com.cincuentazo.cincuentazo.model;
    exports com.cincuentazo.cincuentazo.controller;
}