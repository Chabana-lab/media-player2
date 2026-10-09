package za.ac.diim3212.neonrift;

/**
 * Plain-Java entry point for IDEs. Starting this class prevents the Java
 * launcher from incorrectly treating the JavaFX Application subclass as a
 * standalone classpath application.
 */
public final class Launcher {
    private Launcher() { }

    public static void main(String[] args) {
        NeonRiftApp.main(args);
    }
}
