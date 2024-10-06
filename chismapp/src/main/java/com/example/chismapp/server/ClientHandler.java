package com.example.chismapp.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The `ClientHandler` class in the Chismapp server handles communication between clients, manages
 * group messaging, direct messaging, and call initiation and management.
 */

public class ClientHandler implements Runnable {

    private static ConcurrentHashMap<String, ClientHandler> userHandlers = new ConcurrentHashMap<>();
    private Socket clientSocket;
    private GroupManager groupManager;
    private CallManager callManager;
    private String userName;
    private PrintWriter out;

// The above code is defining a constructor for the `ClientHandler` class in Java. It takes three
// parameters: a `Socket` object named `socket`, a `GroupManager` object named `groupManager`, and a
// `CallManager` object named `callManager`. Inside the constructor, it assigns the values of these
// parameters to the corresponding instance variables of the `ClientHandler` class.
    public ClientHandler(Socket socket, GroupManager groupManager, CallManager callManager) {
        this.clientSocket = socket;
        this.groupManager = groupManager;
        this.callManager = callManager;
    }

/**
 * This Java function handles incoming messages from a client, processes various commands such as
 * creating groups, sending messages, and managing voice calls, and manages user disconnections in a
 * chat application.
 */
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

/**
 * The `handleVoiceMessage` function processes a voice message by extracting recipient and audio data,
 * checking if the recipient is a group or user, and sending the message accordingly.
 * 
 * @param message The `handleVoiceMessage` method is designed to process a voice message in a specific
 * format. The `message` parameter should be in the format `VOICE:<destinatario>:<datos_audio_base64>`,
 * where:
 */
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


/**
 * The `handleCallInitiate` function processes a call initiation message, checks if the recipient is
 * available for a call, and sends a call request if conditions are met.
 * 
 * @param message The `handleCallInitiate` method is responsible for initiating a call between two
 * users in a chat application. The `message` parameter is expected to be in the format
 * "CALL_INITIATE:<recipient>", where `<recipient>` is the username of the user you want to call.
 */
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


/**
 * The `handleCallAccept` function processes a call acceptance message, checks if the call is valid,
 * accepts the call, and notifies the caller if the call is accepted.
 * 
 * @param message The `handleCallAccept` method is used to process a message related to accepting a
 * call. The `message` parameter contains the information about the call acceptance in the format
 * "CALL_ACCEPT:<caller>".
 */
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



/**
 * The `handleCallReject` method processes a call rejection message, checks if the call is pending and
 * from the correct recipient, rejects the call, and notifies the caller accordingly.
 * 
 * @param message The `handleCallReject` method is used to process a call rejection message received by
 * the user. The `message` parameter contains the information about the call rejection in the format
 * "CALL_REJECT:<caller>".
 */
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



/**
 * The `handleCallEnd` function processes a message indicating the end of a call, checks if the call
 * session exists with the specified participant, ends the call if found, and notifies the other
 * participant if applicable.
 * 
 * @param message The `handleCallEnd` method is used to process a message related to ending a call. The
 * `message` parameter should be in the format "CALL_END:<otherParticipant>", where
 * `<otherParticipant>` is the username of the other participant in the call.
 */
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

/**
 * The `sendMessage` function in Java prints a message to the output stream.
 * 
 * @param message The `sendMessage` method takes a `String` parameter named `message`, which represents
 * the message that will be printed to the output stream using `out.println`.
 */
    public void sendMessage(String message) {
        out.println(message);
    }

/**
 * The `broadcastMessage` function sends a message to all connected clients except for the current
 * client.
 * 
 * @param message A string message that will be broadcasted to all client handlers except for the
 * current handler.
 */
    private void broadcastMessage(String message) {
        for (ClientHandler handler : userHandlers.values()) {
            if (!handler.equals(this)) {
                handler.sendMessage(message);
            }
        }
    }
}
