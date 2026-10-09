package za.ac.diim3212.neonrift;

import javafx.application.Application;

/** Safe IDE entry point. It launches the actual JavaFX Application class. */
public final class NeonRiftApp {
    private NeonRiftApp() { }

    public static void main(String[] args) {
        Application.launch(GameApplication.class, args);
    }
}
