package com.example.chismapp.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler implements Runnable {
    private static ConcurrentHashMap<String, ClientHandler> userHandlers = new ConcurrentHashMap<>();
    private static HashSet<String> alreadyNotified = new HashSet<>();
    
    private Socket clientSocket;
    private GroupManager groupManager;
    private CallManager callManager;
    private String userName;
    private PrintWriter out;

    public ClientHandler(Socket socket, GroupManager groupManager, CallManager callManager) {
        this.clientSocket = socket;
        this.groupManager = groupManager;
        this.callManager = callManager;
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter outWriter = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            this.out = outWriter;
            String message;
            while ((message = in.readLine()) != null) {
                processMessage(message);
            }
        } catch (IOException e) {
            System.out.println("Error handling client, removing user if applicable.");
        } finally {
            cleanUp();
        }
    }

    private void processMessage(String message) {
        if (message.startsWith("USERNAME:")) {
            userName = message.substring(9).trim();
            userHandlers.put(userName, this);
            System.out.println("User connected: " + userName);
            sendMessage("SYSTEM: Welcome " + userName + "!");
        } else if (message.startsWith("/group")) {
            handleGroupCommand(message);
        } else if (message.startsWith("/message")) {
            handleGroupMessage(message);
        } else if (message.startsWith("/dm")) {
            handleDirectMessage(message);
        } else if (message.startsWith("VOICE:")) {
            handleVoiceMessage(message);
        } else if (message.startsWith("CALL_INITIATE:")) {
            handleCallInitiate(message);
        } else if (message.startsWith("CALL_ACCEPT:")) {
            handleCallAccept(message);
        } else if (message.startsWith("CALL_REJECT:")) {
            handleCallReject(message);
        } else if (message.startsWith("CALL_END:")) {
            handleCallEnd(message);
        } else {
            sendMessage("SYSTEM: Invalid command. Use /group, /message, /dm, or VOICE/CALL commands.");
        }
    }

    private void handleGroupCommand(String message) {
        String[] parts = message.split(" ", 2);
        if (parts.length < 2) {
            sendMessage("SYSTEM: Usage: /group <groupName>");
            return;
        }
        String groupName = parts[1].trim();
        groupManager.createGroup(groupName, this);
        sendMessage("SYSTEM: You have created/joined the group: " + groupName);
    }

    private void handleGroupMessage(String message) {
        String[] parts = message.split(" ", 3);
        if (parts.length < 3) {
            sendMessage("SYSTEM: Usage: /message <groupName> <message>");
            return;
        }
        String groupName = parts[1].trim();
        String msgContent = parts[2].trim();
        String fullMessage = "[" + groupName + "] " + userName + ": " + msgContent;
        groupManager.sendMessageToGroup(groupName, fullMessage);
    }

    private void handleDirectMessage(String message) {
        String[] parts = message.split(" ", 3);
        if (parts.length < 3) {
            sendMessage("SYSTEM: Usage: /dm <username> <message>");
            return;
        }
        String targetUserName = parts[1].trim();
        String msgContent = parts[2].trim();
        String fullMessage = "[Direct Message] " + userName + ": " + msgContent;

        ClientHandler targetHandler = userHandlers.get(targetUserName);
        if (targetHandler != null) {
            targetHandler.sendMessage(fullMessage);
        } else {
            handleUserNotFound(targetUserName);
        }
    }

    private void handleUserNotFound(String user) {
        if (!alreadyNotified.contains(user)) {
            sendMessage("SYSTEM: User " + user + " not found.");
            alreadyNotified.add(user);
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    private void cleanUp() {
        if (userName != null) {
            userHandlers.remove(userName);
            alreadyNotified.remove(userName);
            groupManager.removeUserFromAllGroups(this);
            sendMessage("SYSTEM: User " + userName + " has left the chat.");
            System.out.println("User disconnected: " + userName);
        }
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleVoiceMessage(String message) {
        String[] parts = message.split(":", 3);
        if (parts.length < 3) {
            sendMessage("SYSTEM: Malformed VOICE message.");
            return;
        }
        String recipient = parts[1].trim();
        String encodedAudio = parts[2].trim();
        String fullMessage = "VOICE:" + userName + ":" + encodedAudio;
    
        ClientHandler targetHandler = userHandlers.get(recipient);
        if (targetHandler != null) {
            targetHandler.sendMessage(fullMessage);
        } else {
            if (!alreadyNotified.contains(userName)) { // 'alreadyNotified' es un HashSet que mantiene un registro de usuarios notificados.
                sendMessage("SYSTEM: User " + recipient + " not found. Finalizando llamada.");
                alreadyNotified.add(userName); // Asegura que solo se envíe una vez
                callManager.endCall(this.userName); // Asume que existe un método para finalizar llamadas correctamente.
            }
        }
    }    

    // Método para manejar la iniciación de una llamada
    private void handleCallInitiate(String message) {
        // Formato: CALL_INITIATE:<recipient>
        String[] parts = message.split(":", 2);
        if (parts.length < 2) {
            sendMessage("SYSTEM: Malformed CALL_INITIATE message.");
            return;
        }
        String recipient = parts[1].trim();

        // Verificar si el destinatario existe y no está en otra llamada
        if (!callManager.isInCall(recipient)) {
            boolean initiated = callManager.initiateCall(this.userName, recipient);
            if (initiated) {
                sendMessage("SYSTEM: Call initiated to " + recipient);
                ClientHandler targetHandler = userHandlers.get(recipient);
                if (targetHandler != null) {
                    targetHandler.sendMessage("CALL_INITIATED:" + this.userName);
                }
            } else {
                sendMessage("SYSTEM: Unable to initiate call. " + recipient + " is already in a call.");
            }
        } else {
            sendMessage("SYSTEM: User " + recipient + " is already in a call.");
        }
    }

    // Método para manejar la aceptación de una llamada
    private void handleCallAccept(String message) {
        // Formato: CALL_ACCEPT:<caller>
        String[] parts = message.split(":", 2);
        if (parts.length < 2) {
            sendMessage("SYSTEM: Malformed CALL_ACCEPT message.");
            return;
        }
        String caller = parts[1].trim();

        CallSession session = callManager.getCallSession(caller);
        if (session != null && session.getRecipient().equals(this.userName)) {
            callManager.acceptCall(this.userName, caller);
            sendMessage("SYSTEM: Call accepted with " + caller);
            ClientHandler callerHandler = userHandlers.get(caller);
            if (callerHandler != null) {
                callerHandler.sendMessage("CALL_ACCEPTED:" + this.userName);
            }
        } else {
            sendMessage("SYSTEM: No incoming call from " + caller + ".");
        }
    }

    // Método para manejar el rechazo de una llamada
    private void handleCallReject(String message) {
        // Formato: CALL_REJECT:<caller>
        String[] parts = message.split(":", 2);
        if (parts.length < 2) {
            sendMessage("SYSTEM: Malformed CALL_REJECT message.");
            return;
        }
        String caller = parts[1].trim();

        CallSession session = callManager.getCallSession(caller);
        if (session != null && session.getRecipient().equals(this.userName)) {
            callManager.rejectCall(this.userName, caller);
            sendMessage("SYSTEM: Call rejected with " + caller);
            ClientHandler callerHandler = userHandlers.get(caller);
            if (callerHandler != null) {
                callerHandler.sendMessage("CALL_REJECTED:" + this.userName);
            }
        } else {
            sendMessage("SYSTEM: No incoming call from " + caller + ".");
        }
    }

    // Método para manejar el final de una llamada
    private void handleCallEnd(String message) {
        // Formato: CALL_END:<otherParticipant>
        String[] parts = message.split(":", 2);
        if (parts.length < 2) {
            sendMessage("SYSTEM: Malformed CALL_END message.");
            return;
        }
        String otherParticipant = parts[1].trim();

        CallSession session = callManager.getCallSession(this.userName);
        if (session != null && (session.getCaller().equals(otherParticipant) || session.getRecipient().equals(otherParticipant))) {
            callManager.endCall(this.userName);
            sendMessage("SYSTEM: Call ended with " + otherParticipant);
            ClientHandler otherHandler = userHandlers.get(otherParticipant);
            if (otherHandler != null) {
                otherHandler.sendMessage("CALL_ENDED:" + this.userName);
            }
        } else {
            sendMessage("SYSTEM: No active call with " + otherParticipant + ".");
        }
    }

    // Método para difundir mensajes a todos los usuarios excepto este
    private void broadcastMessage(String message) {
        for (ClientHandler handler : userHandlers.values()) {
            if (!handler.equals(this)) {
                handler.sendMessage(message);
            }
        }
    }
}
