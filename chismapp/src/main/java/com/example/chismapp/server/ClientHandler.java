package com.example.chismapp.server;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private Socket clientSocket;
    private String clientName;
    private GroupManager groupManager;

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

            out.println("Welcome! Enter your name:");
            this.clientName = in.readLine();
            System.out.println("Client connected: " + clientName);

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Message from " + clientName + ": " + message);


                if (message.startsWith("/group")) {
                    String[] parts = message.split(" ");
                    String groupName = parts[1];
                    groupManager.createGroup(groupName);
                    groupManager.addClientToGroup(groupName, this);
                    out.println("Group " + groupName + " created and you were added to it.");
                }


                else if (message.startsWith("/message")) {
                    String[] parts = message.split(" ", 3);
                    String groupName = parts[1];
                    String groupMessage = parts[2];
                    groupManager.sendMessageToGroup(groupName, clientName + ": " + groupMessage);
                }


                else {
                    out.println("Server received: " + message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
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
