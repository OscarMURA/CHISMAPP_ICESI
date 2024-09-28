package com.example.chismapp.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ClientDiscovery {

    private String serverIp;
    private int serverPort;

    public void discoverServer() {
        try {
            DatagramSocket socket = new DatagramSocket(4446);
            socket.setBroadcast(true); // Configurar para recibir broadcast

            System.out.println("Listening for server broadcasts...");
            byte[] buffer = new byte[256];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            socket.receive(packet);
            String received = new String(packet.getData(), 0, packet.getLength());

            if (received.startsWith("CHAT_SERVER:")) {
                String[] parts = received.split(":");
                serverIp = parts[1];
                serverPort = Integer.parseInt(parts[2]);
                System.out.println("Server discovered at: " + serverIp + ":" + serverPort);
            }

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getServerIp() {
        return serverIp;
    }

    public int getServerPort() {
        return serverPort;
    }
}
