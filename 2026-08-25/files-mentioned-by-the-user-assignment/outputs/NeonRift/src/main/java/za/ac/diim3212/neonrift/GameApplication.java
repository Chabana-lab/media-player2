package za.ac.diim3212.neonrift;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** JavaFX application lifecycle class. */
public final class GameApplication extends Application {
    @Override public void start(Stage stage) {
        GameController controller = new GameController(stage);
        Scene scene = new Scene(controller.root(), 1100, 720);
        scene.getStylesheets().add(getClass().getResource("/styles/neon-rift.css").toExternalForm());
        stage.setTitle("Neon Rift | Interactive Multimedia");
        stage.setMinWidth(390); stage.setMinHeight(600);
        stage.setScene(scene); stage.show();
        controller.showMenu();
    }
}
