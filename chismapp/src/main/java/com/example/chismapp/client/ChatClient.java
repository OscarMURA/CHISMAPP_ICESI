package com.example.chismapp.client;

import com.example.chismapp.util.TCPConnection;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class ChatClient {

    public static void main(String[] args) {
        // Initialize the client connection
        TCPConnection clientConnection = TCPConnection.getInstance();
        clientConnection.initAsClient("127.0.0.1", 5000);

        // Assign a listener to handle messages from the server
        clientConnection.setListener(message -> {
            System.out.println("Server message: " + message);
        });

        clientConnection.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.println("Enter your username:");
            String clientName = reader.readLine();
            clientConnection.sendMessage("USERNAME:" + clientName);  // Sends the client's username to the server

            // Show available commands
            System.out.println("COMMAND MENUS:");
            System.out.println("/group (group_name) - To create a group");
            System.out.println("/message (group_name) (message) - To send a message to a created group");

            String line;
            while ((line = reader.readLine()) != null) {
                // Logic to create a group or send a message to a group
                if (line.startsWith("/group")) {
                    clientConnection.sendMessage(line);  // Send command to create a group
                } else if (line.startsWith("/message")) {
                    clientConnection.sendMessage(line);  // Send a message to a group
                } else {
                    // Command not listed? treat it as a normal message
                    clientConnection.sendMessage("MESSAGE:" + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}