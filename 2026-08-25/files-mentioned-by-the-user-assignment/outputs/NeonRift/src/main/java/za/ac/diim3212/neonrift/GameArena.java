package za.ac.diim3212.neonrift;

import javafx.animation.AnimationTimer;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/** A compact playable arcade mission used by the Game Screen. */
public final class GameArena extends javafx.scene.layout.Pane {
    public interface Listener {
        void update(int score, int shields, int collected, int target);
        void complete(int score);
    }

    private final Random random = new Random();
    private final Group ship = new Group();
    private final List<Circle> shards = new ArrayList<>();
    private final List<Circle> hazards = new ArrayList<>();
    private final Listener listener;
    private final AnimationTimer loop;
    private double horizontal, vertical;
    private int score = 0, shields = 3, collected = 0;
    private boolean paused, started, finished;

    public GameArena(Listener listener) {
        this.listener = listener;
        getStyleClass().add("arena");
        setMinSize(300, 320);
        buildShip();
        getChildren().add(ship);
        for (int i = 0; i < 12; i++) addShard();
        for (int i = 0; i < 5; i++) addHazard();
        loop = new AnimationTimer() {
            @Override public void handle(long now) { tick(); }
        };
        loop.start();
    }

    public void move(double x, double y) { horizontal = x; vertical = y; }
    public void stopMoving() { horizontal = vertical = 0; }
    public void togglePause() { paused = !paused; stopMoving(); }
    public boolean isPaused() { return paused; }

    private void buildShip() {
        Polygon body = new Polygon(0, -25, 18, 20, 0, 12, -18, 20);
        body.getStyleClass().add("ship");
        Circle core = new Circle(5, Color.web("#fff3a1"));
        core.setCenterY(4);
        Rectangle flame = new Rectangle(-5, 17, 10, 16);
        flame.setArcWidth(8); flame.setArcHeight(8); flame.setFill(Color.web("#c063ff"));
        ship.getChildren().addAll(flame, body, core);
    }

    private void addShard() {
        Circle shard = new Circle(7, Color.web("#71efff"));
        shard.getStyleClass().add("shard");
        place(shard, 40, 40);
        shards.add(shard); getChildren().add(shard);
    }

    private void addHazard() {
        Circle hazard = new Circle(13, Color.web("#f465a7"));
        hazard.getStyleClass().add("hazard");
        place(hazard, 70, 100);
        hazard.setUserData(1.2 + random.nextDouble() * 1.8);
        hazards.add(hazard); getChildren().add(hazard);
    }

    private void place(Shape object, double xPadding, double yPadding) {
        object.setLayoutX(xPadding + random.nextDouble() * Math.max(1, getWidth() - xPadding * 2));
        object.setLayoutY(yPadding + random.nextDouble() * Math.max(1, getHeight() - yPadding * 2));
    }

    private void tick() {
        if (paused || finished || getWidth() < 100 || getHeight() < 100) return;
        if (!started) {
            ship.setLayoutX(getWidth() / 2); ship.setLayoutY(getHeight() - 65);
            shards.forEach(shard -> place(shard, 40, 45));
            hazards.forEach(hazard -> place(hazard, 70, 100));
            started = true;
        }
        ship.setLayoutX(clamp(ship.getLayoutX() + horizontal * 5, 24, getWidth() - 24));
        ship.setLayoutY(clamp(ship.getLayoutY() + vertical * 5, 36, getHeight() - 35));
        for (Circle hazard : hazards) {
            hazard.setLayoutY(hazard.getLayoutY() + (double) hazard.getUserData());
            if (hazard.getLayoutY() > getHeight() + 20) { hazard.setLayoutY(-20); hazard.setLayoutX(35 + random.nextDouble() * (getWidth() - 70)); }
        }
        collectShards();
        hitHazards();
    }

    private void collectShards() {
        Iterator<Circle> iterator = shards.iterator();
        while (iterator.hasNext()) {
            Circle shard = iterator.next();
            if (ship.getBoundsInParent().intersects(shard.getBoundsInParent())) {
                getChildren().remove(shard); iterator.remove(); collected++; score += 500; SoundService.get().collect();
                listener.update(score, shields, collected, 12);
                if (collected == 12) { finished = true; listener.complete(score + shields * 300); }
            }
        }
    }

    private void hitHazards() {
        for (Circle hazard : hazards) {
            if (ship.getBoundsInParent().intersects(hazard.getBoundsInParent())) {
                shields--; score = Math.max(0, score - 250); SoundService.get().hit();
                hazard.setLayoutY(-25); hazard.setLayoutX(35 + random.nextDouble() * (getWidth() - 70));
                listener.update(score, shields, collected, 12);
                if (shields == 0) { finished = true; listener.complete(score); }
                break;
            }
        }
    }

    private double clamp(double value, double min, double max) { return Math.max(min, Math.min(max, value)); }
}
