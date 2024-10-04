package com.example.chismapp.client;

import java.io.ByteArrayInputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

public class RecordPlayer {
    private AudioFormat format;
    private SourceDataLine out;  // salida a la tarjeta de audio
    private boolean isPlaying = false;

    public RecordPlayer(AudioFormat format) {
        this.format = format;
        try {
            // Abrir y empezar la línea de salida de audio
            out = AudioSystem.getSourceDataLine(format);
            out.open(format);
            out.start();
            isPlaying = true;
            System.out.println("Audio playback started");
        } catch (Exception e) {
            System.out.println("Error al inicializar RecordPlayer: " + e.getMessage());
        }
    }

    public synchronized void initiateAudio(byte[] audioData) {
        if (!isPlaying) {
            System.out.println("La reproducción de audio no está activa.");
            return;
        }
        try {
            out.write(audioData, 0, audioData.length);  // Escribir los datos directamente
            System.out.println("Audio written to SourceDataLine: " + audioData.length + " bytes");
        } catch (Exception e) {
            System.out.println("Error durante la reproducción de audio: " + e.getMessage());
        }
    }

    public void stopPlayback() {
        if (out != null) {
            try {
                out.drain();
                out.stop();
                out.close();
                isPlaying = false;
                System.out.println("Playback stopped.");
            } catch (Exception e) {
                System.out.println("Error al detener la reproducción: " + e.getMessage());
            }
        }
    }
}
