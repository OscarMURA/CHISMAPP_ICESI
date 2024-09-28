package com.example.chismapp.client;

import com.example.chismapp.util.TCPConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
            System.out.println(message);
        });

        clientConnection.start();

        // Manejar la entrada del cliente
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.println("Enter your username:");
            String clientName = reader.readLine();
            clientConnection.sendMessage("USERNAME:" + clientName);  // Enviar el nombre de usuario del cliente al servidor

            // Mostrar comandos disponibles
            System.out.println("Available commands:");
            System.out.println("/group group_name - To create a group");
            System.out.println("/message group_name <message - To send a message to a group");
            System.out.println("/dm username message - To send a direct message to a user");

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("/group")) {
                    clientConnection.sendMessage(line);  // Enviar comando para crear un grupo
                } else if (line.startsWith("/message")) {
                    clientConnection.sendMessage(line);  // Enviar un mensaje a un grupo
                } else if (line.startsWith("/dm")) {
                    clientConnection.sendMessage(line);  // Enviar un mensaje directo
                } else {
                    System.out.println("Invalid command. Use /group, /message, or /dm.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
