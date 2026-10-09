# JavaFX Media Player

A starter project for the BIIM3210 Assignment 2 brief. It includes audio/video playback, a playlist, file add/remove controls, and keyboard shortcuts.

## Requirements

- JDK 17 or newer
- Maven 3.8 or newer
- Internet access the first time Maven downloads JavaFX dependencies

## Run

Open a terminal in this folder and run:

```powershell
mvn javafx:run
```

Select **Add files**, choose one or more supported media files, then select a playlist item to start it.

## Controls

- Space: play/pause
- S: stop
- N: next playlist item
- P: previous playlist item
- Up / Down: volume up/down
- M: mute/unmute

The Next and Previous controls wrap around at the ends of the playlist. Selecting a playlist item loads and starts that file. Removing the current item clears its player.

## Notes for your submission

This is a learning starter, not a finished assignment submission. Read the methods and adapt the interface, error handling, comments, and features yourself. Be prepared to explain how `Media`, `MediaPlayer`, `MediaView`, the `ListView`, and the key event handler work. JavaFX codec support depends on the operating system and installed JavaFX runtime; if a file does not play, try MP3, WAV, or MP4 media supported by your setup.
