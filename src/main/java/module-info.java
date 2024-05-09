module application {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example.game_demo_2 to javafx.fxml;
    exports com.example.game_demo_2;
}