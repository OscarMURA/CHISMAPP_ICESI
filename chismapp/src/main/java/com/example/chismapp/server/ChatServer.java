package com.example.chismapp.server;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {

    private static final int THREAD_POOL_SIZE = 10;
    private static GroupManager groupManager;
    private static CallManager callManager;

    public static void main(String[] args) {

        ExecutorService pool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        groupManager = new GroupManager();  // Inicializa el gestor de grupos
        callManager = new CallManager();    // Inicializa el gestor de llamadas

        try (ServerSocket serverSocket = new ServerSocket(0)) { // Puerto automático asignado
            int assignedPort = serverSocket.getLocalPort(); // Obtener el puerto asignado
            System.out.println("Server listening on port " + assignedPort);

            // Inicia el descubrimiento del servidor en un hilo separado
            ServerDiscovery serverDiscovery = new ServerDiscovery(assignedPort);
            Thread discoveryThread = new Thread(serverDiscovery::startDiscovery);
            discoveryThread.start(); // Iniciar el descubrimiento del servidor

            while (true) {
                // Aceptar una nueva conexión del cliente
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                // Crea un nuevo ClientHandler para cada cliente y lo ejecuta en el pool de hilos
                ClientHandler clientHandler = new ClientHandler(clientSocket, groupManager, callManager);
                pool.execute(clientHandler);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            pool.shutdown();
        }
    }
}
