package com.example.chismapp.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * The `ServerDiscovery` class in Java sends broadcast announcements containing server information at
 * regular intervals.
 */

public class ServerDiscovery {

    private int serverPort;


    public ServerDiscovery(int serverPort) {
        this.serverPort = serverPort;
    }

/**
 * The `startDiscovery` function sends a broadcast announcement containing server information at
 * regular intervals.
 */
    public void startDiscovery() {
        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true); // Configurar para broadcast
            String serverAddress = InetAddress.getLocalHost().getHostAddress();
            String announcement = "CHAT_SERVER:" + serverAddress + ":" + serverPort; // IP del servidor y puerto

            while (true) {
                byte[] buffer = announcement.getBytes();
                InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, broadcastAddress, 4446);
                socket.send(packet);
                System.out.println("Server broadcast announcement sent: " + announcement);
                Thread.sleep(5000);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
