package za.ac.limkokwing.mediaplayer;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
public class MediaPlayerApp extends Application {
    private final ObservableList<Path> playlist = FXCollections.observableArrayList();
    private ListView<Path> playlistView;
    private MediaView mediaView;
    private Label statusLabel;
    private Slider volumeSlider;
    private MediaPlayer mediaPlayer;
    private boolean muted;
    private double volumeBeforeMute = 0.5;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Interactive Multimedia Player");

        mediaView = new MediaView();
        mediaView.setPreserveRatio(true);
        StackPane videoArea = new StackPane(mediaView);
        videoArea.getStyleClass().add("media-area");
        videoArea.setMinHeight(300);

        playlistView = new ListView<>(playlist);
        playlistView.setPlaceholder(new Label("Add audio or video files to begin"));
        playlistView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Path path, boolean empty) {
                super.updateItem(path, empty);
                setText(empty || path == null ? null : path.getFileName().toString());
            }
        });
        playlistView.getSelectionModel().selectedItemProperty().addListener((obs, oldFile, newFile) -> {
            if (newFile != null) {
                loadAndPlay(newFile);
            }
        });

        Button addButton = new Button("Add files");
        Button removeButton = new Button("Remove selected");
        addButton.setOnAction(event -> addFiles(stage));
        removeButton.setOnAction(event -> removeSelected());

        Button previousButton = new Button("Previous");
        Button playButton = new Button("Play");
        playButton.getStyleClass().add("primary-button");
        Button pauseButton = new Button("Pause");
        Button stopButton = new Button("Stop");
        Button nextButton = new Button("Next");
        Button muteButton = new Button("Mute");
        previousButton.setOnAction(event -> playRelative(-1));
        playButton.setOnAction(event -> play());
        pauseButton.setOnAction(event -> pause());
        stopButton.setOnAction(event -> stopPlayback());
        nextButton.setOnAction(event -> playRelative(1));
        muteButton.setOnAction(event -> toggleMute());

        volumeSlider = new Slider(0, 1, 0.5);
        volumeSlider.setPrefWidth(130);
        volumeSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newValue.doubleValue());
                mediaPlayer.setMute(false);
            }
            if (newValue.doubleValue() > 0) {
                volumeBeforeMute = newValue.doubleValue();
                muted = false;
                muteButton.setText("Mute");
            }
        });

        HBox transport = new HBox(8, previousButton, playButton, pauseButton, stopButton, nextButton,
                new Label("Volume"), volumeSlider, muteButton);
        transport.getStyleClass().add("transport-bar");
        transport.setAlignment(Pos.CENTER);
        HBox fileActions = new HBox(8, addButton, removeButton);
        fileActions.getStyleClass().add("file-actions");
        fileActions.setAlignment(Pos.CENTER_LEFT);
        statusLabel = new Label("Ready — use Add files to choose media");
        statusLabel.getStyleClass().add("status-label");
        Label playlistTitle = new Label("Playlist");
        playlistTitle.getStyleClass().add("panel-title");
        VBox playlistPanel = new VBox(8, playlistTitle, playlistView, fileActions);
        playlistPanel.getStyleClass().add("playlist-panel");
        playlistPanel.setPadding(new Insets(12));
        playlistPanel.setPrefWidth(270);
        VBox.setVgrow(playlistView, Priority.ALWAYS);

        BorderPane root = new BorderPane();
        root.setCenter(videoArea);
        root.setRight(playlistPanel);
        root.setBottom(new VBox(8, transport, statusLabel));
        BorderPane.setMargin(videoArea, new Insets(12, 0, 12, 12));
        root.getBottom().setStyle("-fx-padding: 8 12 12 12;");
        root.setFocusTraversable(true);

        Scene scene = new Scene(root, 1000, 620);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        scene.setOnKeyPressed(event -> {
            KeyCode key = event.getCode();
            if (key == KeyCode.SPACE) {
                togglePlayPause();
            } else if (key == KeyCode.S) {
                stopPlayback();
            } else if (key == KeyCode.N) {
                playRelative(1);
            } else if (key == KeyCode.P) {
                playRelative(-1);
            } else if (key == KeyCode.UP) {
                adjustVolume(0.05);
            } else if (key == KeyCode.DOWN) {
                adjustVolume(-0.05);
            } else if (key == KeyCode.M) {
                toggleMute();
            } else {
                return;
            }
            event.consume();
        });

        stage.setScene(scene);
        stage.show();
        root.requestFocus();
    }

    private void addFiles(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose media files");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Audio and video", "*.mp3", "*.wav", "*.aiff", "*.mp4", "*.m4v"),
                new FileChooser.ExtensionFilter("All files", "*.*"));
        List<File> selectedFiles = chooser.showOpenMultipleDialog(stage);
        if (selectedFiles != null) {
            for (File file : selectedFiles) {
                playlist.add(file.toPath());
            }
            if (playlistView.getSelectionModel().getSelectedIndex() < 0 && !playlist.isEmpty()) {
                playlistView.getSelectionModel().select(0);
            }
        }
    }

    private void removeSelected() {
        int selected = playlistView.getSelectionModel().getSelectedIndex();
        if (selected < 0) {
            statusLabel.setText("Select a playlist item to remove it.");
            return;
        }
        boolean removingCurrent = playlist.get(selected).equals(currentFile);
        if (removingCurrent) {
            disposePlayer();
            mediaView.setMediaPlayer(null);
            currentFile = null;
        }
        playlist.remove(selected);
        if (playlist.isEmpty()) {
            statusLabel.setText("Playlist is empty.");
        }
    }

    private Path currentFile;

    private void loadAndPlay(Path file) {
        disposePlayer();
        currentFile = file;
        try {
            Media media = new Media(file.toUri().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);
            mediaPlayer.setVolume(muted ? 0 : volumeSlider.getValue());
            mediaPlayer.setOnReady(() -> {
                Duration duration = media.getDuration();
                mediaView.setFitWidth(Math.max(400, mediaView.getScene().getWidth() - 300));
                statusLabel.setText("Playing: " + file.getFileName()
                        + (duration != null && !duration.isUnknown() ? " (" + formatTime(duration) + ")" : ""));
                mediaPlayer.play();
            });
            mediaPlayer.setOnEndOfMedia(() -> playRelative(1));
            mediaPlayer.setOnError(() -> showMediaError(file));
        } catch (MediaException | IllegalArgumentException ex) {
            statusLabel.setText("Could not open: " + file.getFileName());
            showAlert("Unable to play media", ex.getMessage());
        }
    }

    private void play() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        } else if (!playlist.isEmpty()) {
            int selected = playlistView.getSelectionModel().getSelectedIndex();
            playlistView.getSelectionModel().select(selected < 0 ? 0 : selected);
        } else {
            statusLabel.setText("Add a media file first.");
        }
    }

    private void pause() {
        if (mediaPlayer != null) mediaPlayer.pause();
    }

    private void stopPlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            statusLabel.setText("Stopped: " + currentFile.getFileName());
        }
    }

    private void togglePlayPause() {
        if (mediaPlayer == null) {
            play();
        } else if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            pause();
        } else {
            mediaPlayer.play();
        }
    }

    private void playRelative(int offset) {
        if (playlist.isEmpty()) return;
        int selected = playlistView.getSelectionModel().getSelectedIndex();
        if (selected < 0) selected = 0;
        int next = Math.floorMod(selected + offset, playlist.size());
        playlistView.getSelectionModel().select(next);
        playlistView.scrollTo(next);
    }

    private void adjustVolume(double amount) {
        double newVolume = Math.max(0, Math.min(1, volumeSlider.getValue() + amount));
        volumeSlider.setValue(newVolume);
        statusLabel.setText("Volume: " + Math.round(newVolume * 100) + "%");
    }

    private void toggleMute() {
        muted = !muted;
        if (muted) {
            if (volumeSlider.getValue() > 0) volumeBeforeMute = volumeSlider.getValue();
            if (mediaPlayer != null) mediaPlayer.setMute(true);
            statusLabel.setText("Muted");
        } else {
            if (mediaPlayer != null) mediaPlayer.setMute(false);
            if (volumeSlider.getValue() == 0) volumeSlider.setValue(volumeBeforeMute);
            statusLabel.setText("Unmuted");
        }
    }

    private void showMediaError(Path file) {
        String message = mediaPlayer == null || mediaPlayer.getError() == null
                ? "The format may not be supported by the installed JavaFX media codecs."
                : mediaPlayer.getError().getMessage();
        statusLabel.setText("Playback error: " + file.getFileName());
        showAlert("Media playback error", message);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message == null ? "Check that the file is valid and supported." : message);
        alert.show();
    }

    private String formatTime(Duration duration) {
        long seconds = (long) duration.toSeconds();
        return String.format("%d:%02d", seconds / 60, seconds % 60);
    }

    private void disposePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }

    @Override
    public void stop() {
        disposePlayer();
    }

    public static void main(String[] args) {
        launch(args);
    }
}


