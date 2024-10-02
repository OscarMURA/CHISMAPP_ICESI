package com.example.chismapp.client;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Base64;

import javax.sound.sampled.AudioFormat;

import com.example.chismapp.util.TCPConnection;

public class ChatClient {

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
        TCPConnection clientConnection = TCPConnection.getInstance();
        clientConnection.initAsClient(serverIp, serverPort);

        // Asignar un listener para manejar mensajes del servidor
        clientConnection.setListener(message -> {
            if (message.startsWith("VOICE:")) {
                handleVoiceMessage(message);
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
                } else {
                    System.out.println("Invalid command. Use /group, /message, /dm, or /voice.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
}
