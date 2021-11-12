module apas {
    requires javafx.controls;
    requires javafx.fxml;

    opens apas to javafx.fxml;
    exports apas.model;
    exports apas;
}