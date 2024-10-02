package com.example.chismapp.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPConnection extends Thread {

    private static TCPConnection instance;
    private Socket socket;
    private OnMessageReceivedListener listener;

    private TCPConnection() {}

    public synchronized static TCPConnection getInstance() {
        if (instance == null) {
            instance = new TCPConnection();
        }
        return instance;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                listener.onMessageReceived(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    public void initAsServer(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            this.socket = serverSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initAsClient(String remoteIp, int remotePort) {
        try {
            this.socket = new Socket(remoteIp, remotePort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setListener(OnMessageReceivedListener listener) {
        this.listener = listener;
    }

    public interface OnMessageReceivedListener {
        void onMessageReceived(String msg);
    }
}
