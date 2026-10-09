package za.ac.diim3212.neonrift;

import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.Random;

/** Owns screen navigation and all interactive game-demo state. */
public final class GameController {
    private final Stage stage;
    private final StackPane root = new StackPane();
    private final Random random = new Random();
    private int score = 12450;
    private final StringPropertyLike difficulty = new StringPropertyLike("NORMAL");

    public GameController(Stage stage) { this.stage = stage; root.getStyleClass().add("app-root"); }
    public StackPane root() { return root; }

    public void showMenu() {
        VBox menu = new VBox(16); menu.setAlignment(Pos.CENTER); menu.setMaxWidth(370); menu.getStyleClass().add("menu");
        Text eyebrow = label("INTERACTIVE MULTIMEDIA // 2026", "eyebrow");
        Text title = label("NEON\nRIFT", "game-title"); title.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        Text sub = label("SURVIVE THE SIGNAL", "subtitle");
        Button play = action("▶  PLAY MISSION", this::showGame); Button instructions = action("▤  HOW TO PLAY", this::showInstructions);
        Button settings = action("⚙  SETTINGS", this::showSettings); Button exit = action("✕  EXIT GAME", () -> stage.close()); exit.getStyleClass().add("quiet-button");
        menu.getChildren().addAll(eyebrow, title, sub, spacer(16), play, instructions, settings, spacer(4), exit);
        show(screenBackdrop(), menu); floatingTitle(menu);
    }

