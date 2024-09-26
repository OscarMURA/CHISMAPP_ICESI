package com.example.chismapp.server;

import com.example.chismapp.util.TCPConnection;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.chismapp.util.TCPConnection;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {

    private static final int PORT = 5000;
    private static final int THREAD_POOL_SIZE = 10; // Número de hilos en el pool

    public static void main(String[] args) {
        // Crea un ThreadPool con un tamaño fijo de 10 hilos
        ExecutorService pool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor escuchando en el puerto " + PORT);

            while (true) {
                // Acepta una nueva conexión del cliente
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nuevo cliente conectado: " + clientSocket.getInetAddress());

                // Crea un nuevo manejador de clientes y lo ejecuta en el ThreadPool
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                pool.execute(clientHandler);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Cierra el ThreadPool cuando ya no se usará más
            pool.shutdown();
        }
    }
}