package com.example.chismapp.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * The `TCPConnection` class represents a TCP connection handler that can act as a server or client,
 * sending and receiving messages asynchronously.
 */
public class TCPConnection extends Thread {

    private static TCPConnection instance;
    private Socket socket;
    private OnMessageReceivedListener listener;

    private TCPConnection() {}

/**
 * The function getInstance() returns a single instance of TCPConnection using lazy initialization and
 * synchronization.
 * 
 * @return An instance of the `TCPConnection` class is being returned.
 */
    public synchronized static TCPConnection getInstance() {
        if (instance == null) {
            instance = new TCPConnection();
        }
        return instance;
    }

/**
 * This function reads messages from a socket's input stream and notifies a listener for each message
 * received, handling disconnection gracefully.
 */
    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                listener.onMessageReceived(line);
            }
        } catch (IOException e) {
           System.out.println("the server has been disconnected, Goodbye!");
           System.exit(0);
        }
    }

/**
 * The `sendMessage` function sends a message over a socket connection in a separate thread.
 * 
 * @param message The `sendMessage` method takes a `String` parameter named `message`, which represents
 * the message that will be sent over a socket connection. The method creates a new `Thread` to handle
 * the sending of the message asynchronously. Inside the thread, the message is written to the output
 * stream of the socket
 */
    public void sendMessage(String message) {
        new Thread(() -> {
            try {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                writer.write(message + "\n");
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

/**
 * The function `initAsServer` initializes a server socket on the specified port and accepts incoming
 * connections.
 * 
 * @param port The `port` parameter in the `initAsServer` method is an integer value that represents
 * the port number on which the server socket will listen for incoming connections. It is used to
 * specify the communication endpoint for network connections.
 */
    public void initAsServer(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            this.socket = serverSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

/**
 * The `initAsClient` function initializes a socket connection as a client to a specified remote IP
 * address and port.
 * 
 * @param remoteIp The `remoteIp` parameter in the `initAsClient` method is the IP address of the
 * remote server or host that the client will connect to. This IP address is used to establish a
 * network connection with the server over the specified port.
 * @param remotePort The `remotePort` parameter in the `initAsClient` method is an integer value that
 * represents the port number on the remote server to which the client socket will connect. Ports are
 * used to uniquely identify different network services running on a single machine. Common port
 * numbers include 80 for HTTP,
 */
    public void initAsClient(String remoteIp, int remotePort) {
        try {
            this.socket = new Socket(remoteIp, remotePort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

/**
 * The function `setListener` assigns an `OnMessageReceivedListener` to a class variable.
 * 
 * @param listener The `listener` parameter is an object of type `OnMessageReceivedListener`, which is
 * used to set a listener for receiving messages.
 */
    public void setListener(OnMessageReceivedListener listener) {
        this.listener = listener;
    }

// The `OnMessageReceivedListener` interface in the `TCPConnection` class defines a contract for
// classes that want to listen for incoming messages. It declares a single method
// `onMessageReceived(String msg)` that must be implemented by any class that implements this
// interface. This method is called whenever a new message is received by the TCP connection, allowing
// the implementing class to handle the received message as needed.
    public interface OnMessageReceivedListener {
        void onMessageReceived(String msg);
    }
}
