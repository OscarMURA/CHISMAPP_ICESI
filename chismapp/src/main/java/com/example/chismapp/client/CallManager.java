package com.example.chismapp.client;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.TargetDataLine;
import java.util.Arrays;
import java.util.Base64;

public class CallManager {
    private ChatClient chatClient;
    public String currentCallParticipant;
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
        chatClient.sendMessage("CALL_INITIATE:" + recipient);  // Comando para iniciar la llamada
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
        startAudioSession();  // Iniciar la sesión de audio después de aceptar la llamada
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
        chatClient.sendMessage("CALL_END:" + participant);  // Comando para finalizar la llamada
        chatClient.displayMessage("SYSTEM: Llamada finalizada con " + participant);
        stopAudioSession();  // Detener la sesión de grabación de audio
        currentCallParticipant = null;
        if (chatClient.recordPlayer != null) {
            chatClient.recordPlayer.stopPlayback();  // Detener la reproducción
        }
    }

    // Manejar recepción de llamada iniciada
    public void handleIncomingCall(String caller) {
        acceptCall(caller);  // Aceptar automáticamente la llamada
    }

    // Manejar aceptación de llamada
    public void handleCallAccepted(String recipient) {
        currentCallParticipant = recipient;
        chatClient.displayMessage("SYSTEM: " + recipient + " aceptó tu llamada.");
        startAudioSession();
    }

    // Iniciar sesión de audio (capturar audio desde el micrófono y enviar)
    private void startAudioSession() {
        try {
            AudioFormat format = getAudioFormat();
            TargetDataLine microphone = AudioSystem.getTargetDataLine(format);
            microphone.open(format);
            microphone.start();  // Comenzar a capturar audio desde el micrófono

            // Crear el hilo de grabación
            recordThread = new Thread(() -> {
                try {
                    byte[] buffer = new byte[1024];  // Buffer para capturar datos del micrófono
                    while (!Thread.currentThread().isInterrupted() && currentCallParticipant != null) {
                        int bytesRead = microphone.read(buffer, 0, buffer.length);
                        if (bytesRead > 0) {
                            //System.out.println("Bytes capturados: " + bytesRead);  // Verificar cuántos bytes se están capturando
                            String encodedAudio = Base64.getEncoder().encodeToString(Arrays.copyOf(buffer, bytesRead));  // Codificar solo los bytes leídos
                            chatClient.sendMessage("VOICE:" + currentCallParticipant + ":" + encodedAudio);  // Enviar audio
                        }

                        if (Thread.currentThread().isInterrupted()) {
                            break;
                        }
                    }
                    microphone.close();
                    chatClient.displayMessage("SYSTEM: Sesión de audio detenida.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            recordThread.start();  // Iniciar la grabación en un hilo separado
            chatClient.displayMessage("SYSTEM: Sesión de audio iniciada.");

        } catch (Exception e) {
            chatClient.displayMessage("SYSTEM: No se pudo iniciar la sesión de audio.");
            e.printStackTrace();
        }
    }

    // Detener la sesión de audio
    public void stopAudioSession() {
        if (recordThread != null && recordThread.isAlive()) {
            recordThread.interrupt();  // Interrumpir el hilo de grabación
            try {
                recordThread.join();  // Esperar a que el hilo termine
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
        float sampleRate = 16000.0F;  // Frecuencia de muestreo de 16 kHz
        int sampleSizeInBits = 16;
        int channels = 1;  // Mono
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }
}
