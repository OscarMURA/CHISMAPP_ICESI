// File: ChatClient.java
package com.example.chismapp.client;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Base64;

import javax.sound.sampled.AudioFormat;

import com.example.chismapp.util.TCPConnection;

public class ChatClient {

    private static CallManager callManager;
    private static TCPConnection clientConnection;

    public static void main(String[] args) {
        // Crear una instancia de ClientDiscovery y buscar el servidor
        ClientDiscovery discovery = new ClientDiscovery();
        discovery.discoverServer();  // Descubrir el servidor automáticamente

        String serverIp = discovery.getServerIp();
        int serverPort = discovery.getServerPort();

        // Comprobar si se encontró la IP del servidor
        if (serverIp == null) {
            System.err.println("Could not find the server. Please make sure the server is running and discoverable.");
            return;
        }

        // Inicializar la conexión del cliente con la IP y el puerto del servidor descubierto
        clientConnection = TCPConnection.getInstance();
        clientConnection.initAsClient(serverIp, serverPort);

        // Inicializar CallManager
        ChatClient chatClient = new ChatClient();
        callManager = new CallManager(chatClient);

        // Asignar un listener para manejar mensajes del servidor
        clientConnection.setListener(message -> {
            if (message.startsWith("VOICE:")) {
                handleVoiceMessage(message);
            } else if (message.startsWith("CALL_INITIATED:")) {
                String caller = message.substring("CALL_INITIATED:".length()).trim();
                callManager.handleIncomingCall(caller);
            } else if (message.startsWith("CALL_ACCEPTED:")) {
                String recipient = message.substring("CALL_ACCEPTED:".length()).trim();
                callManager.handleCallAccepted(recipient);
            } else if (message.startsWith("CALL_REJECTED:")) {
                String recipient = message.substring("CALL_REJECTED:".length()).trim();
                callManager.handleCallRejected(recipient);
            } else if (message.startsWith("CALL_ENDED:")) {
                String participant = message.substring("CALL_ENDED:".length()).trim();
                callManager.handleCallEnded(participant);
            } else {
                System.out.println(message);
            }
        });

        clientConnection.start();

        // Manejar la entrada del cliente
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.println("Enter your username:");
            String clientName = reader.readLine();

            clientConnection.sendMessage("USERNAME:" + clientName);  // Enviar el nombre de usuario del cliente al servidor

            // Mostrar comandos disponibles
            System.out.println("Available commands:");
            System.out.println("/group group_name - To create/join a group");
            System.out.println("/message group_name <message> - To send a message to a group");
            System.out.println("/dm username <message> - To send a direct message to a user");
            System.out.println("/voice <username|group_name> - To send a voice message");
            System.out.println("/call <username> - To initiate a call to a user");
            System.out.println("/endcall <username> - To end a call with a user");

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("/group")) {
                    clientConnection.sendMessage(line);  // Enviar comando para crear un grupo
                } else if (line.startsWith("/message")) {
                    clientConnection.sendMessage(line);  // Enviar un mensaje a un grupo
                } else if (line.startsWith("/dm")) {
                    clientConnection.sendMessage(line);  // Enviar un mensaje directo
                } else if (line.startsWith("/voice")) {
                    handleVoiceCommand(line, clientConnection);
                } else if (line.startsWith("/call")) {
                    handleCallCommand(line);
                } else if (line.startsWith("/endcall")) {
                    handleEndCallCommand(line);
                } else {
                    System.out.println("Invalid command. Use /group, /message, /dm, /voice, /call, or /endcall.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Manejar el comando de llamada
    private static void handleCallCommand(String command) {
        // Formato: /call <username>
        String[] parts = command.split(" ", 2);
        if (parts.length < 2) {
            System.out.println("Usage: /call <username>");
            return;
        }
        String recipient = parts[1].trim();
        callManager.initiateCall(recipient);
    }

    // Manejar el comando de finalizar llamada
    private static void handleEndCallCommand(String command) {
        // Formato: /endcall <username>
        String[] parts = command.split(" ", 2);
        if (parts.length < 2) {
            System.out.println("Usage: /endcall <username>");
            return;
        }
        String participant = parts[1].trim();
        callManager.endCall(participant);
    }

    // Manejar el envío de mensajes de voz
    private static void handleVoiceCommand(String command, TCPConnection clientConnection) {
        String[] parts = command.split(" ", 2);
        if (parts.length < 2) {
            System.out.println("Usage: /voice <username|group_name>");
            return;
        }
        String recipient = parts[1];

        // Configurar el formato de audio
        AudioFormat format = getAudioFormat();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        RecordAudio recorder = new RecordAudio(format, out);
        Thread recordThread = new Thread(recorder);
        recordThread.start();

        System.out.println("Recording... Press Enter to stop.");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        recorder.stopRecording();
        try {
            recordThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Obtener los datos de audio
        byte[] audioData = out.toByteArray();
        String encodedAudio = Base64.getEncoder().encodeToString(audioData);

        // Enviar el mensaje de voz
        String voiceMessage = "VOICE:" + recipient + ":" + encodedAudio;
        clientConnection.sendMessage(voiceMessage);
        System.out.println("Voice message sent to " + recipient);
    }

    // Manejar la recepción de mensajes de voz
    private static void handleVoiceMessage(String message) {
        // Formato: VOICE:<sender>:<data_audio_base64>
        String[] parts = message.split(":", 3);
        if (parts.length < 3) {
            System.out.println("Received malformed voice message.");
            return;
        }
        String sender = parts[1];
        String encodedAudio = parts[2];
        byte[] audioData = Base64.getDecoder().decode(encodedAudio);

        // Reproducir el audio
        RecordPlayer player = new RecordPlayer(getAudioFormat());
        player.initiateAudio(audioData);
        System.out.println("Received voice message from " + sender);
    }

    private static AudioFormat getAudioFormat() {
        float sampleRate = 16000.0F;
        int sampleSizeInBits = 16;
        int channels = 1; // Mono
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }

    // Métodos para interactuar con CallManager desde otras clases
    public void displayMessage(String message) {
        System.out.println(message);
    }

    public void sendMessage(String message) {
        clientConnection.sendMessage(message);
    }
}
