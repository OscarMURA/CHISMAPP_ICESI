package com.example.chismapp.client;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Base64;

import javax.sound.sampled.AudioFormat;

import com.example.chismapp.util.HistorialRecorder;
import com.example.chismapp.util.TCPConnection;
import com.example.chismapp.util.eTypeRecord;

/**
 * The {@code ChatClient} class is the main entry point for the chat client application.
 * It provides functionality for discovering a server, establishing a TCP connection, sending
 * messages, initiating and managing voice calls, and recording message history.
 */
public class ChatClient {

    private static CallManager callManager;
    private static TCPConnection clientConnection;
    public static RecordPlayer recordPlayer; // Persistent instance for audio playback
    private static HistorialRecorder recorder;

    /**
     * The main method that starts the chat client, discovers the server, and initializes the connection.
     *
     * @param args command-line arguments (not used).
     */
    public static void main(String[] args) throws InterruptedException {
        // Discover the server automatically using ClientDiscovery
        ClientDiscovery discovery = new ClientDiscovery();
        try {
            discovery.discoverServer();
        } catch (Exception  e) {
            Thread.sleep(5000);
            discovery.discoverServer();
        }  // Attempt to discover the server

        String serverIp = discovery.getServerIp();
        int serverPort = discovery.getServerPort();

        if (serverIp == null) {
            System.err.println("Could not find the server. Please make sure the server is running and discoverable.");
            return;
        }

        // Initialize the client connection to the discovered server
        clientConnection = TCPConnection.getInstance();
        clientConnection.initAsClient(serverIp, serverPort);

        // Initialize CallManager
        ChatClient chatClient = new ChatClient();
        callManager = new CallManager(chatClient);

        // Initialize RecordPlayer for voice playback
        recordPlayer = new RecordPlayer(getAudioFormat());

        // Set up a listener to handle messages from the server
        clientConnection.setListener(message -> {
            if (message.startsWith("VOICE:")) {
                handleVoiceMessage(message);
            } else if (message.startsWith("CALL_INITIATED:")) {
                String caller = message.substring("CALL_INITIATED:".length()).trim();
                callManager.handleIncomingCall(caller);
            } else if (message.startsWith("CALL_ACCEPTED:")) {
                String recipient = message.substring("CALL_ACCEPTED:".length()).trim();
                callManager.handleCallAccepted(recipient);
            } else if (message.startsWith("CALL_ENDED:")) {
                String participant = message.substring("CALL_ENDED:".length()).trim();
                callManager.handleCallEnded(participant);  // Handle the end of a call
            } else {
                System.out.println(message);
            }
        });

        clientConnection.start();

        // Handle user input
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.println("Enter your username:");
            String clientName = reader.readLine();
            clientConnection.sendMessage("USERNAME:" + clientName);  // Send the username to the server

            recorder = new HistorialRecorder();
            recorder.addMessage(clientName, eTypeRecord.STARTED_CONNECTION);

            // Display available commands
            System.out.println("Available commands:");
            System.out.println("/group group_name - To create/join a group");
            System.out.println("/message group_name <message> - To send a message to a group");
            System.out.println("/dm username <message> - To send a direct message to a user");
            System.out.println("/voice <username|group_name> - To send a voice message");
            System.out.println("/call <username> - To initiate a call to a user");
            System.out.println("/endcall <username> - To end a call with a user");
            System.out.println("/historical - To generate the record of the messages");
            System.out.println("/acceptcall <caller> - To accept an incoming call");

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("/group")) {
                    clientConnection.sendMessage(line);  // Send a command to create or join a group
                    recorder.addMessage(clientName + " joined or created a group " + line.substring(7), eTypeRecord.GROUP);
                } else if (line.startsWith("/message")) {
                    clientConnection.sendMessage(line);  // Send a group message
                    recorder.addMessage(clientName + " sent message to the group " + line.substring(9), eTypeRecord.TEXT);
                } else if (line.startsWith("/dm")) {
                    clientConnection.sendMessage(line);  // Send a direct message
                    recorder.addMessage(clientName + " sent message to the user " + line.substring(4), eTypeRecord.TEXT);
                } else if (line.startsWith("/voice")) {
                    handleVoiceCommand(line, clientConnection);
                    recorder.addMessage(clientName + " voice messaged " + line.substring(7), eTypeRecord.AUDIO);
                } else if (line.startsWith("/call")) {
                    handleCallCommand(line);
                    recorder.addMessage(clientName + " called " + line.substring(6), eTypeRecord.CALL);
                } else if (line.startsWith("/endcall")) {
                    handleEndCallCommand(line);
                    try {
                        recorder.addMessage("Ended call " + line.substring(9), eTypeRecord.CALL);
                    } catch (StringIndexOutOfBoundsException e) {
                        System.out.println("Invalid command. Use /endcall <username>.");
                    }
                } else if (line.startsWith("/historical")) {
                    recorder.generate();
                    System.out.println("Generating the record of messages");
                } else if (line.startsWith("/acceptcall")) {
                    handleAcceptCallCommand(line);
                    recorder.addMessage("Accepted call from " + line.substring(12), eTypeRecord.CALL);
                } else if (line.startsWith("/rejectcall")) {
                    handleRejectCallCommand(line);
                    recorder.addMessage("Rejected call from " + line.substring(12), eTypeRecord.CALL);
                } else {
                    System.out.println("Invalid command. Use /group, /message, /dm, /voice, /call, /endcall or /historical.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the /call command from the client input.
     * Initiates a call to a specified user.
     *
     * @param command the command entered by the user (in the format /call <username>).
     */
    private static void handleCallCommand(String command) {
        String[] parts = command.split(" ", 2);
        if (parts.length < 2) {
            System.out.println("Usage: /call <username>");
            return;
        }
        String recipient = parts[1].trim();
        callManager.initiateCall(recipient);
    }

    /**
     * Handles the /endcall command from the client input.
     * Ends a call with a specified participant.
     *
     * @param command the command entered by the user (in the format /endcall <username>).
     */
    private static void handleEndCallCommand(String command) {
        String[] parts = command.split(" ", 2);
        if (parts.length < 2) {
            System.out.println("Usage: /endcall <username>");
            return;
        }
        String participant = parts[1].trim();
        callManager.endCall(participant);
    }

    /**
     * Handles the /voice command for sending a voice message.
     *
     * @param command          the command entered by the user (in the format /voice <username|group_name>).
     * @param clientConnection the connection used to send the voice message.
     */
    private static void handleVoiceCommand(String command, TCPConnection clientConnection) {
        String[] parts = command.split(" ", 2);
        if (parts.length < 2) {
            System.out.println("Usage: /voice <username|group_name>");
            return;
        }
        String recipient = parts[1];

        // Set up audio format for recording
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
        recordThread.interrupt();  // Ensure thread interruption
        try {
            recordThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Retrieve and encode the audio data
        byte[] audioData = out.toByteArray();
        String encodedAudio = Base64.getEncoder().encodeToString(audioData);

        // Send the voice message
        String voiceMessage = "VOICE:" + recipient + ":" + encodedAudio;
        clientConnection.sendMessage(voiceMessage);
        System.out.println("Voice message sent to " + recipient);
    }

    /**
     * Handles the reception of a voice message.
     *
     * @param message the message received from the server (in the format VOICE:<sender>:<data_audio_base64>).
     */
    private static void handleVoiceMessage(String message) {
        String[] parts = message.split(":", 3);
        if (parts.length < 3) {
            System.out.println("Received malformed voice message.");
            return;
        }
        String sender = parts[1];
        String encodedAudio = parts[2];

        byte[] audioData = Base64.getDecoder().decode(encodedAudio);

        // Play the audio using the persistent instance of RecordPlayer
        recordPlayer.initiateAudio(audioData);
        recorder.addMessage("Received audio made by: " + message.split(":")[1], eTypeRecord.RECEIVED);
    }

    /**
     * Returns the {@code AudioFormat} to be used for recording and playback.
     *
     * @return the {@code AudioFormat} for voice communication (16 kHz, 16-bit, mono).
     */
    private static AudioFormat getAudioFormat() {
        float sampleRate = 16000.0F;
        int sampleSizeInBits = 16;
        int channels = 1; // Mono
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }

    /**
     * Handles the /acceptcall command from the client input.
     * Accepts an incoming call from a specified user.
     *
     * @param command the command entered by the user (in the format /acceptcall <caller>).
     */
    private static void handleAcceptCallCommand(String command) {
        String[] parts = command.split(" ", 2);
        if (parts.length < 2) {
            System.out.println("Usage: /acceptcall <caller>");
            return;
        }
        String caller = parts[1].trim();
        callManager.acceptCall(caller);
    }

    /**
     * Handles the /rejectcall command from the client input.
     * Rejects an incoming call from a specified user.
     *
     * @param command the command entered by the user (in the format /rejectcall <caller>).
     */
    private static void handleRejectCallCommand(String command) {
        String[] parts = command.split(" ", 2);
        if (parts.length < 2) {
            System.out.println("Usage: /rejectcall <caller>");
            return;
        }
        String caller = parts[1].trim();
        callManager.rejectCall(caller);
    }

    /**
     * Displays a message to the console and records it in the chat history.
     *
     * @param message the message to display and record.
     */
    public void displayMessage(String message) {
        System.out.println(message);
        recorder.addMessage(message, eTypeRecord.RECEIVED);
    }

    /**
     * Sends a message to the server via the client connection.
     *
     * @param message the message to send to the server.
     */
    public void sendMessage(String message) {
        clientConnection.sendMessage(message);
    }

    /**
     * Returns the {@code HistorialRecorder} instance used to log chat and call messages.
     *
     * @return the {@code HistorialRecorder} instance.
     */
    public HistorialRecorder getRecorder() {
        return recorder;
    }
}