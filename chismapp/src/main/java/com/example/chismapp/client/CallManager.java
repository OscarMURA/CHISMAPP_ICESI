package com.example.chismapp.client;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.TargetDataLine;

import com.example.chismapp.util.eTypeRecord;

import java.util.Arrays;
import java.util.Base64;

/**
 * The {@code CallManager} class is responsible for managing audio call sessions between users.
 * It provides functionality to initiate, accept, reject, and end calls, as well as managing audio capture and transmission during a call.
 * The class interacts with the {@code ChatClient} for sending messages related to call state and session management.
 */
public class CallManager {
    private ChatClient chatClient;
    public String currentCallParticipant;
    private Thread recordThread;

    /**
     * Constructs a new {@code CallManager} with the specified {@code ChatClient}.
     *
     * @param chatClient the {@code ChatClient} instance used to communicate call state and send audio messages.
     */
    public CallManager(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Initiates a call to the specified recipient.
     * If a call is already active, it informs the user that they are already in a call.
     *
     * @param recipient the user to call.
     */
    public void initiateCall(String recipient) {
        if (currentCallParticipant != null) {
            chatClient.displayMessage("SYSTEM: Ya estás en una llamada con " + currentCallParticipant);
            return;
        }
        chatClient.sendMessage("CALL_INITIATE:" + recipient);
        chatClient.displayMessage("SYSTEM: Iniciando llamada a " + recipient);
    }

    /**
     * Accepts an incoming call from the specified caller.
     * If the user is already in a call, the incoming call will be rejected automatically.
     *
     * @param caller the user who is calling.
     */
    public void acceptCall(String caller) {
        if (currentCallParticipant != null) {
            chatClient.displayMessage("SYSTEM: Ya estás en una llamada con " + currentCallParticipant);
            sendRejectCall(caller);
            return;
        }
        chatClient.sendMessage("CALL_ACCEPT:" + caller);
        chatClient.getRecorder().addMessage("Called by: " + caller, eTypeRecord.RECEIVED);
        currentCallParticipant = caller;
        chatClient.displayMessage("SYSTEM: Llamada aceptada con " + caller);
        chatClient.getRecorder().addMessage("Call accepted with " + caller, eTypeRecord.RECEIVED);

        if (chatClient.recordPlayer != null) {
            chatClient.recordPlayer.stopPlayback();
            chatClient.recordPlayer.restartPlayback();
        }
        startAudioSession();
    }

    /**
     * Rejects an incoming call from the specified caller.
     *
     * @param caller the user who initiated the call.
     */
    public void rejectCall(String caller) {
        chatClient.sendMessage("CALL_REJECT:" + caller);
        chatClient.displayMessage("SYSTEM: Llamada rechazada con " + caller);
    }

    /**
     * Ends the active call with the specified participant.
     * If no active call exists, the user is informed that there is no ongoing call with the participant.
     *
     * @param participant the user with whom the call is being ended.
     */
    public void endCall(String participant) {
        if (currentCallParticipant == null || !currentCallParticipant.equals(participant)) {
            chatClient.displayMessage("SYSTEM: No tienes una llamada activa con " + participant);
            return;
        }
        chatClient.sendMessage("CALL_END:" + participant);
        handleCallEnded(participant);
    }

    /**
     * Handles an incoming call request from the specified caller.
     * Displays a message to the user with instructions to accept or reject the call.
     *
     * @param caller the user who is calling.
     */
    public void handleIncomingCall(String caller) {
        chatClient.displayMessage("SYSTEM: Recibiste una solicitud de llamada de " + caller + ". Usa /acceptcall o /rejectcall para responder.");
        chatClient.getRecorder().addMessage("Received a call by " + caller, eTypeRecord.RECEIVED);
    }

    /**
     * Handles the termination of a call with the specified participant.
     * Resets the call state, stops audio capture, and clears the current participant.
     *
     * @param participant the user with whom the call ended.
     */
    public void handleCallEnded(String participant) {
        chatClient.displayMessage("SYSTEM: Llamada finalizada con " + participant);
        chatClient.getRecorder().addMessage("Call ended with " + participant, eTypeRecord.RECEIVED);
        currentCallParticipant = null;
        stopAudioSession();
        if (chatClient.recordPlayer != null) {
            chatClient.recordPlayer.stopPlayback();
        }
    }

    /**
     * Handles the acceptance of a call by the specified recipient.
     * Updates the current call state and initiates an audio session.
     *
     * @param recipient the user who accepted the call.
     */
    public void handleCallAccepted(String recipient) {
        currentCallParticipant = recipient;
        chatClient.displayMessage("SYSTEM: " + recipient + " aceptó tu llamada.");
        chatClient.getRecorder().addMessage("Accepted call", eTypeRecord.RECEIVED);
        startAudioSession();
    }

    /**
     * Stops the current audio session, interrupting the recording thread if it is running.
     * Ensures that resources are properly cleaned up and the audio system is stopped.
     */
    public void stopAudioSession() {
        if (recordThread != null && recordThread.isAlive()) {
            recordThread.interrupt();
            try {
                recordThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            recordThread = null;
        }
        chatClient.displayMessage("SYSTEM: Sesión de audio detenida.");
    }

    /**
     * Starts an audio session by capturing audio from the microphone and sending it to the participant.
     * The session runs in a separate thread for continuous audio capture and transmission.
     */
    private void startAudioSession() {
        stopAudioSession();
        try {
            AudioFormat format = getAudioFormat();
            TargetDataLine microphone = AudioSystem.getTargetDataLine(format);
            microphone.open(format);
            microphone.start();

            recordThread = new Thread(() -> {
                try {
                    byte[] buffer = new byte[1024];
                    while (!Thread.currentThread().isInterrupted() && currentCallParticipant != null) {
                        int bytesRead = microphone.read(buffer, 0, buffer.length);
                        if (bytesRead > 0) {
                            String encodedAudio = Base64.getEncoder().encodeToString(Arrays.copyOf(buffer, bytesRead));
                            chatClient.sendMessage("VOICE:" + currentCallParticipant + ":" + encodedAudio);
                        }
                        if (Thread.currentThread().isInterrupted()) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (microphone != null && microphone.isOpen()) {
                        microphone.stop();
                        microphone.close();
                    }
                    chatClient.displayMessage("SYSTEM: Sesión de audio detenida.");
                }
            });

            recordThread.start();
            chatClient.displayMessage("SYSTEM: Sesión de audio iniciada.");

        } catch (Exception e) {
            chatClient.displayMessage("SYSTEM: No se pudo iniciar la sesión de audio.");
            e.printStackTrace();
        }
    }

    /**
     * Sends a rejection message for a call from the specified caller.
     *
     * @param caller the user whose call is being rejected.
     */
    private void sendRejectCall(String caller) {
        rejectCall(caller);
    }

    /**
     * Returns the {@code AudioFormat} to be used for capturing audio.
     * The format has a sample rate of 16 kHz, 16-bit samples, mono channel, signed data, and little-endian byte order.
     *
     * @return the {@code AudioFormat} for audio capture.
     */
    private AudioFormat getAudioFormat() {
        float sampleRate = 16000.0F;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }
}