package com.example.chismapp.server;

import java.io.*;
import java.net.Socket;


public class ClientHandler implements Runnable {

    private Socket clientSocket;
    private GroupManager groupManager; // Gestor de grupos
    private String userName;

    public ClientHandler(Socket socket, GroupManager groupManager) {
        this.clientSocket = socket;
        this.groupManager = groupManager;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String message;
            // Escucha mensajes del cliente y responde
            while ((message = in.readLine()) != null) {
                // Procesar el mensaje recibido
                if (message.startsWith("USERNAME:")) {
                    this.userName = message.substring(9); // Extrae el nombre del usuario
                    System.out.println("User connected: " + userName);
                } else if (message.startsWith("/group")) {
                    String[] parts = message.split(" ", 2);
                    String groupName = parts[1];
                    groupManager.createGroup(groupName, this);
                    out.println("You have created/joined the group: " + groupName);
                } else if (message.startsWith("/message")) {
                    String[] parts = message.split(" ", 3);
                    String groupName = parts[1];
                    String msgContent = parts[2];
                    String fullMessage = "[" + groupName + "] " + userName + ": " + msgContent;

                    // Reenviar el mensaje al grupo
                    groupManager.sendMessageToGroup(groupName, fullMessage);
                } else {
                    out.println("Invalid command. Use /group or /message.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close(); // Cierra la conexión con el cliente cuando se termina
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message) {
        try {
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            out.println(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
