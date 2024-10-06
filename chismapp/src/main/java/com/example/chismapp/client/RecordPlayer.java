package com.example.chismapp.client;

import java.io.ByteArrayInputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

/**
 * The {@code RecordPlayer} class handles audio playback using a specified {@code AudioFormat}.
 * It initializes a {@code SourceDataLine} to send audio data to the system's sound card and provides methods to play, stop, and restart audio playback.
 */
public class RecordPlayer {

    private AudioFormat format;
    private SourceDataLine out;  // Output to the audio system
    private boolean isPlaying = false;  // Indicates whether audio playback is active

    /**
     * Constructs a new {@code RecordPlayer} with the specified audio format.
     * It initializes the audio playback system using the provided format.
     *
     * @param format the {@code AudioFormat} to be used for audio playback.
     */
    public RecordPlayer(AudioFormat format) {
        this.format = format;
        initializePlayback();
    }

    /**
     * Initializes the audio playback system by opening a {@code SourceDataLine} with the specified format.
     * The method sets up the line for playing audio and marks the playback state as active.
     */
    private void initializePlayback() {
        try {
            out = AudioSystem.getSourceDataLine(format);
            out.open(format);
            out.start();
            isPlaying = true;
            System.out.println("Audio playback started");
        } catch (Exception e) {
            System.out.println("Error initializing RecordPlayer: " + e.getMessage());
        }
    }

    /**
     * Plays the provided audio data. If playback is not active, it attempts to restart playback.
     * The audio data is written directly to the {@code SourceDataLine} to be played.
     *
     * @param audioData the byte array containing the audio data to play.
     */
    public synchronized void initiateAudio(byte[] audioData) {
        if (!isPlaying) {
            System.out.println("Audio playback is not active. Restarting...");
            restartPlayback();  // Restart playback if it is not currently active
        }
        try {
            out.write(audioData, 0, audioData.length);  // Write audio data directly to the audio output line
        } catch (Exception e) {
            System.out.println("Error during audio playback: " + e.getMessage());
        }
    }

    /**
     * Stops the current audio playback by draining and closing the {@code SourceDataLine}.
     * This method ensures that all buffered audio is played before stopping the playback.
     */
    public void stopPlayback() {
        if (out != null) {
            try {
                out.drain();  // Ensure all audio data is played before stopping
                out.stop();
                out.close();
                isPlaying = false;
                System.out.println("Playback stopped.");
            } catch (Exception e) {
                System.out.println("Error stopping playback: " + e.getMessage());
            }
        }
    }

    /**
     * Restarts the audio playback by stopping any active playback and reinitializing the audio line.
     * If the playback line is not available, it attempts to reinitialize it.
     */
    public void restartPlayback() {
        stopPlayback();  // Stop current playback if active
        try {
            if (out != null) {
                out.open(format);  // Open the audio format again
                out.start();  // Start playback
                isPlaying = true;
                System.out.println("Playback restarted.");
            } else {
                initializePlayback();  // Reinitialize playback if `out` is null
            }
        } catch (Exception e) {
            System.out.println("Error restarting playback: " + e.getMessage());
            initializePlayback();  // Try to reinitialize playback if an error occurs
        }
    }
}
