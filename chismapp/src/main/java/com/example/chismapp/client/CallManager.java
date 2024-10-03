// File: CallManager.java
package com.example.chismapp.client;

import javax.sound.sampled.AudioFormat;
import java.io.ByteArrayOutputStream;

public class CallManager {
    private ChatClient chatClient;
    public String currentCallParticipant;
    private RecordAudio recorder;
    public RecordPlayer player;
    private Thread recordThread;

    public CallManager(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    // Iniciar una llamada a un usuario específico
    public void initiateCall(String recipient) {
        if (currentCallParticipant != null) {
            chatClient.displayMessage("SYSTEM: Ya estás en una llamada con " + currentCallParticipant);
            return;
        }
        chatClient.sendMessage("CALL_INITIATE:" + recipient);
        chatClient.displayMessage("SYSTEM: Iniciando llamada a " + recipient);
    }

    // Aceptar una llamada entrante
    public void acceptCall(String caller) {
        if (currentCallParticipant != null) {
            chatClient.displayMessage("SYSTEM: Ya estás en una llamada con " + currentCallParticipant);
            sendRejectCall(caller);
            return;
        }
        chatClient.sendMessage("CALL_ACCEPT:" + caller);
        currentCallParticipant = caller;
        chatClient.displayMessage("SYSTEM: Llamada aceptada con " + caller);
        startAudioSession();
    }

    // Rechazar una llamada entrante
    public void rejectCall(String caller) {
        chatClient.sendMessage("CALL_REJECT:" + caller);
        chatClient.displayMessage("SYSTEM: Llamada rechazada con " + caller);
    }

    // Finalizar una llamada activa
    public void endCall(String participant) {
        if (currentCallParticipant == null || !currentCallParticipant.equals(participant)) {
            chatClient.displayMessage("SYSTEM: No tienes una llamada activa con " + participant);
            return;
        }
        chatClient.sendMessage("CALL_END:" + participant);
        chatClient.displayMessage("SYSTEM: Llamada finalizada con " + participant);
        currentCallParticipant = null;
        stopAudioSession();
    }

    // Manejar recepción de llamada iniciada
    public void handleIncomingCall(String caller) {
        acceptCall(caller); // Auto-aceptar para simplificar
    }

    // Manejar aceptación de llamada
    public void handleCallAccepted(String recipient) {
        currentCallParticipant = recipient;
        chatClient.displayMessage("SYSTEM: " + recipient + " aceptó tu llamada.");
        startAudioSession();
    }

    // Manejar rechazo de llamada
    public void handleCallRejected(String recipient) {
        chatClient.displayMessage("SYSTEM: " + recipient + " rechazó tu llamada.");
    }

    // Manejar finalización de llamada
    public void handleCallEnded(String participant) {
        chatClient.displayMessage("SYSTEM: Llamada finalizada con " + participant);
        currentCallParticipant = null;
        stopAudioSession();
    }

    // Iniciar sesión de audio
    private void startAudioSession() {
        try {
            AudioFormat format = getAudioFormat();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            recorder = new RecordAudio(format, out);
            player = new RecordPlayer(format);

            recordThread = new Thread(recorder);
            recordThread.start();

            chatClient.displayMessage("SYSTEM: Sesión de audio iniciada.");
        } catch (Exception e) {
            chatClient.displayMessage("SYSTEM: No se pudo iniciar la sesión de audio.");
            e.printStackTrace();
        }
    }

    // Detener sesión de audio
    private void stopAudioSession() {
        if (recorder != null) {
            recorder.stopRecording();
        }
        if (recordThread != null && recordThread.isAlive()) {
            try {
                recordThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        chatClient.displayMessage("SYSTEM: Sesión de audio detenida.");
    }

    // Enviar mensaje de rechazo de llamada
    private void sendRejectCall(String caller) {
        rejectCall(caller);
    }

    // Obtener formato de audio
    private AudioFormat getAudioFormat() {
        float sampleRate = 16000.0F;
        int sampleSizeInBits = 16;
        int channels = 1; // Mono
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }
}
