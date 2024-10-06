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
        initializePlayback();
    }

    // Inicializar la línea de salida de audio
    private void initializePlayback() {
        try {
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
            System.out.println("La reproducción de audio no está activa. Reiniciando...");
            restartPlayback();  // Reiniciar si la reproducción no está activa
        }
        try {
            out.write(audioData, 0, audioData.length);  // Escribir los datos directamente
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

    // Método para reiniciar la reproducción
    public void restartPlayback() {
        stopPlayback(); // Detener la reproducción si está activa
        try {
            if (out != null) {
                out.open(format);  // Asegurarse de abrir el formato de audio nuevamente
                out.start();  // Comenzar a reproducir
                isPlaying = true;  // Marcar que la reproducción está activa
                System.out.println("Playback restarted.");
            } else {
                initializePlayback(); // Si `out` es null, vuelve a inicializarlo
            }
        } catch (Exception e) {
            System.out.println("Error al reiniciar la reproducción: " + e.getMessage());
            initializePlayback(); // Intentar reiniciar si hay un problema
        }
    }

}
