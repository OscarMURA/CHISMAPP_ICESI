package com.example.chismapp.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {

    private static final int PORT = 5000;
    private static final int THREAD_POOL_SIZE = 10;
    private static GroupManager groupManager;

    public static void main(String[] args) {
        // Initialize thread pool and group manager
        ExecutorService pool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        groupManager = new GroupManager();  // Create the group manager

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server listening on port " + PORT);

            // Accept new client connections
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                // Create a new ClientHandler for each client and pass the GroupManager
                ClientHandler clientHandler = new ClientHandler(clientSocket, groupManager);
                pool.execute(clientHandler);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            pool.shutdown();
        }
    }
}