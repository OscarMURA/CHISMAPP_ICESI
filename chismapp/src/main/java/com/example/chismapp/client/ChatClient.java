package com.example.chismapp.client;

import com.example.chismapp.util.TCPConnection;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@SpringBootApplication
public class ChatClient {

    public static void main(String[] args) {
        // Inicializamos la conexión del cliente
        TCPConnection clientConnection = TCPConnection.getInstance();
        clientConnection.initAsClient("127.0.0.1", 5000);

        // Asignamos un listener para escuchar los mensajes del servidor
        clientConnection.setListener(message -> {
            System.out.println("Mensaje del servidor: " + message);
        });

        clientConnection.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                clientConnection.sendMessage(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}