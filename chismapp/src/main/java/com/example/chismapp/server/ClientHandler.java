package com.example.chismapp.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler implements Runnable {

    private static ConcurrentHashMap<String, ClientHandler> userHandlers = new ConcurrentHashMap<>();
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
            this.out = outWriter; // Asignar PrintWriter a la variable de instancia
            String message;
            // Escucha mensajes del cliente y responde
            while ((message = in.readLine()) != null) {
                // Procesar el mensaje recibido
                if (message.startsWith("USERNAME:")) {
                    this.userName = message.substring(9).trim(); // Extrae el nombre del usuario
                    userHandlers.put(userName, this); // Añadir el usuario a la lista de manejadores
                    System.out.println("User connected: " + userName);
                    sendMessage("SYSTEM: Welcome " + userName + "!");
                } else if (message.startsWith("/group")) {
                    String[] parts = message.split(" ", 2);
                    if (parts.length < 2) {
                        out.println("SYSTEM: Usage: /group <groupName>");
                        continue;
                    }
                    String groupName = parts[1].trim();
                    groupManager.createGroup(groupName, this);
                    out.println("SYSTEM: You have created/joined the group: " + groupName);
                } else if (message.startsWith("/message")) {
                    String[] parts = message.split(" ", 3);
                    if (parts.length < 3) {
                        out.println("SYSTEM: Usage: /message <groupName> <message>");
                        continue;
                    }
                    String groupName = parts[1].trim();
                    String msgContent = parts[2].trim();
                    String fullMessage = "[" + groupName + "] " + userName + ": " + msgContent;

                    // Reenviar el mensaje al grupo
                    groupManager.sendMessageToGroup(groupName, fullMessage);
                } else if (message.startsWith("/dm")) {
                    String[] parts = message.split(" ", 3);
                    if (parts.length < 3) {
                        out.println("SYSTEM: Usage: /dm <username> <message>");
                        continue;
                    }
                    String targetUserName = parts[1].trim();
                    String msgContent = parts[2].trim();
                    String fullMessage = "[Direct Message] " + userName + ": " + msgContent;

                    // Enviar el mensaje directo al usuario específico
                    ClientHandler targetHandler = userHandlers.get(targetUserName);
                    if (targetHandler != null) {
                        targetHandler.sendMessage(fullMessage);
                    } else {
                        out.println("SYSTEM: User " + targetUserName + " not found.");
                    }
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
                    out.println("SYSTEM: Invalid command. Use /group, /message, /dm, or VOICE/CALL commands.");
                }
            }
        } catch (IOException e) {
            System.out.println("Error handling client, removing user if applicable.");
        } finally {
            // Manejar la desconexión del usuario
            try {
                if (userName != null) {
                    userHandlers.remove(userName); // Eliminar el usuario de la lista al desconectarse
                    groupManager.removeUserFromAllGroups(this);
                    broadcastMessage("SYSTEM: User " + userName + " has left the chat.");
                    System.out.println("User disconnected: " + userName);
                }
                clientSocket.close(); // Cierra la conexión con el cliente cuando se termina
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleVoiceMessage(String message) {
        // Formato: VOICE:<destinatario>:<datos_audio_base64>
        String[] parts = message.split(":", 3);
        if (parts.length < 3) {
            sendMessage("SYSTEM: Malformed VOICE message.");
            return;
        }
        String recipient = parts[1].trim();
        String encodedAudio = parts[2].trim();
        String fullMessage = "VOICE:" + userName + ":" + encodedAudio;

        // Verificar si el destinatario es un grupo o un usuario
        if (groupManager.isGroup(recipient)) {
            groupManager.sendMessageToGroup(recipient, fullMessage);
        } else {
            // Enviar mensaje directo
            ClientHandler targetHandler = userHandlers.get(recipient);
            if (targetHandler != null) {
                targetHandler.sendMessage(fullMessage);
            } else {
                sendMessage("SYSTEM: User " + recipient + " not found.");
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
        if (!callManager.isInCall(recipient) && !callManager.isInCall(this.userName)) {
            // Enviar solicitud de llamada al destinatario
            ClientHandler targetHandler = userHandlers.get(recipient);
            if (targetHandler != null) {
                // Marcar la llamada como pendiente en el CallManager
                boolean pending = callManager.initiatePendingCall(this.userName, recipient);
                if (pending) {
                    targetHandler.sendMessage("CALL_REQUEST:" + this.userName);
                    sendMessage("SYSTEM: Call request sent to " + recipient + ". Waiting for response...");
                }
            } else {
                sendMessage("SYSTEM: User " + recipient + " not found.");
            }
        } else {
            sendMessage("SYSTEM: User " + recipient + " is already in a call or you are in a call.");
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

        CallSession session = callManager.getPendingCallSession(caller);
        if (session != null && session.getRecipient().equals(this.userName)) {
            callManager.acceptCall(this.userName, caller);
            session.setAccepted(true);
            session.setActive(true);  // Establecer llamada como activa
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

        CallSession session = callManager.getPendingCallSession(caller);
        if (session != null && session.getRecipient().equals(this.userName)) {
            callManager.rejectCall(this.userName, caller);
            sendMessage("SYSTEM: Call rejected from " + caller);
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
            session.endCall();  // Marcar la sesión como finalizada
            sendMessage("SYSTEM: Call ended with " + otherParticipant);
            ClientHandler otherHandler = userHandlers.get(otherParticipant);
            if (otherHandler != null) {
                otherHandler.sendMessage("CALL_ENDED:" + this.userName);
            }
        } else {
            sendMessage("SYSTEM: No active call with " + otherParticipant + ".");
        }
    }

    public void sendMessage(String message) {
        out.println(message);
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