    public void showGame() {
        BorderPane game = new BorderPane(); game.setPadding(new Insets(22)); game.getStyleClass().add("game-screen");
        HBox hud = new HBox(14); hud.setAlignment(Pos.CENTER); hud.getStyleClass().add("hud");
        Label scoreLabel = hudStat("SCORE", "0"); Label level = hudStat("MISSION", "0 / 12 SHARDS");
        Label lives = hudStat("SHIELD", "◆ ◆ ◆");
        GameArena arena = new GameArena(new GameArena.Listener() {
            @Override public void update(int newScore, int shields, int collected, int target) {
                score = newScore; scoreLabel.setText("SCORE\n" + String.format("%,d", newScore));
                level.setText("MISSION\n" + collected + " / " + target + " SHARDS");
                lives.setText("SHIELD\n" + "◆ ".repeat(Math.max(0, shields)).trim());
            }
            @Override public void complete(int finalScore) { score = finalScore; showComplete(); }
        });
        Button pause = action("Ⅱ  PAUSE", () -> showPauseOverlay(game, arena)); pause.getStyleClass().add("small-button");
        Region spring = new Region(); HBox.setHgrow(spring, Priority.ALWAYS); hud.getChildren().addAll(scoreLabel, level, lives, spring, pause); game.setTop(hud);
        BorderPane.setMargin(arena, new Insets(18, 0, 12, 0)); game.setCenter(arena);
        HBox controls = new HBox(10, label("⌨  Use Arrow Keys or WASD to move", "hint"), label("•", "hint"), label("Pick up blue shards • avoid pink mines", "hint")); controls.setAlignment(Pos.CENTER); game.setBottom(controls);
        show(screenBackdrop(), game);
        root.getScene().setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case LEFT, A -> arena.move(-1, 0); case RIGHT, D -> arena.move(1, 0);
                case UP, W -> arena.move(0, -1); case DOWN, S -> arena.move(0, 1);
                case ESCAPE, P -> showPauseOverlay(game, arena); default -> { }
            }
        });
        root.getScene().setOnKeyReleased(e -> { if (e.getCode().isArrowKey() || e.getCode() == KeyCode.W || e.getCode() == KeyCode.A || e.getCode() == KeyCode.S || e.getCode() == KeyCode.D) arena.stopMoving(); });
    }

    public void showInstructions() {
        VBox page = page("HOW TO PLAY", "Use these simple commands to play the game.");
        GridPane grid = new GridPane(); grid.setHgap(16); grid.setVgap(16); grid.getStyleClass().add("card-grid");
        grid.add(infoCard("◎", "YOUR GOAL", "Move your ship. Collect all 12 blue shards. Do not touch the pink mines."), 0,0);
        grid.add(infoCard("⌨", "MOVE", "Use the Arrow Keys, or use W, A, S and D, to move your ship."), 1,0);
        grid.add(infoCard("Ⅱ", "PAUSE", "Press P or Esc to pause the game. Press Resume to keep playing."), 0,1);
        grid.add(infoCard("◆", "RULES AND SCORE", "A blue shard gives points. A pink mine removes one shield. You have 3 shields."), 1,1);
        page.getChildren().addAll(grid, backButton()); show(screenBackdrop(), page);
    }

    public void showSettings() {
        VBox page = page("SETTINGS", "Tune the experience to your signal.");
        VBox card = new VBox(17); card.getStyleClass().add("settings-card");
        CheckBox sound = toggle("Sound effects", true); sound.selectedProperty().addListener((o, oldValue, enabled) -> SoundService.get().setSoundEnabled(enabled));
        CheckBox music = toggle("Ambient music", false); music.selectedProperty().addListener((o, oldValue, enabled) -> SoundService.get().setMusicEnabled(enabled));
        Text volumeText = label("MASTER VOLUME", "setting-label"); Slider volume = new Slider(0,100,72); volume.setShowTickMarks(false); volume.setShowTickLabels(false);
        Text difficultyText = label("DIFFICULTY", "setting-label"); ComboBox<String> difficultyBox = new ComboBox<>(); difficultyBox.getItems().addAll("RELAXED", "NORMAL", "INTENSE"); difficultyBox.setValue(difficulty.get()); difficultyBox.valueProperty().addListener((o,a,b)->difficulty.set(b)); difficultyBox.setMaxWidth(Double.MAX_VALUE);
        Text notice = label("Settings are applied instantly.", "hint");
        card.getChildren().addAll(sound, music, volumeText, volume, difficultyText, difficultyBox, notice); page.getChildren().addAll(card, backButton()); show(screenBackdrop(), page);
    }

    public void showComplete() {
        SoundService.get().complete(); VBox panel = new VBox(15); panel.setAlignment(Pos.CENTER); panel.setMaxWidth(460); panel.getStyleClass().add("completion");
        Text icon = label("✦", "completion-icon"); Text status = label("MISSION COMPLETE", "completion-title"); Text scoreText = label(String.format("%,d", score + 2250), "final-score");
        panel.getChildren().addAll(icon, status, label("FINAL SIGNAL SCORE", "eyebrow"), scoreText, label("The rift is stable. Excellent work, pilot.", "subtitle"), spacer(10), action("PLAY AGAIN", this::showGame), action("MAIN MENU", this::showMenu)); show(screenBackdrop(), panel); pulse(icon);
    }

    private void showPauseOverlay(Node game, GameArena arena) { if (arena.isPaused()) return; arena.togglePause(); VBox box = new VBox(16); box.setAlignment(Pos.CENTER); box.setMaxWidth(360); box.getStyleClass().add("pause-overlay"); box.getChildren().addAll(label("PAUSED", "completion-title"), label("Take a breath. The rift can wait.", "subtitle"), action("RESUME", () -> { arena.togglePause(); root.getChildren().remove(box); }), action("MAIN MENU", this::showMenu)); root.getChildren().add(box); fadeIn(box); }
    private VBox page(String heading, String copy) { VBox page=new VBox(22); page.setAlignment(Pos.CENTER); page.setPadding(new Insets(34)); page.setMaxWidth(950); page.getStyleClass().add("page"); page.getChildren().addAll(label(heading,"page-title"), label(copy,"subtitle")); return page; }
    private Node backButton() { Button b=action("←  BACK TO MENU",this::showMenu); b.getStyleClass().add("back-button"); return b; }
    private VBox infoCard(String icon,String heading,String content) { VBox card=new VBox(9); card.getStyleClass().add("info-card"); card.getChildren().addAll(label(icon,"card-icon"),label(heading,"card-title"),label(content,"card-copy")); return card; }
    private CheckBox toggle(String text, boolean selected) { CheckBox box=new CheckBox(text); box.setSelected(selected); box.getStyleClass().add("toggle"); return box; }
    private Label hudStat(String top,String bottom) { Label result=new Label(top+"\n"+bottom); result.getStyleClass().add("hud-stat"); return result; }
    private Text label(String text,String style) { Text node=new Text(text); node.getStyleClass().add(style); return node; }
    private Button action(String text,Runnable event) { Button b=new Button(text); b.setMaxWidth(Double.MAX_VALUE); b.setOnAction(e->{ SoundService.get().click(); event.run(); }); b.getStyleClass().add("action-button"); return b; }
    private Region spacer(double height) { Region r=new Region(); r.setMinHeight(height); return r; }
    private StackPane screenBackdrop() { StackPane back=new StackPane(); back.getStyleClass().add("backdrop"); return back; }
    private void show(Node... nodes) { root.getChildren().setAll(nodes); fadeIn(nodes[nodes.length-1]); }
    private void fadeIn(Node node) { node.setOpacity(0); FadeTransition fade = new FadeTransition(Duration.millis(380), node); fade.setToValue(1); fade.play(); }
    private void floatingTitle(Node node) { TranslateTransition t=new TranslateTransition(Duration.seconds(2.2),node); t.setByY(-8); t.setAutoReverse(true); t.setCycleCount(Animation.INDEFINITE); t.play(); }
    private void pulse(Node node) { ScaleTransition s=new ScaleTransition(Duration.seconds(1.1),node); s.setToX(1.12);s.setToY(1.12);s.setAutoReverse(true);s.setCycleCount(Animation.INDEFINITE);s.play(); }
    private static final class StringPropertyLike { private String value; StringPropertyLike(String v){value=v;} String get(){return value;} void set(String v){value=v;} }
}
