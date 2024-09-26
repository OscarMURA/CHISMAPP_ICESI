package com.example.chismapp.server;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
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
                System.out.println("Mensaje del cliente: " + message);
                // Aquí podrías procesar el mensaje o distribuirlo a otros clientes
                out.println("Servidor dice: " + message);  // Respuesta al cliente
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
}
