package com.example.chismapp.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * The {@code ClientDiscovery} class is responsible for discovering the chat server by listening for
 * broadcast messages. It listens on a specified UDP port and retrieves the server's IP address and port
 * when a valid broadcast message is received.
 */
public class ClientDiscovery {

    private String serverIp;
    private int serverPort;

    /**
     * Discovers the chat server by listening for broadcast messages.
     * The method listens on UDP port 4446 for a broadcast message in the format {@code "CHAT_SERVER:<server_ip>:<server_port>"}.
     * Once the server is discovered, the server IP address and port are extracted from the broadcast message.
     */
    public void discoverServer() {
        try {
            // Create a DatagramSocket to listen for server broadcasts on port 4446
            DatagramSocket socket = new DatagramSocket(4446);
            socket.setBroadcast(true); // Enable receiving broadcast messages

            System.out.println("Listening for server broadcasts...");
            byte[] buffer = new byte[256];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            // Receive the broadcast packet
            socket.receive(packet);
            String received = new String(packet.getData(), 0, packet.getLength());

            // Parse the server information if the broadcast message is in the correct format
            if (received.startsWith("CHAT_SERVER:")) {
                String[] parts = received.split(":");
                serverIp = parts[1];
                serverPort = Integer.parseInt(parts[2]);
                System.out.println("Server discovered at: " + serverIp + ":" + serverPort);
            }

            // Close the socket after receiving the message
            socket.close();
        } catch (IOException e) {
            // Handle any IOExceptions that occur during the process
            System.out.println("Error discovering server. Repeat the discovery process.");
        }
    }

    /**
     * Returns the IP address of the discovered server.
     *
     * @return the IP address of the server, or {@code null} if the server has not been discovered yet.
     */
    public String getServerIp() {
        return serverIp;
    }

    /**
     * Returns the port number of the discovered server.
     *
     * @return the port number of the server, or {@code 0} if the server has not been discovered yet.
     */
    public int getServerPort() {
        return serverPort;
    }
}